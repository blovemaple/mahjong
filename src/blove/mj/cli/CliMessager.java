package blove.mj.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import blove.mj.Cpk;
import blove.mj.GameResult;
import blove.mj.GameResult.WinInfo;
import blove.mj.PlayerLocation;
import blove.mj.PlayerLocation.Relation;
import blove.mj.PointItem;
import blove.mj.PointsResult;
import blove.mj.Tile;
import blove.mj.TileType;
import blove.mj.TileType.Suit;
import blove.mj.board.PlayerTiles;
import blove.mj.cli.CliView.CharHandler;
import blove.mj.event.TimeLimitEvent;
import blove.mj.record.Recorder;

/**
 * 提供向CliView提供展示信息的便捷方法。
 * 
 * @author blovemaple
 */
class CliMessager {
	private final CliView cliView;

	/**
	 * 新建一个实例。
	 * 
	 * @param cliView
	 *            CliView
	 */
	CliMessager(CliView cliView) {
		this.cliView = cliView;
	}

	/**
	 * @throws IOException
	 * @see blove.mj.cli.CliView#init()
	 */
	void initView() throws IOException {
		cliView.init();
	}

	/**
	 * @param handler
	 * @see blove.mj.cli.CliView#addCharHandler(blove.mj.cli.CliView.CharHandler,
	 *      boolean)
	 */
	void addCharHandler(CharHandler handler) {
		try {
			cliView.addCharHandler(handler, false);
		} catch (InterruptedException e) {
			throw new RuntimeException();// 不等待，不可能出现
		}
	}

	/**
	 * @param handler
	 * @throws InterruptedException
	 * @see blove.mj.cli.CliView#addCharHandler(blove.mj.cli.CliView.CharHandler,
	 *      boolean)
	 */
	void addCharHandlerAndWait(CharHandler handler) throws InterruptedException {
		cliView.addCharHandler(handler, true);
	}

	/**
	 * 显示消息。
	 * 
	 * @param action
	 *            动作
	 * @param location
	 *            玩家位置。如果为null表示与具体玩家无关。
	 * @param playerName
	 *            玩家名称。如果为null表示与具体玩家无关。
	 * @param message
	 *            信息
	 * @param myLocation
	 *            当前玩家位置
	 * @throws IOException
	 */
	void showMessage(String action, PlayerLocation location, String playerName,
			String message, PlayerLocation myLocation) throws IOException {
		// [LOCATION]player [ACTION] message
		StringBuilder messageViewStr = new StringBuilder();

		if (location != null) {
			messageViewStr.append(String.format("%-20s",
					toString(location, myLocation) + playerName));
		}

		messageViewStr.append(String.format("%-10s", "[" + action.toUpperCase()
				+ "]"));

		if (message != null) {
			messageViewStr.append(' ');
			messageViewStr.append(message);
		}

		cliView.printMessage(messageViewStr.toString());
	}

	/**
	 * 显示玩家信息。
	 * 
	 * @param playerNames
	 *            玩家名称
	 * @param recorder
	 *            记录管理器
	 * @param readyHandLocations
	 *            听和的玩家位置
	 * @throws IOException
	 */
	void showPlayerInfos(Map<PlayerLocation, String> playerNames,
			Recorder recorder, Set<PlayerLocation> readyHandLocations)
			throws IOException {
		final int width = 10;

		StringBuilder locationStr = new StringBuilder();
		StringBuilder playerNameStr = new StringBuilder();
		StringBuilder pointsStr = new StringBuilder();
		StringBuilder readyHandStr = new StringBuilder();

		for (Map.Entry<PlayerLocation, String> playerNameEntry : playerNames
				.entrySet()) {
			PlayerLocation location = playerNameEntry.getKey();
			String playerName = playerNameEntry.getValue();
			int point = recorder.getPoints(playerName);
			boolean readyHand = readyHandLocations.contains(location);

			locationStr.append(String.format("%-" + width + "s", "[" + location
					+ "]"));
			playerNameStr.append(String.format("%-" + width + "s", playerName));
			pointsStr.append(String.format("%-" + width + "d", point));
			readyHandStr.append(String.format("%-" + width + "s",
					readyHand ? "(READYHAND)" : ""));
		}

		String playerInfos = locationStr + System.lineSeparator()
				+ playerNameStr + System.lineSeparator() + pointsStr;
		if (!readyHandLocations.isEmpty())
			playerInfos += System.lineSeparator() + readyHandStr;

		cliView.printSpecialMessage("Player Information", playerInfos);
	}

