package com.github.blovemaple.mj.rule.simple;

import static com.github.blovemaple.mj.action.standard.PlayerActionTypes.*;

import java.util.List;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.AutoActionType;
import com.github.blovemaple.mj.action.PlayerActionType;
import com.github.blovemaple.mj.action.StageSwitchAction;
import com.github.blovemaple.mj.action.standard.AutoActionTypes;
import com.github.blovemaple.mj.action.standard.PlayerActionTypes;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.rule.GameStage;

/**
 * 打牌中的阶段。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class PlayingStage implements GameStage {
	public static final String NAME = "PLAYING";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public List<? extends PlayerActionType> getPlayerActionTypes() {
		return List.of(PlayerActionTypes.values());
	}

	@Override
	public List<? extends AutoActionType> getAutoActionTypes() {
		return List.of();
	}

	@Override
	public Action getPriorAction(GameContext context) {
		if (context.getDoneActions().stream().map(Action::getType).anyMatch(type -> type == WIN))
			return new StageSwitchAction(FinishedStage.NAME);
		return null;
	}

	@Override
	public Action getFinalAction(GameContext context) {
		return new Action(AutoActionTypes.LIUJU);
	}

}
