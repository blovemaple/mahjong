package com.github.blovemaple.mj.cli.framework;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

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
		CliCellGroup newScreen = CliRootPanel.get().paint(CliRootPanel.get().getWidth(),
				CliRootPanel.get().getHeight());

		SgrParam[] crtParams = null;
		for (int rowIndex = 0; rowIndex < newScreen.height(); rowIndex++) {
			boolean continuous = false;
			int remainingSkips = 0;
			for (int columnIndex = 0; columnIndex < newScreen.width(); columnIndex++) {
				CliCell crtCell = crtScreen.cellAt(rowIndex, columnIndex);
				CliCell newCell = newScreen.cellAt(rowIndex, columnIndex);

				if (newCell == null || newCell.equalByContent(crtCell)) {
					// 新单元格为null（应该不会出现，除非控制台不是方的）或者内容无变化，不重复输出，下一个单元格不连续
					continuous = false;
					continue;
				}

				if (remainingSkips > 0) {
					// 由于前面的单元格大于1个宽度，需要跳过此单元格
					remainingSkips--;
					continuous = false;
					continue;
				}

				// 单元格需要输出

				if (!continuous) {
					// 与上一个输出的单元格不连续，移动光标位置
					out.print(Csi.cup(rowIndex + 1, columnIndex + 1));
					continuous = true;
				}

				SgrParam[] newParams = newCell.getSgrParams();
				if (!Arrays.equals(crtParams, newParams)) {
					// 与上一个输出的单元格SGR参数有变化，重新输出SGR参数
					out.print(Csi.sgr(SgrParam.RESET));
					out.print(Csi.sgr(newParams));
					crtParams = newParams;
				}

				// 输出文字
				char text;
				if (newCell.getTextWidth() <= newScreen.width() - columnIndex) {
					text = newCell.getText();
				} else {
					// 文字宽度大于当前行剩余单元格数，则改为输出空格
					text = ' ';
				}
				out.print(text);

				// 如果文字多于1个宽度，则当前行后面要跳过n-1个单元格
				if (newCell.getTextWidth() > 1)
					remainingSkips = newCell.getTextWidth() - 1;
			}
		}
		out.print(Csi.sgr(SgrParam.RESET));

		crtScreen = newScreen;
	}
}
