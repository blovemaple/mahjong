package com.github.blovemaple.mj.cli.framework.layout;

import java.util.function.IntBinaryOperator;

/**
 * 尺寸及位置设置项。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public enum CliLayoutSettingType {
	WIDTH("RIGHT", "LEFT", (right, left) -> right - left + 1), //
	HEIGHT("BOTTOM", "TOP", (bottom, top) -> bottom - top + 1), //
	LEFT("RIGHT", "WIDTH", (right, width) -> right - width + 1), //
	RIGHT("LEFT", "WIDTH", (left, width) -> left + width - 1), //
	TOP("BOTTOM", "HEIGHT", (bottom, height) -> bottom - height + 1), //
	BOTTOM("TOP", "HEIGHT", (top, height) -> top + height - 1), //
	;

	private final String calcParamType1;
	private final String calcParamType2;
	private final IntBinaryOperator calculator;

	private CliLayoutSettingType(String calcParamType1, String calcParamType2, IntBinaryOperator calculator) {
		this.calcParamType1 = calcParamType1;
		this.calcParamType2 = calcParamType2;
		this.calculator = calculator;
	}

	public CliLayoutSettingType getCalcParamType1() {
		return CliLayoutSettingType.valueOf(calcParamType1);
	}

	public CliLayoutSettingType getCalcParamType2() {
		return CliLayoutSettingType.valueOf(calcParamType2);
	}

	public IntBinaryOperator getCalculator() {
		return calculator;
	}

}
