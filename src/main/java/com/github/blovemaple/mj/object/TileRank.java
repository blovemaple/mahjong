package com.github.blovemaple.mj.object;

/**
 * 牌的花色下面的小种类。如万牌中的“一”、“二”，风牌中的“东”、“南”等。<br>
 * 实现类必须有values()方法返回相应的种类数组（枚举类就可以）。因为TileSuit会用values()方法获取对应的种类。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface TileRank<T extends TileRank<T>> extends Comparable<T> {
	/**
	 * 返回名称。用于唯一标识。
	 */
	public String name();

	/**
	 * 万、条、饼等花色使用的数字种类。
	 */
	public enum NumberRank implements TileRank<NumberRank> {
		YI(1), ER(2), SAN(3), SI(4), WU(5), LIU(6), QI(7), BA(8), JIU(9);

		private final int number;

		private NumberRank(int number) {
			this.number = number;
		}

		public int getNumber() {
			return number;
		}
	}

	/**
	 * 字牌的种类。
	 */
	public enum ZiRank implements TileRank<ZiRank> {
		DONG_FENG, NAN, XI, BEI, ZHONG, FA, BAI
	}

	/**
	 * 花牌的种类。
	 */
	public enum HuaRank implements TileRank<HuaRank> {
		CHUN, XIA, QIU, DONG_HUA, MEI, LAN, ZHU, JU
	}
}
