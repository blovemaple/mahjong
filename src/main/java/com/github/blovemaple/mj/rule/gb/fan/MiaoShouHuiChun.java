package com.github.blovemaple.mj.rule.gb.fan;

import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.rule.win.FanTypeMatcher;
import com.github.blovemaple.mj.rule.win.WinInfo;

/**
 * 妙手回春。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class MiaoShouHuiChun implements FanTypeMatcher {

	@Override
	public int matchCount(WinInfo winInfo) {
		Boolean ziMo = winInfo.getZiMo();
		if (ziMo == null || !ziMo)
			// 没有是否自摸的信息，或不是自摸
			return 0;

		GameContextPlayerView contextView = winInfo.getContextView();
		if (contextView == null)
			// 没有contextView
			return 0;

		if (contextView.getTableView().getTileWallSize() > 0)
			// 牌墙还有牌
			return 0;

		return 1;
	}

}
