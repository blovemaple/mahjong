package blove.mj.cli;

import java.util.Set;
import java.util.TreeSet;

import blove.mj.Cpk;
import blove.mj.CpkType;
import blove.mj.GameBoardView;
import blove.mj.Player;
import blove.mj.PlayerLocation;
import blove.mj.Tile;
import blove.mj.TileType;
import blove.mj.board.GameBoard;
import blove.mj.board.GameBoardFullException;
import blove.mj.board.PlayerTiles;
import blove.mj.cli.CliView.CharHandler;
import blove.mj.event.GameEventListener;
import blove.mj.event.GameOverEvent;
import blove.mj.event.GameStartEvent;
import blove.mj.event.PlayerActionEvent;
import blove.mj.event.PlayerActionEvent.ActionType;
import blove.mj.event.PlayerEvent;
import blove.mj.event.PlayerEvent.PlayerEventType;
import blove.mj.event.TimeLimitEvent;
import blove.mj.rules.WinStrategy;

/**
 * 命令行界面游戏。此类实例的活动负责从进入一个游戏桌到退出游戏桌的过程。
 * 
 * @author blovemaple
 */
public class CliGame {
	private final CliMessager messager;

	private boolean inGameBoard;// 进入游戏桌前置true，退出游戏桌时置false
	private Object inGameBoardWaiter = new Object();// play方法在此对象上等待，退出游戏桌时被唤醒
	private GameBoard gameBoard;
	private GameBoardView gameBoardView;

	private final GameEventListener gameListener = new CliGameListener();

	/**
	 * 新建一个实例。
	 * 
	 * @param cliView
	 *            命令行显示
	 */
	public CliGame(CliView cliView) {
		this.messager = new CliMessager(cliView);
		this.messager.addCharHandler(generalCharHandler);
	}

	private CharHandler generalCharHandler = new CharHandler() {
		boolean quitQuerying = false;

		@Override
		public boolean handle(char c) {
			switch (c) {
			case 'q':
				messager.tempStatus("Do you want to quit game?(y/n)");
				quitQuerying = true;
				break;
			case 'y':
				if (quitQuerying) {
					gameBoardView.leave();
					quitQuerying = false;
					inGameBoardWaiter.notifyAll();
				}
				break;
			default:
				if (quitQuerying) {
					messager.clearTempStatus();
					quitQuerying = false;
				}
				break;
			}
			return true;
		}
	};

	/**
	 * 加入指定的游戏桌进行游戏，直到退出游戏桌才返回。
	 * 
	 * @param gameBoard
	 *            游戏桌
	 * @param name
	 *            玩家名称
	 * @throws GameBoardFullException
	 *             游戏桌已满
	 * @throws InterruptedException
	 *             游戏中本线程被中断
	 */
	public synchronized void play(GameBoard gameBoard, String name)
			throws GameBoardFullException, InterruptedException {
		gameBoardView = gameBoard.newPlayer(name);
		this.gameBoard = gameBoard;
		this.inGameBoard = true;
		messager.initView();

		gameBoardView.addGameEventListener(gameListener);
		synchronized (inGameBoardWaiter) {
			new Thread() {
				public void run() {
					forReady();
				}
			}.start();

			while (inGameBoard) {
				inGameBoardWaiter.wait();
			}
		}
		this.gameBoardView = null;
		this.gameBoard = null;
	}

	/**
	 * 等待用户选择准备好游戏或退出游戏桌。
	 */
	private void forReady() {
		messager.directStatus("If you are ready for new game, press space:");
		messager.addCharHandler(new CharHandler() {

			@Override
			public boolean handle(char c) {
				switch (c) {
				case ' ':
					gameBoardView.readyForGame();
					messager.directStatus("Waiting for other players to be ready for game...");
					return false;
				default:
					return true;
				}
			}
		});
	}

	private class CliGameListener implements GameEventListener {
		private boolean inGame;

		@Override
		public void newEvent(PlayerEvent event) {
			messager.clearTimeStatus();

			Player player = event.getPlayer();
			PlayerLocation location = event.getLocation();

			String message;
			switch (event.getType()) {
			case IN:
				message = "joined";
				break;
			case OUT:
				message = "leaved";
				break;
			case READY:
				message = "is ready for new game";
				break;
			default:
				throw new RuntimeException();// 前面已列举完，不可能出现
			}

			messager.showMessage(event.getType().toString(), location, player,
					message, gameBoardView.getMyLocation());

			if (inGame && event.getType() == PlayerEventType.OUT) {
				inGame = false;
				forReady();
			}
		}

