package com.github.blovemaple.mj.action;

/**
 * 动作。
 * 
 * @param <A>
 *            附加信息类型
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class Action {
	/**
	 * 动作类型。
	 */
	private ActionType type;

	/**
	 * 新建一个实例。
	 */
	public Action(ActionType type) {
		this.type = type;
	}

	public ActionType getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Action))
			return false;
		Action other = (Action) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + type + "]";
	}

}
