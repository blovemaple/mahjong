package com.github.blovemaple.mj.rule.load;

import java.util.List;

public interface FunctionExpression<T> extends RuleExpression<T> {

	public static FunctionExpression<?> newInstance(char funcName, List<RuleExpression<?>> params) {
		Class<? extends FunctionExpression<?>> exprClass = switch (funcName) {

		case 'D' -> VarDistinctFunction.class;

		default -> throw new IllegalArgumentException("Unrecognized function name: " + funcName);
		};

		try {
			if (params.isEmpty())
				return exprClass.getConstructor().newInstance(params);
			else
				return exprClass.getConstructor(List[].class).newInstance(params);
		} catch (Exception e) {
			throw new RuntimeException("Error instantiating function class " + exprClass + " with params " + params);
		}
	}

	public static class VarDistinctFunction implements FunctionExpression<Boolean>, ConditionExpression {

		@Override
		public Boolean getValue() {
			// TODO Auto-generated method stub
			return null;
		}

	}

}
