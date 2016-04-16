package com.github.blovemaple.mj.rule;

import com.github.blovemaple.mj.object.PlayerInfo;

/**
 * 番种。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface FanType {
	/**
	 * 返回番种的名称，作为标识。
	 */
	public String name();

	/**
	 * 检查和牌是否符合此番种。
	 */
	public boolean match(PlayerInfo playerInfo);
}
