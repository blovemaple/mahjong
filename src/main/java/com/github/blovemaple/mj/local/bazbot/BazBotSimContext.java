package com.github.blovemaple.mj.local.bazbot;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionAndLocation;
import com.github.blovemaple.mj.action.IllegalActionException;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.game.GameContextPlayerViewImpl;
import com.github.blovemaple.mj.game.GameResult;
import com.github.blovemaple.mj.object.MahjongTable;
import com.github.blovemaple.mj.object.MahjongTablePlayerView;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroupPlayerView;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.rule.GameStrategy;
import com.github.blovemaple.mj.rule.TimeLimitStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.blovemaple.mj.action.standard.StandardActionType.DISCARD;
import static com.github.blovemaple.mj.utils.LambdaUtils.rethrowFunction;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

/**
 * BazBot模拟执行动作使用的GameContext。对BazBot提供模拟执行动作和计算评分的功能。<br>
 * 从GameContextPlayerView构建，提供GameContext接口有限功能，足够模拟执行动作。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
class BazBotSimContext implements GameContext {
	// 以下字段是构造器传进来的，不可变，只用于获取信息
	private GameContextPlayerView oriContextView;
	private BazBotSimContext oriContext;

	// 以下字段记录实时状态，可变
	private PlayerInfo crtMyInfo;
	private List<ActionAndLocation> doneActions;
	private GameContextPlayerView crtContextView; // 延迟生成

	public BazBotSimContext(GameContextPlayerView contextView) {
		this.oriContextView = contextView;

		crtMyInfo = contextView.getMyInfo().clone();
		doneActions = new ArrayList<>(contextView.getDoneActions());
	}

	private BazBotSimContext(BazBotSimContext oriContext) {
		this.oriContextView = oriContext.oriContextView;
		this.oriContext = oriContext;

		crtMyInfo = oriContext.crtMyInfo.clone();
		doneActions = new ArrayList<>(oriContext.doneActions);
	}

	/**
	 * 模拟执行指定动作，并返回新的实例。模拟动作在新实例上执行，不改变本实例的状态。
	 * 
	 * @param action
	 *            模拟执行的动作
	 * @return 执行完动作的新实例。如果模拟动作为放弃（null），则直接返回当前实例。
	 */
	public BazBotSimContext afterSimAction(Action action) {
		if (action == null)
			return this;
		BazBotSimContext newContext = new BazBotSimContext(this);
		newContext.simAction(action);
		return newContext;
	}

	private void simAction(Action action) {
		try {
			if (action != null)
				action.getType().doAction(this, oriContextView.getMyLocation(), action);
		} catch (IllegalActionException e) {
			// 调用方保证传进来的action都是合法的，不抛出异常，省去调用方处理的麻烦
			throw new RuntimeException(e);
		}
	}

	/**
	 * 进行评分并返回分数。
	 */
	public double score() {
		if (crtMyInfo.getAliveTiles().size() % 3 == 2) {
			// 待出牌状态，从出每一张牌后的状态中选最高评分
			return crtMyInfo.getAliveTiles().stream()
					// 生成、模拟出牌动作
					.map(aliveTile -> new Action(DISCARD, aliveTile)) //
					.map(rethrowFunction(this::afterSimAction))
					// 评分并选出最高
					.map(BazBotSimContext::score).max(Comparator.naturalOrder()).orElse(0d);
		} else {
			// 不是待出牌状态，直接评分
			return
			// 计算和牌需要得到的牌型
			BazBotAliveTiles.of(crtMyInfo.getAliveTiles()).tileTypesToWin()
					// 计算每组牌型出现的概率
					.stream().mapToDouble(tileTypes -> prob(tileTypes))
					// 相加
					.sum();
		}
	}

	private double prob(Collection<TileType> tileTypes) {
		if (oriContext != null)
			// 为了避免重复统计不可见牌，如果存在oriContext就调用oriContext的功能，最终效果是BazBot的每次调用只调用最上层Context的calcProb
			return oriContext.prob(tileTypes);
		else
			return calcProb(tileTypes);
	}

	// 所有不可见的牌按牌型统计数量
	private long invisibleTotleCount;
	private Map<TileType, Long> invisibleCountByTileType;

	private void initInvisibleCount() {
		if (invisibleCountByTileType != null)
			return; // already inited

		synchronized (this) {
			if (invisibleCountByTileType != null)
				return; // already inited

			// 在所有牌中去掉所有可见牌，留下不可见的牌
			Set<Tile> invisibleTiles = new HashSet<>(getGameStrategy().getAllTiles());
			// 去掉：自己的活牌、打出的牌、牌组中的牌
			invisibleTiles.removeAll(crtMyInfo.getAliveTiles());
			invisibleTiles.removeAll(crtMyInfo.getDiscardedTiles());
			crtMyInfo.getTileGroups().forEach(group -> invisibleTiles.removeAll(group.getTiles()));
			// 去掉：其他玩家打出的牌、牌组中可见的牌
			oriContextView.getTableView().getPlayerInfoView().forEach((location, playerView) -> {
				if (location != oriContextView.getMyLocation()) {
					invisibleTiles.removeAll(playerView.getDiscardedTiles());
//					System.out.println(location + playerView.getDiscardedTiles()
//									.toString() + playerView.isTing());
					playerView.getTileGroups().stream().map(TileGroupPlayerView::getTiles).filter(Objects::nonNull)
							.forEach(invisibleTiles::removeAll);
				}
			});

			// 统计不可见的牌
			invisibleTotleCount = invisibleTiles.size();
			invisibleCountByTileType = invisibleTiles.stream().collect(groupingBy(Tile::type, counting()));
		}
	}

	private double calcProb(Collection<TileType> tileTypes) {
		initInvisibleCount();

		Map<TileType, Integer> removedInvisibleCountByType = new HashMap<>();
		AtomicInteger removedTotle = new AtomicInteger(0);
		// 每个tileType的概率相乘
		return tileTypes.stream()
				// 取该牌型不可见的牌数
				.map(type -> {
					long oriCount = invisibleCountByTileType.getOrDefault(type, 0L);
					if (oriCount == 0L)
						return 0L;
					int removed = removedInvisibleCountByType.getOrDefault(type, 0);
					removedInvisibleCountByType.put(type, removed + 1);
					removedTotle.incrementAndGet();
					return oriCount + removed;
				})
				// 除以不可见牌的总数得出概率
				.mapToDouble(count -> count.doubleValue() / (invisibleTotleCount - removedTotle.get()))
				// 所有概率相乘
				.reduce((prob1, prob2) -> prob1 * prob2).orElse(1d);

		// XXX - 计算prob有两个问题：
		// 1. 通过摸/吃/碰/和牌得牌的概率是不一样的，每个tileType需要根据可得牌的方式区别对待；
		// 2. 需要考虑每轮中其他玩家和牌的概率，根据tileType数量计算在每组tileType的prob内。
	}

	// 以下方法是实现GameContext接口的方法，部分支持，主要是足够模拟动作使用即可

	@Override
	public MahjongTable getTable() {
		throw new UnsupportedOperationException();
	}

	@Override
	public GameStrategy getGameStrategy() {
		return oriContextView.getGameStrategy();
	}

	@Override
	public TimeLimitStrategy getTimeLimitStrategy() {
		return oriContextView.getTimeLimitStrategy();
	}

	@Override
	public PlayerInfo getPlayerInfoByLocation(PlayerLocation location) {
		if (location == oriContextView.getMyLocation())
			return crtMyInfo;

		throw new UnsupportedOperationException();
	}

	@Override
	public PlayerLocation getZhuangLocation() {
		return oriContextView.getZhuangLocation();
	}

	@Override
	public void setZhuangLocation(PlayerLocation zhuangLocation) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void actionDone(Action action, PlayerLocation location) {
		doneActions.add(new ActionAndLocation(action, location));
	}

	@Override
	public ActionAndLocation getLastActionAndLocation() {
		return doneActions.isEmpty() ? null : doneActions.get(doneActions.size() - 1);
	}

	@Override
	public Action getLastAction() {
		return doneActions.isEmpty() ? null : doneActions.get(doneActions.size() - 1).getAction();
	}

	@Override
	public PlayerLocation getLastActionLocation() {
		return doneActions.isEmpty() ? null : doneActions.get(doneActions.size() - 1).getLocation();
	}

	@Override
	public List<ActionAndLocation> getDoneActions() {
		return doneActions;
	}

	@Override
	public GameResult getGameResult() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setGameResult(GameResult gameResult) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GameContextPlayerView getPlayerView(PlayerLocation location) {
		if (location == oriContextView.getMyLocation()) {
			if (crtContextView == null)
				crtContextView = new GameContextPlayerViewImpl(this, location) {
					@Override
					public MahjongTablePlayerView getTableView() {
						return oriContextView.getTableView();
					}
				};
			return crtContextView;
		}

		throw new UnsupportedOperationException();
	}

}
