package blove.mj.cli;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import blove.mj.Cpk;
import blove.mj.CpkType;
import blove.mj.Player;
import blove.mj.PlayerLocation;
import blove.mj.PlayerView;
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
import blove.mj.event.TimeLimitEvent;

import static blove.mj.cli.CliView.CharHandler.HandlingResult.*;

/**
 * 命令行游戏。
 * 
 * @author blovemaple
 */
class CliGame {
	private static final String WAITING_START_STATUS_KEY = "waiting_for_start";

	private final CliPlayer player;

	private final CliMessager messager;

	private boolean inGameBoard;// 进入游戏桌前置true，退出游戏桌时置false
	private Object inGameBoardWaiter = new Object();// play方法在此对象上等待，退出游戏桌时被唤醒
	private GameBoard gameBoard;
	private PlayerView playerView;

	/**
	 * 新建一个实例。
	 * 
	 * @param name
	 *            玩家名称
	 * @param cliView
	 *            命令行界面
	 */
	public CliGame(String name, CliView cliView) {
		player = new CliPlayer(name);
		this.messager = new CliMessager(cliView);
	}

	private CharHandler quitCharHandler = new CharHandler() {
		private static final String QUITING_STATUS_KEY = "quiting";

		boolean quitQuerying = false;

		@Override
		public HandlingResult handle(char c) {
			switch (c) {
			case 'y':
				if (quitQuerying) {
					synchronized (inGameBoardWaiter) {
						playerView.leave();
						inGameBoard = false;
						quitQuerying = false;
						inGameBoardWaiter.notifyAll();
						return ACCEPT_FOR_QUITING;
					}
				} else
					return IGNORE;
			case 'q':
				if (!quitQuerying) {
					messager.tempStatus(QUITING_STATUS_KEY,
							"Do you want to quit Mahjong?(y/n)");
					quitQuerying = true;
					return ACCEPT_FOR_MONOPOLIZATION;
				}
			default:
				if (quitQuerying) {
					messager.clearTempStatus(QUITING_STATUS_KEY);
					quitQuerying = false;
					return ACCEPT;
				} else
					return IGNORE;
			}
		}
	};

	/**
	 * 加入指定的游戏桌进行游戏，直到退出游戏桌才返回。
	 * 
	 * @param gameBoard
	 *            游戏桌
	 * @throws GameBoardFullException
	 *             游戏桌已满
	 * @throws InterruptedException
	 *             游戏中本线程被中断
	 * @throws IOException
	 */
	public synchronized void join(GameBoard gameBoard)
			throws GameBoardFullException, InterruptedException, IOException {
		playerView = gameBoard.newPlayer(player);
		playerView.addGameEventListener(new CliGameListener());
		this.gameBoard = gameBoard;

		this.inGameBoard = true;
		messager.addCharHandler(quitCharHandler);

		synchronized (inGameBoardWaiter) {
			while (inGameBoard)
				inGameBoardWaiter.wait();
		}
		this.playerView = null;
		this.gameBoard = null;
	}

	/**
	 * 等待用户选择准备好游戏或退出游戏桌。
	 */
	private void forReady() {
		final String WAITING_READY_STATUS_KEY = "waiting_for_ready";

		messager.tempStatus(WAITING_READY_STATUS_KEY,
				"If you are ready for new game, press space.");
		messager.addCharHandler(new CharHandler() {

			@Override
			public HandlingResult handle(char c) {
				switch (c) {
				case ' ':
					messager.clearTempStatus(WAITING_READY_STATUS_KEY);
					messager.tempStatus(WAITING_START_STATUS_KEY,
							"Waiting for other players to be ready for game...");
					playerView.readyForGame();
					return ACCEPT_FOR_QUITING;
				default:
					return IGNORE;
				}
			}
		});
	}

	private class CliPlayer extends Player {

		CliPlayer(String name) {
			super(name);
		}

		@Override
		public void forReady(PlayerView playerView) {
			new Thread() {
				public void run() {
					CliGame.this.forReady();
				}
			}.start();
		}

