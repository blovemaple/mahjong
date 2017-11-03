package com.github.blovemaple.mj.rule.gb.fan;

import com.github.blovemaple.mj.rule.win.FanTypeMatcher;
import com.github.blovemaple.mj.rule.win.WinInfo;

/**
 * 无番和。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class WuFanHu implements FanTypeMatcher {

	@Override
	public int matchCount(WinInfo winInfo) {
		return winInfo.getFans().isEmpty() ? 1 : 0;
	}

}
