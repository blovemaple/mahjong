package blove.mj.board;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import blove.mj.Tile;
import blove.mj.TileType;

/**
 * 已打出放在桌上的牌。
 * 
 * @author blovemaple
 */
public class DiscardedTiles implements Serializable {
	private static final long serialVersionUID = -9017431981336491117L;

	/**
	 * 所有牌。
	 */
	protected Set<Tile> tiles;

	/**
	 * 生成指定实例的只读拷贝。
	 * 
	 * @param tiles
	 *            实例
	 * @return 实例
	 */
	public static DiscardedTiles copyOf(DiscardedTiles tiles) {
		DiscardedTiles copy = new DiscardedTiles();
		copy.tiles = Collections.unmodifiableSet(new HashSet<>(tiles.tiles));
		return copy;
	}

	/**
	 * 返回牌的数目。
	 * 
	 * @return 数
	 */
	public int getTileCount() {
		return tiles.size();
	}

	/**
	 * 返回所有牌。
	 * 
	 * @return 所有牌的集合
	 */
	public Set<Tile> getTiles() {
		return Collections.unmodifiableSet(tiles);
	}

	/**
	 * 返回指定牌型的所有牌。
	 * 
	 * @param type
	 *            牌型
	 * @return 牌集合
	 */
	public Set<Tile> getTiles(TileType type) {
		Set<Tile> typeTiles = new HashSet<>();
		for (Tile tile : tiles)
			if (type.equals(tile.getType()))
				typeTiles.add(tile);
		return typeTiles;
	}

}
