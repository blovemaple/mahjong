package blove.mj.local;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.CancelledKeyException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import blove.mj.Cpk;
import blove.mj.CpkType;
import blove.mj.GameResult;
import blove.mj.GameResult.WinInfo;
import blove.mj.Player;
import blove.mj.Player.CpkwChoice;
import blove.mj.Player.DiscardChoice;
import blove.mj.PlayerLeavedException;
import blove.mj.PlayerLocation;
import blove.mj.PlayerLocation.Relation;
import blove.mj.PlayerView;
import blove.mj.Tile;
import blove.mj.TileType;
import blove.mj.board.DiscardedTiles;
import blove.mj.board.GameBoard;
import blove.mj.board.GameBoardFullException;
import blove.mj.board.PlayerTiles;
import blove.mj.board.Wall;
import blove.mj.event.GameEvent;
import blove.mj.event.GameEventListener;
import blove.mj.event.GameOverEvent;
import blove.mj.event.GameStartEvent;
import blove.mj.event.PlayerActionEvent;
import blove.mj.event.PlayerEvent;
import blove.mj.event.PlayerEvent.PlayerEventType;
import blove.mj.event.TimeLimitEvent;
import blove.mj.local.Timer.TimerAction;
import blove.mj.rules.SimpleWinStrategy;
import blove.mj.rules.TimeLimitStrategy;
import blove.mj.rules.WinStrategy;

/**
 * 本地麻将桌。
 * 
 * @author blovemaple
 */
public class LocalGameBoard implements GameBoard {
	private final Map<PlayerLocation, PlayerInfo> playerInfos = new EnumMap<>(
			PlayerLocation.class);

	// 1.设置或读取任意玩家ready状态时，都在此对象上同步
	// 2.等待所有玩家准备好游戏的线程在此对象上等待，有玩家准备好时在此对象上notifyAll
	private final Object readyWaitingLock = new Object();

	private final LocalWall wall = new LocalWall();
	private final LocalDiscardTiles discardTiles = new LocalDiscardTiles();

	private final TimeLimitStrategy timeStrategy;
	private final WinStrategy winStrategy;

	private boolean inGame = false;
	private PlayerLocation dealerLocation = PlayerLocation.EAST;// 一局结束时设置下局庄家

	private DiscardedHandler discardedHandler = new DiscardedHandler();
	private DiscardingHandler discardingHandler = new DiscardingHandler();
	private Timer timer = new Timer();

	private final List<GameResult> resultList = new LinkedList<>();

	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	private Future<LocationAndTile> handling;

	private boolean endGame;
	private PlayerLocation winner, paoer;
	private Tile winTile;

	/**
	 * 新建一个实例，采用默认的和牌策略。
	 * 
	 * @param timeStrategy
	 *            限时策略
	 */
	public LocalGameBoard(TimeLimitStrategy timeStrategy) {
		this(timeStrategy, new SimpleWinStrategy());
	}

	/**
	 * 新建一个实例。
	 * 
	 * @param timeStrategy
	 *            限时策略
	 * @param winStrategy
	 *            和牌策略
	 */
	public LocalGameBoard(TimeLimitStrategy timeStrategy,
			WinStrategy winStrategy) {
		this.timeStrategy = timeStrategy;
		this.winStrategy = winStrategy;
	}

