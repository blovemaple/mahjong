package com.github.blovemaple.mj.rule;

import java.util.Set;
import java.util.stream.Stream;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileUnit;

/**
 * 和牌类型。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface WinType {
	/**
	 * 判断指定条件下是否可和牌。如果aliveTiles非null，则用于替换playerInfo中的信息做出判断，
	 * 否则利用playerInfo中的aliveTiles做出判断。
	 * 
	 * @param playerInfo
	 *            玩家信息
	 * @param aliveTiles
	 *            玩家手中的牌
	 * @return 是否可以和牌
	 */
	public default boolean match(PlayerInfo playerInfo, Set<Tile> aliveTiles) {
		return parseWinTileUnits(playerInfo,
				aliveTiles != null ? aliveTiles : playerInfo.getAliveTiles())
						.findAny().isPresent();
	}

	/**
	 * 全部解析成可以和牌的完整的TileUnit集合的流，失败返回空集合。
	 * 
	 * @param aliveTiles
	 * @param anyOne
	 *            true表示最多只解析一种可能的集合，用于快速判断是否可解析
	 * @return 完整的TileUnit集合的流
	 */
	public Stream<Set<TileUnit>> parseWinTileUnits(PlayerInfo playerInfo,
			Set<Tile> aliveTiles);

	/**
	 * 获取ChangingForWin的流，移除changeCount个牌，增加(changeCount+1)个牌。
	 */
	public Stream<ChangingForWin> changingsForWin(PlayerInfo playerInfo,
			int changeCount);

	/**
	 * 一种结果时和牌的换牌方法，移除removedTiles并增加addedTiles。
	 * 
	 * @author blovemaple <blovemaple2010(at)gmail.com>
	 */
	public static class ChangingForWin {
		public Set<Tile> addedTiles, removedTiles;

		public ChangingForWin(Set<Tile> addedTiles, Set<Tile> removedTiles) {
			this.addedTiles = addedTiles;
			this.removedTiles = removedTiles;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((addedTiles == null) ? 0 : addedTiles.hashCode());
			result = prime * result
					+ ((removedTiles == null) ? 0 : removedTiles.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof ChangingForWin))
				return false;
			ChangingForWin other = (ChangingForWin) obj;
			if (addedTiles == null) {
				if (other.addedTiles != null)
					return false;
			} else if (!addedTiles.equals(other.addedTiles))
				return false;
			if (removedTiles == null) {
				if (other.removedTiles != null)
					return false;
			} else if (!removedTiles.equals(other.removedTiles))
				return false;
			return true;
		}

	}
}
