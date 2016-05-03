package com.github.blovemaple.mj.object;

import static com.github.blovemaple.mj.object.StandardTileUnitType.*;

import java.util.Set;

/**
 * 牌组类型。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public enum TileGroupType {
	/**
	 * 吃
	 */
	CHI_GROUP(SHUNZI),
	/**
	 * 碰
	 */
	PENG_GROUP(KEZI),
	/**
	 * 直杠
	 */
	ZHIGANG_GROUP(GANGZI),
	/**
	 * 补杠
	 */
	BUGANG_GROUP(GANGZI),
	/**
	 * 暗杠
	 */
	ANGANG_GROUP(GANGZI),
	/**
	 * 补花
	 */
	BUHUA_GROUP(HUA_UNIT);

	private final TileUnitType unitType;

	private TileGroupType(TileUnitType unitType) {
		this.unitType = unitType;
	}

	public TileUnitType getUnitType() {
		return unitType;
	}

	/**
	 * 返回一个单元中有几张牌。
	 */
	public int size() {
		return unitType.size();
	}

	/**
	 * 判断指定牌集合是否是合法的牌组。
	 */
	public boolean isLegalTiles(Set<Tile> tiles) {
		return unitType.isLegalTiles(tiles);
	}

}