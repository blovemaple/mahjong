package com.github.blovemaple.mj.cli.v2;

import com.github.blovemaple.mj.object.PlayerLocation.Relation;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public enum CliViewDirection {
	LOWER, RIGHT, UPPER, LEFT;

	/**
	 * 返回本家方向。
	 */
	public static final CliViewDirection self() {
		return LOWER;
	}

	/**
	 * 返回另一个方向相对于此方向的关系。
	 * 
	 * @param other
	 *            另一个方向
	 * @return 关系
	 */
	public Relation getRelationOf(CliViewDirection other) {
		int dis = other.ordinal() - this.ordinal();
		if (dis < 0)
			dis += 4;
		return Relation.values()[dis];
	}

	/**
	 * 返回相对于此方向的指定关系的方向。
	 * 
	 * @param relation
	 *            关系
	 * @return 方向
	 */
	public CliViewDirection getLocationOf(Relation relation) {
		return CliViewDirection.values()[(this.ordinal() + relation.ordinal()) % 4];
	}
}
