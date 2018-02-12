package com.github.blovemaple.mj.cli.framework.component;

import static com.github.blovemaple.mj.cli.framework.layout.CliLayoutSettingType.*;
import static com.github.blovemaple.mj.utils.MyUtils.*;
import static java.util.Comparator.*;

import java.util.Map;

import com.github.blovemaple.mj.cli.ansi.AnsiColor;
import com.github.blovemaple.mj.cli.framework.CliCell;
import com.github.blovemaple.mj.cli.framework.CliCellGroup;
import com.github.blovemaple.mj.cli.framework.layout.CliFreeLayout;
import com.github.blovemaple.mj.cli.framework.layout.CliLayout;
import com.github.blovemaple.mj.cli.framework.layout.CliLayoutSetting;

/**
 * 命令行界面面板。一个面板可以包含0至多个子组件。尺寸默认由子组件决定，为满足子组件尺寸、位置需求的最小值。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliPanel extends CliComponent {
	private CliLayout layout;
	private AnsiColor background;

	public CliPanel() {
		this(new CliFreeLayout());
	}

	public CliPanel(CliLayout layout) {
		super(false);
		this.layout = layout;
		setDefaultWidthAndHeight();
	}

	protected void setDefaultWidthAndHeight() {
		setWidthBySelf(() -> layout.layout(this).values().stream().map(setting -> setting.get(RIGHT) + 1)
				.max(naturalOrder()).orElse(0));
		setHeightBySelf(() -> layout.layout(this).values().stream().map(setting -> setting.get(BOTTOM) + 1)
				.max(naturalOrder()).orElse(0));
	}

	public CliLayout getLayout() {
		return layout;
	}

	public void setLayout(CliLayout layout) {
		this.layout = layout;
	}

	public AnsiColor getBackground() {
		return background;
	}

	public void setBackground(AnsiColor background) {
		this.background = background;
	}

	@Override
	public CliCellGroup paint(int width, int height) {
		CliCellGroup cellGroup = new CliCellGroup();

		// 填充背景色
		CliCell bgCell = new CliCell();
		bgCell.setBackground(background);
		for (int rowIndex = 0; rowIndex < height; rowIndex++) {
			for (int columnIndex = 0; columnIndex < width; columnIndex++) {
				cellGroup.setCellAt(rowIndex, columnIndex, bgCell);
			}
		}

		// 依次画所有子组件
		Map<CliComponent, CliLayoutSetting> layouts = layout.layout(this);
		for (CliComponent child : getChildren()) {
			CliLayoutSetting layoutSetting = layouts.get(child);
			if (layoutSetting == null)
				continue;

			CliCellGroup childCells = child.paint(layoutSetting.get(WIDTH), layoutSetting.get(HEIGHT));
			int childTop = layoutSetting.get(TOP);
			int childLeft = layoutSetting.get(LEFT);
			for (int rowIndex = childTop; rowIndex < height; rowIndex++) {
				for (int columnIndex = childLeft; columnIndex < width; columnIndex++) {
					CliCell childCell = childCells.cellAt(rowIndex - childTop, columnIndex - childLeft);
					if (childCell == null)
						continue;

					CliCell oriCell = cellGroup.cellAt(rowIndex, columnIndex);
					if (childCell != null && childCell.getBackground() == null) {
						if (oriCell != null) {
							childCell = (CliCell) childCell.clone();
							childCell.setBackground(oriCell.getBackground());
						}
					}
					cellGroup.setCellAt(rowIndex, columnIndex, childCell);

					if (oriCell != null) {
						// 如果以前该单元格的字符长度大于1，则取消其后的placeholder
						int oriCharWidth = strWidth(String.valueOf(oriCell.getText()));
						if (oriCharWidth > 1) {
							for (int phOffset = 1; phOffset < oriCharWidth; phOffset++) {
								CliCell phCell = cellGroup.cellAt(rowIndex, columnIndex + phOffset);
								if (phCell != null)
									phCell.setPlaceholder(false);
							}
						}
					}
				}
			}
		}

		return cellGroup;
	}

}
