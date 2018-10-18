package com.github.blovemaple.mj.game;

import java.util.List;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.object.MahjongTable;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.rule.GameStage;
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

	public GameStage getStage();

	public void setStage(GameStage stage);

	public void actionDone(Action action);

	public Action getLastAction();

	public PlayerLocation getLastActionLocation();

	public List<Action> getDoneActions();

	public GameResult getGameResult();

	public void setGameResult(GameResult gameResult);

	public GameContextPlayerView getPlayerView(PlayerLocation location);

}
