package com.github.blovemaple.mj.cli.framework.layout;

import static com.github.blovemaple.mj.cli.framework.layout.CliLayoutSettingType.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliLayoutSetting {
	private Map<CliLayoutSettingType, Integer> values = new EnumMap<>(CliLayoutSettingType.class);

	public CliLayoutSetting() {
	}

	public void validate() {
		// TODO
	}

	public void set(CliLayoutSettingType type, Integer value) {
		Objects.requireNonNull(value);

		// 合法性校验
		Integer paramValue1 = values.get(type.getCalcParamType1());
		Integer paramValue2 = values.get(type.getCalcParamType2());
		if (paramValue1 != null && paramValue2 != null) {
			Integer expectedValue = type.getCalculator().applyAsInt(paramValue1, paramValue2);
			if (!value.equals(expectedValue))
				throw new IllegalArgumentException(
						"Illegal " + type + " value " + value + " with expected " + expectedValue);
		}

		// 保存值
		values.put(type, value);

		// 自动填充可计算的值
		for (CliLayoutSettingType aType : CliLayoutSettingType.values()) {
			if (aType.getCalcParamType1() == type || aType.getCalcParamType2() == type) {
				Integer value1 = values.get(aType.getCalcParamType1());
				Integer value2 = values.get(aType.getCalcParamType2());
				if (value1 != null && value2 != null) {
					Integer aTypeValue = aType.getCalculator().applyAsInt(value1, value2);
					values.put(aType, aTypeValue);
				}
			}
		}
	}

	public Integer get(CliLayoutSettingType type) {
		return values.get(type);
	}

	public void moveRight(int distance) {
		Integer left = values.get(LEFT);
		if (left != null)
			values.put(LEFT, left + distance);

		Integer right = values.get(RIGHT);
		if (right != null)
			values.put(RIGHT, right + distance);
	}

	public void moveDown(int distance) {
		Integer top = values.get(TOP);
		if (top != null)
			values.put(TOP, top + distance);

		Integer bottom = values.get(BOTTOM);
		if (bottom != null)
			values.put(BOTTOM, bottom + distance);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
