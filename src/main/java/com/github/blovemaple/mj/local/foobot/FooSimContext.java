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
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.rule.TimeLimitStrategy;

/**
 * TODO
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class FooSimContext extends GameContext implements Runnable {

	private MahjongGame gameTool;
	private BiConsumer<FooSimContext, Collection<FooSimContext>> splitSubmitter;
	private Consumer<FooSimContext> doneSubmitter;

	/**
	 * 此状态的层级，即达到此状态已经模拟的动作数。层级小的会先处理。
	 */
	private int level;

	/**
	 * 进行模拟时记录的，达到此状态前的最后一个模拟动作。
	 */
	private ActionAndLocation lastSimActionAndLocation;

	/**
	 * 进行模拟后得出的下一个动作是否是被动的（即概率性的，别家出牌或本家摸牌）。
	 */
	private boolean isNextActionPassive;
	/**
	 * 进行模拟后得出的下一个动作（如果是被动的）每种牌型的概率。
	 */
	private Map<TileType, Double> nextDiscardAndProb;
	/**
	 * 进行模拟后得出此状态下其他玩家直接和牌的概率。
	 */
	private Map<PlayerLocation.Relation, Double> otherPlayerWinProb;

	/**
	 * 计算结果时记录的每种下一个动作的胜率。
	 */
	private Map<Action, Double> nextActionAndWinProb;

	/**
	 * 计算结果时得出的此context状态下的胜率。<br>
	 * 如果模拟动作时已经确定当前状态下可以和牌或流局，则模拟动作时写入（这样的情况即此context是最底层的）。
	 */
	private double winProb;
	/**
	 * 计算结果时得出的保证胜率需要做出的主动动作。如果此状态下需要做的动作是被动的，或没有后续动作，则为null。（null也表示主动但放弃）<br>
	 * 如果模拟动作时已经确定当前状态下可以和牌或流局，则模拟动作时写入。
	 */
	private Action bestAction;

	public FooSimContext(GameContext.PlayerView realContextView, MahjongGame gameTool,
			BiConsumer<FooSimContext, Collection<FooSimContext>> splitSubmitter,
			Consumer<FooSimContext> doneSubmitter) {
		super(new FooMahjongTable(realContextView.getTableView(), realContextView.getMyInfo()),
				gameTool.getGameStrategy(), TimeLimitStrategy.NO_LIMIT);
		setZhuangLocation(realContextView.getZhuangLocation());
		// XXX - doneActions列表比较大，可能导致耗资源过多
		setDoneActions(new ArrayList<>(realContextView.getDoneActions()));
		setGameResult(realContextView.getGameResult());

		this.gameTool = gameTool;
		this.splitSubmitter = splitSubmitter;
		this.doneSubmitter = doneSubmitter;
	}

	public FooSimContext(FooSimContext lastContext) {
		super(new FooMahjongTable((FooMahjongTable) lastContext.getTable()), lastContext.getGameStrategy(),
				lastContext.getTimeLimitStrategy());
		setZhuangLocation(lastContext.getZhuangLocation());
		// XXX - doneActions列表比较大，可能导致耗资源过多
		setDoneActions(new ArrayList<>(lastContext.getDoneActions()));
		setGameResult(lastContext.getGameResult());

		this.gameTool = lastContext.gameTool;
		this.splitSubmitter = lastContext.splitSubmitter;
		this.doneSubmitter = lastContext.doneSubmitter;
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
		} catch (InterruptedException e) {
		}
	}

	protected int getLevel() {
		return level;
	}

	protected void setLevel(int level) {
		this.level = level;
	}

	@Override
	public ActionAndLocation getLastActionAndLocation() {
		return lastSimActionAndLocation != null ? lastSimActionAndLocation : super.getLastActionAndLocation();
	}

	public boolean isNextActionPassive() {
		return isNextActionPassive;
	}

	public void setNextActionPassive(boolean isNextActionPassive) {
		this.isNextActionPassive = isNextActionPassive;
	}

	public Map<TileType, Double> getNextDiscardAndProb() {
		return nextDiscardAndProb;
	}

	public void setNextDiscardAndProb(Map<TileType, Double> nextDiscardAndProb) {
		this.nextDiscardAndProb = nextDiscardAndProb;
	}

	public Map<PlayerLocation.Relation, Double> getOtherPlayerWinProb() {
		return otherPlayerWinProb;
	}

	public void setOtherPlayerWinProb(PlayerLocation.Relation relation, Double winProb) {
		// TODO
	}

	public Map<Action, Double> getNextActionAndWinProb() {
		return nextActionAndWinProb;
	}

	public void setNextActionAndWinProb(Map<Action, Double> nextActionAndWinProb) {
		this.nextActionAndWinProb = nextActionAndWinProb;
	}

	public double getWinProb() {
		return winProb;
	}

	public void setWinProb(double winProb) {
		this.winProb = winProb;
	}

	public Action getBestAction() {
		return bestAction;
	}

	public void setBestAction(Action bestAction) {
		this.bestAction = bestAction;
	}

	private static class FooMahjongTable extends MahjongTable {

		private int wallSize;

		public FooMahjongTable(MahjongTable.PlayerView tableView, PlayerInfo myInfo) {
			setInitBottomSize(tableView.getInitBottomSize());
			setDrawedBottomSize(tableView.getDrawedBottomSize());
			Map<PlayerLocation, PlayerInfo> playerInfos = new EnumMap<>(PlayerLocation.class);
			tableView.getPlayerInfoView()
					.forEach((location, infoView) -> playerInfos.put(location, location == tableView.getMyLocation()
							? new FooSimPlayerInfo(myInfo) : new FooSimPlayerInfo(infoView)));
			setPlayerInfos(playerInfos);
		}

		public FooMahjongTable(FooMahjongTable table) {
			setInitBottomSize(table.getInitBottomSize());
			setDrawedBottomSize(table.getDrawedBottomSize());
			Map<PlayerLocation, PlayerInfo> playerInfos = new EnumMap<>(PlayerLocation.class);
			table.getPlayerInfos().forEach(
					(location, info) -> playerInfos.put(location, new FooSimPlayerInfo((FooSimPlayerInfo) info)));
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
		 * 初始化非本家。
		 */
		public FooSimPlayerInfo(PlayerInfo.PlayerView infoView) {
			setPlayer(new FooSimOthers());
			setDiscardedTiles(new ArrayList<>(infoView.getDiscardedTiles()));
			setTileGroups(infoView.getTileGroups());
			setTing(infoView.isTing());
		}

		/**
		 * 初始化本家。
		 */
		public FooSimPlayerInfo(PlayerInfo info) {
			setPlayer(new FooSimSelf());
			setAliveTiles(new HashSet<>(info.getAliveTiles()));
			setLastDrawedTile(info.getLastDrawedTile());
			setDiscardedTiles(new ArrayList<>(info.getDiscardedTiles()));
			setTileGroups(new ArrayList<>(info.getTileGroups()));
			setTing(info.isTing());
		}

		public FooSimPlayerInfo(FooSimPlayerInfo info) {
			setPlayer(info.getPlayer());
			setAliveTiles(info.getAliveTiles() == null ? null : new HashSet<>(info.getAliveTiles()));
			setLastDrawedTile(info.getLastDrawedTile());
			setDiscardedTiles(new ArrayList<>(info.getDiscardedTiles()));
			setTileGroups(new ArrayList<>(info.getTileGroups()));
			setTing(info.isTing());
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
		public Action chooseAction(PlayerView contextView, Set<ActionType> actionTypes, Action illegalAction)
				throws InterruptedException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void actionDone(PlayerView contextView, PlayerLocation actionLocation, Action action) {
			// TODO Auto-generated method stub

		}

		@Override
		public void timeLimit(PlayerView contextView, Integer secondsToGo) {
			// TODO Auto-generated method stub

		}

	}

	static class FooSimOthers implements Player {

		@Override
		public String getName() {
			return "FooSimOthers";
		}

		@Override
		public Action chooseAction(PlayerView contextView, Set<ActionType> actionTypes, Action illegalAction)
				throws InterruptedException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void actionDone(PlayerView contextView, PlayerLocation actionLocation, Action action) {
			// TODO Auto-generated method stub

		}

		@Override
		public void timeLimit(PlayerView contextView, Integer secondsToGo) {
			// TODO Auto-generated method stub

		}

	}

}
