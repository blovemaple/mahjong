package com.github.blovemaple.mj.object;

import java.util.Set;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.event.GameEventListener;
import com.github.blovemaple.mj.game.GameContext;

/**
 * 玩家。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface Player {
	/**
	 * 返回玩家名字。
	 */
	public String getName();

	/**
	 * 返回游戏事件监听器。将通过返回的监听器通知游戏事件。
	 */
	public GameEventListener getEventListener();

	/**
	 * 选择一个要执行的动作。
	 * 
	 * @param contextView
	 *            游戏上下文
	 * @param actionTypes
	 *            可选动作类型
	 * @return 要执行的动作
	 * @throws InterruptedException
	 *             线程被中断时抛出此异常。选择过程中随时可能被中断，实现时应该经常检查。
	 */
	public default Action chooseAction(GameContext.PlayerView contextView,
			Set<ActionType> actionTypes) throws InterruptedException {
		return chooseAction(contextView, actionTypes, null);
	}

	/**
	 * 选择一个要执行的动作。
	 * 
	 * @param contextView
	 *            游戏上下文
	 * @param actionTypes
	 *            可选动作类型
	 * @param illegalAction
	 *            如果非null，则表示上一次选择的动作不符合规则，需要重新选择。此参数提供上次选择的动作。
	 * @return 要执行的动作
	 * @throws InterruptedException
	 *             线程被中断时抛出此异常。选择过程中随时可能被中断，实现时应该经常检查。
	 */
	public Action chooseAction(GameContext.PlayerView contextView,
			Set<ActionType> actionTypes, Action illegalAction)
			throws InterruptedException;

}
