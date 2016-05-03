package com.github.blovemaple.mj.local.barbot;

import java.util.List;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionAndLocation;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameResult;
import com.github.blovemaple.mj.object.MahjongTable;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.rule.GameStrategy;
import com.github.blovemaple.mj.rule.TimeLimitStrategy;

/**
 * 模拟选择时执行动作使用的GameContext。因为是机器人任务使用，所以只能从GameContext.PlayerView构建，
 * 只允许获取PlayerView能看到的一些信息。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class BarBotSimContext extends GameContext {
	private GameContext.PlayerView contextView;
	private PlayerInfo myInfo;

	public BarBotSimContext(GameContext.PlayerView contextView,
			PlayerInfo myInfo) {
		super(null, contextView.getGameStrategy(),
				contextView.getTimeLimitStrategy());
		this.contextView = contextView;
		this.myInfo = myInfo;
	}

	@Override
	public MahjongTable getTable() {
		throw new UnsupportedOperationException();
	}

	@Override
	public GameStrategy getGameStrategy() {
		return super.getGameStrategy();
	}

	@Override
	public TimeLimitStrategy getTimeLimitStrategy() {
		return super.getTimeLimitStrategy();
	}

	@Override
	public PlayerInfo getPlayerInfoByLocation(PlayerLocation location) {
		if (location == contextView.getMyLocation())
			return myInfo;
		else
			throw new UnsupportedOperationException();
	}

	@Override
	public PlayerLocation getZhuangLocation() {
		return contextView.getZhuangLocation();
	}

	@Override
	public void setZhuangLocation(PlayerLocation zhuangLocation) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void actionDone(Action action, PlayerLocation location) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ActionAndLocation getLastActionAndLocation() {
		return contextView.getLastActionAndLocation();
	}

	@Override
	public Action getLastAction() {
		return contextView.getLastAction();
	}

	@Override
	public PlayerLocation getLastActionLocation() {
		return contextView.getLastActionLocation();
	}

	@Override
	public List<ActionAndLocation> getDoneActions() {
		return contextView.getDoneActions();
	}

	@Override
	protected void setDoneActions(List<ActionAndLocation> doneActions) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GameResult getGameResult() {
		return contextView.getGameResult();
	}

	@Override
	public void setGameResult(GameResult gameResult) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PlayerView getPlayerView(PlayerLocation location) {
		if (location == contextView.getMyLocation())
			return contextView;
		else
			throw new UnsupportedOperationException();
	}

	@Override
	protected PlayerView newPlayerView(PlayerLocation location) {
		throw new UnsupportedOperationException();
	}

}
