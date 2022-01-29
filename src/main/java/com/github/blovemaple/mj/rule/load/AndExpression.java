package com.github.blovemaple.mj.rule.load;

public class AndExpression implements ConditionExpression {
	private RuleExpression<Boolean> expr1, expr2;

	public AndExpression(RuleExpression<Boolean> expr1, RuleExpression<Boolean> expr2) {
		this.expr1 = expr1;
		this.expr2 = expr2;
	}

	@Override
	public Boolean getValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
