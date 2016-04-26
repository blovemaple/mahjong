package com.github.blovemaple.mj.local.foobot;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;
import static com.github.blovemaple.mj.object.TileGroupType.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionAndLocation;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.action.ActionTypeAndLocation;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.MahjongGame;
import com.github.blovemaple.mj.object.MahjongTable;
import com.github.blovemaple.mj.object.Player;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroup;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.rule.TimeLimitStrategy;

/**
 * FooBot模拟时使用的GameContext。每一步的context都不同，每个context会产生一个胜率。<br>
 * <p>
 * 一个顶层FooSimContext的生命周期：
 * <li>根据实际context新建——FooBot；
 * <li>作为任务被提交，等待处理——FooBot；
 * <li>开始处理，让模拟玩家给出可能的动作——gameTool（MahjongGame）、模拟玩家；
 * <li>将此context进行分割或结束模拟——FooSimContext；
 * <li>如果分割了，等待最后计算胜率——FooBot。
 * </p>
 * <br>
 * <p>
 * 一个非顶层FooSimContext的生命周期：
 * <li>根据上一个context新建——上一个FooSimContext；
 * <li>模拟一步，到达此context需要表示的状态——上一个FooSimContext；
 * <li>作为任务被提交，等待处理——上一个FooSimContext；
 * <li>开始处理，让模拟玩家给出可能的动作——gameTool（MahjongGame）、模拟玩家；
 * <li>将此context进行分割或结束模拟——FooSimContext；
 * <li>如果分割了，等待最后计算胜率——FooBot。
 * </p>
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class FooSimContext extends GameContext implements Runnable {
	private static final Logger logger = Logger
			.getLogger(FooSimContext.class.getSimpleName());

	private MahjongGame gameTool;
	private BiFunction<FooSimContext, Map<ActionAndLocation, FooSimContext>, Map<ActionAndLocation, FooSimContext>> splitSubmitter;
	private Consumer<FooSimContext> doneSubmitter;
	private PlayerLocation selfLocation;

	/**
	 * 此状态的层级，即达到此状态已经模拟的动作数。层级小的会先处理。
	 */
	private int level;

	// 进行处理时产生以下信息

	/**
	 * 模拟非本家选择动作时记录的，可能打出牌的玩家位置。
	 */
	private PlayerLocation locationCanDiscard;
	/**
	 * 模拟非本家选择动作时记录的，任意非本家是否可能和牌。<br>
	 * TODO 目前逻辑没有考虑其他玩家和牌的可能性。
	 */
	private boolean othersCanWin;
	/**
	 * 模拟本家选择动作时记录的，本家可以选择的动作集合。
	 */
	private Set<Action> selfActions;
	/**
	 * 未出现（本家看不到）的各种牌型的牌。
	 */
	private Map<TileType, List<Tile>> tileTypeAndTiles;
	/**
	 * 未出现（本家看不到）的各种牌型出现的概率。
	 */
	private Map<TileType, Double> tileTypeAndProbs;
	/**
	 * 上面记录完毕后，算出的：本家选择动作 -> (最终决定动作 -> 决定动作的概率)<br>
	 * 本家没有动作可做时，key记为null
	 */
	private Map<Action, Map<ActionAndLocation, Double>> finalProbs;

	/**
	 * 提交时全局去重产生的，下一个动作的context集合。
	 */
	private Map<ActionAndLocation, FooSimContext> nextContexts;

	// 计算结果时产生以下信息

	/**
	 * 计算结果时记录的，finalProbs中的每种本家动作进行后本家的胜率。
	 */
	private Map<Action, Double> selfActionAndWinProb;

	/**
	 * 计算结果时得出的此context状态下的胜率。<br>
	 * 如果模拟动作时已经确定当前状态下本家可以和牌或流局，则模拟动作时写入（这样的情况即此context是最底层的）。
	 */
	private Double selfWinProb;
	/**
	 * 计算结果时得出的保证胜率本家需要做出的主动动作，即finalProbs的一个key。<br>
	 * 如果模拟动作时已经确定当前状态下本家可以和牌或流局，则模拟动作时写入。
	 */
	private Action bestSelfAction;

	public FooSimContext(GameContext.PlayerView realContextView,
			MahjongGame gameTool,
			BiFunction<FooSimContext, Map<ActionAndLocation, FooSimContext>, Map<ActionAndLocation, FooSimContext>> splitSubmitter,
			Consumer<FooSimContext> doneSubmitter) {
		super(new FooMahjongTable(realContextView.getTableView(),
				realContextView.getMyInfo()), gameTool.getGameStrategy(),
				TimeLimitStrategy.NO_LIMIT);
		setZhuangLocation(realContextView.getZhuangLocation());
		setDoneActions(new ArrayList<>(realContextView.getDoneActions()));
		setGameResult(realContextView.getGameResult());

		this.gameTool = gameTool;
		this.splitSubmitter = splitSubmitter;
		this.doneSubmitter = doneSubmitter;
		this.selfLocation = realContextView.getMyLocation();

		this.level = 0;

		// 生成typeTypeAndTiles，供equals用
		tileTypeAndProbs();
	}

	public FooSimContext(FooSimContext lastContext) {
		super(new FooMahjongTable((FooMahjongTable) lastContext.getTable()),
				lastContext.getGameStrategy(),
				lastContext.getTimeLimitStrategy());
		setZhuangLocation(lastContext.getZhuangLocation());
		setDoneActions(new ArrayList<>(lastContext.getDoneActions()));
		setGameResult(lastContext.getGameResult());

		this.gameTool = lastContext.gameTool;
		this.splitSubmitter = lastContext.splitSubmitter;
		this.doneSubmitter = lastContext.doneSubmitter;
		this.selfLocation = lastContext.selfLocation;

		this.level = lastContext.level + 1;
	}

	/**
	 * 因为每个FooContext只用于模拟一步，所以这个方法只会被调用一次。<br>
	 * 执行这一步是生成此状态的前置步骤，然后此context才会处于待处理的状态。
	 * 
	 * @see com.github.blovemaple.mj.game.GameContext#actionDone(com.github.blovemaple.mj.action.Action,
	 *      com.github.blovemaple.mj.object.PlayerLocation)
	 */
	@Override
	public void actionDone(Action action, PlayerLocation location) {
		super.actionDone(action, location);

		// 生成typeTypeAndTiles，供equals用
		tileTypeAndProbs();
	}

	/**
	 * 让gameTool对此context选择一次后续动作，然后对此context进行处理。
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			if (level >= 3) {
				selfWinProb = 0d;
				doneSubmitter.accept(this);
				return;
			}

			long startTime = System.currentTimeMillis();

			logger.info("CURRENT LEVEL: " + level);

			// 使用gameTool的选择action功能，让模拟玩家填充otherActionProbs和selfActions
			gameTool.chooseAction(this);

			if (selfActions == null && locationCanDiscard == null
					&& !othersCanWin) {
				// 没动作可做，流局，胜率为0
				selfWinProb = 0d;
				doneSubmitter.accept(this);
				return;
			}

			if (selfActions == null)
				selfActions = Collections.emptySet();

			if (selfActions.stream().map(Action::getType)
					.anyMatch(WIN::matchBy)) {
				// 本家可以选择和牌，胜率为1
				bestSelfAction = selfActions.iterator().next();
				selfWinProb = 1d;
				doneSubmitter.accept(this);
				return;
			}

			if (level == 0 && selfActions.size() == 1) {
				// 顶层context只有一个选择，就不再模拟了
				bestSelfAction = selfActions.iterator().next();
				selfWinProb = -1d; // 胜率没有意义
				doneSubmitter.accept(this);
				return;
			}

			long time1 = System.currentTimeMillis();

			// 生成finalProbs
			genFinalProbs();

			long time2 = System.currentTimeMillis();

			// 拆分成新的contexts
			// 遇到和牌的新context，则直接将其提交为done状态，否则提交拆分
			Map<ActionAndLocation, FooSimContext> nextContexts = finalProbs
					.values().stream().flatMap(map -> map.keySet().stream())
					.distinct()
					.collect(Collectors.toMap(Function.identity(), al -> {
						FooSimContext nextContext = new FooSimContext(this);
						try {
							gameTool.doAction(nextContext, al.getLocation(),
									al.getAction());
							return nextContext;
						} catch (Exception e) {
							logger.log(Level.SEVERE, "Illegal action: " + al,
									e);
							return null;
						}
					}));
			this.nextContexts = splitSubmitter.apply(this, nextContexts);

			long time3 = System.currentTimeMillis();

			logger.info("TIME " + (time3 - time2) + " " + (time2 - time1) + " "
					+ (time1 - startTime));
		} catch (InterruptedException e) {
		}
	}

	// finalProbs中，不能出现同一个位置、同一个动作、tiles是相同type但不同id的情况，
	// 否则后面会生成重复的下层context，浪费资源
	private void genFinalProbs() {
		finalProbs = new HashMap<>();
		if (selfActions.isEmpty()) {
			// 本家没有动作可做，key记为null
			Map<ActionAndLocation, Double> actionAndProbs = new HashMap<>();
			finalProbs.put(null, actionAndProbs);
			tileTypeAndProbs().forEach((discardTileType, prob) -> {
				ActionAndLocation othersDiscard = new ActionAndLocation(
						new Action(FooSimOthersDiscardActionType.type(),
								tile(discardTileType)),
						locationCanDiscard);
				actionAndProbs.put(othersDiscard, prob);
			});
		} else
			selfActions.forEach(selfAction -> {
				if (locationCanDiscard == null) {
					// 非本家不可以出牌，本家的动作就是唯一的finalAction
					putSelfAction(selfAction);
				} else {
					// 某非本家可以出牌，使用策略的comparator决定finalAction
					ActionTypeAndLocation selfAtl = new ActionTypeAndLocation(
							selfAction.getType(), selfLocation, this);
					ActionTypeAndLocation othersAtl = new ActionTypeAndLocation(
							FooSimOthersDiscardActionType.type(),
							locationCanDiscard, this);
					int c = getGameStrategy().getActionPriorityComparator()
							.compare(selfAtl, othersAtl);
					if (c < 0)
						putSelfAction(selfAction);
					else if (c > 0) {
						Map<ActionAndLocation, Double> actionAndProbs = new HashMap<>();
						finalProbs.put(selfAction, actionAndProbs);
						tileTypeAndProbs().forEach((discardTileType, prob) -> {
							ActionAndLocation othersDiscard = new ActionAndLocation(
									new Action(
											FooSimOthersDiscardActionType
													.type(),
											tile(discardTileType)),
									locationCanDiscard);
							actionAndProbs.put(othersDiscard, prob);
						});
					} else
						throw new RuntimeException(
								"Equal to sim discard: " + selfAction);
				}

			});
	}

	// 把指定的本家动作塞入finalProbs，如果是摸牌则进行转换拆分为SimDraw
	private void putSelfAction(Action selfAction) {
		if (!DRAW.matchBy(selfAction.getType())) {
			finalProbs.put(selfAction, Collections.singletonMap(
					new ActionAndLocation(selfAction, selfLocation), 1d));
		} else {
			Map<ActionAndLocation, Double> actionAndProbs = new HashMap<>();
			finalProbs.put(selfAction, actionAndProbs);
			tileTypeAndProbs().forEach((discardTileType, prob) -> {
				ActionAndLocation selfSimDraw = new ActionAndLocation(
						new Action(FooSimSelfDrawActionType.type(),
								tile(discardTileType)),
						selfLocation);
				actionAndProbs.put(selfSimDraw, prob);
			});
		}
	}

	private Map<TileType, Double> tileTypeAndProbs() {
		if (tileTypeAndProbs == null) {
			// 除了本家手牌、所有玩家groups、已经打出的牌
			Set<Tile> existTiles = new HashSet<>();
			existTiles.addAll(
					getPlayerInfoByLocation(selfLocation).getAliveTiles());
			getTable().getPlayerInfos().values().forEach(playerInfo -> {
				playerInfo.getTileGroups().stream()
						// XXX - 写死了暗杠不应该看到
						.filter(group -> group.getType() != ANGANG_GROUP)
						.map(TileGroup::getTiles).forEach(existTiles::addAll);
			});
			AtomicInteger tileCount = new AtomicInteger();
			tileTypeAndTiles = getGameStrategy().getAllTiles().stream()
					.filter(tile -> !existTiles.contains(tile))
					.peek(tile -> tileCount.incrementAndGet())
					.collect(Collectors.groupingBy(Tile::getType));
			tileTypeAndProbs = new HashMap<>();
			tileTypeAndTiles.forEach((tileType, tiles) -> tileTypeAndProbs
					.put(tileType, (double) tiles.size() / tileCount.get()));
		}
		return tileTypeAndProbs;
	}

	private Tile tile(TileType type) {
		List<Tile> tiles = tileTypeAndTiles.get(type);
		if (tiles.isEmpty())
			throw new RuntimeException("No tile for type: " + type);
		return tiles.get(0);
	}

	public int getLevel() {
		return level;
	}

	public Map<Action, Map<ActionAndLocation, Double>> getFinalProbs() {
		return finalProbs;
	}

	public Map<ActionAndLocation, FooSimContext> getNextContexts() {
		return nextContexts;
	}

	public Map<Action, Double> getSelfActionAndWinProb() {
		return selfActionAndWinProb;
	}

	public void setSelfActionAndWinProb(
			Map<Action, Double> selfActionAndWinProb) {
		this.selfActionAndWinProb = selfActionAndWinProb;
	}

	public Double getSelfWinProb() {
		return selfWinProb;
	}

	public void setSelfWinProb(Double selfWinProb) {
		this.selfWinProb = selfWinProb;
	}

	public Action getBestSelfAction() {
		return bestSelfAction;
	}

	public void setBestSelfAction(Action bestSelfAction) {
		this.bestSelfAction = bestSelfAction;
	}

	public void decreaseWallSize() {
		((FooMahjongTable) getTable()).wallSize--;
	}

	// 上一个动作位置、tileTypeAndTiles、本家手牌和groups、本家可以做的动作
	public int contextHash() {
		if (tileTypeAndTiles == null)
			throw new IllegalStateException("tileTypeAndTiles not exists.");

		final int prime = 31;
		int result = 1;
		result = prime * result + getLastAction().hashCode();
		result = prime * result + tileTypeAndTiles.hashCode();
		PlayerInfo myInfo = getPlayerInfoByLocation(selfLocation);
		result = prime * result + myInfo.getAliveTiles().hashCode();
		for (TileGroup group : myInfo.getTileGroups()) {
			result = prime * result + group.getTiles().hashCode();
			result = prime * result + group.getType().hashCode();
		}
		if (selfActions != null) {
			for (Action action : selfActions) {
				result = prime * result + action.getType().hashCode();
				for (Tile tile : action.getTiles())
					result = prime * result + tile.getType().hashCode();
			}
		}
		return result;
	}

	@Override
	protected PlayerView newPlayerView(PlayerLocation location) {
		return new FooPlayerView(location);
	}

	private class FooPlayerView extends GameContext.PlayerView {

		FooPlayerView(PlayerLocation location) {
			super(location);
		}

		FooSimContext getContext() {
			return FooSimContext.this;
		}

	}

	private static class FooMahjongTable extends MahjongTable {

		private int wallSize;

		public FooMahjongTable(MahjongTable.PlayerView tableView,
				PlayerInfo myInfo) {
			setInitBottomSize(tableView.getInitBottomSize());
			setDrawedBottomSize(tableView.getDrawedBottomSize());
			Map<PlayerLocation, PlayerInfo> playerInfos = new EnumMap<>(
					PlayerLocation.class);
			tableView.getPlayerInfoView()
					.forEach((location, infoView) -> playerInfos.put(location,
							location == tableView.getMyLocation()
									? new FooSimPlayerInfo(myInfo)
									: new FooSimPlayerInfo(infoView)));
			setPlayerInfos(playerInfos);
		}

		public FooMahjongTable(FooMahjongTable table) {
			setInitBottomSize(table.getInitBottomSize());
			setDrawedBottomSize(table.getDrawedBottomSize());
			Map<PlayerLocation, PlayerInfo> playerInfos = new EnumMap<>(
					PlayerLocation.class);
			table.getPlayerInfos()
					.forEach((location, info) -> playerInfos.put(location,
							new FooSimPlayerInfo((FooSimPlayerInfo) info)));
			setPlayerInfos(playerInfos);
		}

		@Override
		public int getTileWallSize() {
			return wallSize;
		}

		@Override
		public List<Tile> draw(int count) {
			// 没有tileWall，下同
			throw new UnsupportedOperationException();
		}

		@Override
		public List<Tile> drawBottom(int count) {
			throw new UnsupportedOperationException();
		}
	}

	private static class FooSimPlayerInfo extends PlayerInfo {
		/**
		 * 新建顶层context的非本家信息。
		 */
		public FooSimPlayerInfo(PlayerInfo.PlayerView infoView) {
			setPlayer(new FooSimOthers());
			setDiscardedTiles(new ArrayList<>(infoView.getDiscardedTiles()));
			setTileGroups(infoView.getTileGroups());
			setTing(infoView.isTing());
		}

		/**
		 * 新建顶层context的本家信息。
		 */
		public FooSimPlayerInfo(PlayerInfo info) {
			setPlayer(new FooSimSelf());
			setAliveTiles(new HashSet<>(info.getAliveTiles()));
			setLastDrawedTile(info.getLastDrawedTile());
			setDiscardedTiles(new ArrayList<>(info.getDiscardedTiles()));
			setTileGroups(new ArrayList<>(info.getTileGroups()));
			setTing(info.isTing());
		}

		/**
		 * 新建非顶层context的玩家信息。
		 */
		public FooSimPlayerInfo(FooSimPlayerInfo infoOfLastContext) {
			setPlayer(infoOfLastContext.getPlayer());
			setAliveTiles(infoOfLastContext.getAliveTiles() == null ? null
					: new HashSet<>(infoOfLastContext.getAliveTiles()));
			setLastDrawedTile(infoOfLastContext.getLastDrawedTile());
			setDiscardedTiles(
					new ArrayList<>(infoOfLastContext.getDiscardedTiles()));
			setTileGroups(new ArrayList<>(infoOfLastContext.getTileGroups()));
			setTing(infoOfLastContext.isTing());
		}

		@Override
		public Set<Tile> getAliveTiles() {
			Set<Tile> aliveTiles = super.getAliveTiles();
			if (aliveTiles == null)
				throw new UnsupportedOperationException();
			return aliveTiles;
		}

	}

	static class FooSimOthers implements Player {
		@Override
		public String getName() {
			return "FooSimOthers";
		}

		@Override
		public Action chooseAction(PlayerView contextView,
				Set<ActionType> actionTypes, Action illegalAction)
				throws InterruptedException {
			FooSimContext context = ((FooPlayerView) contextView).getContext();

			// 只考虑SimWin和SimDiscard
			if (actionTypes.contains(FooSimWinActionType.type()))
				context.othersCanWin = true;
			if (actionTypes.contains(FooSimOthersDiscardActionType.type())) {
				if (context.locationCanDiscard != null)
					throw new RuntimeException(
							"locationCanDiscard already exists: "
									+ context.locationCanDiscard);
				context.locationCanDiscard = contextView.getMyLocation();
			}

			// 返回empty action
			return FooSimEmptyActionType.action();
		}

		@Override
		public void actionDone(PlayerView contextView,
				PlayerLocation actionLocation, Action action) {
		}

		@Override
		public void timeLimit(PlayerView contextView, Integer secondsToGo) {
		}

	}

	static class FooSimSelf implements Player {
		@Override
		public String getName() {
			return "FooSimSelf";
		}

		@Override
		public Action chooseAction(PlayerView contextView,
				Set<ActionType> actionTypes, Action illegalAction)
				throws InterruptedException {
			FooSimContext context = ((FooPlayerView) contextView).getContext();

			// 在selfAction里塞上所有的可选动作（TileType去重）（SimDiscard除外）
			Set<Action> selfActions = new HashSet<Action>();
			context.selfActions = selfActions;
			actionTypes.forEach(actionType -> {
				if (actionType
						.getClass() == FooSimOthersDiscardActionType.class)
					return;
				if (actionType.getClass() == FooSimSelfDrawActionType.class)
					return;
				Set<Set<TileType>> typesDistinct = new HashSet<>();
				actionType.getLegalActionTiles(contextView).forEach(tiles -> {
					Set<TileType> types = tiles.stream().map(Tile::getType)
							.collect(Collectors.toSet());
					if (typesDistinct.add(types))
						selfActions.add(new Action(actionType, tiles));
				});
			});

			// 返回empty action
			return FooSimEmptyActionType.action();
		}

		@Override
		public void actionDone(PlayerView contextView,
				PlayerLocation actionLocation, Action action) {
		}

		@Override
		public void timeLimit(PlayerView contextView, Integer secondsToGo) {
		}

	}

}