	/**
	 * 显示一局结果。
	 * 
	 * @param result
	 *            结果
	 * @param myLocation
	 *            当前玩家位置
	 * @param recorder
	 *            记录管理器。如果提供了此参数，则会显示当前各玩家总分。
	 * @throws IOException
	 */
	void showResult(GameResult result, PlayerLocation myLocation,
			Recorder recorder) throws IOException {
		cliView.printSpecialMessage("RESULT",
				toString(result, myLocation, recorder));
	}

	private PlayerTiles tiles;
	private Tile drawedTile;
	private Set<Tile> focusTiles;
	private String extraOptions;
	private String statusInfo;
	private long time = TimeLimitEvent.STOP_TIME_LIMIT;
	private final Deque<String> tempStatusKeys = new LinkedList<>();
	private final Map<String, String> tempStatuses = new HashMap<>();

	/**
	 * 在状态栏显示与本玩家的牌相关的信息。
	 * 
	 * @param tiles
	 *            玩家的牌
	 * @param drawedTile
	 *            刚摸的牌，放在最后显示。null表示没有。
	 * @param focusTile
	 *            聚焦的牌。吃/碰/杠中的牌，只有一组全部聚焦才能聚焦。
	 * @param extraOptions
	 *            额外选项。如果为null表示没有额外选项。
	 * @param statusInfo
	 *            状态信息。如果为null表示没有状态信息。
	 */
	void tilesStatus(PlayerTiles tiles, Tile drawedTile, Tile focusTile,
			String extraOptions, String statusInfo) {
		tilesStatus(tiles, drawedTile,
				focusTile != null ? Collections.singleton(focusTile) : null,
				extraOptions, statusInfo);
	}

	/**
	 * 在状态栏显示与本玩家的牌相关的信息。
	 * 
	 * @param tiles
	 *            玩家的牌
	 * @param drawedTile
	 *            刚摸的牌，放在最后显示。null表示没有。
	 * @param focusTiles
	 *            聚焦的多张牌。此集合内不包含于玩家手中的牌将被忽略。吃/碰/杠中的牌，只有一组全部聚焦才能聚焦。
	 * @param extraOptions
	 *            额外选项。如果为null表示没有额外选项。
	 * @param statusInfo
	 *            状态信息。如果为null表示没有状态信息。
	 */
	void tilesStatus(PlayerTiles tiles, Tile drawedTile, Set<Tile> focusTiles,
			String extraOptions, String statusInfo) {
		this.tiles = tiles;
		this.drawedTile = drawedTile;
		this.focusTiles = focusTiles != null ? focusTiles : Collections
				.<Tile> emptySet();
		this.extraOptions = extraOptions;
		this.statusInfo = statusInfo;
		refreshTileStatus();
	}

	/**
	 * 状态栏显示时间信息。
	 * 
	 * @param time
	 *            时间
	 */
	void timeStatus(long time) {
		this.time = time;
		refreshTileStatus();
	}

	/**
	 * 在状态栏清除时间信息。
	 */
	void clearTimeStatus() {
		timeStatus(TimeLimitEvent.STOP_TIME_LIMIT);
	}

	private void refreshTileStatus() {
		if (tempStatuses.isEmpty()) {
			// playerTiles <extra options> [STATUS] time
			StringBuilder status = new StringBuilder();
			if (tiles != null) {
				status.append(toString(tiles, drawedTile, focusTiles));
				if (extraOptions != null) {
					status.append(" <");
					status.append(extraOptions);
					status.append('>');
				}
				if (this.statusInfo != null) {
					status.append(" [");
					status.append(this.statusInfo.toUpperCase());
					status.append(']');
				}
				if (time != TimeLimitEvent.STOP_TIME_LIMIT) {
					status.append(' ');
					status.append(time);
				}
			}
			cliView.updateStatus(status.toString());
		}
	}

	private static final char SINGLE_FOCUS_HEAD = /* '→' */'(';
	private static final char SINGLE_FUCUS_TAIL = /* '←' */')';
	private static final char MULTI_FOCUS_HEAD = '[';
	private static final char MULTI_FOCUS_TAIL = ']';

