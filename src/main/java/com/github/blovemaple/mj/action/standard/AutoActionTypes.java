package com.github.blovemaple.mj.action.standard;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.action.AutoActionType;
import com.github.blovemaple.mj.action.IllegalActionException;
import com.github.blovemaple.mj.game.GameContext;

/**
 * 自动动作类型枚举。<br>
 * 枚举的每种动作类型包含对应Type类的单例，并委托调用其对应的方法。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public enum AutoActionTypes implements AutoActionType {
	/**
	 * 发牌
	 */
	DEAL(new DealActionType()),
	/**
	 * 流局
	 */
	LIUJU(new LiujuActionType());

	private final ActionType type;

	private AutoActionTypes(ActionType type) {
		this.type = type;
	}

	// 以下都是委托方法，调用type的对应方法

	@Override
	public void doAction(GameContext context, Action action) throws IllegalActionException {
		type.doAction(context, action);
	}

	@Override
	public boolean matchBy(ActionType testType) {
		return type.matchBy(testType);
	}

	@Override
	public Class<? extends ActionType> getRealTypeClass() {
		return type.getRealTypeClass();
	}

	@Override
	public ActionType getRealTypeObject() {
		return type;
	}

	@Override
	public boolean isLegalAction(GameContext context, Action action) {
		return type.isLegalAction(context, action);
	}

}
