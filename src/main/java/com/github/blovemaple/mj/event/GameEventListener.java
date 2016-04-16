package com.github.blovemaple.mj.event;

import java.util.EventListener;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.object.PlayerLocation;

/**
 * 游戏事件监听器。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface GameEventListener extends EventListener {
	/**
	 * 完成一个动作时通知。
	 */
	void actionDone(GameContext.PlayerView contextView,
			PlayerLocation actionLocation, Action action);

	/**
	 * 倒计时有变化时通知。（会通知所有监听器，被通知的玩家不一定要做动作）
	 * 
	 * @param secondsToGo
	 *            剩余秒数。null表示倒计时结束或取消。
	 */
	void timeLimit(GameContext.PlayerView contextView, Integer secondsToGo);
}
