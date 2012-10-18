package blove.mj;

/**
 * 玩家位置。
 * 
 * @author 陈通
 */
public enum PlayerLocation {
	// 顺序勿动。计算位置依赖此枚举的顺序！
	EAST("东"), SOUTH("南"), WEST("西"), NORTH("北");

	private final String name;

	private PlayerLocation(String name) {
		this.name = name;
	}

	/**
	 * 返回可显示名称。
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * 位置关系。
	 * 
	 * @author 陈通
	 */
	public enum Relation {
		// 顺序勿动。计算位置依赖此枚举的顺序！
		SELF, NEXT, OPPOSITE, PREVIOUS
	}

	/**
	 * 返回另一个位置相对于此位置的关系。
	 * 
	 * @param other
	 *            另一个位置
	 * @return 关系
	 */
	public Relation getRelationOf(PlayerLocation other) {
		int dis = other.ordinal() - this.ordinal();
		if (dis < 0)
			dis += 4;
		return Relation.values()[dis];
	}

	/**
	 * 返回相对于此位置的指定关系的位置。
	 * 
	 * @param relation
	 *            关系
	 * @return 位置
	 */
	public PlayerLocation getLocationOf(Relation relation) {
		return PlayerLocation.values()[(this.ordinal() + relation.ordinal()) % 4];
	}

}