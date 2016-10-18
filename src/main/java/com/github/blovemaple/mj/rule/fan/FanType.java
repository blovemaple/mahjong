package com.github.blovemaple.mj.rule.fan;

import java.util.Set;

/**
 * 番种。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface FanType extends FanTypeMatcher {
	/**
	 * 返回番种的名称，作为标识。
	 */
	public String name();

	/**
	 * 返回计入一次的番数。
	 */
	public int score();

	/**
	 * 返回被此番种覆盖的番种。如果此番种已计入，则不计被覆盖的番种。
	 */
	public Set<FanType> covered();
}
