package com.github.blovemaple.mj.local.barbot;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;
import static com.github.blovemaple.mj.object.TileGroupType.*;
import static com.github.blovemaple.mj.utils.MyUtils.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.game.GameContext.PlayerView;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroup;
import com.github.blovemaple.mj.object.TileType;

/**
 * TODO 换牌n个：remove n，add n+1
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class BarBotCpgdSelectTask implements Callable<Action> {
	private static final Logger logger = Logger.getLogger(BarBotCpgdSelectTask.class.getSimpleName());

	private static final Set<ActionType> ACTION_TYPES = new HashSet<>(
			Arrays.asList(CHI, PENG, ZHIGANG, BUGANG, ANGANG, DISCARD, DISCARD_WITH_TING));
	private static final int EXTRA_CHANGE_COUNT = 2; //TODO

	private PlayerView contextView;
	private Set<ActionType> actionTypes;
	private PlayerInfo playerInfo;

	@SuppressWarnings("unused")
	private boolean stopRequest = false;

	public BarBotCpgdSelectTask(PlayerView contextView, Set<ActionType> actionTypes) {
		this.contextView = contextView;
		this.actionTypes = actionTypes;
		this.playerInfo = contextView.getMyInfo();
	}

	@Override
	// TODO interrupt
	public Action call() throws Exception {
		// 生成所有可选动作（包括不做动作）后的状态
		List<BarBotCpgdChoice> choices = allChoices();
		if (choices.isEmpty())
			return null;

		// 如果只有一个选择，就直接选择
		if (choices.size() == 1)
			return choices.get(0).getAction();

		// 从0次换牌开始，计算每个状态换牌后和牌的概率
		AtomicInteger minChangeCount = new AtomicInteger(-1);
		int aliveTileSize = playerInfo.getAliveTiles().size();
		int changeCount = 0;
		for (; true; changeCount++) {
			if (changeCount >= aliveTileSize)
				break;
			if (minChangeCount.get() >= 0 && changeCount - minChangeCount.get() > EXTRA_CHANGE_COUNT)
				break;
			long startTime = System.currentTimeMillis();
			System.out.println("start " + changeCount);
			int crtChangeCount = changeCount;
			choices.parallelStream().map(choice -> choice.testWinProb(crtChangeCount))
					.filter(result -> result == Boolean.TRUE)
					.forEach(win -> minChangeCount.compareAndSet(-1, crtChangeCount));
			System.out.println("end   " + changeCount + " " + (System.currentTimeMillis() - startTime));
		}

		logger.info("Finished changes: " + (changeCount - 1));

		// 返回和牌概率最大的一个动作
		Action bestAction = choices.stream()
				.peek(choice -> logger.info(String.format("%.5f %s", choice.getFinalWinProb(), choice.getAction())))
				.peek(choice->{
					choice.getWinProbByChangeCount().forEach((cc,prob)->{
						System.out.println("change "+cc+" prob "+prob);
					});
				})
				.peek(choice->{
					choice.getWinChangingByChangeCount().values().stream().flatMap(List::stream)
					.forEach(System.out::println);
				})
				// 选和牌概率最大的一个，和牌概率相同者，听牌优先
				.max(Comparator.comparing(BarBotCpgdChoice::getFinalWinProb)
						.thenComparing(choice -> choice.getPlayerInfo().isTing()))
				// 返回其动作
				.map(BarBotCpgdChoice::getAction)
				// （不可能选不出来）
				.orElseThrow(RuntimeException::new);
		return bestAction;
	}

	public static void main(String[] args) throws InterruptedException {
		Thread thread = new Thread() {
			@Override
			public void run() {
				System.out.println("Thread start");
				IntStream.of(1, 2).parallel().forEach(i -> {
					try {
						System.out.println("start: " + i);
						IntStream.generate(() -> 1).sum();
						System.out.println("over: " + i);
					} catch (Exception e) {
						System.out.println("interrupted: " + i);
						e.printStackTrace();
					}
				});
				System.out.println("Thread end");
			}
		};
		thread.start();

		TimeUnit.SECONDS.sleep(1);
		thread.interrupt();
		TimeUnit.SECONDS.sleep(1);
		thread.interrupt();
		System.out.println("main: interrupted");
		thread.join();
	}

	private List<BarBotCpgdChoice> allChoices() {
		List<BarBotCpgdChoice> choices = actionTypes.stream().filter(ACTION_TYPES::contains).flatMap(actionType -> {
			Stream<Set<Tile>> legalTileSets = actionType.getLegalActionTiles(contextView).stream();
			legalTileSets = distinctCollBy(legalTileSets, Tile::type);
			return legalTileSets
					.map(tiles -> new BarBotCpgdChoice(contextView, playerInfo, new Action(actionType, tiles), this));
		}).filter(Objects::nonNull).collect(Collectors.toList());
		if (actionTypes.stream().noneMatch(DISCARD::matchBy))
			choices.add(new BarBotCpgdChoice(contextView, playerInfo, null, this));
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
						// XXX - 写死了暗杠不应该看到
						.filter(group -> group.getType() != ANGANG_GROUP).map(TileGroup::getTiles)
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

	/**
	 * 中止任务，根据当前已经进行的判断尽快产生结果。
	 */
	public void stop() {
		stopRequest = true;
	}

}
