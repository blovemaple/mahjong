package com.github.blovemaple.mj.rule.gb.fan;

import com.github.blovemaple.mj.action.standard.PlayerActionTypes;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.rule.win.FanTypeMatcher;
import com.github.blovemaple.mj.rule.win.WinInfo;

/**
 * 海底捞月。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class HaiDiLaoYue implements FanTypeMatcher {

	@Override
	public int matchCount(WinInfo winInfo) {
		Boolean ziMo = winInfo.getZiMo();
		if (ziMo != null && ziMo)
			// 是自摸
			return 0;

		GameContextPlayerView contextView = winInfo.getContextView();
		if (contextView == null)
			// 没有contextView
			return 0;

		if (PlayerActionTypes.DISCARD.matchBy(contextView.getLastAction().getType()))
			// 不是和别人打出的牌
			return 0;

		if (contextView.getTableView().getTileWallSize() > 0)
			// 牌墙中还有剩余牌
			return 0;

		return 1;
	}

}
