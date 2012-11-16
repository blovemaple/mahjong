package blove.mj.local;

import java.util.HashSet;

import blove.mj.Tile;
import blove.mj.board.DiscardedTiles;

/**
 * 本地游戏桌上的Discard Tiles。
 * 
 * @author blovemaple
 */
class LocalDiscardTiles extends DiscardedTiles {
	private static final long serialVersionUID = -5177012996591944475L;

	/**
	 * 新建一个实例。
	 */
	public LocalDiscardTiles() {
		tiles = new HashSet<>();
	}

	/**
	 * 初始化（清空）。
	 */
	public void init() {
		tiles.clear();
	}

	/**
	 * 将一张牌放进来。
	 * 
	 * @param tile
	 *            牌
	 */
	public void putIn(Tile tile) {
		boolean added = tiles.add(tile);
		if (!added)
			throw new IllegalStateException("已经存在：" + tile);
	}
}
