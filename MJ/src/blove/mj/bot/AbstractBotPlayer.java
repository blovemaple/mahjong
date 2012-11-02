package blove.mj.bot;

import java.util.Set;

import blove.mj.Cpk;
import blove.mj.CpkType;
import blove.mj.GameBoardView;
import blove.mj.PlayerLocation;
import blove.mj.Tile;
import blove.mj.PlayerLocation.Relation;
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

		@Override
		public void newEvent(PlayerEvent event) {
			if (event.getType() == PlayerEventType.OUT)
				gameBoardView.readyForGame();
		}

		@Override
		public void newEvent(GameStartEvent event) {
		}

		@Override
		// XXX - 代码重复：
		// 与blove.mj.cli.CliGame.CliGameListener.newEvent(PlayerActionEvent)有代码重复
		public void newEvent(PlayerActionEvent event) {
			try {
				win = false;
				readyHand = false;

				Tile eventTile = event.getTile(gameBoardView);
				PlayerLocation eventLocation = event.getPlayerLocation();

				if (myTiles.isForDiscarding()) {
					// 应该打牌

					// 如果不是吃/碰之后，则检查是否已和牌
					boolean winChance = false;
					if (event.getType() != ActionType.CPK)
						winChance = winStrategy.isWin(myTiles);

					// 检查杠牌机会
					Set<Cpk> kongChances = CpkType.getAllChances(myTiles,
							eventTile, Relation.SELF);

					boolean winOrKong = false;// 是否选择了和牌或杠牌
					if (winChance || !kongChances.isEmpty()) {
						// 如果和牌或有杠牌机会，则询问并执行选择的操作
						Cpk cpkChoose = chooseCpk(myTiles, eventTile,
								kongChances, winChance);
						if (cpkChoose != null) {
							gameBoardView.cpk(cpkChoose);
							winOrKong = true;
						} else if (win) {
							gameBoardView.win();
							winOrKong = true;
						}
					}
					if (!winOrKong) {
						// 如果没有机会或没有选择操作，询问打出一张牌
						Tile discardTile = chooseDiscard(myTiles);
						gameBoardView.discard(discardTile, readyHand);
					}
				} else {
					// 不应该打牌
					if (event.getType() == ActionType.DISCARD) {
						// 别人打出了一张牌，检查是否有和牌或吃/碰/杠机会
						boolean winChance = winStrategy.getWinChances(myTiles)
								.contains(eventTile);
						Set<Cpk> cpkChances = CpkType.getAllChances(myTiles,
								eventTile,
								myLocation.getRelationOf(eventLocation));
						if (winChance || !cpkChances.isEmpty()) {
							// 如果有和牌或吃/碰/杠机会，则询问并执行选择的操作
							Cpk cpkChoose = chooseCpk(myTiles, eventTile,
									cpkChances, winChance);
							if (cpkChoose != null)
								gameBoardView.cpk(cpkChoose);
							else if (win)
								gameBoardView.win();
							else
								// 如果没有选择，则选择放弃
								gameBoardView.giveUpCpkw();
						}

					}
				}
			} catch (InterruptedException e) {
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
	 * @throws InterruptedException
	 */
	protected abstract Cpk chooseCpk(PlayerTiles myTiles, Tile newTile,
			Set<Cpk> cpkChances, boolean winChance) throws InterruptedException;

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
	 * @throws InterruptedException
	 */
	protected abstract Tile chooseDiscard(PlayerTiles myTiles)
			throws InterruptedException;

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
