package com.github.blovemaple.mj.rule.simple;

import java.util.Set;

import com.github.blovemaple.mj.rule.fan.FanType;
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
	public int match(WinInfo winInfo) {
		return 1;
	}

	@Override
	public int score() {
		return 1;
	}

	@Override
	public Set<FanType> covered() {
		return null;
	}

}
