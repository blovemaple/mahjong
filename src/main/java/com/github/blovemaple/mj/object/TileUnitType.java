package com.github.blovemaple.mj.object;

import java.util.Collection;

/**
 * 牌的单元的类型。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface TileUnitType {

	/**
	 * 返回一个单元中有几张牌。
	 */
	int size();

	/**
	 * 判断指定牌集合是否是合法的单元。
	 */
	boolean isLegalTiles(Collection<Tile> tiles);

}