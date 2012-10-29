package blove.mj.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import blove.mj.Cpk;
import blove.mj.GameResult;
import blove.mj.GameResult.WinInfo;
import blove.mj.Player;
import blove.mj.PlayerLocation;
import blove.mj.PlayerLocation.Relation;
import blove.mj.PointItem;
import blove.mj.PointsResult;
import blove.mj.Tile;
import blove.mj.TileType;
import blove.mj.TileType.Suit;
import blove.mj.board.PlayerTiles;
import blove.mj.cli.CliView.CharHandler;

/**
 * 提供向CliView提供展示信息的便捷方法。
 * 
 * @author blovemaple
 */
class CliMessager {
	private final CliView cliView;

	private String crtStatus;

	private static final String DEVIDING_LINE = "**************************************************";

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
	 * @see blove.mj.cli.CliView#init()
	 */
	void initView() {
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
	 * @param player
	 *            玩家。如果为null表示与具体玩家无关。
	 * @param message
	 *            信息
	 * @param myLocation
	 *            当前玩家位置
	 */
	void showMessage(String action, PlayerLocation location, Player player,
			String message, PlayerLocation myLocation) {
		// [ACTION] [LOCATION]player message
		StringBuilder messageViewStr = new StringBuilder();

		messageViewStr.append(String.format("%-10s", "[" + action.toUpperCase()
				+ "]"));

		if (location != null) {
			messageViewStr.append(toString(location, myLocation));
			messageViewStr.append(player);
			messageViewStr.append(' ');
		}

		if (message != null) {
			messageViewStr.append(' ');
			messageViewStr.append(message);
		}

		cliView.showMessage(messageViewStr.toString());
	}

	/**
	 * 显示一局结果。
	 * 
	 * @param result
	 *            结果
	 * @param myLocation
	 *            当前玩家位置
	 */
	void showResult(GameResult result, PlayerLocation myLocation) {
		cliView.showMessage(DEVIDING_LINE);
		cliView.showMessage(toString(result, myLocation));
		cliView.showMessage(DEVIDING_LINE);
	}

	private PlayerTiles tiles;
	private Set<Tile> focusTiles;
	private String extraOptions;
	private String status;
	private long time = -1;

	/**
	 * 在状态栏显示与本玩家的牌相关的信息。
	 * 
	 * @param tiles
	 *            玩家的牌
	 * @param focusTile
	 *            聚焦的牌。吃/碰/杠中的牌，只有一组全部聚焦才能聚焦。
	 * @param extraOptions
	 *            额外选项。如果为null表示没有额外选项。
	 * @param status
	 *            状态信息。如果为null表示没有状态信息。
	 */
	void tilesStatus(PlayerTiles tiles, Tile focusTile, String extraOptions,
			String status) {
		tilesStatus(tiles, focusTile != null ? Collections.singleton(focusTile)
				: null, extraOptions, status);
	}

	/**
	 * 在状态栏显示与本玩家的牌相关的信息。
	 * 
	 * @param tiles
	 *            玩家的牌
	 * @param focusTiles
	 *            聚焦的多张牌。此集合内不包含于玩家手中的牌将被忽略。吃/碰/杠中的牌，只有一组全部聚焦才能聚焦。
	 * @param extraOptions
	 *            额外选项。如果为null表示没有额外选项。
	 * @param status
	 *            状态信息。如果为null表示没有状态信息。
	 */
	void tilesStatus(PlayerTiles tiles, Set<Tile> focusTiles,
			String extraOptions, String status) {
		this.tiles = tiles;
		this.focusTiles = focusTiles != null ? focusTiles : Collections
				.<Tile> emptySet();
		this.extraOptions = extraOptions;
		this.status = status;
		refreshTileStatus();
	}

	/**
	 * 在状态栏显示时间信息。
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
		this.time = -1;
		refreshTileStatus();
	}

	private void refreshTileStatus() {
		// playerTiles <extra options> [STATUS] time
		StringBuilder status = new StringBuilder();
		if (tiles != null) {
			status.append(toString(tiles, focusTiles));
			if (extraOptions != null) {
				status.append(" <");
				status.append(extraOptions);
				status.append('>');
			}
			if (this.status != null) {
				status.append(" [");
				status.append(this.status.toUpperCase());
				status.append(']');
			}
			if (time >= 0) {
				status.append(' ');
				status.append(time);
			}
		}

		cliView.updateStatus(crtStatus = status.toString());
	}

	private static final char SINGLE_FOCUS_HEAD = '→';
	private static final char SINGLE_FUCUS_TAIL = '←';
	private static final char MULTI_FOCUS_HEAD = '[';
	private static final char MULTI_FOCUS_TAIL = ']';

	private static String toString(PlayerTiles playerTiles, Set<Tile> focusTiles) {
		// playerTiles(with →fucus← or [focuses])
		// eg: < W3W4W5 > < T7T8T9 > W1 W8 [T4 T5] O3 O5 E N
		// eg: < W3W4W5 > <[T7T8T9]> W1 W8 T4 T5 O3 O5 E N
		// eg: < W3W4W5 > < T7T8T9 > W1→W8← T4 T5 O3 O5 E N
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
		for (Tile tile : new TreeSet<>(playerTiles.getAliveTiles())) {
			Suit suit = tile.getType().getSuit();
			if (lastSuit != suit) {
				str.append(' ');
				lastSuit = suit;
			}

			boolean focus = focusTiles.contains(tile);
			if (!lastFocus && focus)
				str.append(focusHead);
			else if (lastFocus && !focus)
				str.append(focusTail);
			else
				str.append(' ');
			str.append(toString(tile));
			lastFocus = focus;
		}
		str.append(lastFocus ? focusTail : ' ');

		return str.toString();
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

	private static String toString(GameResult result, PlayerLocation myLocation) {
		StringBuilder str = new StringBuilder();

		WinInfo winInfo = result.getWinInfo();
		for (Map.Entry<PlayerLocation, Player> locationPlayer : result
				.getPlayers().entrySet()) {
			PlayerLocation location = locationPlayer.getKey();
			Player player = locationPlayer.getValue();
			PlayerTiles playerTiles = result.getTiles().get(location);
			boolean isWinner = winInfo.getWinnerLocation() == location;
			boolean isPaoer = winInfo.getPaoerLocation() == location;
			boolean isDealer = result.getDealerLocation() == location;

			// [LOCATION][DEALER][WIN][PAO]player
			str.append('[');
			str.append(toString(location, myLocation));
			str.append(']');
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
			str.append(toString(playerTiles,
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
			str.append(String.format("%-20s%d", toString(location, myLocation)
					+ result.getPlayers().get(location)));
			str.append(points.getPoints(location));
			str.append(System.lineSeparator());
		}

		return str.toString();
	}

	private static String toString(PlayerLocation location,
			PlayerLocation myLocation) {
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
			return tileType.getSuit().name();
		else
			return tileType.getSuit().name() + tileType.getRank();
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
	 * 在整个状态栏直接显示指定信息。
	 * 
	 * @param message
	 *            信息
	 */
	void directStatus(String message) {
		cliView.updateStatus(crtStatus = "[" + message + "]");
	}

	/**
	 * 显示临时状态信息。调用此方法后可用{@link #tempStatus(String)}方法恢复之前的状态。
	 * 
	 * @param status
	 *            状态信息
	 */
	void tempStatus(String status) {
		cliView.updateStatus(status);
	}

	/**
	 * 取消临时状态信息。
	 */
	void clearTempStatus() {
		cliView.updateStatus(crtStatus != null ? crtStatus : "");
	}
}