	@Override
	public void run() {
		try {
			while (true) {
				waitForAllReady();

				startGameInit();
				fireGameStart();

				Tile lastTile = deal();
				fireDealOver(lastTile);
				LocationAndTile locationAndTileInfo = new LocationAndTile(
						dealerLocation, lastTile);

				endGame = false;
				A_GAME: while (!endGame) {
					for (StateHandler handler : new StateHandler[] {
							discardingHandler, discardedHandler }) {
						handling = threadPool.submit(new HandlingTask(
								locationAndTileInfo, handler));
						try {
							locationAndTileInfo = handling.get();
						} catch (CancellationException e) {
						}
						if (endGame) {
							endGame();
							break A_GAME;
						} else if (!isPlayersFull()) {
							stopGame();
							break A_GAME;
						}

					}
				}
			}
		} catch (InterruptedException e) {
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inGame)
				stopGame();
		}
	}

	private class HandlingTask implements Callable<LocationAndTile> {

		protected final StateHandler handler;
		protected final LocationAndTile locationAndTile;

		HandlingTask(LocationAndTile locationAndTile, StateHandler handler) {
			this.locationAndTile = locationAndTile;
			this.handler = handler;
		}

		@Override
		public LocationAndTile call() throws Exception {
			return handler.handle(locationAndTile);
		}

	}

	/**
	 * 等待所有玩家准备好开始游戏。
	 * 
	 * @throws InterruptedException
	 */
	private void waitForAllReady() throws InterruptedException {
		synchronized (readyWaitingLock) {
			while (!arePlayersAllReady())
				readyWaitingLock.wait();
		}
	}

	/**
	 * 返回玩家是否全准备好了。
	 */
	private boolean arePlayersAllReady() {
		if (isPlayersFull())
			return false;
		synchronized (readyWaitingLock) {
			for (PlayerInfo playerInfo : playerInfos.values())
				if (!playerInfo.ready)
					return false;
			return true;
		}
	}

	/**
	 * 返回玩家是否满。
	 */
	private boolean isPlayersFull() {
		return playerInfos.size() == 4;
	}

	/**
	 * 初始化各种变量，准备开始一局游戏。
	 */
	private void startGameInit() {
		if (inGame)
			throw new IllegalStateException("已在游戏中");
		wall.init();
		discardTiles.init();
		for (PlayerInfo info : playerInfos.values())
			info.tiles.init();
		inGame = true;
	}

	/**
	 * 发牌。
	 * 
	 * @return 发给庄家的最后一张牌
	 */
	private Tile deal() {
		try {
			PlayerLocation loc = dealerLocation;
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++) {
					LocalPlayerTiles tiles = playerInfos.get(loc).tiles;
					for (int k = 0; k < (i < 3 ? 4 : 1); k++)
						tiles.addTile(wall.draw());
					loc = loc.getLocationOf(Relation.NEXT);
				}
			}
			Tile lastTile = wall.draw();
			playerInfos.get(dealerLocation).tiles.addTile(lastTile);

			return lastTile;
		} catch (DrawGameException e) {
			// 发牌时不可能流局
			throw new RuntimeException(e);
		}
	}

	PlayerInfo getPlayerInfo(PlayerLocation location) {
		return playerInfos.get(location);
	}

	/**
	 * 向公用的线程池提交一个任务。
	 * 
	 * @param task
	 *            任务
	 * @return Futureg
	 */
	<T> Future<T> submitToThreadPool(Callable<T> task) {
		return threadPool.submit(task);
	}

	/**
	 * 摸牌。此方法将摸的牌放入玩家的牌中，并返回摸的牌。
	 * 
	 * @param location
	 *            位置
	 * @param fromSeabed
	 *            是否从海底摸牌
	 * @param timeout
	 *            是否是超时自动摸牌
	 * @return 摸的牌
	 * @throws DrawGameException
	 *             流局
	 */
	Tile draw(PlayerLocation location, boolean fromSeabed, boolean timeout)
			throws DrawGameException {
		Tile tile = fromSeabed ? wall.drawFromSeabed() : wall.draw();
		playerInfos.get(location).tiles.addTile(tile);

		fireDraw(location, tile);

		return tile;
	}

	/**
	 * 结束一局游戏时调用。
	 * 
	 * @param winnerLocation
	 *            赢家位置。如果为null则表示流局。
	 * @param paoerLocation
	 *            点炮者位置。如果为null则表示流局或自摸和。
	 * @param winTile
	 *            和牌。如果为null则表示流局或天和。
	 */
	void endGame(PlayerLocation winnerLocation, PlayerLocation paoerLocation,
			Tile winTile) {
		endGame = true;
		this.winner = winnerLocation;
		this.paoer = paoerLocation;
		this.winTile = winTile;
	}

	/**
	 * 确认一局游戏结束后由控制程序调用。记录结果并重置状态。
	 */
	private void endGame() {
		if (!inGame)
			throw new IllegalStateException("未在游戏中");

		recordResult(winner, paoer, winTile);
		dealerLocation = dealerLocation.getLocationOf(Relation.NEXT);
		stopGame();
	}

	private void recordResult(PlayerLocation winnerLocation,
			PlayerLocation paoerLocation, Tile winTile) {
		Map<PlayerLocation, String> players = new EnumMap<>(
				PlayerLocation.class);
		WinInfo winInfo = new WinInfo(winnerLocation, winTile, paoerLocation);
		Map<PlayerLocation, PlayerTiles> tiles = new EnumMap<>(
				PlayerLocation.class);
		for (Entry<PlayerLocation, PlayerInfo> playerInfoEntry : playerInfos
				.entrySet())
			tiles.put(playerInfoEntry.getKey(),
					playerInfoEntry.getValue().tiles);
		for (Entry<PlayerLocation, PlayerInfo> playerInfoEntry : playerInfos
				.entrySet()) {
			PlayerLocation location = playerInfoEntry.getKey();
			PlayerInfo playerInfo = playerInfoEntry.getValue();

			players.put(location, playerInfo.player.getName());
		}

		DiscardedTiles discardTiles = DiscardedTiles.copyOf(this.discardTiles);
		Wall wall = Wall.copyOf(this.wall);

		GameResult result = new GameResult(players, winInfo, dealerLocation,
				tiles, discardTiles, wall, winStrategy);
		resultList.add(result);

		fireGameOver(result);
	}

	private void stopGame() {
		if (!inGame)
			throw new IllegalStateException("未在游戏中");

		synchronized (readyWaitingLock) {
			for (PlayerInfo info : playerInfos.values())
				info.ready = false;
		}
		inGame = false;
	}

	/**
	 * 玩家离开时调用。此方法会负责通知监听器。
	 * 
	 * @param location
	 *            离开的玩家
	 */
	void leavePlayer(PlayerLocation location) {
		if (handling != null && !handling.isDone())
			handling.cancel(true);

		PlayerInfo playerInfo = playerInfos.remove(location);

		firePlayerOut(playerInfo.player.getName(), location);
	}

	@Override
	public PlayerView newPlayer(Player player) throws GameBoardFullException {
		resultList.clear();
		PlayerView view = null;
		PlayerLocation playerLocation = null;
		for (PlayerLocation location : PlayerLocation.values()) {
			if (playerInfos.get(location) == null) {
				PlayerInfo playerInfo = new PlayerInfo(player, location);
				playerLocation = location;
				playerInfos.put(location, playerInfo);
				view = playerInfo.playerView;
				break;
			}
		}
		if (view == null)
			throw new GameBoardFullException(this);

		firePlayerIn(player.getName(), playerLocation);
		return view;
	}

	@Override
	public Map<PlayerLocation, String> getPlayerNames() {
		Map<PlayerLocation, String> players = new EnumMap<>(
				PlayerLocation.class);
		for (Map.Entry<PlayerLocation, PlayerInfo> playerInfoEntry : playerInfos
				.entrySet()) {
			players.put(playerInfoEntry.getKey(),
					playerInfoEntry.getValue().player.getName());
		}
		return players;
	}

	@Override
	public PlayerLocation getDealerLocation() {
		return dealerLocation;
	}

	@Override
	public boolean isInGame() {
		return inGame;
	}

	@Override
	public TimeLimitStrategy getTimeLimitStrategy() {
		return timeStrategy;
	}

	@Override
	public WinStrategy getWinStrategy() {
		return winStrategy;
	}

	protected PlayerEvent firePlayerIn(String playerName,
			PlayerLocation location) {
		PlayerEvent event = new PlayerEvent(this, PlayerEventType.IN,
				playerName, location);
		fireGameEvent(event);
		return event;
	}

	protected PlayerEvent firePlayerReady(String playerName,
			PlayerLocation location) {
		PlayerEvent event = new PlayerEvent(this, PlayerEventType.READY,
				playerName, location);
		fireGameEvent(event);
		return event;
	}

	protected PlayerEvent firePlayerOut(String playerName,
			PlayerLocation location) {
		PlayerEvent event = new PlayerEvent(this, PlayerEventType.OUT,
				playerName, location);
		fireGameEvent(event);
		return event;
	}

	protected GameStartEvent fireGameStart() {
		GameStartEvent event = new GameStartEvent(this);
		fireGameEvent(event);
		return event;
	}

	protected PlayerActionEvent fireDealOver(Tile dealerTile) {
		PlayerActionEvent event = PlayerActionEvent.newForDealOver(this,
				dealerTile);
		fireGameEvent(event);
		return event;
	}

	protected TimeLimitEvent fireTimeLimit(long timeLimit) {
		TimeLimitEvent event = new TimeLimitEvent(this, timeLimit);
		fireGameEvent(event);
		return event;
	}

	protected PlayerActionEvent fireDraw(PlayerLocation playerLocation,
			Tile tile) {
		PlayerActionEvent event = PlayerActionEvent.newForDraw(this,
				playerLocation, tile);
		fireGameEvent(event);
		return event;
	}

	protected PlayerActionEvent fireDiscard(PlayerLocation playerLocation,
			Tile tile, boolean newReadyHand, boolean forTimeOut) {
		PlayerActionEvent event = PlayerActionEvent.newForDiscard(this,
				playerLocation, tile, newReadyHand, forTimeOut);
		fireGameEvent(event);
		return event;
	}

	protected PlayerActionEvent fireCpk(PlayerLocation playerLocation, Cpk cpk) {
		PlayerActionEvent event = PlayerActionEvent.newForCpk(this,
				playerLocation, cpk);
		fireGameEvent(event);
		return event;
	}

	protected GameOverEvent fireGameOver(GameResult result) {
		GameOverEvent event = new GameOverEvent(this, result);
		fireGameEvent(event);
		return event;
	}

	private synchronized void fireGameEvent(final GameEvent event) {
		try {
			final Method m = GameEventListener.class.getMethod(
					GameEventListener.METHOD_NAME, event.getClass());
			for (PlayerInfo playerInfo : playerInfos.values()) {
				for (final GameEventListener listener : playerInfo.listenerList) {
					try {
						m.invoke(listener, event);
					} catch (IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
						e.printStackTrace();// XXX - 监听器实现中的异常直接打印了
					}
				}
			}
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("监听器方法不能调用", e);
		}
	}

	private class PlayerInfo {
		private final Player player;
		private final PlayerView playerView;
		private final LocalPlayerTiles tiles = new LocalPlayerTiles();
		private final List<GameEventListener> listenerList = new CopyOnWriteArrayList<>();
		private boolean ready = false;

		PlayerInfo(Player player, PlayerLocation location) {
			this.player = player;
			this.playerView = new LocalGameBoardView(player, location);
		}
	}

	private class LocalGameBoardView implements PlayerView {
		private final Player player;
		private final PlayerLocation location;

		private boolean leaved = false;

		LocalGameBoardView(Player player, PlayerLocation location) {
			this.player = player;
			this.location = location;
		}

		private void checkNotLeaved() {
			if (leaved)
				throw new PlayerLeavedException(player, LocalGameBoard.this);
		}

		private void checkInGame(boolean forInGame) {
			checkNotLeaved();
			synchronized (LocalGameBoard.this) {
				if (inGame != forInGame)
					throw new IllegalStateException((forInGame ? "不" : "已")
							+ "在游戏中");
			}
		}

		@Override
		public GameBoard getBoard() {
			checkNotLeaved();
			return LocalGameBoard.this;
		}

		@Override
		public Player getPlayer() {
			return player;
		}

		@Override
		public PlayerLocation getMyLocation() {
			return location;
		}

		@Override
		public void addGameEventListener(GameEventListener listener) {
			playerInfos.get(location).listenerList.add(listener);
		}

		@Override
		public void removeGameEventListener(GameEventListener listener) {
			playerInfos.get(location).listenerList.remove(listener);
		}

		@Override
		public PlayerTiles getMyTiles() {
			checkNotLeaved();
			return playerInfos.get(location).tiles;
		}

		@Override
		public void readyForGame() {
			checkNotLeaved();
			synchronized (LocalGameBoard.this) {
				checkInGame(false);

				synchronized (readyWaitingLock) {
					playerInfos.get(location).ready = true;
					firePlayerReady(player.getName(), location);

					readyWaitingLock.notifyAll();
				}
			}
		}

		@Override
		public List<GameResult> getResults() {
			return Collections.unmodifiableList(resultList);
		}

		@Override
		public void leave() {
			checkNotLeaved();
			leavePlayer(location);
			leaved = true;
		}

	}

	private interface StateHandler {
		LocationAndTile handle(LocationAndTile locationAndTile)
				throws InterruptedException;
	}

	/**
	 * 打牌后等待状态的处理器，处理从此状态到下一个玩家等待摸牌的状态的过程。
	 * 
	 * @author blovemaple
	 */
	private class DiscardedHandler implements StateHandler {
		private PlayerLocation discardedLocation;
		private Tile discardedTile;

		private Map<PlayerLocation, Set<CpkwChoice>> cpkwChances = new EnumMap<>(
				PlayerLocation.class);// 无选择或放弃者置null
		private Map<PlayerLocation, Future<Void>> playerChoosings;

		private Map<PlayerLocation, CpkwChoice> cpkwChoices = new EnumMap<>(
				PlayerLocation.class);// 所有做出选择的过程在此对象上同步

		private PlayerLocation finalChoiceLocation;// 最终选择的玩家位置。null表示未确定或无选择。
		private CpkwChoice finalChoice;// 最终选择。null表示未确定。

		private TimerAction timerAction = new TimerAction() {

			@Override
			public void countRun(long remainSecs) {
				fireTimeLimit(remainSecs);
			}

			@Override
			public void timeoutRun() {
				synchronized (cpkwChoices) {
					if (Thread.interrupted())
						return;
					for (Future<?> playerChoosing : playerChoosings.values()) {
						playerChoosing.cancel(true);
					}
				}
			}

			@Override
			public void stopRun() {
				fireTimeLimit(TimeLimitEvent.STOP_TIME_LIMIT);
			}
		};

		/**
		 * 打牌后调用。
		 * 
		 * @param locationAndTile
		 *            打出牌的玩家和打出的牌
		 * @return 待出牌的玩家和摸的牌。如果是吃/碰得牌，则牌为null。
		 * @throws InterruptedException
		 */
		@Override
		public synchronized LocationAndTile handle(
				LocationAndTile locationAndTile) throws InterruptedException {
			try {
				this.discardedLocation = locationAndTile.location;
				this.discardedTile = locationAndTile.tile;

				prepareForWaiting();

				LocationAndTile result = null;

				if (!checkForStop()) {
					timer.start(timeStrategy.discardLimit(), TimeUnit.SECONDS,
							timerAction);
					// 开始各Player选择CPKW的线程
					for (final PlayerLocation location : PlayerLocation
							.values()) {
						final Set<CpkwChoice> cpkwChances = this.cpkwChances
								.get(location);
						if (!cpkwChances.isEmpty()) {
							Future<Void> choiceFuture = submitToThreadPool(new Callable<Void>() {

								@Override
								public Void call() throws Exception {
									PlayerInfo playerInfo = playerInfos
											.get(location);
									CpkwChoice choice;
									choice = playerInfo.player.chooseCpk(
											playerInfo.playerView, cpkwChances,
											discardedTile, false);
									if (choice != null
											&& !cpkwChances.contains(choice))
										choice = null;// XXX - 选择错误未处理并视为放弃
									synchronized (cpkwChoices) {
										if (Thread.interrupted())
											// 如果被中断则返回，关键是不会执行下面的checkForStop
											// 因为如果其他玩家线程调用了checkForStop导致最终选择的话，此线程会被中断，checkForStop就不该再次调用。
											return null;
										cpkwChances.remove(location);
										if (choice != null)
											cpkwChoices.put(location, choice);
										checkForStop();
									}
									return null;
								}

							});
							playerChoosings.put(location, choiceFuture);
						}
					}
					// 等待各Player选择的线程结束。当checkForStop确认后所有选择线程会中止。
					try {
						for (Future<Void> playerChoosing : playerChoosings
								.values())
							playerChoosing.get();
					} catch (CancellationException e) {
						// 被取消
					} catch (ExecutionException e) {
						e.printStackTrace();// XXX - 玩家选择吃/碰/杠/和实现中的异常未处理
					}
				}
				result = importCpkAndDraw();
				return result;
			} catch (InterruptedException e) {
				// 被中断时，停止所有玩家的选择线程，并重新抛出InterruptedException
				for (Future<Void> playerChoosing : playerChoosings.values())
					playerChoosing.cancel(true);
				throw e;
			} finally {
				end();
			}
		}

		/**
		 * 开始等待前调用，准备各种等待时需要的变量。
		 */
		private void prepareForWaiting() {
			for (Map.Entry<PlayerLocation, PlayerInfo> playerInfoEntry : playerInfos
					.entrySet()) {
				PlayerLocation location = playerInfoEntry.getKey();
				PlayerInfo playerInfo = playerInfoEntry.getValue();
				if (location != discardedLocation) {
					Set<CpkwChoice> cpkwChoices = new HashSet<>();

					PlayerTiles tiles = playerInfo.tiles;
					Set<Cpk> cpkChances = CpkType.getAllChances(tiles,
							discardedTile,
							location.getRelationOf(discardedLocation));
					if (!cpkChances.isEmpty()) {
						for (Cpk cpkChance : cpkChances)
							cpkwChoices.add(CpkwChoice.chooseCpk(cpkChance));
					}

					if (winStrategy.getWinChances(tiles).contains(
							discardedTile.getType()))
						cpkwChoices.add(CpkwChoice.chooseWin());

					if (!cpkwChoices.isEmpty())
						cpkwChances.put(location, cpkwChoices);
				}
			}

		}

		/**
		 * 根据优先级比较CpkwChoice大小的比较器。
		 * 
		 * @author blovemaple
		 */
		private final Comparator<Map.Entry<PlayerLocation, CpkwChoice>> cpkwChoicePriorityComparator = new Comparator<Map.Entry<PlayerLocation, CpkwChoice>>() {

			@Override
			public int compare(Map.Entry<PlayerLocation, CpkwChoice> o1,
					Map.Entry<PlayerLocation, CpkwChoice> o2) {
				PlayerLocation location1 = o1.getKey(), location2 = o2.getKey();
				CpkwChoice choice1 = o1.getValue(), choice2 = o2.getValue();

				if (choice1.win && !choice2.win)
					return -1;
				else if (!choice1.win && choice2.win)
					return 1;
				else if (choice1.win && choice2.win)
					return discardedLocation.getRelationOf(location1)
							.compareTo(
									discardedLocation.getRelationOf(location2));
				else {
					Cpk cpk1 = choice1.cpk, cpk2 = choice2.cpk;

					int typeCompare = compare(cpk1.getType(), cpk2.getType());
					if (typeCompare != 0)
						return typeCompare;
					int relationCompare = cpk2.getFromRelation().compareTo(
							cpk1.getFromRelation());
					if (relationCompare != 0)
						return relationCompare;
					return cpk1.compareTo(cpk2);
				}

			}

			private int compare(CpkType type1, CpkType type2) {
				int priority1 = getPriority(type1), priority2 = getPriority(type2);
				return priority1 < priority2 ? -1 : priority1 > priority2 ? 1
						: 0;
			}

			private int getPriority(CpkType type) {
				switch (type) {
				case CONCEALED_KONG:
				case EXPOSED_KONG:
					return 0;
				case PONG:
					return 1;
				case CHOW:
					return 2;
				default:
					throw new RuntimeException();// 已列举完
				}
			}

		};

		private class MapEntryAsTuple<K, V> implements Map.Entry<K, V> {
			private final K key;
			private final V value;

			public MapEntryAsTuple(K key, V value) {
				this.key = key;
				this.value = value;
			}

			@Override
			public K getKey() {
				return key;
			}

			@Override
			public V getValue() {
				return value;
			}

			@Override
			public V setValue(V value) {
				throw new UnsupportedOperationException();
			}

		}

		/**
		 * 检查当前是否可以中止等待。若可以中止则中止，并将{@link #finalChoiceLocation}和
		 * {@link #finalChoice}两个变量设为合适的值。
		 * 
		 * @return 如果已确定最终选择，返回true，否则返回false。
		 */
		private boolean checkForStop() {
			List<Map.Entry<PlayerLocation, CpkwChoice>> cpkwChances = new ArrayList<>();
			for (Map.Entry<PlayerLocation, Set<CpkwChoice>> cpkwChancesForOne : this.cpkwChances
					.entrySet()) {
				for (CpkwChoice cpkwChance : cpkwChancesForOne.getValue())
					cpkwChances.add(new MapEntryAsTuple<>(cpkwChancesForOne
							.getKey(), cpkwChance));
			}

			List<Map.Entry<PlayerLocation, CpkwChoice>> cpkwChoices = new ArrayList<>();
			cpkwChoices.addAll(this.cpkwChoices.entrySet());

			Map.Entry<PlayerLocation, CpkwChoice> firstChance = Collections
					.min(cpkwChances, cpkwChoicePriorityComparator);
			Map.Entry<PlayerLocation, CpkwChoice> firstChoice = Collections
					.min(cpkwChoices, cpkwChoicePriorityComparator);

			finalChoiceLocation = null;
			finalChoice = null;

			if (firstChoice != null
					&& (firstChance == null || cpkwChoicePriorityComparator
							.compare(firstChoice, firstChance) < 0)) {
				// 已经决定动作，就是firstChoice
				finalChoiceLocation = firstChoice.getKey();
				finalChoice = firstChoice.getValue();
				return true;
			} else if (firstChoice == null && firstChance == null) {
				return true;
			} else {
				return false;
			}
		}

		/**
		 * 应用@link #finalChoiceLocation}和{@link #finalChoice}
		 * 两个变量的选择，并自动选择是否为下一个的玩家摸牌。
		 * 
		 * @return 得牌玩家和摸的牌
		 */
		private LocationAndTile importCpkAndDraw() {
			try {
				PlayerLocation resultLocation;
				Tile drawedTile;
				if (finalChoiceLocation != null) {
					PlayerInfo playerInfo = playerInfos
							.get(finalChoiceLocation);
					if (finalChoice.win) {
						// win
						playerInfo.tiles.addTile(discardedTile);
						endGame(finalChoiceLocation, discardedLocation,
								discardedTile);
						return null;
					} else {
						// cpk
						Cpk cpk = finalChoice.cpk;
						playerInfo.tiles.importCpk(cpk);

						fireCpk(finalChoiceLocation, cpk);

						if (cpk.getType().isKong())
							drawedTile = draw(finalChoiceLocation, true, false);
						else
							drawedTile = null;
						resultLocation = finalChoiceLocation;
					}
				} else {
					// 玩家没有机会，或已全部放弃
					// 为下一个玩家摸牌
					PlayerLocation nextLocation = discardedLocation
							.getLocationOf(Relation.NEXT);
					drawedTile = draw(nextLocation, true, false);
					resultLocation = nextLocation;
				}
				return new LocationAndTile(resultLocation, drawedTile);
			} catch (DrawGameException e) {
				// 流局
				endGame(null, null, null);
				return null;
			}
		}

		/**
		 * 结束等待时调用。清除等待时使用的各种变量，并停止计时。
		 */
		private void end() {
			if (discardedLocation == null)
				return;

			timer.stop();
			discardedLocation = null;
			discardedTile = null;
			cpkwChances.clear();
			cpkwChoices.clear();
			playerChoosings.clear();
			finalChoiceLocation = null;
			finalChoice = null;
		}
	}

	/**
	 * 等待出牌的处理器，负责根据玩家的请求做出适当的动作。同时还负责计时，并在超时时做出必要动作。
	 * 
	 * @author blovemaple
	 */
	private class DiscardingHandler implements StateHandler {

		private Future<?> choosing;

		private TimerAction timerAction = new TimerAction() {

			@Override
			public void countRun(long remainSecs) {
				fireTimeLimit(remainSecs);
			}

			@Override
			public void timeoutRun() {
				if (choosing != null)
					choosing.cancel(true);
			}

			@Override
			public void stopRun() {
				fireTimeLimit(TimeLimitEvent.STOP_TIME_LIMIT);
			}
		};

		/**
		 * 摸牌后调用。
		 * 
		 * @param locationAndTile
		 *            摸牌的玩家和摸的牌。如果摸的牌为null，表示是吃/碰之后。
		 * @return 出牌的玩家和出的牌
		 * 
		 * @throws InterruptedException
		 * @throws IllegalStateException
		 *             此时已经在等待中，或者此时指定玩家不能等待出牌
		 * @throws IllegalArgumentException
		 *             摸的牌不在指定玩家手中
		 */
		@Override
		public LocationAndTile handle(LocationAndTile locationAndTile)
				throws InterruptedException {
			try {
				PlayerLocation drawedLocation = locationAndTile.location;
				final Tile drawedTile = locationAndTile.tile;

				final PlayerInfo playerInfo = playerInfos.get(drawedLocation);

				if (drawedLocation != null)
					throw new IllegalStateException("在等待位置[" + drawedLocation
							+ "]的玩家出牌");
				if (!playerInfo.tiles.isForDiscarding())
					throw new IllegalStateException(drawedLocation + "玩家不能等待出牌");
				if (drawedTile != null
						&& !playerInfo.tiles.getAliveTiles().contains(
								drawedTile))
					throw new IllegalArgumentException("牌" + drawedTile + "不在"
							+ drawedLocation + "玩家手中");

				final Set<TileType> readyHandChances = winStrategy
						.getReadyHandChances(playerInfo.tiles);
				timer.start(timeStrategy.discardLimit(), TimeUnit.SECONDS,
						timerAction);

				// 如果有杠/和机会，让玩家选择
				final Set<CpkwChoice> kwChances = new HashSet<>();
				for (Cpk cpk : CpkType.getAllChances(playerInfo.tiles,
						drawedTile, Relation.SELF))
					kwChances.add(CpkwChoice.chooseCpk(cpk));
				if (getWinStrategy().isWin(playerInfo.tiles))
					kwChances.add(CpkwChoice.chooseWin());

				CpkwChoice kwChoice = null;
				if (!kwChances.isEmpty()) {
					choosing = submitToThreadPool(new Callable<CpkwChoice>() {

						@Override
						public CpkwChoice call() throws Exception {
							return playerInfo.player.chooseCpk(
									playerInfo.playerView, kwChances,
									drawedTile, true);
						}
					});
					try {
						kwChoice = (CpkwChoice) choosing.get();
					} catch (ExecutionException e) {
						e.printStackTrace();// XXX - 玩家选择吃/碰/杠/和实现中的异常未处理
					} catch (CancelledKeyException e) {
						// 被取消，保持kwChoice为null，即放弃选择
					}
				}
				if (kwChoice != null) {
					if (kwChoice.win) {
						// 选择和牌
						endGame(drawedLocation, null, drawedTile);
						return null;
					} else {
						// 选择杠牌
						playerInfo.tiles.importCpk(kwChoice.cpk);
						fireCpk(drawedLocation, kwChoice.cpk);
					}
				}

				// 让玩家选择出牌
				Tile discardTile;
				boolean readyHand = false;
				boolean timeout = false;
				choosing = submitToThreadPool(new Callable<DiscardChoice>() {

					@Override
					public DiscardChoice call() throws Exception {
						return playerInfo.player.chooseDiscard(
								playerInfo.playerView, readyHandChances,
								drawedTile);
					}
				});
				try {
					DiscardChoice choice = (DiscardChoice) choosing.get();
					discardTile = choice.discardTile;
					readyHand = choice.readyHand;
					if (playerInfo.tiles.getAliveTiles().contains(discardTile)
							|| (readyHand && !readyHandChances
									.contains(discardTile.getType()))) {
						throw new CancellationException();// XXX - 选择错误未处理并视同超时
					}
				} catch (ExecutionException | CancellationException e) {
					if (e instanceof ExecutionException)
						e.printStackTrace();// XXX - 玩家选择出牌实现中的异常未处理

					// 超时
					timeout = true;
					discardTile = drawedTile;
					if (discardTile == null) {
						for (Tile tile : playerInfos.get(drawedLocation).tiles
								.getAliveTiles()) {
							if (discardTile == null
									|| tile.compareTo(discardTile) > 0)
								discardTile = tile;
						}
					}
				}
				discard(drawedLocation, discardTile, readyHand, timeout);

				return new LocationAndTile(drawedLocation, discardTile);
			} catch (InterruptedException e) {
				timer.stop();
				if (choosing != null)
					choosing.cancel(true);
				throw e;
			}

		}

		private void discard(PlayerLocation location, Tile tile,
				boolean readyHand, boolean timeout) {
			timer.stop();
			playerInfos.get(location).tiles.removeTile(tile, readyHand);

			fireDiscard(location, tile, readyHand, timeout);
		}
	}

}
