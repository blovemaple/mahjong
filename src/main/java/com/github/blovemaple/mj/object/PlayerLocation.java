package com.github.blovemaple.mj.object;

/**
 * 玩家位置。
 * 
 * @author blovemaple
 */
public enum PlayerLocation {
	// 顺序勿动。计算位置依赖此枚举的顺序！
	EAST, NORTH, WEST, SOUTH;

	/**
	 * 位置关系。
	 * 
	 * @author blovemaple
	 */
	public enum Relation {
		// 顺序勿动。计算位置依赖此枚举的顺序！
		SELF, NEXT, OPPOSITE, PREVIOUS;

		/**
		 * 判断是否是其他人（非SELF）。
		 */
		public boolean isOther() {
			return this != SELF;
		}
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
		return PlayerLocation.values()[(this.ordinal() + relation.ordinal())
				% 4];
	}

}