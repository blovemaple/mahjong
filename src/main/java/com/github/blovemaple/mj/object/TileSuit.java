package com.github.blovemaple.mj.object;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import com.github.blovemaple.mj.object.TileRank.HuaRank;
import com.github.blovemaple.mj.object.TileRank.NumberRank;
import com.github.blovemaple.mj.object.TileRank.ZiRank;

/**
 * 牌的“花色”，即万、条、饼等。
 */
public enum TileSuit {
	WAN(NumberRank.class, 4), TIAO(NumberRank.class, 4), BING(NumberRank.class,
			4), ZI(ZiRank.class, 4), HUA(HuaRank.class, 1);

	private final Class<? extends TileRank<?>> rankClass;
	private final int tileCountByType;

	private TileSuit(Class<? extends TileRank<?>> rankClass,
			int tileCountByType) {
		this.rankClass = rankClass;
		this.tileCountByType = tileCountByType;
	}

	public Class<? extends TileRank<?>> getRankClass() {
		return rankClass;
	}

	/**
	 * 此种花色的每个牌型的牌的个数。
	 */
	public int getTileCountByType() {
		return tileCountByType;
	}

	public List<TileRank<?>> getAllRanks() {
		try {
			return Arrays.asList(
					(TileRank[]) rankClass.getMethod("values").invoke(null));
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException(
					"Error invoke values method of rankType: " + rankClass);
		}
	}

}