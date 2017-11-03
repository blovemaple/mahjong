package com.github.blovemaple.mj.rule.gb.fan;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;

import java.util.Arrays;
import java.util.List;

import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.rule.win.FanTypeMatcher;
import com.github.blovemaple.mj.rule.win.WinInfo;

/**
 * 杠上开花。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class GangShangKaiHua implements FanTypeMatcher {

	private static final List<ActionType> GANG_ACTIONS = Arrays.asList(ZHIGANG, BUGANG, ANGANG);

	@Override
	public int matchCount(WinInfo winInfo) {
		Boolean ziMo = winInfo.getZiMo();
		if (ziMo == null || !ziMo)
			// 没有自摸信息或不是自摸
			return 0;

		GameContext.PlayerView contextView = winInfo.getContextView();
		if (contextView == null)
			// 没有contextView
			return 0;

		ActionType lastActionType = contextView.getLastAction().getType();
		if (!DRAW_BOTTOM.matchBy(lastActionType))
			// 最后一个动作不是摸底
			return 0;

		if (GANG_ACTIONS.stream().noneMatch(gangAction -> gangAction.matchBy(lastActionType)))
			// 倒数第二个动作不是杠
			return 0;

		return 1;
	}

}
