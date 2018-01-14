package com.github.blovemaple.mj.cli.framework;

import static com.github.blovemaple.mj.utils.MyUtils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

/**
 * 文字标签。默认宽度与其中的文字宽度一致，默认高度为1。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliLabel extends CliComponent {

	private String text;

	public CliLabel(String text) {
		super(true);
		this.text = text;
	}

	public CliLabel() {
		this(null);
	}

	public String getText() {
		return text;
	}

	@Override
	protected OptionalInt getDefaultWidth() {
		return OptionalInt.of(text == null ? 0 : strWidth(text));
	}

	@Override
	protected OptionalInt getDefaultHeight() {
		return OptionalInt.of(1);
	}

	@Override
	protected List<List<CliCell>> paintSelf() {
		int height = getHeight(getParent()).orElse(0);
		int width = getWidth(getParent()).orElse(0);

		int nextCharIndex = 0;
		int phRemain = 0;

		List<List<CliCell>> cells = new ArrayList<>();
		for (int rowIndex = 0; rowIndex < height; rowIndex++) {
			List<CliCell> row = new ArrayList<>();
			cells.add(row);
			for (int columnIndex = 0; columnIndex < width; columnIndex++) {
				if (phRemain > 0) {
					row.add(CliCell.CHAR_PLACEHOLDER);
					phRemain--;
				}

				CliCell cell = new CliCell();
				if (nextCharIndex < text.length()) {
					char crtChar = text.charAt(nextCharIndex);
					int charWidth = strWidth(String.valueOf(crtChar));
					if (charWidth <= width - columnIndex) {
						cell.setText(crtChar);
						nextCharIndex++;
						if (charWidth > 1)
							phRemain = charWidth - 1;
					}
				}
				row.add(cell);
			}
		}
		return cells;
	}

}
