package blove.mj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import blove.mj.PlayerLocation.Relation;

/**
 * 吃、碰、杠。<br/>
 * 此类实现Comparable接口，其比较方法按照非得牌的先后顺序比较大小。
 * 
 * @author blovemaple
 */
public class Cpk implements Comparable<Cpk> {
	private final CpkType type;
	private final Set<Tile> tiles;
	private final Relation fromRelation;
	private final Tile forTile;

	/**
	 * 新建一个实例。
	 * 
	 * @param type
	 *            类型
	 * @param forTile
	 *            得牌
	 * @param fromRelation
	 *            得牌来自于哪个关系的玩家
	 * @param tiles
	 *            牌组
	 * @throws IllegalArgumentException
	 *             不合法
	 */
	public Cpk(CpkType type, Tile forTile, Relation fromRelation, Tile... tiles) {
		if (!type.isValid(new HashSet<Tile>(Arrays.asList(tiles))))
			throw new IllegalArgumentException("牌组不合法：[" + type + "]"
					+ Arrays.asList(tiles));

		this.type = type;
		this.forTile = forTile;
		this.fromRelation = fromRelation;
		this.tiles = Collections.unmodifiableSet(new HashSet<>(Arrays
				.asList(tiles)));
	}

	/**
	 * 返回类型。
	 * 
	 * @return 类型
	 */
	public CpkType getType() {
		return type;
	}

	/**
	 * 返回牌组中所有牌。
	 * 
	 * @return tiles 集合
	 */
	public Set<Tile> getTiles() {
		return Collections.unmodifiableSet(tiles);
	}

	/**
	 * 返回得牌来自于哪个关系的玩家。
	 * 
	 * @return 玩家位置
	 */
	public Relation getFromRelation() {
		return fromRelation;
	}

	/**
	 * 返回得牌。
	 * 
	 * @return 得牌
	 */
	public Tile getForTile() {
		return forTile;
	}

	@Override
	public int compareTo(Cpk o) {
		int compare;

		TreeSet<Tile> orderSetThis = new TreeSet<>();
		orderSetThis.addAll(tiles);
		orderSetThis.remove(forTile);

		TreeSet<Tile> orderSetO = new TreeSet<>();
		orderSetO.addAll(o.tiles);
		orderSetO.remove(o.forTile);

		Tile smallTileOfThis = orderSetThis.first();
		Tile smallTileOfO = orderSetO.first();

		do {
			compare = smallTileOfThis.compareTo(smallTileOfO);
			if (compare != 0)
				return compare;

			smallTileOfThis = orderSetThis.higher(smallTileOfThis);
			smallTileOfO = orderSetO.higher(smallTileOfO);
		} while (smallTileOfThis != null && smallTileOfO != null);
		if (smallTileOfThis == null)
			return 1;
		else if (smallTileOfO == null)
			return -1;
		else
			return 0;
	}

	@Override
	public String toString() {
		return "从" + fromRelation + type + "牌" + forTile + "，组成" + tiles;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((forTile == null) ? 0 : forTile.hashCode());
		result = prime * result
				+ ((fromRelation == null) ? 0 : fromRelation.hashCode());
		result = prime * result + ((tiles == null) ? 0 : tiles.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Cpk))
			return false;
		Cpk other = (Cpk) obj;
		if (forTile == null) {
			if (other.forTile != null)
				return false;
		} else if (!forTile.equals(other.forTile))
			return false;
		if (fromRelation == null) {
			if (other.fromRelation != null)
				return false;
		} else if (!fromRelation.equals(other.fromRelation))
			return false;
		if (tiles == null) {
			if (other.tiles != null)
				return false;
		} else if (!tiles.equals(other.tiles))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	/**
	 * 按照牌的类型比较大小的比较器。牌的类型如果相同则认为相等。
	 */
	public static final Comparator<Cpk> tileTypeComparator = new Comparator<Cpk>() {

		@Override
		public int compare(Cpk o1, Cpk o2) {
			int compare;

			TreeSet<Tile> orderSet1 = new TreeSet<>();
			orderSet1.addAll(o1.getTiles());

			TreeSet<Tile> orderSet2 = new TreeSet<>();
			orderSet2.addAll(o2.getTiles());

			Tile smallTileOfO1 = orderSet1.first();
			Tile smallTileOfO2 = orderSet2.first();

			do {
				compare = smallTileOfO1.getType().compareTo(
						smallTileOfO2.getType());
				if (compare != 0)
					return compare;

				smallTileOfO1 = orderSet1.higher(smallTileOfO1);
				smallTileOfO2 = orderSet2.higher(smallTileOfO2);
			} while (smallTileOfO1 != null && smallTileOfO2 != null);
			if (smallTileOfO1 == null)
				return 1;
			else if (smallTileOfO2 == null)
				return -1;
			else
				return 0;
		}
	};

	public boolean tileTypeEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Cpk))
			return false;
		Cpk other = (Cpk) obj;
		if (type != other.type)
			return false;
		if (forTile == null) {
			if (other.forTile != null)
				return false;
		} else if (!forTile.equals(other.forTile))
			return false;
		if (fromRelation == null) {
			if (other.fromRelation != null)
				return false;
		} else if (!fromRelation.equals(other.fromRelation))
			return false;
		if (tiles == null) {
			if (other.tiles != null)
				return false;
		} else {
			List<TileType> tileTypeList1 = new ArrayList<>(tiles.size());
			for (Tile tile : tiles)
				tileTypeList1.add(tile.getType());
			Collections.sort(tileTypeList1);

			List<TileType> tileTypeList2 = new ArrayList<>(tiles.size());
			for (Tile tile : other.tiles)
				tileTypeList2.add(tile.getType());
			Collections.sort(tileTypeList2);

			if (!tileTypeList1.equals(tileTypeList2))
				return false;
		}

		return true;
	}

	/**
	 * 按照优先级比较吃/碰/杠大小的比较器。
	 */
	public static final Comparator<Cpk> priorityComparator = new Comparator<Cpk>() {

		@Override
		public int compare(Cpk o1, Cpk o2) {
			int typeCompare = o1.getType().getPriority() > o2.getType()
					.getPriority() ? 1 : o1.getType().getPriority() == o2
					.getType().getPriority() ? 0 : -1;
			if (typeCompare != 0)
				return typeCompare;
			int relationCompare = o2.getFromRelation().compareTo(
					o1.getFromRelation());
			if (relationCompare != 0)
				return relationCompare;
			return o1.compareTo(o2);
		}

	};

}