		@Override
		public CpkwChoice chooseCpkw(PlayerView playerView,
				Set<CpkwChoice> cpkwChances, Tile newTile, boolean drawed)
				throws InterruptedException {
			final PlayerTiles myTiles = playerView.getMyTiles();
			final Tile drawedTile = drawed ? newTile : null;
			final CpkwChoice winChance = getWinChance(cpkwChances);
			Set<CpkwChoice> cpkChances = getCpkChances(cpkwChances);

			final String winOption = (winChance != null ? "w:win / " : "")
					+ "g:give up";
			final String status = "choose c/p/k/w";
			final TreeSet<CpkwChoice> cpkAsTileTypes = new TreeSet<>(
					cpkTileTypeComparator);
			cpkAsTileTypes.addAll(cpkChances);

			messager.tilesStatus(myTiles, drawedTile,
					cpkAsTileTypes.isEmpty() ? null
							: cpkAsTileTypes.first().cpk.getTiles(), winOption,
					status);

			class ChooseCpkCharHandler implements CharHandler {
				private CpkwChoice crtChoose = cpkAsTileTypes.isEmpty() ? null
						: cpkAsTileTypes.first();
				private boolean win;

				@Override
				public HandlingResult handle(char c) {
					switch (c) {
					case ',':
						if (!cpkAsTileTypes.isEmpty()) {
							crtChoose = cpkAsTileTypes.lower(crtChoose);
							if (crtChoose == null)
								crtChoose = cpkAsTileTypes.last();
							focus();
						}
						return ACCEPT;
					case '.':
						if (!cpkAsTileTypes.isEmpty()) {
							crtChoose = cpkAsTileTypes.higher(crtChoose);
							if (crtChoose == null)
								crtChoose = cpkAsTileTypes.first();
							focus();
						}
						return ACCEPT;
					case ' ':
						win = false;
						return ACCEPT_FOR_QUITING;
					case 'w':
						if (winChance != null) {
							win = true;
							crtChoose = null;
							return ACCEPT_FOR_QUITING;
						} else
							return IGNORE;
					case 'g':
						win = false;
						crtChoose = null;
						return ACCEPT_FOR_QUITING;
					default:
						return IGNORE;
					}
				}

				private void focus() {
					messager.tilesStatus(
							myTiles,
							drawedTile,
							crtChoose != null ? crtChoose.cpk.getTiles() : null,
							winOption, status);
				}

				/**
				 * 返回选择的吃/碰/杠。
				 * 
				 * @return 如果选择了吃/碰/杠，则返回；否则返回null。
				 */
				CpkwChoice getCpkChoosed() {
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

			CpkwChoice cpkChoosed = charHandler.getCpkChoosed();
			if (cpkChoosed != null)
				return cpkChoosed;
			else if (charHandler.isWinChoosed())
				return winChance;
			else
				return null;
		}

		private Comparator<CpkwChoice> cpkTileTypeComparator = new Comparator<CpkwChoice>() {

			@Override
			public int compare(CpkwChoice o1, CpkwChoice o2) {
				return Cpk.tileTypeComparator.compare(o1.cpk, o2.cpk);
			}

		};

		private CpkwChoice getWinChance(Collection<CpkwChoice> cpkwChances) {
			for (CpkwChoice choice : cpkwChances)
				if (choice.win)
					return choice;
			return null;
		}

		private Set<CpkwChoice> getCpkChances(Collection<CpkwChoice> cpkwChances) {
			Set<CpkwChoice> cpkChances = new HashSet<>();
			for (CpkwChoice choice : cpkwChances)
				if (choice.cpk != null)
					cpkChances.add(choice);
			return cpkChances;
		}

		@Override
		public DiscardChoice chooseDiscard(PlayerView playerView,
				Set<TileType> readyHandTypes, final Tile drawedTile)
				throws InterruptedException {
			final PlayerTiles myTiles = playerView.getMyTiles();

			final TreeSet<Tile> aliveTiles = new TreeSet<>(
					new PlayerTilesViewComparator(drawedTile));
			aliveTiles.addAll(myTiles.getAliveTiles());

			final TreeSet<Tile> readyHandTiles = new TreeSet<>(
					new PlayerTilesViewComparator(drawedTile));
			for (Tile tile : myTiles.getAliveTiles())
				if (readyHandTypes.contains(tile.getType()))
					readyHandTiles.add(tile);

			final String option = readyHandTiles.isEmpty() ? null
					: "r:ready hand";
			final String status = "discard";
			final String statusWithReadyHand = "discard with rh";

			final Tile firstFocusTile = drawedTile != null ? drawedTile
					: aliveTiles.last();
			messager.tilesStatus(myTiles, drawedTile, firstFocusTile, option,
					status);

			class ChooseDiscardCharHandler implements CharHandler {
				private Tile crtTile = firstFocusTile;
				private boolean readyHand = false;

				@Override
				public HandlingResult handle(char c) {
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
							} else {
								tilesForChoose = readyHandTiles;
								crtTile = readyHandTiles.first();
							}
						}
						break;
					case ' ':
						return ACCEPT_FOR_QUITING;
					default:
						return IGNORE;
					}

					messager.tilesStatus(myTiles, drawedTile, crtTile, option,
							readyHand ? statusWithReadyHand : status);
					return ACCEPT;
				}

				boolean isReadyHand() {
					return readyHand;
				}

				Tile getChoice() {
					return crtTile;
				}
			}

