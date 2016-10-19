package com.github.blovemaple.mj.rule.fan;

import com.github.blovemaple.mj.rule.win.WinInfo;

/**
 * 判断是否符合番种的接口，定义一个完整的判断条件。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface FanTypeMatcher {
	/**
	 * 检查和牌是否符合此番种，如果符合，则向winInfo填入番数信息。如果检查过程中检查出了符合其他番种，应该一并填入winInfo。
	 * 
	 * @param winInfo
	 *            和牌信息。
	 * @return 如果不符合，返回0；如果符合，返回应该计入的次数。
	 */
	public int match(WinInfo winInfo);

}
