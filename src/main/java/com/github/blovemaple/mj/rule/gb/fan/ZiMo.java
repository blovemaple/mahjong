package com.github.blovemaple.mj.rule.gb.fan;

import com.github.blovemaple.mj.rule.win.FanTypeMatcher;
import com.github.blovemaple.mj.rule.win.WinInfo;

/**
 * 自摸。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class ZiMo implements FanTypeMatcher {

	@Override
	public int matchCount(WinInfo winInfo) {
		Boolean ziMo = winInfo.getZiMo();
		return (ziMo != null && ziMo) ? 1 : 0;
	}

}
