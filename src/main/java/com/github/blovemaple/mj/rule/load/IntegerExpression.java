package com.github.blovemaple.mj.rule.load;

public class IntegerExpression implements RuleExpression<Integer> {
	private int value;

	@Override
	public Integer getValue() {
		return value;
	}

}
