package com.github.blovemaple.mj.local.bazbot;

import java.util.ArrayList;
import java.util.List;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionAndLocation;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.game.GameContextPlayerViewImpl;
import com.github.blovemaple.mj.game.GameResult;
import com.github.blovemaple.mj.object.MahjongTable;
import com.github.blovemaple.mj.object.MahjongTablePlayerView;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.rule.GameStrategy;
import com.github.blovemaple.mj.rule.TimeLimitStrategy;

/**
 * BazBot模拟执行动作使用的GameContext。从GameContextPlayerView构建，提供有限功能，足够模拟执行动作。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class BazBotSimContext implements GameContext {
	// oriContextView是构造器传进来的，不可变，只用于获取信息
	private GameContextPlayerView oriContextView;

	// 以下字段记录实时状态，可变
	private PlayerInfo crtMyInfo;
	private List<ActionAndLocation> doneActions;
	private GameContextPlayerView crtContextView; // 延迟生成

	public BazBotSimContext(GameContextPlayerView contextView) {
		this.oriContextView = contextView;

		crtMyInfo = contextView.getMyInfo().clone();
		doneActions = new ArrayList<>(contextView.getDoneActions());
	}

	@Override
	public MahjongTable getTable() {
		throw new UnsupportedOperationException();
	}

	@Override
	public GameStrategy getGameStrategy() {
		return oriContextView.getGameStrategy();
	}

	@Override
	public TimeLimitStrategy getTimeLimitStrategy() {
		return oriContextView.getTimeLimitStrategy();
	}

	@Override
	public PlayerInfo getPlayerInfoByLocation(PlayerLocation location) {
		if (location == oriContextView.getMyLocation())
			return crtMyInfo;

		throw new UnsupportedOperationException();
	}

	@Override
	public PlayerLocation getZhuangLocation() {
		return oriContextView.getZhuangLocation();
	}

	@Override
	public void setZhuangLocation(PlayerLocation zhuangLocation) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void actionDone(Action action, PlayerLocation location) {
		doneActions.add(new ActionAndLocation(action, location));
	}

	@Override
	public ActionAndLocation getLastActionAndLocation() {
		return doneActions.isEmpty() ? null : doneActions.get(doneActions.size() - 1);
	}

	@Override
	public Action getLastAction() {
		return doneActions.isEmpty() ? null : doneActions.get(doneActions.size() - 1).getAction();
	}

	@Override
	public PlayerLocation getLastActionLocation() {
		return doneActions.isEmpty() ? null : doneActions.get(doneActions.size() - 1).getLocation();
	}

	@Override
	public List<ActionAndLocation> getDoneActions() {
		return doneActions;
	}

	@Override
	public GameResult getGameResult() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setGameResult(GameResult gameResult) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GameContextPlayerView getPlayerView(PlayerLocation location) {
		if (location == oriContextView.getMyLocation()) {
			if (crtContextView == null)
				crtContextView = new GameContextPlayerViewImpl(this, location) {
					@Override
					public MahjongTablePlayerView getTableView() {
						return oriContextView.getTableView();
					}
				};
			return crtContextView;
		}

		throw new UnsupportedOperationException();
	}

}
