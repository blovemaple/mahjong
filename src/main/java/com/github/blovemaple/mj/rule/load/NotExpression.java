package com.github.blovemaple.mj.rule.load;

public class NotExpression implements ConditionExpression {
	private RuleExpression<Boolean> expr;

	public NotExpression(RuleExpression<Boolean> expr) {
		this.expr = expr;
	}

	@Override
	public Boolean getValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
