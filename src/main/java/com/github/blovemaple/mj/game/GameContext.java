package com.github.blovemaple.mj.game;

import java.util.List;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionAndLocation;
import com.github.blovemaple.mj.object.MahjongTable;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.rule.GameStrategy;
import com.github.blovemaple.mj.rule.TimeLimitStrategy;

/**
 * 一局游戏进行中的上下文信息。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface GameContext {

	public MahjongTable getTable();

	public GameStrategy getGameStrategy();

	public TimeLimitStrategy getTimeLimitStrategy();

	public PlayerInfo getPlayerInfoByLocation(PlayerLocation location);

	public PlayerLocation getZhuangLocation();

	public void setZhuangLocation(PlayerLocation zhuangLocation);

	public void actionDone(Action action, PlayerLocation location);

	public ActionAndLocation getLastActionAndLocation();

	public Action getLastAction();

	public PlayerLocation getLastActionLocation();

	public List<ActionAndLocation> getDoneActions();

	public GameResult getGameResult();

	public void setGameResult(GameResult gameResult);

	public GameContextPlayerView getPlayerView(PlayerLocation location);

}
