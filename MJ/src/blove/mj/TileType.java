package blove.mj;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 牌型。每个牌型的牌通常有四张。
 * 
 * @author blovemaple
 */
public class TileType implements Comparable<TileType> {
	/**
	 * “花色”。
	 * 
	 * @author blovemaple
	 */
	public enum Suit {
		// 以下排列顺序决定了牌型的比较顺序！
		// 非字牌
		CHARACTER("萬", false), DOT("饼", false), BAMBOO("条", false),
		// 风牌
		EAST("東", true), SOUTH("南", true), WEST("西", true), NORTH("北", true),
		// 箭牌
		RED("中", true), GREEN("發", true), WHITE("白", true);

		private final String name;
		private final boolean isHonor;

		private Suit(String name, boolean isHonor) {
			this.isHonor = isHonor;
			this.name = name;
		}

		/**
		 * 返回可读名称。
		 * 
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return name;
		}

		/**
		 * 返回是否是字牌。
		 * 
		 * @return 如果是字牌，返回true；否则返回false。
		 */
		public boolean isHonor() {
			return isHonor;
		}

	}

	/**
	 * 字牌的占位大小值。
	 */
	public static final int HONOR_RANK = 0;

	private static Map<Suit, Map<Integer, TileType>> types;
	static {
		// 初始化所有牌型
		types = new EnumMap<>(Suit.class);
		for (Suit suit : Suit.values()) {
			Map<Integer, TileType> suitTypes = new HashMap<>();
			if (suit.isHonor())
				suitTypes.put(HONOR_RANK, new TileType(suit, HONOR_RANK));
			else
				for (int rank = 1; rank <= 9; rank++)
					suitTypes.put(rank, new TileType(suit, rank));
			suitTypes = Collections.unmodifiableMap(suitTypes);
			types.put(suit, suitTypes);
		}
		types = Collections.unmodifiableMap(types);
	}

	/**
	 * 获取指定字牌牌型。
	 * 
	 * @param suit
	 *            字牌花色
	 * @return 牌型
	 * @throws IllegalArgumentException
	 *             花色不是字牌花色。
	 */
	public static TileType get(Suit suit) {
		if (!suit.isHonor())
			throw new IllegalArgumentException("获取非字牌类型未指定大小" + suit);
		return get(suit, HONOR_RANK);
	}

	/**
	 * 获取指定牌型。
	 * 
	 * @param suit
	 *            花色
	 * @param rank
	 *            大小
	 * @return 牌型
	 * @throws IllegalArgumentException
	 *             花色为非字牌且大小超出范围；或者花色为字牌，且大小不为0。
	 */
	public static TileType get(Suit suit, int rank) {
		TileType type = types.get(suit).get(rank);
		if (type == null)
			if (suit.isHonor())
				throw new IllegalArgumentException("字牌" + suit + "大小只能为0，不能是"
						+ rank);
			else
				throw new IllegalArgumentException("非字牌" + suit
						+ "大小范围是1-9，不能是" + rank);
		return type;
	}

	private final Suit suit;
	private final int rank;// 若是字牌则为0

	/**
	 * 新建一个实例。
	 * 
	 * @param suit
	 *            花色
	 * @param rank
	 *            大小
	 */
	private TileType(Suit suit, int rank) {
		this.suit = suit;
		this.rank = rank;
	}

	/**
	 * 返回花色。
	 * 
	 * @return 花色
	 */
	public Suit getSuit() {
		return suit;
	}

	/**
	 * 返回大小。
	 * 
	 * @return 大小
	 */
	public int getRank() {
		return rank;
	}

	/**
	 * 从指定牌集合中查找所有类型为此类型的牌，并返回。
	 * 
	 * @param tiles
	 *            牌集合
	 * @return 此类型的牌集合
	 */
	public Set<Tile> findTiles(Set<Tile> tiles) {
		Set<Tile> typeTiles = new HashSet<>();
		for (Tile tile : tiles)
			if (this.equals(tile.getType()))
				typeTiles.add(tile);
		return typeTiles;
	}

	@Override
	public int compareTo(TileType o) {
		if (this == o)
			return 0;
		if (this.suit.ordinal() < o.suit.ordinal())
			return -1;
		else if (this.suit.ordinal() > o.suit.ordinal())
			return 1;
		else if (this.rank < o.rank)
			return -1;
		else if (this.rank > o.rank)
			return 1;
		else
			return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + rank;
		result = prime * result + ((suit == null) ? 0 : suit.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TileType))
			return false;
		TileType other = (TileType) obj;
		if (rank != other.rank)
			return false;
		if (suit != other.suit)
			return false;
		return true;
	}

}
