package com.github.blovemaple.mj.rule.load;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RuleExpressionParserOriginal {
	private RuleExpressionTokenizer tokenizer = new RuleExpressionTokenizer();

	public ConditionExpression parse(String exprStr) {
		List<RuleExpressionToken> tokens = tokenizer.tokenize(exprStr);
		ConditionExpression expression = parse(tokens, null);
		return expression;
	}

	private ConditionExpression parse(List<RuleExpressionToken> tokens, AtomicInteger position) {
		if (position == null)
			position = new AtomicInteger(0);

		int startPosition = position.get();

		ConditionExpression result = null;
		RuleExpressionToken logicToken = null;
		RuleExpressionToken notToken = null;

		TOKEN_LOOP: while (position.get() < tokens.size()) {
			RuleExpressionToken crtToken = tokens.get(position.get());

			ConditionExpression crtRule = null;

			switch (crtToken.value()) {
			case "(":
				position.incrementAndGet();
				crtRule = parse(tokens, position);
				// XXX incorrect position in exception messages
				if (position.get() >= tokens.size() || !tokens.get(position.get()).value().equals(")"))
					throw new IllegalArgumentException("')' expected with '(' on position " + crtToken.oriPosition());
				position.incrementAndGet();
				break;
			case ")":
				break TOKEN_LOOP;
			case "&&":
			case "||":
				if (result == null)
					throw new IllegalArgumentException("Boolean expression expected before '" + crtToken.value()
							+ "' on position " + crtToken.oriPosition());
				if (logicToken != null)
					throw new IllegalArgumentException(
							"Unexpected '" + crtToken.value() + "' on position " + crtToken.oriPosition());
				logicToken = crtToken;
				position.incrementAndGet();
				break;
			case "!!":
				if (result != null && logicToken == null)
					throw new IllegalArgumentException(
							"Logic token(and/or) expected on position " + crtToken.oriPosition());
				notToken = (notToken == null ? crtToken : null);
				position.incrementAndGet();
				break;
			default:
				if (result != null && logicToken == null)
					throw new IllegalArgumentException(
							"Logic token(and/or) expected on position " + crtToken.oriPosition());
				crtRule = parseSingleConditionExpression(tokens, position);
			}

			if (crtRule != null) {
				if (notToken != null) {
					crtRule = new NotExpression(crtRule);
					notToken = null;
				}

				if (logicToken == null) {
					result = crtRule;
				} else if ("&&".equals(logicToken.value())) {
					result = new AndExpression(result, crtRule);
					logicToken = null;
				} else if ("||".equals(logicToken.value())) {
					result = new OrExpression(result, crtRule);
					logicToken = null;
				}
			}
		}

		if (logicToken != null || notToken != null)
			throw new IllegalArgumentException("Condition expression expected after '" + logicToken.value()
					+ "' on position " + (notToken != null ? notToken : logicToken).oriPosition());
		if (result == null)
			// XXX incorrect position in exception messages
			throw new IllegalArgumentException("Condition expression expected on position " + startPosition);

		return result;
	}

	private ConditionExpression parseSingleConditionExpression(List<RuleExpressionToken> tokens,
			AtomicInteger position) {
		// TODO
		return null;
	}
}
