package com.github.blovemaple.mj.rule.simple;

import static com.github.blovemaple.mj.action.standard.PlayerActionTypes.*;

import java.util.List;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.AutoActionType;
import com.github.blovemaple.mj.action.PlayerActionType;
import com.github.blovemaple.mj.action.StageSwitchAction;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.rule.GameStage;

/**
 * 开始打牌前的阶段，玩家在此阶段进行补花等动作。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class BeforePlayingStage implements GameStage {
	public static final String NAME = "BEFORE_PLAYING";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public List<? extends PlayerActionType> getPlayerActionTypes() {
		return List.of(BUHUA, DRAW_BOTTOM);
	}

	@Override
	public List<? extends AutoActionType> getAutoActionTypes() {
		return List.of();
	}

	@Override
	public Action getPriorAction(GameContext context) {
		return null;
	}

	@Override
	public Action getFinalAction(GameContext context) {
		return new StageSwitchAction(PlayingStage.NAME);
	}

}
