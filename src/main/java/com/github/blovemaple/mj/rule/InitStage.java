package com.github.blovemaple.mj.rule;

import java.util.List;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.AutoActionType;
import com.github.blovemaple.mj.action.PlayerActionType;
import com.github.blovemaple.mj.action.StageSwitchAction;
import com.github.blovemaple.mj.game.GameContext;

/**
 * 初始阶段。此阶段是开局的默认阶段，唯一的作用是跳转到策略提供的第一阶段。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class InitStage implements GameStage {
	public static final String NAME = "INIT";

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
	public Action getPriorAction(GameContext context) {
		return new StageSwitchAction(context.getGameStrategy().getFirstStage().getName());
	}

	@Override
	public Action getFinalAction(GameContext context) {
		return null;
	}

}
