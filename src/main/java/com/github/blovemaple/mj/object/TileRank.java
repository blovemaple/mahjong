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
		// 顺序勿动！ofNumber依赖顺序
		YI(1), ER(2), SAN(3), SI(4), WU(5), LIU(6), QI(7), BA(8), JIU(9);

		private final int number;

		private NumberRank(int number) {
			this.number = number;
		}

		public int number() {
			return number;
		}

		public static NumberRank ofNumber(int number) {
			return values()[number - 1];
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static int compare(TileRank<?> r1, TileRank<?> r2) {
		if (r1.getClass() != r2.getClass()) {
			Integer.compare(r1.getClass().hashCode(), r2.getClass().hashCode());
		}
		return compare0((TileRank) r1, (TileRank) r2);
	}

	private static <T extends TileRank<T>> int compare0(T r1, T r2) {
		return r1.compareTo(r2);
	}
}
