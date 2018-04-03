package com.github.blovemaple.mj.object;

import java.util.Map;

/**
 * {@link MahjongTable}给玩家的视图接口。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface MahjongTablePlayerView {

	/**
	 * 返回指定位置的玩家名称。
	 */
	public String getPlayerName(PlayerLocation location);

	/**
	 * 返回牌墙。
	 */
	public TileWallPlayerView getTileWall();

	/**
	 * 返回PlayerInfo。
	 */
	public Map<PlayerLocation, ? extends PlayerInfoPlayerView> getPlayerInfos();

	/**
	 * 返回PlayerInfo。
	 */
	public PlayerInfoPlayerView getPlayerInfo(PlayerLocation location);

}
