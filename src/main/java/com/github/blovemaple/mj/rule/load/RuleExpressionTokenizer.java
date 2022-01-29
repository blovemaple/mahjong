package com.github.blovemaple.mj.rule.load;

import static com.github.blovemaple.mj.rule.load.RuleExpressionToken.RuleExpressionTokenType.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RuleExpressionTokenizer {

	private static final Pattern STRING_CHARS = Pattern.compile("[A-Za-z0-9_]");

	public List<RuleExpressionToken> tokenize(String exprStr) {
		List<RuleExpressionToken> result = new ArrayList<>();

		for (int i = 0; i < exprStr.length(); i++) {
			char crtChar = exprStr.charAt(i);

			if (crtChar >= 'A' && crtChar <= 'Z') {
				result.add(new RuleExpressionToken(CAPITAL_LETTER, String.valueOf(crtChar), i));

			} else if (crtChar >= 'a' && crtChar <= 'z') {
				result.add(new RuleExpressionToken(SMALL_LETTER, String.valueOf(crtChar), i));

			} else if (crtChar >= '0' && crtChar <= '9') {
				result.add(new RuleExpressionToken(DIGIT, String.valueOf(crtChar), i));

			} else if (crtChar == '_') {
				int oriPosition = i;
				StringBuilder crtValue = new StringBuilder();
				while (i < exprStr.length() && (STRING_CHARS.matcher(String.valueOf(crtChar)).matches())) {
					crtValue.append(exprStr.charAt(i));
					i++;
				}
				result.add(new RuleExpressionToken(STRING, crtValue.toString(), oriPosition));
				i--;

			} else if (crtChar == '&' || crtChar == '|' || crtChar == '!') {
				if (i + 1 < exprStr.length() && exprStr.charAt(i + 1) == crtChar) {
					result.add(new RuleExpressionToken(SYMBOL, exprStr.substring(i, i + 2), i));
					i++;
				} else
					result.add(new RuleExpressionToken(SYMBOL, String.valueOf(crtChar), i));

			} else if (!Character.isWhitespace(crtChar)) {
				result.add(new RuleExpressionToken(SYMBOL, String.valueOf(crtChar), i));

			}
		}

		return result;
	}
}
