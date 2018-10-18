package com.github.blovemaple.mj.object;

import java.util.List;

/**
 * {@link PlayerInfo}给其他玩家的视图接口。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface PlayerInfoPlayerView {
	/**
	 * 返回玩家名称。
	 */
	public String getPlayerName();

	/**
	 * 返回手中的牌数。
	 */
	public int getAliveTileSize();

	/**
	 * 返回已经打出的牌。
	 */
	public List<Tile> getDiscardedTiles();

	/**
	 * 返回牌组视图列表。
	 */
	public List<TileGroupPlayerView> getTileGroups();

	/**
	 * 返回是否听和。
	 */
	public boolean isTing();
}
