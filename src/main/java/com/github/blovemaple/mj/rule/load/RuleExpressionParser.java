package com.github.blovemaple.mj.rule.load;

import static com.github.blovemaple.mj.rule.load.RuleExpressionAST.ASTNodeType.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.blovemaple.mj.rule.load.RuleExpressionAST.ASTNode;

public class RuleExpressionParser {
	public RuleExpressionAST parse(List<RuleExpressionToken> tokens) {
		ASTNode root = parseCondition(tokens, new AtomicInteger(0));
		return new RuleExpressionAST(root);
	}

	private ASTNode parseCondition(List<RuleExpressionToken> tokens, AtomicInteger position) {
		int startPosition = position.get();

		ASTNode result = null;
		RuleExpressionToken logicToken = null;
		RuleExpressionToken notToken = null;

		TOKEN_LOOP: while (position.get() < tokens.size()) {
			RuleExpressionToken crtToken = tokens.get(position.get());

			ASTNode crtRule = null;

			switch (crtToken.value()) {
			case "(":
				position.incrementAndGet();
				crtRule = parseCondition(tokens, position);
				if (position.get() >= tokens.size() || !tokens.get(position.get()).value().equals(")"))
					throw new IllegalArgumentException(
							"')' expected with '(' on position " + getOriPosition(tokens, position.get()));
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
				crtRule = parseSingleCondition(tokens, position);
			}

			if (crtRule != null) {
				if (notToken != null) {
					crtRule = new ASTNode(LOGICAL_CONDITION, notToken.value(), crtRule);
					notToken = null;
				}

				if (logicToken == null) {
					result = crtRule;
				} else if ("&&".equals(logicToken.value()) || "||".equals(logicToken.value())) {
					result = new ASTNode(LOGICAL_CONDITION, logicToken.value(), crtRule);
					logicToken = null;
				}
			}
		}

		if (logicToken != null || notToken != null)
			throw new IllegalArgumentException("Condition expression expected after '" + logicToken.value()
					+ "' on position " + (notToken != null ? notToken : logicToken).oriPosition());
		if (result == null)
			throw new IllegalArgumentException(
					"Condition expression expected on position " + getOriPosition(tokens, startPosition));

		return result;
	}

	private ASTNode parseSingleCondition(List<RuleExpressionToken> tokens, AtomicInteger position) {
		// TODO
		return null;
	}

	private int getOriPosition(List<RuleExpressionToken> tokens, int tokenPosition) {
		if (tokenPosition <= 0)
			return 0;

		if (tokenPosition >= tokens.size())
			return tokens.get(tokens.size() - 1).oriPosition() + tokens.get(tokens.size() - 1).value().length();

		return tokens.get(tokenPosition).oriPosition();
	}
}
