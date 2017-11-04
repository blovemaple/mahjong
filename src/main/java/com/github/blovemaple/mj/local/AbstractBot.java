package com.github.blovemaple.mj.local;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.object.Player;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public abstract class AbstractBot implements Player {
	private static final Logger logger = Logger.getLogger(AbstractBot.class.getSimpleName());

	private String name;

	public AbstractBot(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Action chooseAction(GameContextPlayerView contextView, Set<ActionType> actionTypes)
			throws InterruptedException {
		// 如果可以和，就和
		if (actionTypes.contains(WIN))
			return new Action(WIN);

		// 如果可以补花，就补花
		if (actionTypes.contains(BUHUA)) {
			Collection<Set<Tile>> buhuas = BUHUA.getLegalActionTiles(contextView);
			if (!buhuas.isEmpty()) {
				// 补花前延迟
				return new Action(BUHUA, buhuas.iterator().next());
			}
		}

		// 如果可以吃/碰/杠/出牌，就选择
		Action action = chooseCpgdAction(contextView, actionTypes);
		if (action != null)
			return action;

		// 如果可以摸牌，就摸牌
		for (ActionType drawType : Arrays.asList(DRAW, DRAW_BOTTOM))
			if (actionTypes.contains(drawType)) {
				// 摸牌前延迟
				return new Action(drawType);
			}

		// 啥都没选择，放弃了
		return null;
	}

	protected abstract Action chooseCpgdAction(GameContextPlayerView contextView, Set<ActionType> actionTypes)
			throws InterruptedException;

	@Override
	public Action chooseAction(GameContextPlayerView contextView, Set<ActionType> actionTypes, Action illegalAction)
			throws InterruptedException {
		logger.severe("Selected illegal action: " + illegalAction);
		return null;
	}

	@Override
	public void actionDone(GameContextPlayerView contextView, PlayerLocation actionLocation, Action action) {
	}

	@Override
	public void timeLimit(GameContextPlayerView contextView, Integer secondsToGo) {
	}

}
