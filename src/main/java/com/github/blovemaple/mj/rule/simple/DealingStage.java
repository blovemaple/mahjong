package com.github.blovemaple.mj.rule.simple;

import java.util.List;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.AutoActionType;
import com.github.blovemaple.mj.action.PlayerActionType;
import com.github.blovemaple.mj.action.StageSwitchAction;
import com.github.blovemaple.mj.action.standard.AutoActionTypes;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.rule.GameStage;

/**
 * 发牌阶段。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class DealingStage implements GameStage {
	public static final String NAME = "DEALING";

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
		return List.of(AutoActionTypes.DEAL);
	}

	@Override
	public Action chooseAction(GameContext context) {
		return null;
	}

	@Override
	public Action getFinalAction(GameContext context) {
		return new StageSwitchAction(BeforePlayingStage.NAME);
	}

}
