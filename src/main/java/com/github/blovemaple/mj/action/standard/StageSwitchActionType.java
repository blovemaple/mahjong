package com.github.blovemaple.mj.action.standard;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.action.IllegalActionException;
import com.github.blovemaple.mj.action.StageSwitchAction;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.rule.GameStage;

/**
 * 状态切换的动作类型。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class StageSwitchActionType implements ActionType {
	public static final StageSwitchActionType INSTANCE=new StageSwitchActionType();


	@Override
	public boolean isLegalAction(GameContext context, Action action) {
		if (!(action instanceof StageSwitchAction))
			return false;
		return context.getGameStrategy().getStageByName(((StageSwitchAction) action).getNextStageName()) != null;
	}

	@Override
	public void doAction(GameContext context, Action action) throws IllegalActionException {
		GameStage nextStage = context.getGameStrategy().getStageByName(((StageSwitchAction) action).getNextStageName());
		if (nextStage == null)
			throw new IllegalActionException(context, action);
		context.setStage(nextStage);
	}

}