	private static String toString(PlayerTiles playerTiles,
			final Tile drawedTile, Set<Tile> focusTiles) {
		// playerTiles(with →fucus← or [focuses])
		// eg: < W3W4W5 > < T7T8T9 > W1 W8 [T4 T5] O3 O5 E N
		// eg: < W3W4W5 > <[T7T8T9]> W1 W8 T4 T5 O3 O5 E N
		// eg: < W3W4W5 > < T7T8T9 > W1→W8← T4 T5 O3 O5 E N

		if (focusTiles == null)
			focusTiles = Collections.emptySet();

		StringBuilder str = new StringBuilder();
		for (Cpk cpk : playerTiles.getCpks()) {
			str.append(toString(cpk, focusTiles));
			str.append(' ');
		}

		final char focusHead = focusTiles.size() == 1 ? SINGLE_FOCUS_HEAD
				: MULTI_FOCUS_HEAD;
		final char focusTail = focusTiles.size() == 1 ? SINGLE_FUCUS_TAIL
				: MULTI_FOCUS_TAIL;
		Suit lastSuit = null;
		boolean lastFocus = false;
		List<Tile> aliveTiles = new ArrayList<>(playerTiles.getAliveTiles());
		Collections.sort(aliveTiles, new PlayerTilesViewComparator(drawedTile));
		for (Tile tile : aliveTiles) {
			Suit suit = tile.getType().getSuit();
			String separation = "";
			if (tile == drawedTile || lastSuit != suit) {
				separation = " ";
				lastSuit = suit;
			}

			boolean focus = focusTiles.contains(tile);
			if (!lastFocus && focus)
				str.append(separation).append(focusHead);
			else if (lastFocus && !focus)
				str.append(focusTail).append(separation);
			else
				str.append(' ').append(separation);
			str.append(toString(tile));
			lastFocus = focus;
		}
		str.append(lastFocus ? focusTail : ' ');

		return str.toString();
	}

	/**
	 * 此比较器将drawedTile排到最后，其余Tile按照自然顺序。
	 * 
	 * @author blovemaple
	 */
	static class PlayerTilesViewComparator implements Comparator<Tile> {
		private final Tile drawedTile;

		/**
		 * 新建一个实例。
		 * 
		 * @param drawedTile
		 */
		PlayerTilesViewComparator(Tile drawedTile) {
			this.drawedTile = drawedTile;
		}

		@Override
		public int compare(Tile o1, Tile o2) {
			if (o1 == o2)
				return 0;
			else if (o1 == drawedTile)
				return 1;
			else if (o2 == drawedTile)
				return -1;
			else
				return o1.compareTo(o2);
		}
	}

	private static String toString(Cpk cpk, Set<Tile> focusTiles) {
		boolean focus = focusTiles.containsAll(cpk.getTiles());

		StringBuilder str = new StringBuilder();
		str.append('<');
		str.append(focus ? MULTI_FOCUS_HEAD : ' ');
		for (Tile cpkTile : new TreeSet<>(cpk.getTiles()))
			str.append(toString(cpkTile));
		str.append(focus ? MULTI_FOCUS_TAIL : ' ');
		str.append('>');

		return str.toString();
	}

