package com.github.blovemaple.mj.rule.load;

import org.springframework.core.GenericTypeResolver;

public interface RuleExpression<T> {
	T getValue();

	@SuppressWarnings("unchecked")
	default Class<? extends T> getType() {
		return (Class<? extends T>) GenericTypeResolver.resolveTypeArgument(this.getClass(), RuleExpression.class);
	}

}
