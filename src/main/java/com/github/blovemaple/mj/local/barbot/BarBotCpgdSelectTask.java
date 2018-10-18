package com.github.blovemaple.mj.local.barbot;

import static com.github.blovemaple.mj.action.standard.PlayerActionTypes.*;
import static com.github.blovemaple.mj.utils.LambdaUtils.*;
import static com.github.blovemaple.mj.utils.MyUtils.*;
import static java.util.Comparator.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.action.PlayerAction;
import com.github.blovemaple.mj.action.PlayerActionType;
import com.github.blovemaple.mj.cli.CliGameView;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroupPlayerView;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.rule.win.WinType;

/**
 * 换牌n个：remove n，add n+1
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
@Deprecated
public class BarBotCpgdSelectTask implements Callable<PlayerAction> {
	private static final Logger logger = Logger.getLogger(BarBotCpgdSelectTask.class.getSimpleName());

	public static final Set<ActionType> ACTION_TYPES = new HashSet<>(
			Arrays.asList(CHI, PENG, ZHIGANG, BUGANG, ANGANG, DISCARD, DISCARD_WITH_TING));
	private static final int EXTRA_CHANGE_COUNT = 2;
	private static final int EXTENDED_MAX_CHANGE_COUNT = 4;
	private static final int MAX_CHANGE_COUNT = 10;

	private GameContextPlayerView contextView;
	private Set<PlayerActionType> actionTypes;
	private PlayerInfo playerInfo;

	@SuppressWarnings("unused")
	private boolean stopRequest = false;

	public BarBotCpgdSelectTask(GameContextPlayerView contextView, Set<PlayerActionType> actionTypes) {
		this.contextView = contextView;
		this.actionTypes = actionTypes;
		this.playerInfo = contextView.getMyInfo();
	}

	private String aliveTilesStr() {
		StringBuilder aliveTilesStr = new StringBuilder();
		CliGameView.appendAliveTiles(aliveTilesStr, playerInfo.getAliveTiles(), playerInfo.getLastDrawedTile(), null);
		return aliveTilesStr.toString();
	}

	@Override
	// interrupt
	public PlayerAction call() throws Exception {
		long startTime = System.currentTimeMillis();

		// 生成所有可选动作（包括不做动作）后的状态
		List<BarBotCpgdChoice> choices = allChoices();
		if (choices.isEmpty()) {
			logger.info("[No choices]");
			logger.info("Bot alivetiles " + aliveTilesStr());
			return null;
		}

		// 如果只有一个选择，就直接选择
		if (choices.size() == 1) {
			PlayerAction action = choices.get(0).getAction();
			logger.info("[Single choice] " + action);
			logger.info("Bot alivetiles " + aliveTilesStr());
			return action;
		}

		// 从0次换牌开始，计算每个状态换牌后和牌的概率
		AtomicInteger minChangeCount = new AtomicInteger(-1);
		int aliveTileSize = playerInfo.getAliveTiles().size();
		int changeCount = 0;
		for (; true; changeCount++) {
			if (changeCount >= aliveTileSize)
				break;
			if (changeCount > MAX_CHANGE_COUNT)
				break;
			if (minChangeCount.get() >= 0 && changeCount > EXTENDED_MAX_CHANGE_COUNT)
				break;
			if (minChangeCount.get() >= 0 && changeCount - minChangeCount.get() > EXTRA_CHANGE_COUNT)
				break;
			int crtChangeCount = changeCount;
			choices.parallelStream().map(rethrowFunction(choice -> choice.testWinProb(crtChangeCount)))
					.filter(result -> result == Boolean.TRUE)
					.forEach(win -> minChangeCount.compareAndSet(-1, crtChangeCount));
		}

		// 算出和牌概率最大的一个动作
		PlayerAction bestAction = choices.stream()
				.peek(choice -> logger
						.info("[Win prob] " + String.format("%.7f %s", choice.getFinalWinProb(), choice.getAction())))
				// 第一条件：和牌概率大
				.max(comparing(BarBotCpgdChoice::getFinalWinProb)
						// 第二条件：杠优先（因为杠了之后可以多摸一张牌，这个好处在算概率的时候没算进去）
						.thenComparing(choice -> choice.getAction() != null && Arrays.asList(ZHIGANG, BUGANG, ANGANG)
								.stream().anyMatch(gang -> gang.matchBy(choice.getAction().getType())))
						// 第三条件：听牌优先
						.thenComparing(choice -> choice.getPlayerInfo().isTing())
						// 第四条件：前面的优先（和牌类型的返回结果中认为最差的）
						.thenComparing(comparing(choices::indexOf).reversed()))
				// 取出其动作
				.map(BarBotCpgdChoice::getAction).orElse(null);

		StringBuilder aliveTilesStr = new StringBuilder();
		CliGameView.appendAliveTiles(aliveTilesStr, playerInfo.getAliveTiles(), playerInfo.getLastDrawedTile(), null);
		logger.info("Bot alivetiles " + aliveTilesStr);
		logger.info("Bot Choose action " + bestAction);
		logger.info("Bot time " + (System.currentTimeMillis() - startTime));
		return bestAction;
	}

	private List<BarBotCpgdChoice> allChoices() {
		List<BarBotCpgdChoice> choices = actionTypes.stream().filter(ACTION_TYPES::contains).flatMap(actionType -> {
			Stream<Set<Tile>> legalTileSets = actionType.getLegalActionTiles(contextView).stream();
			legalTileSets = distinctCollBy(legalTileSets, Tile::type);
			if (DISCARD != actionType || playerInfo.isTing()) {
				return legalTileSets.map(tiles -> new BarBotCpgdChoice(contextView, playerInfo,
						new PlayerAction(contextView.getMyLocation(), actionType, tiles), this,
						contextView.getGameStrategy().getAllWinTypes()));
			} else {
				Map<? extends WinType, List<Tile>> discardsByWinType = contextView.getGameStrategy().getAllWinTypes()
						.stream().collect(Collectors.toMap(Function.identity(),
								winType -> winType.getDiscardCandidates(playerInfo.getAliveTiles(), remainTiles())));
				Set<Tile> legalTiles = legalTileSets.flatMap(Set::stream).collect(Collectors.toSet());
				Map<Tile, Double> tilesAndPriv = discardsByWinType.values().stream().flatMap(List::stream).distinct()
						.filter(legalTiles::contains).collect(
								Collectors.toMap(Function.identity(),
										tile -> discardsByWinType.values().stream().map(list -> list.indexOf(tile))
												.mapToInt(index -> index >= 0 ? index
														: playerInfo.getAliveTiles().size() - 1)
												.average().getAsDouble()));
				return discardsByWinType.values().stream().flatMap(List::stream).distinct().filter(legalTiles::contains)
						.sorted(Comparator.comparing(tilesAndPriv::get)).map(tile -> {
							List<WinType> winTypes = discardsByWinType.entrySet().stream()
									.filter(entry -> entry.getValue().contains(tile)).map(Map.Entry::getKey)
									.collect(Collectors.toList());
							return new BarBotCpgdChoice(contextView, playerInfo,
									new PlayerAction(contextView.getMyLocation(), actionType, Set.of(tile)), this,
									winTypes);
						});
			}

		}).filter(Objects::nonNull).collect(Collectors.toList());
		if (actionTypes.stream().noneMatch(DISCARD::matchBy))
			choices.add(new BarBotCpgdChoice(contextView, playerInfo, null, this,
					contextView.getGameStrategy().getAllWinTypes()));
		return choices;
	}

	private List<Tile> remainTiles;
	private Map<TileType, Long> remainTilesByType;

	/**
	 * 返回所有剩余牌（本家看不到的所有牌）。
	 */
	public List<Tile> remainTiles() {
		if (remainTiles == null) {
			// 除了本家手牌、所有玩家groups、已经打出的牌
			Set<Tile> existTiles = new HashSet<>();
			existTiles.addAll(contextView.getMyInfo().getAliveTiles());
			contextView.getTableView().getPlayerInfoView().values().forEach(playerInfo -> {
				playerInfo.getTileGroups().stream()
						.map(TileGroupPlayerView::getTiles).filter(Objects::nonNull)
						.forEach(existTiles::addAll);
				existTiles.addAll(playerInfo.getDiscardedTiles());
			});
			List<Tile> remainTiles = contextView.getGameStrategy().getAllTiles().stream()
					.filter(tile -> !existTiles.contains(tile)).collect(Collectors.toList());
			remainTilesByType = remainTiles.stream()
					.collect(Collectors.groupingBy(Tile::type, HashMap::new, Collectors.counting()));
			/*
			 * 因为remainTileCountByType用此方法保证remainTilesByType的存在，
			 * 所以remainTilesByType在remainTiles之前赋值
			 */
			this.remainTiles = remainTiles;
		}
		return remainTiles;
	}

	public Map<TileType, Long> remainTileCountByType() {
		remainTiles();
		return remainTilesByType;
	}

	public int remainTileCount() {
		return remainTiles().size();
	}

	private Map<Integer, Double> probCache = Collections.synchronizedMap(new HashMap<>());

	/**
	 * 计算addedTiles出现的可能性。<br>
	 * addedTiles出现的可能性=(在剩余牌中选到addedTiles的组合数)/(在剩余牌中选addedTiles个牌的组合数)
	 */
	public double getProb(Collection<Tile> addedTiles) {
		int hash = addedTiles.stream().mapToInt(Tile::hashCode).sum();
		Double prob = probCache.get(hash);
		if (prob == null) {
			Map<TileType, List<Tile>> addedByType = addedTiles.stream().collect(Collectors.groupingBy(Tile::type));
			Map<TileType, Long> remainTiles = remainTileCountByType();

			AtomicLong addedComb = new AtomicLong(1);
			addedByType.forEach((type, added) -> addedComb
					.getAndAccumulate(combCount(remainTiles.get(type), added.size()), Math::multiplyExact));

			prob = (double) addedComb.get() / combCount(remainTileCount(), addedTiles.size());
			probCache.put(hash, prob);
		}
		return prob;
	}

	/**
	 * 中止任务，根据当前已经进行的判断尽快产生结果。
	 */
	public void stop() {
		stopRequest = true;
	}

}
