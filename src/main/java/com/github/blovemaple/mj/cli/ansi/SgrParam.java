package com.github.blovemaple.mj.cli.ansi;

/**
 * SGR参数。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public enum SgrParam {
	/**
	 * 重置/正常
	 */
	RESET(0),
	/**
	 * 粗体或增加强度
	 */
	BOLD(1),
	/**
	 * 弱化（降低强度）
	 */
	FAINT(2),
	/**
	 * 斜体
	 */
	ITALIC(3),
	/**
	 * 下划线
	 */
	UNDERLINE(4),
	/**
	 * 缓慢闪烁
	 */
	SLOW_BLINK(5),
	/**
	 * 快速闪烁
	 */
	RAPID_BLINK(6),
	/**
	 * 反显
	 */
	REVERSE(7),
	/**
	 * 隐藏
	 */
	CONCEAL(8),
	/**
	 * 划除
	 */
	CROSSED_OUT(9),
	// 前景色
	FG_BLACK(30), FG_RED(31), FG_GREEN(32), FG_YELLOW(33), FG_BLUE(34), FG_MAGENTA(35), FG_CYAN(36), FG_WHITE(
			37), FG_DEFAULT(39),
	// 背景色
	BG_BLACK(40), BG_RED(41), BG_GREEN(42), BG_YELLOW(44), BG_BLUE(44), BG_MAGENTA(45), BG_CYAN(46), BG_WHITE(
			47), BG_DEFAULT(49)

	;

	private final int code;

	private SgrParam(int param) {
		this.code = param;
	}

	public int get() {
		return code;
	}

}
