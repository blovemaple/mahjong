package com.github.blovemaple.mj.rule.load;

public class CharExpression implements RuleExpression<Character> {
	private char value;

	@Override
	public Character getValue() {
		return value;
	}

}
