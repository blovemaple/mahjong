package com.github.blovemaple.mj.cli.ansi;

import static com.github.blovemaple.mj.cli.ansi.SgrParam.*;

/**
 * ANSI颜色。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public enum AnsiColor {
	DEFAULT(FG_DEFAULT, BG_DEFAULT), //
	BLACK(FG_BLACK, BG_BLACK), //
	RED(FG_RED, BG_RED), //
	GREEN(FG_GREEN, BG_GREEN), //
	YELLOW(FG_YELLOW, BG_YELLOW), //
	BLUE(FG_BLUE, BG_BLUE), //
	MAGENTA(FG_MAGENTA, BG_MAGENTA), //
	CYAN(FG_CYAN, BG_CYAN), //
	WHITE(FG_WHITE, BG_WHITE);

	private final SgrParam fgParam, bgParam;

	private AnsiColor(SgrParam fgParam, SgrParam bgParam) {
		this.fgParam = fgParam;
		this.bgParam = bgParam;
	}

	public SgrParam getFgParam() {
		return fgParam;
	}

	public SgrParam getBgParam() {
		return bgParam;
	}

}
