package com.github.blovemaple.mj.rule.fan;

import java.util.Collection;
import java.util.Map;

import com.github.blovemaple.mj.rule.win.WinInfo;

/**
 * 判断是否符合番种的接口，定义一个完整的判断条件。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface FanTypeMatcher {
	/**
	 * 检查和牌是否符合此番种。如果检查过程中查出了符合uncheckedFanTypes中的番种，需要将其删除，
	 * 并加入到matchedFanTypes中。
	 * 
	 * @param winInfo
	 *            和牌信息
	 * @param matchedFanTypes
	 *            已经检查出符合的番种以及次数。
	 * @param uncheckedFanTypes
	 *            还没检查的番种。不包含当前番种。
	 * @return 如果不符合，返回0；如果符合，返回应该计入的次数。
	 */
	public int match(WinInfo winInfo, Map<FanType, Integer> matchedFanTypes, Collection<FanType> uncheckedFanTypes);

}