		@Override
		public void newEvent(GameStartEvent event) {
			messager.clearTimeStatus();

			inGame = true;
			messager.showMessage("start", null, null, "New game started",
					gameBoardView.getMyLocation());
		}

		@Override
		public void newEvent(TimeLimitEvent event) {
			messager.timeStatus(event.getTimeLimit());
		}

		private Set<TileType> winChances;

		@Override
		// XXX -
		// 与blove.mj.bot.AbstractBotPlayer.newEvent(PlayerActionEvent)有代码重复
		public void newEvent(PlayerActionEvent event) {
			PlayerLocation myLocation = gameBoardView.getMyLocation();
			PlayerTiles myTiles = gameBoardView.getMyTiles();
			WinStrategy winStrategy = gameBoard.getWinStrategy();

			ActionType eventType = event.getType();
			Tile eventTile = event.getTile(gameBoardView);
			PlayerLocation eventLocation = event.getPlayerLocation();
			Player eventPlayer = gameBoard.getPlayers().get(eventLocation);
			String action, message;
			switch (eventType) {
			case DEAL_OVER:
				action = "dealover";
				message = "Dealing is over";
				break;
			case DRAW:
				action = "draw";
				message = "drawed a tile.";
			case DISCARD:
				action = "discard";
				message = "discard tile: " + CliMessager.toString(eventTile);
				break;
			case CPK:
				Cpk cpk = event.getCpk();
				action = cpk.getType().isKong() ? "kong" : cpk.getType().name();
				message = "made a "
						+ cpk.getType().name()
						+ (cpk.getType() == CpkType.CONCEALED_KONG ? ""
								: " with "
										+ CliMessager
												.toString(cpk.getForTile())
										+ " from "
										+ eventLocation.getLocationOf(cpk
												.getFromRelation()))
						+ ", composed: " + CliMessager.toString(cpk.getTiles());

				break;
			default:
				throw new RuntimeException();// 已列举完，不可能出现
			}

			messager.showMessage(action, eventLocation, eventPlayer, message,
					myLocation);

			if (event.getType() == ActionType.DEAL_OVER
					|| (event.getType() == ActionType.DRAW && event
							.getPlayerLocation() == myLocation)
					|| (event.getType() == ActionType.CPK
							&& event.getPlayerLocation() == myLocation && !event
							.getCpk().getType().isKong())
					|| (event.getType() == ActionType.DISCARD && event
							.getPlayerLocation() != myLocation)) {
				// 发牌结束、自己摸牌、自己吃/碰：
				// 检查是否杠/和牌，如果有询问操作，执行操作，如果无机会或无操作询问出牌。
				// 别人出牌：
				// 检查吃/碰/杠/和机会，如果有询问操作，执行操作
				boolean winChance;
				if (event.getType() == ActionType.DEAL_OVER)
					winChance = winStrategy.isWin(myTiles);
				else
					winChance = winChances.contains(eventTile.getType());

				Set<Cpk> cpkChances = CpkType.getAllChances(myTiles, eventTile,
						myLocation.getRelationOf(event.getPlayerLocation()));

				try {
					if (winChance == true || !cpkChances.isEmpty()) {
						boolean cpkChoose = chooseCpk(cpkChances, winChance);
						if (cpkChoose)
							return;
					}

					if (event.getType() == ActionType.DISCARD) {
						gameBoardView.giveUpCpkw();
					} else {
						chooseDiscard();
					}
				} catch (InterruptedException e) {
				}

			} else if (event.getType() == ActionType.DISCARD
					&& event.getPlayerLocation() == myLocation) {
				// 自己出牌：
				// 记录和牌机会
				winChances = winStrategy.getWinChances(myTiles);
			}

			messager.tilesStatus(myTiles, (Tile) null, null, "");
		}

