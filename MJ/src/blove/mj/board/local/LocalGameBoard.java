package blove.mj.board.local;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import blove.mj.Cpk;
import blove.mj.CpkType;
import blove.mj.GameBoardView;
import blove.mj.GameResult;
import blove.mj.GameResult.WinInfo;
import blove.mj.Player;
import blove.mj.PlayerLeavedException;
import blove.mj.PlayerLocation;
import blove.mj.PlayerLocation.Relation;
import blove.mj.Tile;
import blove.mj.TileType;
import blove.mj.board.DiscardedTiles;
import blove.mj.board.GameBoard;
import blove.mj.board.GameBoardFullException;
import blove.mj.board.PlayerTiles;
import blove.mj.board.Wall;
import blove.mj.board.local.Timer.TimerAction;
import blove.mj.event.GameEvent;
import blove.mj.event.GameEventListener;
import blove.mj.event.GameOverEvent;
import blove.mj.event.GameStartEvent;
import blove.mj.event.PlayerActionEvent;
import blove.mj.event.PlayerEvent;
import blove.mj.event.PlayerEvent.PlayerEventType;
import blove.mj.event.TimeLimitEvent;
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
	private final LocalWall wall = new LocalWall();
	private final LocalDiscardTiles discardTiles = new LocalDiscardTiles();

	private final TimeLimitStrategy timeStrategy;
	private final WinStrategy winStrategy;

	private boolean inGame = false;
	private PlayerLocation dealerLocation = PlayerLocation.EAST;// 一局结束时设置下局庄家

	private DiscardedWaiter discardedWaiter = new DiscardedWaiter();
	private DrawedWaiter drawedWaiter = new DrawedWaiter();
	private Timer timer = new Timer();

	private final List<GameResult> resultList = new LinkedList<>();

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
	public Map<PlayerLocation, Player> getPlayers() {
		Map<PlayerLocation, Player> players = new EnumMap<>(
				PlayerLocation.class);
		for (Map.Entry<PlayerLocation, PlayerInfo> playerInfoEntry : playerInfos
				.entrySet()) {
			players.put(playerInfoEntry.getKey(),
					playerInfoEntry.getValue().player);
		}
		return players;
	}

	@Override
	public boolean isInGame() {
		return inGame;
	}

	@Override
	public synchronized GameBoardView newPlayer(String name)
			throws GameBoardFullException {
		GameBoardView boardView = null;
		Player player = null;
		PlayerLocation playerLocation = null;
		for (PlayerLocation location : PlayerLocation.values()) {
			if (playerInfos.get(location) == null) {
				player = new Player(name);
				PlayerInfo playerInfo = new PlayerInfo(player);
				playerLocation = location;
				playerInfos.put(location, playerInfo);
				boardView = new LocalGameBoardView(player, location);
				break;
			}
		}
		if (boardView == null)
			throw new GameBoardFullException(this);

		firePlayerIn(player, playerLocation);
		return boardView;
	}

	@Override
	public TimeLimitStrategy getTimeLimitStrategy() {
		return timeStrategy;
	}

	@Override
	public WinStrategy getWinStrategy() {
		return winStrategy;
	}

	private void startGame() {
		if (inGame)
			throw new IllegalStateException("已在游戏中");

		wall.init();
		discardTiles.init();
		for (PlayerInfo info : playerInfos.values())
			info.tiles.init();
		inGame = true;

		fireGameStart();

		Tile lastTile = deal();
		drawedWaiter.startWaiting(dealerLocation, lastTile);
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

			fireDealOver(lastTile);

			return lastTile;
		} catch (DrawGameException e) {
			// 发牌时不可能流局
			throw new RuntimeException(e);
		}
	}

	private void draw(PlayerLocation location, boolean fromSeabed,
			boolean timeout) {
		try {
			Tile tile = fromSeabed ? wall.drawFromSeabed() : wall.draw();
			playerInfos.get(location).tiles.addTile(tile);

			fireDraw(location, tile);

			drawedWaiter.startWaiting(location, tile);
		} catch (DrawGameException e) {
			endGame(null, null, null);
		}
	}

	/**
	 * 一局游戏结束时调用。记录结果并重置状态。
	 * 
	 * @param winnerLocation
	 *            赢家位置。如果为null则表示流局。
	 * @param paoerLocation
	 *            点炮者位置。如果为null则表示流局或自摸和。
	 * @param winTile
	 *            和牌。如果为null则表示流局或天和。
	 */
	private void endGame(PlayerLocation winnerLocation,
			PlayerLocation paoerLocation, Tile winTile) {
		if (!inGame)
			throw new IllegalStateException("未在游戏中");

		recordResult(winnerLocation, paoerLocation, winTile);
		stopGame();
	}

	private void recordResult(PlayerLocation winnerLocation,
			PlayerLocation paoerLocation, Tile winTile) {
		Map<PlayerLocation, Player> players = new EnumMap<>(
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

			players.put(location, playerInfo.player);
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

		discardedWaiter.reset();
		drawedWaiter.reset();
		for (PlayerInfo info : playerInfos.values())
			info.ready = false;
		inGame = false;
		dealerLocation = dealerLocation.getLocationOf(Relation.NEXT);
	}

	protected void firePlayerIn(Player player, PlayerLocation location) {
		fireGameEvent(new PlayerEvent(this, PlayerEventType.IN, player,
				location));
	}

	protected void firePlayerReady(Player player, PlayerLocation location) {
		fireGameEvent(new PlayerEvent(this, PlayerEventType.READY, player,
				location));
	}

	protected void firePlayerOut(Player player, PlayerLocation location) {
		fireGameEvent(new PlayerEvent(this, PlayerEventType.OUT, player,
				location));
	}

	protected void fireGameStart() {
		fireGameEvent(new GameStartEvent(this));
	}

	protected void fireDealOver(Tile dealerTile) {
		fireGameEvent(PlayerActionEvent.newForDealOver(this, dealerTile));
	}

	protected void fireTimeLimit(long timeLimit) {
		fireGameEvent(new TimeLimitEvent(this, timeLimit));
	}

	protected void fireDraw(PlayerLocation playerLocation, Tile tile) {
		fireGameEvent(PlayerActionEvent.newForDraw(this, playerLocation, tile));
	}

	protected void fireDiscard(PlayerLocation playerLocation, Tile tile,
			boolean newReadyHand, boolean forTimeOut) {
		fireGameEvent(PlayerActionEvent.newForDiscard(this, playerLocation,
				tile, newReadyHand, forTimeOut));
	}

	protected void fireCpk(PlayerLocation playerLocation, Cpk cpk) {
		fireGameEvent(PlayerActionEvent.newForCpk(this, playerLocation, cpk));
	}

	protected void fireGameOver(GameResult result) {
		fireGameEvent(new GameOverEvent(this, result));
	}

	private void fireGameEvent(GameEvent event) {
		try {
			Method m = GameEventListener.class.getMethod(
					GameEventListener.METHOD_NAME, event.getClass());
			for (PlayerInfo playerInfo : playerInfos.values()) {
				Object[] listeners = playerInfo.listenerList.getListenerList();
				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					if (listeners[i] == GameEventListener.class) {
						try {
							m.invoke(listeners[i + 1], event);
						} catch (InvocationTargetException e) {
							// 忽略监听器方法内抛出的异常
							e.printStackTrace();
						}
					}
				}
			}
		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException e) {
			throw new RuntimeException("监听器方法不能调用：", e);
		}
	}

	private static class PlayerInfo {
		Player player;
		LocalPlayerTiles tiles = new LocalPlayerTiles();
		AsyncEventListenerList listenerList = new AsyncEventListenerList();
		boolean ready = false;

		PlayerInfo(Player player) {
			this.player = player;
		}
	}

	private class LocalGameBoardView implements GameBoardView {
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
		public PlayerLocation getDealerLocation() {
			return dealerLocation;
		}

		@Override
		public void addGameEventListener(GameEventListener listener) {
			try {
				playerInfos.get(location).listenerList.add(
						GameEventListener.class, listener,
						GameEventListener.class.getMethod(
								GameEventListener.METHOD_NAME,
								TimeLimitEvent.class));
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException();
			}
		}

		@Override
		public void removeGameEventListener(GameEventListener listener) {
			playerInfos.get(location).listenerList.remove(
					GameEventListener.class, listener);
		}

		@Override
		public Map<PlayerLocation, Player> getPlayers() {
			checkNotLeaved();
			synchronized (LocalGameBoard.this) {
				Map<PlayerLocation, Player> players = new EnumMap<>(
						PlayerLocation.class);
				for (Map.Entry<PlayerLocation, PlayerInfo> locationPlayerInfo : playerInfos
						.entrySet())
					players.put(locationPlayerInfo.getKey(),
							locationPlayerInfo.getValue().player);
				return players;
			}
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

				playerInfos.get(location).ready = true;

				firePlayerReady(player, location);

				// 如果有四个玩家且都准备好了，就开始游戏
				if (playerInfos.size() == 4) {
					boolean allReady = true;
					for (PlayerInfo info : playerInfos.values()) {
						if (!info.ready) {
							allReady = false;
							break;
						}
					}
					if (allReady) {
						startGame();
					}
				}
			}
		}

		@Override
		public void discard(Tile tile, boolean readyHand) {
			checkNotLeaved();
			synchronized (LocalGameBoard.this) {
				checkInGame(true);

				drawedWaiter.discard(location, tile, readyHand);
			}
		}

		@Override
		public void cpk(Cpk cpk) {
			checkNotLeaved();
			synchronized (LocalGameBoard.this) {
				checkInGame(true);

				if (location == drawedWaiter.getWaitingLocation()) {
					drawedWaiter.selfDrawKong(location, cpk);
				} else
					discardedWaiter.cpkRequest(location, cpk);
			}
		}

		@Override
		public void win() {
			checkNotLeaved();
			synchronized (LocalGameBoard.this) {
				checkInGame(true);

				if (location == drawedWaiter.getWaitingLocation())
					drawedWaiter.selfDrawWin(location);
				else
					discardedWaiter.winRequest(location);
			}
		}

		@Override
		public void giveUpCpkw() {
			checkNotLeaved();
			synchronized (LocalGameBoard.this) {
				checkInGame(true);

				discardedWaiter.giveUpCpkw(location);
			}
		}

		@Override
		public List<GameResult> getResults() {
			return Collections.unmodifiableList(resultList);
		}

		@Override
		public void leave() {
			checkNotLeaved();
			synchronized (LocalGameBoard.this) {
				if (inGame)
					stopGame();
				playerInfos.get(location).listenerList.close();// 关闭监听器列表的事件指派线程
				playerInfos.remove(location);
				leaved = true;

				firePlayerOut(player, location);
			}
		}

	}

	/**
	 * 打牌后等待状态的管理员，负责在等待动作时管理它们的优先级别，根据玩家的请求做出适当的动作。同时还负责计时，并在超时时做出必要动作。
	 * 
	 * @author blovemaple
	 */
	private class DiscardedWaiter {
		private PlayerLocation discardedLocation;
		private Tile discardedTile;
		private Map<PlayerLocation, Set<Cpk>> cpkChances = new EnumMap<>(
				PlayerLocation.class);// 无选择或放弃者置null
		private Set<PlayerLocation> winChanceLocations = EnumSet
				.noneOf(PlayerLocation.class);
		private Map<PlayerLocation, Cpk> cpkChoices = new EnumMap<>(
				PlayerLocation.class);

		private TimerAction timerAction = new TimerAction() {

			@Override
			public void countRun(long remainSecs) {
				synchronized (LocalGameBoard.this) {
					fireTimeLimit(remainSecs);
				}
			}

			@Override
			public void timeoutRun() {
				synchronized (LocalGameBoard.this) {
					// 超时将会为下一个玩家摸牌
					end(true, true);
				}
			}

			@Override
			public void stopRun() {
				synchronized (LocalGameBoard.this) {
					fireTimeLimit(TimeLimitEvent.STOP_TIME_LIMIT);
				}
			}
		};

		/**
		 * 打牌后调用。通知监听器。如果有需要，则开始等待；如果没有需要，则为下一个玩家摸牌。
		 * 
		 * @param location
		 *            打出牌的玩家位置
		 * @param discardedTile
		 *            刚刚打出的牌
		 * @param newReadyHand
		 *            打出此牌后听牌
		 * @param timeout
		 *            是否是超时
		 * @throws IllegalStateException
		 *             此时已经在等待中
		 */
		void discarded(PlayerLocation location, Tile discardedTile,
				boolean newReadyHand, boolean timeout) {
			if (discardedLocation != null)
				throw new IllegalStateException("正在等待。上一次出牌的玩家位置是"
						+ discardedLocation + "。");

			this.discardedLocation = location;
			this.discardedTile = discardedTile;

			prepareForWaiting();

			/*try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}*/

			if (checkForStop())
				return;

			timer.start(timeStrategy.discardLimit(), TimeUnit.SECONDS,
					timerAction);
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
					PlayerTiles tiles = playerInfo.tiles;
					Set<Cpk> chances = CpkType.getAllChances(tiles,
							discardedTile,
							location.getRelationOf(discardedLocation));
					if (!chances.isEmpty())
						cpkChances.put(location, chances);

					if (winStrategy.getWinChances(tiles).contains(
							discardedTile.getType()))
						winChanceLocations.add(location);
				}
			}

		}

		/**
		 * 检查当前是否可以中止等待。若可以中止则中止，并进行合适的动作，且为下一个玩家摸牌。
		 * 
		 * @return 如果中止，返回true；否则返回false。
		 */
		private boolean checkForStop() {
			if (!winChanceLocations.isEmpty())
				return false;

			TreeSet<Cpk> cpkChances = new TreeSet<>(Cpk.priorityComparator);
			for (Set<Cpk> cpks : this.cpkChances.values()) {
				if (cpks != null)
					cpkChances.addAll(cpks);
			}

			TreeSet<Cpk> cpkChoices = new TreeSet<>(Cpk.priorityComparator);
			cpkChoices.addAll(this.cpkChoices.values());

			Cpk firstChoice = cpkChoices.isEmpty() ? null : cpkChoices.first();
			Cpk firstChance = cpkChances.isEmpty() ? null : cpkChances.first();

			if (firstChoice != null
					&& (firstChance == null || (Cpk.priorityComparator.compare(
							firstChoice, firstChance) < 0))) {
				// 已经决定动作，就是firstChoice
				PlayerLocation location = getLocationOfCpk(firstChoice);
				LocalPlayerTiles tiles = playerInfos.get(location).tiles;
				tiles.importCpk(firstChoice);

				fireCpk(location, firstChoice);

				end(false, false);
				if (firstChoice.getType().isKong())
					draw(location, false, false);
				else
					drawedWaiter.startWaiting(location, null);
				return true;
			} else if (firstChoice == null && firstChance == null) {
				// 玩家没有机会，或已全部放弃
				end(true, false);
				return true;
			}

			return false;
		}

		private PlayerLocation getLocationOfCpk(Cpk cpk) {
			for (Entry<PlayerLocation, Set<Cpk>> cpks : cpkChances.entrySet()) {
				if (cpks.getValue() != null && cpks.getValue().contains(cpk))
					return cpks.getKey();
			}
			for (Entry<PlayerLocation, Cpk> cpkChoice : cpkChoices.entrySet()) {
				if (cpk.equals(cpkChoice.getValue()))
					return cpkChoice.getKey();
			}
			return null;
		}

		/**
		 * 结束等待时调用。清除等待时使用的各种变量，并停止计时。
		 * 
		 * @param nextDraw
		 *            是否为下一个玩家摸牌
		 * @param timeout
		 *            是否是超时
		 */
		synchronized private void end(boolean nextDraw, boolean timeout) {
			if (discardedLocation == null)
				return;

			PlayerLocation nextLocation = null;
			if (nextDraw)
				nextLocation = discardedLocation.getLocationOf(Relation.NEXT);
			timer.stop();
			discardedLocation = null;
			discardedTile = null;
			cpkChances.clear();
			winChanceLocations.clear();
			cpkChoices.clear();
			if (nextDraw)
				draw(nextLocation, false, timeout);
		}

		/**
		 * 吃/碰/杠请求。
		 * 
		 * @param location
		 *            玩家位置
		 * @param cpk
		 *            吃/碰/明杠
		 * @throws IllegalStateException
		 *             指定玩家此时不该吃/碰/明杠
		 * @throws IllegalArgumentException
		 *             指定玩家此时不能进行指定的吃/碰/杠动作
		 */
		void cpkRequest(PlayerLocation location, Cpk cpk) {
			if (cpkChances.get(location) == null)
				throw new IllegalStateException("玩家位置" + location
						+ "没有任何吃/碰/杠机会。");
			if (!cpkChances.get(location).contains(cpk))
				throw new IllegalArgumentException("玩家位置" + location
						+ "此时不能进行动作：" + cpk);

			cpkChances.remove(location);
			winChanceLocations.remove(location);
			cpkChoices.put(location, cpk);

			checkForStop();
		}

		/**
		 * 非自摸和牌请求。
		 * 
		 * @param location
		 *            玩家位置
		 * @throws IllegalStateException
		 *             指定玩家此时不能和牌
		 */
		void winRequest(PlayerLocation location) {
			if (!winChanceLocations.contains(location))
				throw new IllegalStateException("玩家位置" + location + "此时不能和牌。");

			playerInfos.get(location).tiles.addTile(discardedTile);
			endGame(location, discardedLocation, discardedTile);
		}

		/**
		 * 放弃吃/碰/杠/非自摸和机会。
		 * 
		 * @param location
		 *            玩家位置
		 * @throws IllegalStateException
		 *             指定玩家此时没有吃/碰/杠/非自摸和机会
		 */
		void giveUpCpkw(PlayerLocation location) {
			if (cpkChances.get(location) == null
					&& !winChanceLocations.contains(location))
				throw new IllegalStateException("玩家位置" + location
						+ "没有任何吃/碰/杠/非自摸和机会。");

			cpkChances.remove(location);
			winChanceLocations.remove(location);

			checkForStop();
		}

		/**
		 * 重置。
		 */
		void reset() {
			end(false, false);
		}
	}

	/**
	 * 摸牌后等待的管理员，负责根据玩家的请求做出适当的动作。同时还负责计时，并在超时时做出必要动作。
	 * 
	 * @author blovemaple
	 */
	private class DrawedWaiter {
		private PlayerLocation waitingLocation;
		private Tile drawedTile;
		private Set<TileType> readyHandChances;

		private TimerAction timerAction = new TimerAction() {

			@Override
			public void countRun(long remainSecs) {
				synchronized (LocalGameBoard.this) {
					fireTimeLimit(remainSecs);
				}
			}

			@Override
			public void timeoutRun() {
				synchronized (LocalGameBoard.this) {
					// 如果是摸牌，则超时将会打出刚刚摸的牌，否则会打出最大的牌
					Tile discardTile = drawedTile;
					if (discardTile == null) {
						for (Tile tile : playerInfos.get(waitingLocation).tiles
								.getAliveTiles()) {
							if (discardTile == null
									|| tile.compareTo(discardTile) > 0)
								discardTile = tile;
						}
					}
					discard(waitingLocation, drawedTile, false, true);
				}
			}

			@Override
			public void stopRun() {
				synchronized (LocalGameBoard.this) {
					fireTimeLimit(TimeLimitEvent.STOP_TIME_LIMIT);
				}
			}
		};

		/**
		 * 开始等待玩家出牌。
		 * 
		 * @param drawedLocation
		 *            摸牌的玩家位置
		 * @param drawedTile
		 *            摸的牌。如果是null，则表示是吃/碰之后。
		 * 
		 * @throws IllegalStateException
		 *             此时已经在等待中，或者此时指定玩家不能等待出牌
		 * @throws IllegalArgumentException
		 *             摸的牌不在指定玩家手中
		 */
		void startWaiting(PlayerLocation drawedLocation, Tile drawedTile) {
			if (waitingLocation != null)
				throw new IllegalStateException("在等待位置[" + waitingLocation
						+ "]的玩家出牌");
			if (!playerInfos.get(drawedLocation).tiles.isForDiscarding())
				throw new IllegalStateException(drawedLocation + "玩家不能等待出牌");
			if (drawedTile != null
					&& !playerInfos.get(drawedLocation).tiles.getAliveTiles()
							.contains(drawedTile))
				throw new IllegalArgumentException("牌" + drawedTile + "不在"
						+ drawedLocation + "玩家手中");

			this.waitingLocation = drawedLocation;
			this.drawedTile = drawedTile;
			prepareForWaiting();
			timer.start(timeStrategy.discardLimit(), TimeUnit.SECONDS,
					timerAction);
		}

		/**
		 * 开始等待前调用。寻找此玩家可以听的牌，并记入readyHandChances。
		 * 
		 * @throws IllegalStateException
		 *             此时没有玩家可以等待出牌
		 */
		private void prepareForWaiting() {
			readyHandChances = winStrategy.getReadyHandChances(playerInfos
					.get(waitingLocation).tiles);
		}

		/**
		 * 返回正在等待出牌的玩家位置。
		 * 
		 * @return 位置。如果没在等待，则返回null。
		 */
		PlayerLocation getWaitingLocation() {
			return waitingLocation;
		}

		private void checkLocation(PlayerLocation location) {
			if (location != waitingLocation)
				throw new IllegalStateException("玩家位置" + location + "此时不该打牌");
		}

		/**
		 * 出牌。
		 * 
		 * @param location
		 *            玩家位置
		 * @param tile
		 *            牌
		 * @param readyHand
		 *            是否听牌
		 * @throws IllegalStateException
		 *             指定玩家此时不该打牌
		 * @throws IllegalArgumentException
		 *             指定牌不在指定玩家手中，或指定听牌但出此牌不能叫听，或由于其他原因此牌不能打出
		 */
		void discard(PlayerLocation location, Tile tile, boolean readyHand) {
			checkLocation(location);
			discard(location, tile, readyHand, false);
		}

		private void discard(PlayerLocation location, Tile tile,
				boolean readyHand, boolean timeout) {
			if (waitingLocation == null)
				return;
			LocalPlayerTiles tiles = playerInfos.get(location).tiles;
			if (readyHand && !readyHandChances.contains(tile.getType()))
				throw new IllegalArgumentException("出牌“" + tile + "”不能叫听。");

			timer.stop();
			tiles.removeTile(tile, readyHand);
			waitingLocation = null;

			fireDiscard(location, tile, readyHand, timeout);

			discardedWaiter.discarded(location, tile, readyHand, timeout);
		}

		/**
		 * 自摸杠。
		 * 
		 * @param location
		 *            玩家位置
		 * @param cpk
		 *            杠
		 * @throws IllegalStateException
		 *             指定玩家此时没有摸牌
		 * @throws IllegalArgumentException
		 *             指定吃/碰/杠不是自摸杠，或者指定玩家此时不能进行指定的杠动作
		 */
		void selfDrawKong(PlayerLocation location, Cpk cpk) {
			checkLocation(location);
			if (!(cpk.getType() == CpkType.CONCEALED_KONG || (cpk.getType() == CpkType.EXPOSED_KONG && cpk
					.getFromRelation() == Relation.SELF)))
				throw new IllegalArgumentException(cpk + "不是自摸杠");

			timer.stop();
			playerInfos.get(location).tiles.importCpk(cpk);
			waitingLocation = null;

			fireCpk(location, cpk);

			draw(location, true, false);
		}

		/**
		 * 自摸和。
		 * 
		 * @param location
		 *            玩家位置
		 * @throws IllegalStateException
		 *             指定位置玩家此时不能和牌
		 */
		void selfDrawWin(PlayerLocation location) {
			checkLocation(location);
			if (drawedTile == null
					|| !winStrategy.isWin(playerInfos.get(location).tiles))
				throw new IllegalStateException(location + "玩家此时不能和牌");

			endGame(location, null, drawedTile);
		}

		/**
		 * 重置。
		 */
		void reset() {
			timer.stop();
			waitingLocation = null;
		}
	}

}
