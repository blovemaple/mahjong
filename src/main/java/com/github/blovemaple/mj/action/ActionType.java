package com.github.blovemaple.mj.action;

import com.github.blovemaple.mj.action.standard.CpgActionType;
import com.github.blovemaple.mj.action.standard.PlayerActionTypes;
import com.github.blovemaple.mj.game.GameContext;

/**
 * 做出动作的类型。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface ActionType {

	/**
	 * 名称，用作唯一标识。<br>
	 * 默认实现为{@code this.getClass().getSimpleName()}。<br>
	 * 注意：使用{@link PlayerActionTypes}时，name为枚举值的{@code name(})。
	 * 
	 * @see com.github.blovemaple.mj.action.ActionType#name()
	 */
	public default String name() {
		return this.getClass().getSimpleName();
	}

	/**
	 * 判断指定动作是否合法。
	 */
	public boolean isLegalAction(GameContext context, Action action);

	/**
	 * 执行指定动作。
	 * 
	 * @throws IllegalActionException
	 *             动作非法
	 */
	public void doAction(GameContext context, Action action) throws IllegalActionException;

	/**
	 * 返回指定的动作类型是否是此类表示的动作。<br>
	 * 有些动作有上下从属关系，比如“摸底牌”是“摸牌”，但反过来不是。<br>
	 * 默认实现为判断真正类的从属关系。如果不是这样（例如{@link PlayerActionTypes}和{@link CpgActionType}
	 * ）则需要重写此方法。
	 */
	public default boolean matchBy(ActionType testType) {
		return getRealTypeClass().isAssignableFrom(testType.getRealTypeClass());
	}

	/**
	 * 返回真正的动作类型类。<br>
	 * 默认实现为返回此对象的类。如果不是这样（例如{@link PlayerActionTypes}）则需要重写此方法。
	 */
	public default Class<? extends ActionType> getRealTypeClass() {
		return this.getClass();
	}

	/**
	 * 返回真正的动作类型对象。<br>
	 * 默认实现为返回此对象。如果不是这样（例如{@link PlayerActionTypes}）则需要重写此方法。
	 */
	public default ActionType getRealTypeObject() {
		return this;
	}

}
