package blove.mj.board;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import blove.mj.Tile;

/**
 * 牌墙。
 * 
 * @author 陈通
 */
public class Wall {
	/**
	 * 所有牌。
	 */
	protected List<Tile> tiles;
	/**
	 * 牌墙中剩余的第一张牌是初始牌墙中的第几张。从0开始。
	 */
	protected int startIndex;

	/**
	 * 生成指定实例的只读拷贝。
	 * 
	 * @param tiles
	 *            实例
	 * @return 实例
	 */
	public static Wall copyOf(Wall wall) {
		Wall copy = new Wall();
		copy.tiles = Collections.unmodifiableList(new LinkedList<>(wall.tiles));
		copy.startIndex = wall.startIndex;
		return copy;
	}

	/**
	 * 返回所有牌。
	 * 
	 * @return 牌列表
	 */
	public List<Tile> getTiles() {
		return Collections.unmodifiableList(tiles);
	}

	/**
	 * 返回牌数量。
	 * 
	 * @return 数
	 */
	public int getTileCount() {
		return tiles.size();
	}

	/**
	 * 牌墙中剩余的第一张牌是初始牌墙中的第几张。从0开始。
	 * 
	 * @return 第几张
	 */
	public int getStartIndex() {
		return startIndex;
	}

}
