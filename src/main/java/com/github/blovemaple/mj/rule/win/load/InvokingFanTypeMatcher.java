package com.github.blovemaple.mj.rule.win.load;

import java.util.Map;

import com.github.blovemaple.mj.rule.win.FanType;
import com.github.blovemaple.mj.rule.win.FanTypeMatcher;
import com.github.blovemaple.mj.rule.win.WinInfo;

/**
 * TODO comment
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class InvokingFanTypeMatcher implements FanTypeMatcher {
	private final String invoking;
	private final Map<String, ? extends FanType> fanTypes;

	private FanType target;

	public InvokingFanTypeMatcher(String invoking, Map<String, ? extends FanType> fanTypes) {
		this.invoking = invoking;
		this.fanTypes = fanTypes;
	}

	private synchronized void init() {
		if (target != null)
			return;

		target = fanTypes.get(invoking);
		if (target == null) {
			throw new RuntimeException("Cannot find fan type: " + invoking);
		}
	}

	@Override
	public int matchCount(WinInfo winInfo) {
		if (target == null)
			init();
		int result = target.matchCount(winInfo);
		winInfo.setFans(target, result);
		return result;
	}

	@Override
	public String toString() {
		return "INVOKING[" + invoking + "]";
	}

}
