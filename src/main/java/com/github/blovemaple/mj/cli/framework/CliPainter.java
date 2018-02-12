package com.github.blovemaple.mj.cli.framework;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Objects;

import com.github.blovemaple.mj.cli.ansi.Csi;
import com.github.blovemaple.mj.cli.ansi.SgrParam;
import com.github.blovemaple.mj.cli.framework.component.CliRootPanel;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliPainter {

	private static CliPainter i;

	/**
	 * 返回单例。
	 * 
	 * @throws IOException
	 */
	public static synchronized CliPainter get() throws IOException {
		if (i == null)
			i = new CliPainter();
		return i;
	}

	private final PrintStream out = System.out;
	private CliCellGroup crtScreen = new CliCellGroup();

	private CliPainter() {
	}

	public synchronized void repaintScreen() throws IOException {
		CliCellGroup newScreen = CliRootPanel.get().paint(CliRootPanel.get().getWidth(null).orElse(0),
				CliRootPanel.get().getHeight(null).orElse(0));

		SgrParam[] crtParams = null;
		for (int rowIndex = 0; rowIndex < newScreen.height(); rowIndex++) {
			boolean continuous = false;
			for (int columnIndex = 0; columnIndex < newScreen.width(); columnIndex++) {
				CliCell crtCell = crtScreen.cellAt(rowIndex, columnIndex);
				CliCell newCell = newScreen.cellAt(rowIndex, columnIndex);
				if (newCell != null && !Objects.equals(crtCell, newCell)) {
					// 单元格有内容且有变化
					if (!continuous) {
						// 与上一个输出的单元格不连续，移动光标位置
						out.print(Csi.cup(rowIndex + 1, columnIndex + 1));
						continuous = true;
					}

					if (newCell.isPlaceholder())
						// 占位符跳过
						continue;

					SgrParam[] newParams = newCell.getSgrParams();
					if (!Arrays.equals(crtParams, newParams)) {
						// 与上一个输出的单元格SGR参数有变化，重新输出SGR参数
						out.print(Csi.sgr(SgrParam.RESET));
						out.print(Csi.sgr(newParams));
						crtParams = newParams;
					}

					// 输出文字
					out.print(newCell.getText());
				} else {
					continuous = false;
				}
			}
		}
		out.print(Csi.sgr(SgrParam.RESET));

		crtScreen = newScreen;
	}
}
