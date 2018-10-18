package com.github.blovemaple.mj.game;

import java.util.List;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.object.MahjongTablePlayerView;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.rule.GameStrategy;
import com.github.blovemaple.mj.rule.TimeLimitStrategy;

/**
 * {@link GameContext}给一个玩家的视图接口。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface GameContextPlayerView {

	/**
	 * 返回麻将桌的玩家视图。
	 */
	public MahjongTablePlayerView getTableView();

	/**
	 * 返回游戏策略。
	 */
	public GameStrategy getGameStrategy();

	/**
	 * 返回限时策略。
	 */
	public TimeLimitStrategy getTimeLimitStrategy();

	/**
	 * 返回当前玩家位置。
	 */
	public PlayerLocation getMyLocation();

	/**
	 * 返回当前玩家信息。
	 */
	public PlayerInfo getMyInfo();

	/**
	 * 返回庄家位置。
	 */
	public PlayerLocation getZhuangLocation();

	/**
	 * 返回当前阶段名称。
	 */
	public String getStageName();

	/**
	 * 返回到目前为止做出的最后一个动作。
	 */
	public Action getLastAction();

	/**
	 * 返回到目前为止做出的最后一个动作的玩家位置。
	 */
	public PlayerLocation getLastActionLocation();

	/**
	 * 如果刚刚摸牌，则返回刚摸的牌，否则返回null。
	 */
	public Tile getJustDrawedTile();

	/**
	 * 返回已经做完的动作。
	 */
	public List<Action> getDoneActions();

	/**
	 * 如果已结束则返回游戏结果，否则返回null。
	 */
	public GameResult getGameResult();

}
