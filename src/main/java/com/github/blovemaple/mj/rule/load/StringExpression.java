package com.github.blovemaple.mj.rule.load;

public class StringExpression implements RuleExpression<String> {
	private String value;

	@Override
	public String getValue() {
		return value;
	}

}
