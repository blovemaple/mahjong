package blove.mj.local;

import blove.mj.PlayerLocation;
import blove.mj.Tile;

/**
 * 带有一个玩家位置和一张牌的消息对象。
 * 
 * @author blovemaple
 */
class LocationAndTile {
	/**
	 * 玩家位置
	 */
	final PlayerLocation location;
	/**
	 * 牌
	 */
	final Tile tile;

	/**
	 * 新建一个实例。
	 * 
	 * @param location
	 *            玩家位置
	 * @param tile
	 *            牌
	 */
	LocationAndTile(PlayerLocation location, Tile tile) {
		this.location = location;
		this.tile = tile;
	}

}
