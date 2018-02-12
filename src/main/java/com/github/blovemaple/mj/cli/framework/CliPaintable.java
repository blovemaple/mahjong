package com.github.blovemaple.mj.cli.framework;

import java.io.IOException;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public abstract class CliPaintable {

	/**
	 * 重画。
	 * 
	 * @throws IOException
	 */
	public final void repaint() throws IOException {
		// 暂时实现为从root开始全部重画，如果有性能问题再改
		CliPainter.get().repaintScreen();
	}

	/**
	 * 画自己。
	 */
	public abstract CliCellGroup paint(int width, int height);
}
