package com.github.blovemaple.mj.rule.simple;

import java.util.List;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.AutoActionType;
import com.github.blovemaple.mj.action.PlayerActionType;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.rule.GameStage;

/**
 * 一局游戏结束后的阶段。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class FinishedStage implements GameStage {
	public static final String NAME = "FINISHED";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public List<? extends PlayerActionType> getPlayerActionTypes() {
		return List.of();
	}

	@Override
	public List<? extends AutoActionType> getAutoActionTypes() {
		return List.of();
	}

	@Override
	public Action chooseAction(GameContext context) {
		return null;
	}

	@Override
	public Action getFinalAction(GameContext context) {
		// TODO Auto-generated method stub
		return null;
	}
}
