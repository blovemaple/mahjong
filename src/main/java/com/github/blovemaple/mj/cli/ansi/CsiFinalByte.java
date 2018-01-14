package com.github.blovemaple.mj.cli.ansi;

/**
 * CSI序列最后一个字节。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public enum CsiFinalByte {

	/**
	 * 光标位置（Cursor Position）
	 */
	CUP('H'),
	/**
	 * 擦除显示（Erase in Display）
	 */
	ED('J'),
	/**
	 * 选择图形再现（Select Graphic Rendition）
	 */
	SGR('m');

	private final char code;

	private CsiFinalByte(char code) {
		this.code = code;
	}

	public char get() {
		return code;
	}

}
