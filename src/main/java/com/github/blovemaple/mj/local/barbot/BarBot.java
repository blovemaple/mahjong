package com.github.blovemaple.mj.local.barbot;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.game.GameContext.PlayerView;
import com.github.blovemaple.mj.object.Player;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class BarBot implements Player {
	private static final Logger logger = Logger
			.getLogger(BarBot.class.getSimpleName());

	// 一次思考的最短（含）和最长（不含）时间，单位秒，随机使用
	private static final int THINKING_TIME_MIN = 1, THINKING_TIME_MAX = 4;
	private final Random random = new Random();
	private static final int DRAW_TIME = 500;

	private String name;

	public BarBot(String name) {
		this.name = name;
	}

	public BarBot() {
		this("BarBot");
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Action chooseAction(PlayerView contextView,
			Set<ActionType> actionTypes) throws InterruptedException {
		// 如果可以和，就和
		if (actionTypes.contains(WIN))
			return new Action(WIN);

		// 如果可以补花，就补花
		if (actionTypes.contains(BUHUA)) {
			Collection<Set<Tile>> buhuas = BUHUA.getLegalActionTiles(contextView);
			if (!buhuas.isEmpty()) {
				// 补花前延迟
				TimeUnit.MILLISECONDS.sleep(DRAW_TIME);
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
				TimeUnit.MILLISECONDS.sleep(DRAW_TIME);
				return new Action(drawType);
			}

		// 啥都没选择，放弃了
		return null;
	}

	private Future<Action> selectFuture;

	private Action chooseCpgdAction(PlayerView contextView, Set<ActionType> actionTypes) throws InterruptedException {
		if (selectFuture != null && !selectFuture.isDone())
			throw new IllegalStateException("Another select task is active.");

		if (Collections.disjoint(actionTypes, BarBotCpgdSelectTask.ACTION_TYPES))
			return null;

		// 随机选择思考时间
		long startTime = System.currentTimeMillis();
		int thinkingTime = THINKING_TIME_MIN + random.nextInt(THINKING_TIME_MAX - THINKING_TIME_MIN);

		Future<Action> futureResult = ForkJoinPool.commonPool()
				.submit(new BarBotCpgdSelectTask(contextView, actionTypes));
		try {
			Action action = futureResult.get();
			long endTime = System.currentTimeMillis();
			long remainTimeMs = thinkingTime * 1000 - (endTime - startTime);
			if (remainTimeMs > 0)
				// 时间太短了，假装再想一会儿
				TimeUnit.MILLISECONDS.sleep(remainTimeMs);
			return action;
		} catch (InterruptedException e) {
			// 选择被game中断，不再继续选择了
			selectFuture.cancel(true);
			throw e;
		} catch (ExecutionException e) {
			// 选择过程出现错误
			throw new RuntimeException(e);
		} catch (CancellationException e) {
			// 应该不会在这出来
			throw new RuntimeException(e);
		}
	}

	@Override
	public Action chooseAction(PlayerView contextView,
			Set<ActionType> actionTypes, Action illegalAction)
			throws InterruptedException {
		logger.severe("Selected illegal action: " + illegalAction);
		return null;
	}

	@Override
	public void actionDone(PlayerView contextView,
			PlayerLocation actionLocation, Action action) {
	}

	@Override
	public void timeLimit(PlayerView contextView, Integer secondsToGo) {
		// TODO 机器人对限时的处理
	}

}