		/**
		 * 让用户选择吃/碰/杠/和牌。如果选择吃/碰/杠/和，则进行相应动作并返回true；如果放弃，则直接返回false。
		 * 
		 * @param cpkChances
		 *            吃/碰/杠机会
		 * @param winChance
		 *            和牌机会
		 * @return （见上）
		 * @throws InterruptedException
		 */
		private boolean chooseCpk(Set<Cpk> cpkChances, final boolean winChance)
				throws InterruptedException {
			final PlayerTiles myTiles = gameBoardView.getMyTiles();

			final String winOption = (winChance ? "w:win / " : "")
					+ "g:give up";
			final String status = "choose c/p/k/w";
			final TreeSet<Cpk> cpkAsTileTypes = new TreeSet<>(
					Cpk.tileTypeComparator);
			cpkAsTileTypes.addAll(cpkChances);
			messager.tilesStatus(myTiles, cpkAsTileTypes.first().getTiles(),
					winOption, status);

			class ChooseCpkCharHandler implements CharHandler {
				private Cpk crtChoose = cpkAsTileTypes.first();
				private boolean win;

				@Override
				public boolean handle(char c) {
					switch (c) {
					case ',':
						crtChoose = cpkAsTileTypes.lower(crtChoose);
						if (crtChoose == null)
							crtChoose = cpkAsTileTypes.last();
						focus();
						return true;
					case '.':
						crtChoose = cpkAsTileTypes.higher(crtChoose);
						if (crtChoose == null)
							crtChoose = cpkAsTileTypes.first();
						focus();
						return true;
					case ' ':
						win = false;
						return false;
					case 'w':
						if (winChance) {
							win = true;
							crtChoose = null;
							return false;
						} else
							return true;
					case 'g':
						win = false;
						crtChoose = null;
						return false;
					default:
						return true;
					}
				}

				private void focus() {
					messager.tilesStatus(myTiles, crtChoose.getTiles(),
							winOption, status);
				}

				/**
				 * 返回选择的吃/碰/杠。
				 * 
				 * @return 如果选择了吃/碰/杠，则返回；否则返回null。
				 */
				Cpk getCpkChoosed() {
					return crtChoose;
				}

				/**
				 * 返回是否选择了和牌。
				 * 
				 * @return 如果选择了和牌，则返回true，否则返回false。
				 */
				boolean isWinChoosed() {
					return win;
				}

			}

			ChooseCpkCharHandler charHandler = new ChooseCpkCharHandler();
			messager.addCharHandlerAndWait(charHandler);

			boolean choosed = true;
			Cpk cpkChoosed = charHandler.getCpkChoosed();
			if (cpkChoosed != null)
				gameBoardView.cpk(cpkChoosed);
			else if (charHandler.isWinChoosed())
				gameBoardView.win();
			else
				choosed = false;

			return choosed;
		}

		/**
		 * 让用户选择打出一张牌。如果用户选择了就打出。
		 */
		private void chooseDiscard() {
			final PlayerTiles myTiles = gameBoardView.getMyTiles();

			final String readyHandOption = "r:ready hand";

			final TreeSet<Tile> aliveTiles = new TreeSet<>(
					myTiles.getAliveTiles());

			final TreeSet<Tile> readyHandTiles = new TreeSet<>();
			Set<TileType> readyHandTypes = gameBoard.getWinStrategy()
					.getReadyHandChances(myTiles);
			for (Tile tile : myTiles.getAliveTiles())
				if (readyHandTypes.contains(tile.getType()))
					readyHandTiles.add(tile);

			final String status = "discard";
			final String statusWithReadyHand = "discard with rh";

			messager.tilesStatus(myTiles, aliveTiles.first(), readyHandOption,
					status);
			messager.addCharHandler(new CharHandler() {
				private Tile crtTile = aliveTiles.first();
				private boolean readyHand = false;

				@Override
				public boolean handle(char c) {
					TreeSet<Tile> tilesForChoose = readyHand ? readyHandTiles
							: aliveTiles;

					switch (c) {
					case ',':
						crtTile = tilesForChoose.lower(crtTile);
						if (crtTile == null)
							crtTile = tilesForChoose.last();
						break;
					case '.':
						crtTile = tilesForChoose.higher(crtTile);
						if (crtTile == null)
							crtTile = tilesForChoose.first();
						break;
					case 'r':
						readyHand = !readyHand;
						tilesForChoose = readyHand ? readyHandTiles
								: aliveTiles;
						crtTile = tilesForChoose.first();
						break;
					case ' ':
						gameBoardView.discard(crtTile, readyHand);
						return false;
					default:
						return true;
					}

					messager.tilesStatus(myTiles, crtTile, readyHandOption,
							readyHand ? statusWithReadyHand : status);
					return true;
				}
			});

		}

		@Override
		public void newEvent(GameOverEvent event) {
			messager.clearTimeStatus();

			inGame = false;
			messager.showResult(event.getResult(),
					gameBoardView.getMyLocation());
			forReady();
		}

	}
}
