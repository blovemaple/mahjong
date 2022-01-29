package com.github.blovemaple.mj.rule.load;

import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.object.TileUnitType;

public class MatchingExpression implements ConditionExpression {
	private RuleExpression<? extends TileUnitType> unitExpr1;
	private RuleExpression<? extends Boolean> unitExpr2;
	private RuleExpression<? extends TileType> tileTypeExpr1;
	private RuleExpression<? extends Boolean> tileTypeExpr2;

	@Override
	public Boolean getValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
