package blove.mj;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import blove.mj.TileType.Suit;

/**
 * 一张牌。
 * 
 * @author blovemaple
 */
public class Tile implements Comparable<Tile> {
	private static Set<Tile> tiles;
	private static Map<TileType, Set<Tile>> tilesForType;
	static {
		// 初始化所有牌
		tiles = new HashSet<>();
		tilesForType = new HashMap<>();
		for (Suit suit : Suit.values()) {
			if (suit.isHonor()) {
				TileType type = TileType.get(suit);
				Set<Tile> typeTiles = Collections
						.unmodifiableSet(geneTilesForType(type));
				tilesForType.put(type, typeTiles);
				tiles.addAll(typeTiles);
			} else {
				for (int rank = 1; rank <= 9; rank++) {
					TileType type = TileType.get(suit, rank);
					Set<Tile> typeTiles = Collections
							.unmodifiableSet(geneTilesForType(type));
					tilesForType.put(type, typeTiles);
					tiles.addAll(typeTiles);
				}
			}
		}
		tiles = Collections.unmodifiableSet(tiles);
		tilesForType = Collections.unmodifiableMap(tilesForType);
	}

	private static Set<Tile> geneTilesForType(TileType type) {
		Set<Tile> tiles = new HashSet<>();
		for (int id = 0; id < 4; id++)
			tiles.add(new Tile(type, id));
		return tiles;
	}

	/**
	 * 返回所有牌。
	 * 
	 * @return 所有牌的集合
	 */
	public static Set<Tile> getAllTiles() {
		return tiles;
	}

	/**
	 * 返回指定类型的所有牌。
	 * 
	 * @param type
	 *            类型
	 * @return 牌
	 */
	public static Set<Tile> getTilesForType(TileType type) {
		return tilesForType.get(type);
	}

	/**
	 * 返回一张指定类型的牌。
	 * 
	 * @param type
	 *            类型
	 * @param avoidTiles
	 *            除了哪些牌
	 * @return 牌。如果没有，则返回null。
	 */
	public static Tile getATileForType(TileType type, Set<Tile> avoidTiles) {
		Set<Tile> tilesForType = new HashSet<>(getTilesForType(type));
		if (avoidTiles != null)
			tilesForType.removeAll(avoidTiles);
		if (tilesForType.isEmpty())
			return null;
		else
			return tilesForType.iterator().next();
	}

	private final TileType type;
	private final int id;// 每个牌型从0到3。

	private Tile(TileType type, int id) {
		this.type = type;
		this.id = id;
	}

	/**
	 * 返回牌型。
	 * 
	 * @return 牌型
	 */
	public TileType getType() {
		return type;
	}

	@Override
	public int compareTo(Tile o) {
		if (this == o)
			return 0;
		int typeCompare = this.type.compareTo(o.type);
		if (typeCompare != 0)
			return typeCompare;
		else if (this.id < o.id)
			return -1;
		else if (this.id > o.id)
			return 1;
		else
			return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Tile))
			return false;
		Tile other = (Tile) obj;
		if (id != other.id)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	/**
	 * Just for debug.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + type + ", " + id + "]";
	}

}
