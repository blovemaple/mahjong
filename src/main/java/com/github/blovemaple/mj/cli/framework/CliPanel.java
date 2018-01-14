package com.github.blovemaple.mj.cli.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import com.github.blovemaple.mj.cli.ansi.AnsiColor;

/**
 * 命令行界面面板。一个面板可以包含0至多个子组件。尺寸默认由子组件决定，为满足子组件尺寸、位置需求的最小值。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliPanel extends CliComponent {

	private AnsiColor background;

	public CliPanel() {
		super(false);
	}

	@Override
	protected OptionalInt getDefaultWidth() {
		return getChildren().stream().map(child -> child.getRight(null)).filter(OptionalInt::isPresent)
				.mapToInt(OptionalInt::getAsInt).map(i -> i + 1).max();
	}

	@Override
	protected OptionalInt getDefaultHeight() {
		return getChildren().stream().map(child -> child.getRight(null)).filter(OptionalInt::isPresent)
				.mapToInt(OptionalInt::getAsInt).map(i -> i + 1).max();
	}

	public AnsiColor getBackground() {
		return background;
	}

	public void setBackground(AnsiColor background) {
		this.background = background;
	}

	@Override
	protected List<List<CliCell>> paintSelf() {
		int height = getHeight(getParent()).orElse(0);
		int width = getWidth(getParent()).orElse(0);

		List<List<CliCell>> cells = new ArrayList<>();
		for (int rowIndex = 0; rowIndex < height; rowIndex++) {
			List<CliCell> row = new ArrayList<>();
			cells.add(row);
			for (int columnIndex = 0; columnIndex < width; columnIndex++) {
				CliCell cell = new CliCell();
				cell.setBackground(background);
				row.add(cell);
			}
		}
		return cells;
	}

}
