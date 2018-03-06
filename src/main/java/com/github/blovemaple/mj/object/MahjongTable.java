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
	 * 牌墙。
	 */
	private TileWall wall;
	/**
	 * 所有玩家信息。
	 */
	private Map<PlayerLocation, PlayerInfo> playerInfos;

	public void init() {
		wall = new TileWall();
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
		List<Tile> tileList = new ArrayList<>(allTiles);
		Collections.shuffle(tileList);
		wall.init(tileList);
	}

	/**
	 * 返回牌墙中的剩余牌数。
	 */
	public int getTileWallSize() {
		return wall.getRemainTileCount();
	}

	/**
	 * 从牌墙的头部摸指定数量的牌并返回。
	 */
	public List<Tile> draw(int count) {
		return wall.draw(count);
	}

	/**
	 * 从牌墙的底部摸指定数量的牌并返回。
	 */
	public List<Tile> drawBottom(int count) {
		return wall.drawBottom(count);
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
		public Map<PlayerLocation, PlayerInfoPlayerView> getPlayerInfoView() {
			return playerInfos.entrySet().stream()
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().getOtherPlayerView()));
		}

	}

}
