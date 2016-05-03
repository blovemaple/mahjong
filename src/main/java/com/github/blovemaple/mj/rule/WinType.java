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
}
