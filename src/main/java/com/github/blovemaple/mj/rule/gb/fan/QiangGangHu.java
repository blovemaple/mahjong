package com.github.blovemaple.mj.rule.gb.fan;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;

import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.rule.win.FanTypeMatcher;
import com.github.blovemaple.mj.rule.win.WinInfo;

/**
 * 抢杠和。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class QiangGangHu implements FanTypeMatcher {

	@Override
	public int matchCount(WinInfo winInfo) {
		GameContext.PlayerView contextView = winInfo.getContextView();
		if (contextView == null)
			// 没有contextView
			return 0;

		return contextView.getLastAction().getType() == BUGANG ? 1 : 0;
	}

}
