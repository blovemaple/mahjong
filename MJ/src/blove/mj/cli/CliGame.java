package blove.mj.cli;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import blove.mj.Cpk;
import blove.mj.CpkType;
import blove.mj.GameBoardView;
import blove.mj.Player;
import blove.mj.PlayerLocation;
import blove.mj.PlayerLocation.Relation;
import blove.mj.Tile;
import blove.mj.TileType;
import blove.mj.board.GameBoard;
import blove.mj.board.GameBoardFullException;
import blove.mj.board.PlayerTiles;
import blove.mj.cli.CliMessager.PlayerTilesViewComparator;
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
	 * @throws IOException
	 */
	public synchronized void play(GameBoard gameBoard, String name)
			throws GameBoardFullException, InterruptedException, IOException {
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
		messager.directStatus("If you are ready for new game, press space.");
		messager.addCharHandler(new CharHandler() {

			@Override
			public boolean handle(char c) {
				switch (c) {
				case ' ':
					messager.directStatus("Waiting for other players to be ready for game...");
					gameBoardView.readyForGame();
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
			messager.showMessage(
					"start",
					null,
					null,
					"New game started, dealer is "
							+ CliMessager.toString(
									gameBoardView.getDealerLocation(),
									gameBoardView.getMyLocation()),
					gameBoardView.getMyLocation());
		}

		@Override
		public void newEvent(TimeLimitEvent event) {
			messager.timeStatus(event.getTimeLimit());
		}

		@Override
		// XXX - 代码重复：
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
				break;
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

			if (event.isForTimeOut())
				message += " (TIMEOUT)";

			messager.showMessage(action, eventLocation, eventPlayer, message,
					myLocation);

			try {
				if (myTiles.isForDiscarding()) {
					// 应该打牌

					// 如果不是吃/碰之后，则检查是否已和牌
					boolean winChance = false;
					if (eventType != ActionType.CPK)
						winChance = winStrategy.isWin(myTiles);

					// 检查杠牌机会
					Set<Cpk> kongChances = CpkType.getAllChances(myTiles,
							eventTile, Relation.SELF);

					boolean winOrKong = false;// 是否选择了和牌或杠牌
					if (winChance || !kongChances.isEmpty()) {
						// 如果和牌或有杠牌机会，则询问并执行选择的操作
						winOrKong = chooseCpk(kongChances, winChance, eventTile);
					}
					if (!winOrKong) {
						// 如果没有机会或没有选择操作，询问打出一张牌
						chooseDiscard(event.getTile(gameBoardView));
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
							boolean winOrCpk;
							winOrCpk = chooseCpk(cpkChances, winChance, null);
							if (!winOrCpk)
								// 如果没有选择，则选择放弃
								gameBoardView.giveUpCpkw();
						}

					}
				}

				messager.tilesStatus(myTiles, null, (Tile) null, null, null);
			} catch (InterruptedException e) {
			}
		}

		/**
		 * 让用户选择吃/碰/杠/和牌。如果选择吃/碰/杠/和，则进行相应动作并返回true；如果放弃，则直接返回false。
		 * 
		 * @param cpkChances
		 *            吃/碰/杠机会
		 * @param winChance
		 *            和牌机会
		 * @param drawedTile
		 *            刚摸的牌。如果为null表示吃/碰。
		 * @return （见上）
		 * @throws InterruptedException
		 */
		private boolean chooseCpk(Set<Cpk> cpkChances, final boolean winChance,
				final Tile drawedTile) throws InterruptedException {
			final PlayerTiles myTiles = gameBoardView.getMyTiles();

			final String winOption = (winChance ? "w:win / " : "")
					+ "g:give up";
			final String status = "choose c/p/k/w";
			final TreeSet<Cpk> cpkAsTileTypes = new TreeSet<>(
					Cpk.tileTypeComparator);
			cpkAsTileTypes.addAll(cpkChances);

			messager.tilesStatus(myTiles, drawedTile, cpkAsTileTypes.first()
					.getTiles(), winOption, status);

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
					messager.tilesStatus(myTiles, drawedTile,
							crtChoose.getTiles(), winOption, status);
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
		 * 
		 * @param drawedTile
		 *            刚摸的牌。null表示吃/碰之后。
		 * @throws InterruptedException
		 */
		private void chooseDiscard(final Tile drawedTile)
				throws InterruptedException {
			final PlayerTiles myTiles = gameBoardView.getMyTiles();

			final TreeSet<Tile> aliveTiles = new TreeSet<>(
					new PlayerTilesViewComparator(drawedTile));
			aliveTiles.addAll(myTiles.getAliveTiles());

			final TreeSet<Tile> readyHandTiles = new TreeSet<>();
			Set<TileType> readyHandTypes = gameBoard.getWinStrategy()
					.getReadyHandChances(myTiles);
			for (Tile tile : myTiles.getAliveTiles())
				if (readyHandTypes.contains(tile.getType()))
					readyHandTiles.add(tile);

			final String option = readyHandTiles.isEmpty() ? null
					: "r:ready hand";
			final String status = "discard";
			final String statusWithReadyHand = "discard with rh";

			messager.tilesStatus(myTiles, drawedTile,
					drawedTile != null ? drawedTile : aliveTiles.first(),
					option, status);
			messager.addCharHandlerAndWait(new CharHandler() {
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
						if (!readyHandTiles.isEmpty()) {
							readyHand = !readyHand;
							if (!readyHand) {
								tilesForChoose = aliveTiles;
								crtTile = drawedTile;
							} else {
								tilesForChoose = readyHandTiles;
								crtTile = tilesForChoose.first();
							}
						}
						break;
					case ' ':
						gameBoardView.discard(crtTile, readyHand);
						return false;
					default:
						return true;
					}

					messager.tilesStatus(myTiles, drawedTile, crtTile, option,
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
