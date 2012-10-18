package blove.mj.board;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import blove.mj.Cpk;
import blove.mj.Tile;

/**
 * 一个玩家的牌。
 * 
 * @author blovemaple
 */
public class PlayerTiles {
	protected Set<Tile> aliveTiles;
	protected List<Cpk> cpks;
	protected Tile readyHandDiscardTile;// 玩家叫听才算听和

	/**
	 * 生成指定实例的只读拷贝。
	 * 
	 * @param tiles
	 *            实例
	 * @return 实例
	 */
	public static PlayerTiles copyOf(PlayerTiles tiles) {
		PlayerTiles copy = new PlayerTiles();
		copy.aliveTiles = Collections.unmodifiableSet(new HashSet<>(
				tiles.aliveTiles));
		copy.cpks = Collections.unmodifiableList(new LinkedList<>(tiles.cpks));
		copy.readyHandDiscardTile = tiles.readyHandDiscardTile;
		return copy;
	}

	/**
	 * 返回除了吃/碰/杠外所有的牌。
	 * 
	 * @return 牌集合
	 */
	public Set<Tile> getAliveTiles() {
		return Collections.unmodifiableSet(aliveTiles);
	}

	/**
	 * 返回所有的吃/碰/杠。
	 * 
	 * @return 吃/碰/杠的列表。顺序为进行吃/碰/杠动作的顺序。
	 */
	public List<Cpk> getCpks() {
		return Collections.unmodifiableList(cpks);
	}

	/**
	 * 返回此时的牌数是否可以出牌。
	 * 
	 * @return 如果可以出牌，返回true；否则返回false。
	 */
	public boolean isForDiscarding() {
		return isForDiscarding(aliveTiles);
	}

	/**
	 * 返回指定aliveTiles的牌数是否可以出牌。
	 * 
	 * @return 如果可以出牌，返回true；否则返回false。
	 */
	public static boolean isForDiscarding(Set<Tile> aliveTiles) {
		return (aliveTiles.size() - 2) % 3 == 0;
	}

	/**
	 * 返回听牌时打出的牌。
	 * 
	 * @return 牌。如果此玩家没有叫听，则返回null。
	 */
	public Tile getReadyHandDiscardTile() {
		return readyHandDiscardTile;
	}

	/**
	 * 返回当前是否听牌。
	 * 
	 * @return 是否听牌
	 */
	public boolean isReadyHand() {
		return readyHandDiscardTile != null;
	}

}