			ChooseDiscardCharHandler charHandler = new ChooseDiscardCharHandler();
			messager.addCharHandlerAndWait(charHandler);

			return new DiscardChoice(charHandler.getChoice(),
					charHandler.isReadyHand());
		}

		@Override
		public void forLeaving(PlayerView playerView) {
		}
	}

	private class CliGameListener implements GameEventListener {

		@Override
		public void newEvent(PlayerEvent event) throws IOException {
			messager.clearTimeStatus();

			String playerName = event.getPlayerName();
			PlayerLocation location = event.getLocation();

			messager.showMessage(event.getType().toString(), location,
					playerName, null, playerView.getMyLocation());
		}

		@Override
		public void newEvent(GameStartEvent event) throws IOException {
			messager.initView();
			messager.clearTempStatus(WAITING_START_STATUS_KEY);
			messager.clearTimeStatus();
			messager.showMessage(
					"start",
					null,
					null,
					"New game started, dealer is "
							+ CliMessager.toString(
									gameBoard.getDealerLocation(),
									playerView.getMyLocation()),
					playerView.getMyLocation());
		}

		@Override
		public void newEvent(TimeLimitEvent event) {
			messager.timeStatus(event.getTimeLimit());
		}

		@Override
		public void newEvent(PlayerActionEvent event) throws IOException {
			PlayerLocation myLocation = playerView.getMyLocation();

			ActionType eventType = event.getType();
			Tile eventTile = event.getTile(playerView);
			PlayerLocation eventLocation = event.getPlayerLocation();
			String eventPlayerName = gameBoard.getPlayerNames().get(
					eventLocation);
			String action, message;
			switch (eventType) {
			case DEAL_OVER:
				action = "dealover";
				message = "Dealing is over";
				break;
			case DRAW:
				action = "draw";
				message = null;
				break;
			case DISCARD:
				action = "discard";
				message = CliMessager.toString(eventTile);
				if (event.isNewReadyHand())
					message += " (READYHAND)";
				break;
			case CPK:
				Cpk cpk = event.getCpk();
				action = cpk.getType().name();
				message = cpk.getType() == CpkType.CONCEALED_KONG ? null
						: CliMessager.toString(cpk.getTiles());
				break;
			default:
				throw new RuntimeException();// 已列举完，不可能出现
			}

			if (event.isForTimeOut())
				message += " (TIMEOUT)";

			messager.showMessage(action, eventLocation, eventPlayerName,
					message, myLocation);

			messager.clearTimeStatus();
			messager.tilesStatus(playerView.getMyTiles(),
					(eventType == ActionType.DRAW ? eventTile : null),
					(Tile) null, null, "wait");
		}

		@Override
		public void newEvent(GameOverEvent event) throws IOException {
			messager.clearTimeStatus();
			messager.showResult(event.getResult(), playerView.getMyLocation());
			forReady();
		}

	}
}
