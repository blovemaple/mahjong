package com.github.blovemaple.mj.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 麻将桌。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class MahjongTable {
	/**
	 * 牌墙。从0处摸牌。一局开始时如果产生底牌，应该把底牌从开头挪到尾部。
	 */
	private List<Tile> tileWall;
	/**
	 * 一局开始时的底牌数量。
	 */
	private int initBottomSize;
	/**
	 * 已经从底部摸牌的数量。
	 */
	private int drawedBottomSize;
	/**
	 * 所有玩家信息。
	 */
	private Map<PlayerLocation, PlayerInfo> playerInfos;

	public void init() {
		tileWall = new ArrayList<Tile>();
		playerInfos = new EnumMap<>(PlayerLocation.class);
		for (PlayerLocation location : PlayerLocation.values()) {
			playerInfos.put(location, new PlayerInfo());
		}
	}

	/**
	 * 初始化，准备开始一局。即清空玩家的牌、洗牌、摆牌墙。
	 */
	public void readyForGame(Collection<Tile> allTiles) {
		playerInfos.values().forEach(PlayerInfo::clear);
		tileWall.clear();
		tileWall.addAll(allTiles);
		Collections.shuffle(tileWall);
		initBottomSize = 0;
		drawedBottomSize = 0;
	}

	/**
	 * 返回牌墙中的剩余牌数。
	 */
	public int getTileWallSize() {
		return tileWall.size();
	}

	public int getInitBottomSize() {
		return initBottomSize;
	}

	public void setInitBottomSize(int initBottomSize) {
		this.initBottomSize = initBottomSize;
	}

	public int getDrawedBottomSize() {
		return drawedBottomSize;
	}

	public void setDrawedBottomSize(int drawedBottomSize) {
		this.drawedBottomSize = drawedBottomSize;
	}

	/**
	 * 从牌墙的头部摸指定数量的牌并返回。
	 */
	public List<Tile> draw(int count) {
		if (count <= 0 || count > tileWall.size())
			return Collections.emptyList();
		List<Tile> toBeDrawed = tileWall.subList(0, count);
		List<Tile> drawed = new ArrayList<>(toBeDrawed);
		toBeDrawed.clear();
		return drawed;
	}

	/**
	 * 从牌墙的底部摸指定数量的牌并返回。
	 */
	public List<Tile> drawBottom(int count) {
		if (count <= 0 || count > tileWall.size())
			return Collections.emptyList();
		List<Tile> toBeDrawed = tileWall.subList(tileWall.size() - count,
				tileWall.size());
		List<Tile> drawed = new ArrayList<>(toBeDrawed);
		toBeDrawed.clear();
		drawedBottomSize += drawed.size();
		return drawed;
	}

	public Map<PlayerLocation, PlayerInfo> getPlayerInfos() {
		return playerInfos;
	}

	protected void setPlayerInfos(Map<PlayerLocation, PlayerInfo> playerInfos) {
		this.playerInfos = playerInfos;
	}

	public Player getPlayerByLocation(PlayerLocation location) {
		PlayerInfo info = playerInfos.get(location);
		return info == null ? null : info.getPlayer();
	}

	public void setPlayer(PlayerLocation location, Player player) {
		PlayerInfo playerInfo = playerInfos.get(location);
		if (playerInfo == null) {
			playerInfo = new PlayerInfo();
			playerInfos.put(location, playerInfo);
		}
		playerInfo.setPlayer(player);
	}

	private final Map<PlayerLocation, PlayerView> playerViews = new HashMap<>();

	/**
	 * 获取指定位置的玩家视图。
	 */
	public MahjongTablePlayerView getPlayerView(PlayerLocation location) {
		PlayerView view = playerViews.get(location);
		if (view == null) { // 不需要加锁，因为多创建了也没事
			view = new PlayerView(location);
			playerViews.put(location, view);
		}
		return view;
	}

	private class PlayerView implements MahjongTablePlayerView {

		private final PlayerLocation myLocation;

		private PlayerView(PlayerLocation myLocation) {
			this.myLocation = myLocation;
		}

		@Override
		public PlayerLocation getMyLocation() {
			return myLocation;
		}

		@Override
		public String getPlayerName(PlayerLocation location) {
			Player player = getPlayerByLocation(location);
			return player != null ? player.getName() : null;
		}

		@Override
		public int getTileWallSize() {
			return MahjongTable.this.getTileWallSize();
		}

		@Override
		public int getInitBottomSize() {
			return MahjongTable.this.getInitBottomSize();
		}

		@Override
		public int getDrawedBottomSize() {
			return MahjongTable.this.getDrawedBottomSize();
		}

		@Override
		public Map<PlayerLocation, PlayerInfoPlayerView> getPlayerInfoView() {
			return playerInfos.entrySet().stream()
					.collect(Collectors.toMap(entry -> entry.getKey()
							, entry -> entry.getValue().getOtherPlayerView()));
		}

	}

}
