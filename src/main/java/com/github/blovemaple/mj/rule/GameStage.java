package com.github.blovemaple.mj.rule;

import java.util.List;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.AutoActionType;
import com.github.blovemaple.mj.action.PlayerActionType;
import com.github.blovemaple.mj.game.GameContext;

/**
 * 游戏阶段。定义阶段中玩家可使用的所有动作类型，以及转换到其他阶段的条件。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface GameStage {

	/**
	 * 返回该阶段名称。
	 */
	public String getName();

	/**
	 * 返回该阶段中玩家可使用的所有动作类型。
	 */
	public List<? extends PlayerActionType> getPlayerActionTypes();

	/**
	 * 返回该阶段中可自动做出的所有动作类型。
	 */
	public List<? extends AutoActionType> getAutoActionTypes();

	/**
	 * 根据当前状态决定阶段动作（如切换到其他阶段）并返回，不执行动作时返回null。<br>
	 * 此方法返回的动作具有最高优先级。
	 */
	public Action getPriorAction(GameContext context);

	/**
	 * 返回当玩家和自动动作类型都无动作可做时应该执行的动作（如切换到其他阶段）。<br>
	 * 此方法返回的动作具有最低优先级。
	 */
	public Action getFinalAction(GameContext context);
}
