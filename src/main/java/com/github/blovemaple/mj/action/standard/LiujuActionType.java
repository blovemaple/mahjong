package com.github.blovemaple.mj.action.standard;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.action.IllegalActionException;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameResult;

/**
 * 动作类型“流局”。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class LiujuActionType implements ActionType {

	protected LiujuActionType() {
	}

	@Override
	public boolean isLegalAction(GameContext context, Action action) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void doAction(GameContext context, Action action) throws IllegalActionException {
		GameResult result = new GameResult(context.getTable().getPlayerInfos(),
				context.getZhuangLocation());
		context.setGameResult(result);
	}

}
