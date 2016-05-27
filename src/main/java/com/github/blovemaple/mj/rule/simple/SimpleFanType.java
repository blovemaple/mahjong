package com.github.blovemaple.mj.rule.simple;

import com.github.blovemaple.mj.rule.win.FanType;
import com.github.blovemaple.mj.rule.win.WinInfo;

/**
 * 简单番种，和牌即算。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class SimpleFanType implements FanType {
	public static final String NAME = "SIMPLE";

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public boolean match(WinInfo winInfo) {
		return true;
	}

}
