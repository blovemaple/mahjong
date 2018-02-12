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
		super(true);
		this.text = text;

		setWidthBySelf(() -> strWidth(text));
		setHeightBySelf(() -> getDefaultHeight(null));
		setHeightWithParent(p -> getDefaultHeight(p));
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

	private int getDefaultHeight(CliComponent parent) {
		int width = getWidth(parent).orElse(0);

		int nextCharIndex = 0;
		int phRemain = 0;

		int rowIndex;
		for (rowIndex = 0; nextCharIndex < text.length(); rowIndex++) {
			for (int columnIndex = 0; columnIndex < width; columnIndex++) {
				if (phRemain > 0) {
					phRemain--;
					continue;
				}

				if (nextCharIndex < text.length()) {
					char crtChar = text.charAt(nextCharIndex);
					int charWidth = strWidth(String.valueOf(crtChar));
					if (charWidth <= width - columnIndex) {
						nextCharIndex++;
						if (charWidth > 1)
							phRemain = charWidth - 1;
					}
					if (charWidth > width - columnIndex && columnIndex == 0) {
						nextCharIndex++;
					}
				}
			}
		}
		return rowIndex;
	}

	@Override
	public CliCellGroup paint(int width, int height) {
		int nextCharIndex = 0;
		int phRemain = 0;

		CliCellGroup cellGroup = new CliCellGroup();
		for (int rowIndex = 0; rowIndex < height; rowIndex++) {
			for (int columnIndex = 0; columnIndex < width; columnIndex++) {
				CliCell cell = new CliCell();
				if (foreground != null)
					cell.setForeground(foreground);
				if (background != null)
					cell.setBackground(background);

				if (phRemain > 0) {
					cell.setPlaceholder(true);
					cellGroup.setCellAt(rowIndex, columnIndex, cell);
					phRemain--;
					continue;
				}

				if (nextCharIndex < text.length()) {
					char crtChar = text.charAt(nextCharIndex);
					int charWidth = strWidth(String.valueOf(crtChar));
					if (charWidth <= width - columnIndex) {
						cell.setText(crtChar);
						nextCharIndex++;
						if (charWidth > 1)
							phRemain = charWidth - 1;
					}
					if (charWidth > width - columnIndex && columnIndex == 0) {
						cell.setText(' ');
						nextCharIndex++;
					}
				}
				cellGroup.setCellAt(rowIndex, columnIndex, cell);
			}
		}
		return cellGroup;
	}

}
