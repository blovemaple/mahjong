package com.github.blovemaple.mj.local;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameEventListener;
import com.github.blovemaple.mj.object.Player;

/**
 * 测试机器人。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class TestBot implements Player {

	@Override
	public String getName() {
		return "TestBot";
	}

	@Override
	public Action chooseAction(GameContext.PlayerView contextView,
			Set<ActionType> actionTypes, Action illegalAction)
			throws InterruptedException {
		if (actionTypes.contains(DISCARD)) {
			TimeUnit.SECONDS.sleep(1);
			return new Action(DISCARD,
					contextView.getMyInfo().getAliveTiles().iterator().next());
		}
		if (actionTypes.contains(DRAW))
			return new Action(DRAW);
		return null;
	}

	@Override
	public GameEventListener getEventListener() {
		return null;
	}

}
