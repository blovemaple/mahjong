package com.github.blovemaple.mj.cli.framework.component;

import static com.github.blovemaple.mj.utils.MyUtils.*;

import com.github.blovemaple.mj.cli.ansi.AnsiColor;
import com.github.blovemaple.mj.cli.framework.CliCell;
import com.github.blovemaple.mj.cli.framework.CliCellGroup;

/**
 * 文字标签。默认宽度与其中的文字宽度一致，默认高度为1。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliLabel extends CliComponent {
	private String text;
	private AnsiColor foreground, background;

	public CliLabel(String text) {
		super();
		this.text = text;
	}

	public CliLabel() {
		this("");
	}

	public String getText() {
		return text;
	}

	public AnsiColor getForeground() {
		return foreground;
	}

	public void setForeground(AnsiColor foreground) {
		this.foreground = foreground;
	}

	public AnsiColor getBackground() {
		return background;
	}

	public void setBackground(AnsiColor background) {
		this.background = background;
	}

	@Override
	public int getDefaultWidth(int height) {
		return strWidth(text);
	}

	@Override
	public int getDefaultHeight(int width) {
		return paint(width, PREF_VALUE).height();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param height
	 *            -1表示使用默认高度
	 * @see com.github.blovemaple.mj.cli.framework.component.CliComponent#paint(int,
	 *      int)
	 */
	@Override
	public CliCellGroup paint(int width, int height) {
		int nextCharIndex = 0;
		int remainingSkips = 0;

		CliCellGroup cellGroup = new CliCellGroup();
		for (int rowIndex = 0; height >= 0 ? rowIndex < height
				: nextCharIndex < text.length() || remainingSkips > 0; rowIndex++) {
			for (int columnIndex = 0; width >= 0 ? columnIndex < width
					: nextCharIndex < text.length() || remainingSkips > 0; columnIndex++) {
				CliCell cell = new CliCell(this);
				cellGroup.setCellAt(rowIndex, columnIndex, cell);

				// 设置前景、背景色
				if (foreground != null)
					cell.setForeground(foreground);
				if (background != null)
					cell.setBackground(background);

				if (nextCharIndex >= text.length())
					// 已经超过text末尾，没有字符可输出
					continue;

				if (remainingSkips > 0) {
					// 还有剩余应跳过的单元格，不输出字符
					remainingSkips--;
					continue;
				}

				char crtChar = getText().charAt(nextCharIndex);
				int charWidth = strWidth(String.valueOf(crtChar));

				if (charWidth > width - columnIndex) {
					// 字符宽度超出当前行剩余单元格数，不输出字符
					if (charWidth > width)
						// 字符宽度超出行宽，跳过此字符
						nextCharIndex++;
					continue;
				}

				// 输出字符
				cell.setText(crtChar);
				nextCharIndex++;

				if (charWidth > 1)
					// 字符宽度大于1，在当前行跳过其后的n-1个单元格
					remainingSkips = charWidth - 1;
			}
		}
		return cellGroup;
	}

}
