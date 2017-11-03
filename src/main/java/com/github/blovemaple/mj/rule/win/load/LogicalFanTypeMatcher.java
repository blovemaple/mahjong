package com.github.blovemaple.mj.rule.win.load;

import java.util.Objects;

import com.github.blovemaple.mj.rule.win.FanTypeMatcher;
import com.github.blovemaple.mj.rule.win.WinInfo;

/**
 * 番种的逻辑型判断条件，支持与、或、非三种运算。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class LogicalFanTypeMatcher implements FanTypeMatcher {

	private final LogicalOp op;
	private final FanTypeMatcher matcher1, matcher2;

	public enum LogicalOp {
		AND, OR, NOT
	}

	public LogicalFanTypeMatcher(LogicalOp op, FanTypeMatcher matcher1, FanTypeMatcher matcher2) {
		Objects.requireNonNull(op);
		Objects.requireNonNull(matcher1);
		if (op != LogicalOp.NOT)
			Objects.requireNonNull(matcher2);
		this.op = op;
		this.matcher1 = matcher1;
		this.matcher2 = matcher2;
	}

	@Override
	public int matchCount(WinInfo winInfo) {
		boolean match;
		switch (op) {
		case AND:
			match = matcher1.matchCount(winInfo) > 0
					&& matcher2.matchCount(winInfo) > 0;
			break;
		case OR:
			match = matcher1.matchCount(winInfo) > 0
					|| matcher2.matchCount(winInfo) > 0;
			break;
		case NOT:
			match = matcher1.matchCount(winInfo) == 0;
			break;
		default:
			throw new RuntimeException("Unsupported Op: " + op);
		}
		return match ? 1 : 0;
	}

	@Override
	public String toString() {
		return op + " { " + matcher1 + " , " + matcher2 + " }";
	}

}
