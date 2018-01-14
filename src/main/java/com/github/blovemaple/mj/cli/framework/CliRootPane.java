package com.github.blovemaple.mj.cli.framework;

import static com.github.blovemaple.mj.utils.MyUtils.*;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntSupplier;

import com.github.blovemaple.mj.cli.ansi.AnsiColor;
import com.github.blovemaple.mj.cli.ansi.Csi;
import com.github.blovemaple.mj.cli.ansi.SgrParam;

import jline.console.ConsoleReader;

/**
 * 命令行界面根面板。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliRootPane extends CliPanel {
	public static void main(String[] args) throws IOException {
		CliRootPane rootPane = CliRootPane.get();
		rootPane.repaint();
	}

	private static CliRootPane i;

	/**
	 * 返回单例。
	 * 
	 * @throws IOException
	 */
	public static synchronized CliRootPane get() throws IOException {
		if (i == null)
			i = new CliRootPane();
		return i;
	}

	private final ConsoleReader console;
	private final PrintStream out;

	private CliRootPane() throws IOException {
		console = new ConsoleReader(null, System.in, System.out, null);
		out = System.out;
		setBackground(AnsiColor.DEFAULT);
	}

	private List<List<CliCell>> lastCells = List.of();

	@Override
	protected synchronized void submitPaint(boolean sizePosChanged) {
		List<List<CliCell>> crtCells = getCells();
		validate(crtCells);
		paint(crtCells);
		lastCells = crtCells;
	}

	private void validate(List<List<CliCell>> cells) {
		for (List<CliCell> row : cells) {
			int phExpected = 0;
			for (CliCell cell : row) {
				// 汉字后必须跟占位符单元格
				if (phExpected > 0) {
					if (cell != CliCell.CHAR_PLACEHOLDER)
						throw new RuntimeException("Placeholder is expected.");
					phExpected--;
				}
				phExpected = strWidth(String.valueOf(cell.getText())) - 1;

				// 所有单元格必须有前景色、背景色
				if (cell.getForeground() == null || cell.getBackground() == null)
					throw new RuntimeException("Foreground and background are both expected.");
			}
		}
	}

	private void paint(List<List<CliCell>> crtCells) {
		SgrParam[] crtParams = null;
		int rowIndex = 0;
		for (List<CliCell> row : crtCells) {
			rowIndex++;
			boolean continuous = false;
			int columnIndex = 0;
			for (CliCell cell : row) {
				columnIndex++;
				if (changed(rowIndex, columnIndex, cell)) {
					// 单元格有变化
					if (!continuous) {
						// 与上一个输出的单元格不连续，移动光标位置
						out.print(Csi.cup(columnIndex, rowIndex));
						continuous = true;
					}

					if (cell == CliCell.CHAR_PLACEHOLDER)
						// 占位符跳过
						continue;

					SgrParam[] newParams = cell.getSgrParams();
					if (!Arrays.equals(crtParams, newParams)) {
						// 与上一个输出的单元格SGR参数有变化，重新输出SGR参数
						out.print(Csi.sgr(SgrParam.RESET));
						out.print(Csi.sgr(newParams));
						crtParams = newParams;
					}

					// 输出文字
					out.print(cell.getText());
				} else {
					continuous = false;
				}
			}
		}
	}

	private boolean changed(int rowIndex, int columnIndex, CliCell cell) {
		try {
			CliCell oldCell = lastCells.get(rowIndex).get(columnIndex);
			return !Objects.equals(oldCell, cell);
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}

	@Override
	public OptionalInt getWidth(CliComponent parent) {
		return OptionalInt.of(console.getTerminal().getWidth());
	}

	@Override
	public OptionalInt getHeight(CliComponent parent) {
		return OptionalInt.of(console.getTerminal().getHeight());
	}

	@Override
	public OptionalInt getLeft(CliComponent parent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public OptionalInt getRight(CliComponent parent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public OptionalInt getTop(CliComponent parent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public OptionalInt getBottom(CliComponent parent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setWidthBySelf(IntSupplier widthBySelf) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setHeightBySelf(IntSupplier heightBySelf) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLeftBySelf(IntSupplier leftBySelf) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRightBySelf(IntSupplier rightBySelf) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTopBySelf(IntSupplier topBySelf) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBottomBySelf(IntSupplier bottomBySelf) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setWidthWithParent(Function<CliComponent, Integer> widthWithParent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setHeightWithParent(Function<CliComponent, Integer> heightWithParent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLeftWithParent(Function<CliComponent, Integer> leftWithParent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRightWithParent(Function<CliComponent, Integer> rightWithParent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTopWithParent(Function<CliComponent, Integer> topWithParent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBottomWithParent(Function<CliComponent, Integer> bottomWithParent) {
		throw new UnsupportedOperationException();
	}

}
