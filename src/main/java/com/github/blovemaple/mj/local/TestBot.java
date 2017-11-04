package com.github.blovemaple.mj.local;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.object.Player;
import com.github.blovemaple.mj.object.PlayerLocation;

/**
 * 测试用的机器人，无脑摸牌无脑出牌（出牌之前假装想一会儿），别的不干。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class TestBot implements Player {

	@Override
	public String getName() {
		return "TestBot";
	}

	@Override
	public Action chooseAction(GameContextPlayerView contextView,
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
	public void actionDone(GameContextPlayerView contextView, PlayerLocation actionLocation, Action action) {
	}

	@Override
	public void timeLimit(GameContextPlayerView contextView, Integer secondsToGo) {
	}

}
