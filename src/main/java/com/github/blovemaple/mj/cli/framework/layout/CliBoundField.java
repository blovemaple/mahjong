package com.github.blovemaple.mj.cli.framework.layout;

/**
 * 尺寸及位置设置项。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public enum CliBoundField {
	WIDTH(true), //
	HEIGHT(true), //
	LEFT(false), //
	RIGHT(false), //
	TOP(false), //
	BOTTOM(false), //
	;

	private final boolean isSizeField;

	private CliBoundField(boolean isSizeField) {
		this.isSizeField = isSizeField;
	}

	public boolean isSizeField() {
		return isSizeField;
	}

}
