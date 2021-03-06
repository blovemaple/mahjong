package com.github.blovemaple.mj.local.barbot;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import com.github.blovemaple.mj.action.PlayerAction;
import com.github.blovemaple.mj.action.PlayerActionType;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.local.AbstractBot;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
@Deprecated
public class BarBot extends AbstractBot {
	private String name;

	public BarBot(String name) {
		super(name);
		this.name = name;
	}

	public BarBot() {
		this("BarBot");
	}

	@Override
	public String getName() {
		return name;
	}

	private Future<PlayerAction> selectFuture;

	@Override
	protected PlayerAction chooseCpgdAction(GameContextPlayerView contextView, Set<PlayerActionType> actionTypes,
			List<PlayerAction> actions) throws InterruptedException {
		if (selectFuture != null && !selectFuture.isDone())
			throw new IllegalStateException("Another select task is active.");

		if (Collections.disjoint(actionTypes, BarBotCpgdSelectTask.ACTION_TYPES))
			return null;

		Future<PlayerAction> futureResult = ForkJoinPool.commonPool()
				.submit(new BarBotCpgdSelectTask(contextView, actionTypes));
		try {
			return futureResult.get();
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

}
