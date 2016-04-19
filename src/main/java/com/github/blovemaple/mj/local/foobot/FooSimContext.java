package com.github.blovemaple.mj.local.foobot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionAndLocation;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.MahjongGame;
import com.github.blovemaple.mj.object.MahjongTable;
import com.github.blovemaple.mj.object.Player;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.rule.TimeLimitStrategy;

/**
 * TODO <br>
 * 一个FooSimContext的生命周期：
 * <li>根据上一个context新建——顶层由FooBot，非顶层由上一个FooSimContext；
 * <li>（非顶层）模拟一步，到达此context需要表示的状态——上一个FooSimContext；
 * <li>作为任务被提交，等待处理——顶层由FooBot，非顶层由上一个FooSimContext；
 * <li>开始处理，让模拟玩家选择动作——gameTool（MahjongGame）；
 * <li>模拟玩家给出可能的动作以及概率——模拟玩家；
 * <li>将此context进行分割或结束模拟——上一个FooSimContext。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class FooSimContext extends GameContext implements Runnable {

	private MahjongGame gameTool;
	private BiConsumer<FooSimContext, Collection<FooSimContext>> splitSubmitter;
	private Consumer<FooSimContext> doneSubmitter;

	/**
	 * 上一个context。
	 */
	private FooSimContext lastContext;
	/**
	 * 此状态的层级，即达到此状态已经模拟的动作数。层级小的会先处理。
	 */
	private int level;

	/**
	 * 上一个context进行处理时记录的，达到此状态前的最后一个模拟动作。
	 */
	private ActionAndLocation lastSimActionAndLocation;

	// 进行处理时产生以下信息

	/**
	 * 模拟非本家选择动作时记录的，每个模拟玩家可能选择的动作，以及可能性。
	 */
	private Map<PlayerLocation, Map<Action, Double>> othersActionProbs;
	/**
	 * 模拟本家选择动作时记录的，本家可以选择的动作集合。
	 */
	private Set<Action> selfActions;
	/**
	 * 上面两个集合都记录完毕后，算出的：本家选择动作 -> (最终决定动作 -> 决定动作的概率)
	 */
	private Map<Action, Map<ActionAndLocation, Double>> finalProbs;
	/**
	 * 是否已进行处理，即分割或结束。
	 */
	private boolean handled = false;

	/**
	 * 进行处理时得出的，此状态下其他玩家直接和牌的概率。
	 */
	private Map<PlayerLocation.Relation, Double> otherPlayerWinProb;

	// 计算结果时产生以下信息

	/**
	 * 计算结果时记录的，finalProbs中的每种下一个动作进行后本家的胜率。
	 */
	private Map<ActionAndLocation, Double> nextActionAndWinProb;

	/**
	 * 计算结果时得出的此context状态下的胜率。<br>
	 * 如果模拟动作时已经确定当前状态下本家可以和牌或流局，则模拟动作时写入（这样的情况即此context是最底层的）。
	 */
	private double selfWinProb;
	/**
	 * 计算结果时得出的保证胜率本家需要做出的主动动作，即finalProbs的一个key。<br>
	 * 如果模拟动作时已经确定当前状态下本家可以和牌或流局，则模拟动作时写入。
	 */
	private Action bestSelfAction;

	public FooSimContext(GameContext.PlayerView realContextView,
			MahjongGame gameTool,
			BiConsumer<FooSimContext, Collection<FooSimContext>> splitSubmitter,
			Consumer<FooSimContext> doneSubmitter) {
		super(new FooMahjongTable(realContextView.getTableView(),
				realContextView.getMyInfo()), gameTool.getGameStrategy(),
				TimeLimitStrategy.NO_LIMIT);
		setZhuangLocation(realContextView.getZhuangLocation());
		// XXX - doneActions列表比较大，可能导致耗资源过多
		setDoneActions(new ArrayList<>(realContextView.getDoneActions()));
		setGameResult(realContextView.getGameResult());

		this.gameTool = gameTool;
		this.splitSubmitter = splitSubmitter;
		this.doneSubmitter = doneSubmitter;

		this.level = 0;
	}

	public FooSimContext(FooSimContext lastContext) {
		super(new FooMahjongTable((FooMahjongTable) lastContext.getTable()),
				lastContext.getGameStrategy(),
				lastContext.getTimeLimitStrategy());
		setZhuangLocation(lastContext.getZhuangLocation());
		// XXX - doneActions列表比较大，可能导致耗资源过多
		setDoneActions(new ArrayList<>(lastContext.getDoneActions()));
		setGameResult(lastContext.getGameResult());

		this.gameTool = lastContext.gameTool;
		this.splitSubmitter = lastContext.splitSubmitter;
		this.doneSubmitter = lastContext.doneSubmitter;

		this.lastContext = lastContext;
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
		// XXX - doneActions列表比较大，可能导致耗资源过多
		super.actionDone(action, location);
		lastSimActionAndLocation = new ActionAndLocation(action, location);
	}

	/**
	 * 让gameTool对此context选择一次后续动作，由模拟玩家对此context进行处理。
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			gameTool.chooseAction(this);
			// TODO
		} catch (InterruptedException e) {
		}
	}

	public int getLevel() {
		return level;
	}

	@Override
	public ActionAndLocation getLastActionAndLocation() {
		return lastSimActionAndLocation != null ? lastSimActionAndLocation
				: super.getLastActionAndLocation();
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
			setDiscardedTiles(new ArrayList<>(infoOfLastContext.getDiscardedTiles()));
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

	static class FooSimSelf implements Player {

		@Override
		public String getName() {
			return "FooSimSelf";
		}

		@Override
		public Action chooseAction(PlayerView contextView,
				Set<ActionType> actionTypes, Action illegalAction)
				throws InterruptedException {
			// 如果可以和，则模拟结束
			// 非摸牌动作作为主动任务分割
			// 摸牌动作作为被动任务分割
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void actionDone(PlayerView contextView,
				PlayerLocation actionLocation, Action action) {
		}

		@Override
		public void timeLimit(PlayerView contextView, Integer secondsToGo) {
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
			// 如果可以SimWin（符合和牌的前提条件），则先估算和牌概率，写入上一个context

			// 如果可以SimDiscard，则分割

			// TODO Auto-generated method stub
			return null;
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
