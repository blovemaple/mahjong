package com.github.blovemaple.mj.rule.load;

public class RuleExpressionToken {
	public enum RuleExpressionTokenType {
		CAPITAL_LETTER, SMALL_LETTER, DIGIT, STRING, SYMBOL
	}

	private RuleExpressionTokenType type;
	private String value;
	private int oriPosition;

	public RuleExpressionToken(RuleExpressionTokenType type, String value, int oriPosition) {
		this.type = type;
		this.value = value;
		this.oriPosition = oriPosition;
	}

	public RuleExpressionTokenType type() {
		return type;
	}

	public String value() {
		return value;
	}

	public int oriPosition() {
		return oriPosition;
	}

}
