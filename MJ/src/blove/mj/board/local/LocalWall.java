package blove.mj.board.local;

import java.util.Collections;
import java.util.LinkedList;

import blove.mj.Tile;
import blove.mj.board.Wall;

/**
 * 本地游戏桌上的牌墙。
 * 
 * @author 陈通
 */
class LocalWall extends Wall {
	/**
	 * 新建一个实例。
	 */
	public LocalWall() {
		tiles = new LinkedList<>();
		init();
	}

	/**
	 * 洗牌并初始化为开始游戏前的状态。
	 */
	public synchronized void init() {
		tiles.clear();
		tiles.addAll(Tile.getAllTiles());
		Collections.shuffle(tiles);
		startIndex = 0;
	}

	/**
	 * 摸一张牌。
	 * 
	 * @return 牌
	 * @throws DrawGameException
	 *             牌墙中没有牌
	 */
	public synchronized Tile draw() throws DrawGameException {
		if (getTileCount() < 1)
			throw new DrawGameException();
		startIndex++;
		return tiles.remove(0);
	}

	/**
	 * 从海底摸一张牌。
	 * 
	 * @return 牌
	 * @throws DrawGameException
	 *             牌墙中没有牌
	 */
	public synchronized Tile drawFromSeabed() throws DrawGameException {
		if (getTileCount() < 1)
			throw new DrawGameException();
		return tiles.remove(tiles.size() - 1);
	}
}
