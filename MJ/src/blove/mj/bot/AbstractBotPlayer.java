package blove.mj.bot;

import java.util.Set;

import blove.mj.Cpk;
import blove.mj.CpkType;
import blove.mj.GameBoardView;
import blove.mj.PlayerLocation;
import blove.mj.Tile;
import blove.mj.TileType;
import blove.mj.board.GameBoard;
import blove.mj.board.GameBoardFullException;
import blove.mj.board.PlayerTiles;
import blove.mj.event.GameEventListener;
import blove.mj.event.GameOverEvent;
import blove.mj.event.GameStartEvent;
import blove.mj.event.PlayerActionEvent;
import blove.mj.event.PlayerActionEvent.ActionType;
import blove.mj.event.PlayerEvent;
import blove.mj.event.PlayerEvent.PlayerEventType;
import blove.mj.event.TimeLimitEvent;
import blove.mj.rules.TimeLimitStrategy;
import blove.mj.rules.WinStrategy;

/**
 * 简化的机器人用户接口
 * 
 * @author blovemaple
 */
public abstract class AbstractBotPlayer implements BotPlayer {
	private final String name;

	private GameBoardView gameBoardView;
	private WinStrategy winStrategy;
	private TimeLimitStrategy timeStrategy;
	private PlayerLocation myLocation;
	private PlayerTiles myTiles;

	/**
	 * 新建一个实例。
	 * 
	 * @param name
	 *            名字
	 */
	public AbstractBotPlayer(String name) {
		this.name = name;
	}

	@Override
	public synchronized void join(GameBoard gameBoard)
			throws GameBoardFullException {
		if (gameBoardView != null)
			throw new IllegalStateException("已经加入一个游戏桌");
		gameBoardView = gameBoard.newPlayer(name);
		gameBoardView.addGameEventListener(eventListener);
		winStrategy = gameBoard.getWinStrategy();
		timeStrategy = gameBoard.getTimeLimitStrategy();
		myLocation = gameBoardView.getMyLocation();
		myTiles = gameBoardView.getMyTiles();

		gameBoardView.readyForGame();
	}

	@Override
	public synchronized void leave() {
		if (gameBoardView == null)
			throw new IllegalStateException("没有加入游戏桌");
		gameBoardView.removeGameEventListener(eventListener);
		gameBoardView.leave();
		gameBoardView = null;
	}

	private GameEventListener eventListener = new GameEventListener() {

		private Set<TileType> winChances;

		@Override
		public void newEvent(PlayerEvent event) {
			if (event.getType() == PlayerEventType.OUT)
				gameBoardView.readyForGame();
		}

		@Override
		public void newEvent(GameStartEvent event) {
		}

		@Override
		public void newEvent(PlayerActionEvent event) {
			// deal over:检查是否杠/和牌，如果有询问操作，执行操作，如果无操作询问出牌
			// draw，自己：检查是否杠/和牌，如果有询问操作，执行操作，如果无操作询问出牌
			// discard，自己：记录和牌机会
			// discard，别人：检查吃/碰/杠/和机会，如果有询问操作，执行操作
			// cp，自己：检查是否杠/和牌，如果有询问操作，执行操作，如果无操作询问出牌

			win = false;
			readyHand = false;

			Tile eventTile = event.getTile(gameBoardView);

			if (event.getType() == ActionType.DEAL_OVER
					|| (event.getType() == ActionType.DRAW && event
							.getPlayerLocation() == myLocation)
					|| (event.getType() == ActionType.CPK
							&& event.getPlayerLocation() == myLocation && !event
							.getCpk().getType().isKong())
					|| (event.getType() == ActionType.DISCARD && event
							.getPlayerLocation() != myLocation)) {
				// 发牌结束、自己摸牌、自己吃/碰：
				// 检查是否杠/和牌，如果有询问操作，执行操作，如果无操作询问出牌
				// 别人出牌：
				// 检查吃/碰/杠/和机会，如果有询问操作，执行操作
				boolean winChance;
				if (event.getType() == ActionType.DEAL_OVER)
					winChance = winStrategy.isWin(myTiles);
				else
					winChance = winChances.contains(eventTile.getType());

				Set<Cpk> cpkChances = CpkType.getAllChances(myTiles, eventTile,
						myLocation.getRelationOf(event.getPlayerLocation()));

				Cpk cpkChoose = chooseCpk(myTiles, eventTile, cpkChances,
						winChance);
				if (cpkChoose != null)
					gameBoardView.cpk(cpkChoose);
				else if (win)
					gameBoardView.win();
				else {
					gameBoardView.giveUpCpkw();

					if (event.getType() != ActionType.DISCARD) {
						Tile discardTile = chooseDiscard(myTiles);
						gameBoardView.discard(discardTile, readyHand);
					}
				}
			} else if (event.getType() == ActionType.DISCARD
					&& event.getPlayerLocation() == myLocation) {
				// 自己出牌：
				// 记录和牌机会
				winChances = winStrategy.getWinChances(myTiles);
			}

		}

		@Override
		public void newEvent(TimeLimitEvent event) {
		}

		@Override
		public void newEvent(GameOverEvent event) {
			gameBoardView.readyForGame();
		}
	};

	private boolean win;
	private boolean readyHand;

	/**
	 * 从机会中选择吃/碰/杠/和牌。如果选择吃/碰/杠，则返回响应对象；如果选择和牌，则调用{@link #win()}
	 * ，并返回null；如果选择放弃，则直接返回null。
	 * 
	 * @param myTiles
	 *            自己的牌
	 * @param newTile
	 *            其他玩家打出的牌，或自己刚摸的牌
	 * @param cpkChances
	 *            吃/碰/杠机会
	 * @param winChance
	 *            是否可以和牌
	 * @return 见上述说明
	 */
	protected abstract Cpk chooseCpk(PlayerTiles myTiles, Tile newTile,
			Set<Cpk> cpkChances, boolean winChance);

	/**
	 * 在{@link #chooseCpk(PlayerTiles, Tile, Set, boolean)}方法中选择和牌时调用。
	 */
	protected void win() {
		win = true;
	}

	/**
	 * 选择一张牌打出。如果听牌，则调用{@link #readyHand()}方法。
	 * 
	 * @param myTiles
	 *            自己的牌
	 * @return 打出的牌
	 */
	protected abstract Tile chooseDiscard(PlayerTiles myTiles);

	/**
	 * 在{@link #chooseDiscard(PlayerTiles)}方法中听牌时调用。
	 */
	protected void readyHand() {
		readyHand = true;
	}

	/**
	 * 返回和牌策略。
	 * 
	 * @return 和牌策略
	 */
	protected WinStrategy getWinStrategy() {
		return winStrategy;
	}

	/**
	 * 返回限时策略。
	 * 
	 * @return 限时策略
	 */
	protected TimeLimitStrategy getTimeStrategy() {
		return timeStrategy;
	}

}