	private static String toString(GameResult result,
			PlayerLocation myLocation, Recorder recorder) {
		StringBuilder str = new StringBuilder();

		WinInfo winInfo = result.getWinInfo();
		for (Map.Entry<PlayerLocation, String> locationPlayer : result
				.getPlayers().entrySet()) {
			PlayerLocation location = locationPlayer.getKey();
			String player = locationPlayer.getValue();
			PlayerTiles playerTiles = result.getTiles().get(location);
			boolean isWinner = winInfo.getWinnerLocation() == location;
			boolean isPaoer = winInfo.getPaoerLocation() == location;
			boolean isDealer = result.getDealerLocation() == location;

			// [LOCATION][DEALER][WIN][PAO]player
			str.append(toString(location, myLocation));
			if (isDealer) {
				str.append('[');
				str.append("DEALER");
				str.append(']');
			}
			if (isWinner) {
				str.append('[');
				str.append("WIN");
				str.append(']');
			}
			if (isPaoer) {
				str.append('[');
				str.append("PAO");
				str.append(']');
			}
			str.append(player);
			str.append(System.lineSeparator());

			// player tiles with focus to Win tile
			str.append(toString(playerTiles, null,
					isWinner ? Collections.singleton(winInfo.getWinTile())
							: null));
			str.append(System.lineSeparator());
		}
		str.append(System.lineSeparator());

		PointsResult points = result.getPoints();
		List<PointItem> pointItemList = new ArrayList<>(points.getPointItems());
		str.append("Point items:").append(System.lineSeparator());
		if (!pointItemList.isEmpty()) {
			Collections.sort(pointItemList, PointItem.pointsComparator);
			for (PointItem pointItem : pointItemList) {
				// name point
				str.append(String.format("%-20s%d", pointItem.getName(),
						pointItem.getPoints()));
				str.append(System.lineSeparator());
			}
		}
		str.append(System.lineSeparator());

		str.append("Points:").append(System.lineSeparator());
		for (PlayerLocation location : PlayerLocation.values()) {
			String playerPoints;
			String player = toString(location, myLocation)
					+ result.getPlayers().get(location);
			if (recorder == null)
				playerPoints = String.format("%-20s%4d", player,
						points.getPoints(location));
			else {
				int crtPoint = recorder.getPoints(result.getPlayers().get(
						location));
				int deltaPoint = points.getPoints(location);
				char deltaSign = deltaPoint > 0 ? '+' : deltaPoint < 0 ? '-'
						: ' ';
				int oldPoint = crtPoint - deltaPoint;
				playerPoints = String.format("%-20s%3d %c %3d = %3d", player,
						oldPoint, deltaSign, Math.abs(deltaPoint), crtPoint);
			}
			str.append(playerPoints);
			str.append(System.lineSeparator());
		}

		return str.toString();
	}

	static String toString(PlayerLocation location, PlayerLocation myLocation) {
		String locationStr, relationStr;
		switch (location) {
		case EAST:
			locationStr = "E";
			break;
		case NORTH:
			locationStr = "N";
			break;
		case SOUTH:
			locationStr = "S";
			break;
		case WEST:
			locationStr = "W";
			break;
		default:
			throw new RuntimeException();// 已经列举完
		}
		Relation relation = myLocation.getRelationOf(location);
		switch (relation) {
		case NEXT:
			relationStr = "NEXT";
			break;
		case OPPOSITE:
			relationStr = "OPPO";
			break;
		case PREVIOUS:
			relationStr = "PREV";
			break;
		case SELF:
			relationStr = "SELF";
			break;
		default:
			throw new RuntimeException();// 已经列举完
		}

		return "[" + locationStr + "/" + relationStr + "]";
	}

	static String toString(Tile tile) {
		TileType tileType = tile.getType();
		if (tileType.getSuit().isHonor())
			return tileType.getSuit().toString();
		else
			return tileType.getSuit().toString() + tileType.getRank();
	}

	static String toString(Set<Tile> tiles) {
		List<Tile> tilesList = new ArrayList<>(tiles);
		Collections.sort(tilesList);
		StringBuilder str = new StringBuilder("[");
		for (int i = 0; i < tilesList.size(); i++) {
			str.append(toString(tilesList.get(i)));
			if (i < tilesList.size() - 1)
				str.append(',');
		}
		str.append(']');
		return str.toString();
	}

	/**
	 * 显示临时状态信息。调用此方法后必须用{@link #clearTempStatus(String)}方法恢复之前的状态。
	 * 
	 * @param key
	 *            信息标识
	 * @param status
	 *            状态信息
	 * 
	 * @throws IllegalStateException
	 *             当前已是临时状态
	 */
	void tempStatus(String key, String status) {
		synchronized (tempStatuses) {
			tempStatusKeys.addFirst(key);
			tempStatuses.put(key, status);
			cliView.updateStatus(status);
		}
	}

	/**
	 * 取消临时状态信息。
	 * 
	 * @param key
	 *            信息标识
	 * @throws IllegalStateException
	 *             当前不是临时状态
	 */
	void clearTempStatus(String key) {
		synchronized (tempStatuses) {
			boolean exist = tempStatusKeys.remove(key);
			if (exist) {
				tempStatuses.remove(key);
				if (!tempStatuses.isEmpty())
					cliView.updateStatus(tempStatuses.get(tempStatusKeys
							.getFirst()));
				else
					refreshTileStatus();
			}
		}
	}
}