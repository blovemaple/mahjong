package com.github.blovemaple.mj.rule.fan;

import java.util.HashMap;
import java.util.Map;

import com.github.blovemaple.mj.rule.win.FanType;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class FanTestFailureException extends Exception {
	private static final long serialVersionUID = 1L;

	private final Map<FanType, Integer> expected, result;

	public FanTestFailureException(Map<FanType, Integer> expected, Map<FanType, Integer> result) {
		super("Expected: " + toString(expected) + ", result: " + toString(result));
		this.expected = expected;
		this.result = result;
	}

	private static Map<String, Integer> toString(Map<FanType, Integer> fanTypes) {
		if (fanTypes == null)
			return null;
		Map<String, Integer> result = new HashMap<>();
		fanTypes.forEach((fanType, score) -> result.put(fanType.name(), score));
		return result;
	}

	public Map<FanType, Integer> getExpected() {
		return expected;
	}

	public Map<FanType, Integer> getResult() {
		return result;
	}

}
