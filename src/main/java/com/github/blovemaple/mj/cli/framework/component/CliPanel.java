package com.github.blovemaple.mj.cli.framework.component;

import static java.util.Comparator.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.blovemaple.mj.cli.ansi.AnsiColor;
import com.github.blovemaple.mj.cli.framework.CliCell;
import com.github.blovemaple.mj.cli.framework.CliCellGroup;
import com.github.blovemaple.mj.cli.framework.layout.CliBound;
import com.github.blovemaple.mj.cli.framework.layout.CliFreeLayout;
import com.github.blovemaple.mj.cli.framework.layout.CliLayout;

/**
 * 命令行界面面板。一个面板可以包含0至多个子组件。尺寸默认由子组件决定，为满足子组件尺寸、位置需求的最小值。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliPanel extends CliComponent {
	private final List<CliComponent> children = new ArrayList<>();

	private CliLayout layout;
	private AnsiColor background;

	public CliPanel() {
		this(new CliFreeLayout());
	}

	public CliPanel(CliLayout layout) {
		super();
		this.layout = layout;
	}

	public final List<CliComponent> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public final void addChild(CliComponent child) {
		children.add(child);
	}

	public final void setChild(int index, CliComponent child) {
		children.set(index, child);
	}

	public final void removeChild(CliComponent child) {
		children.remove(child);
	}

	public final void clearChildren() {
		children.clear();
	}

	/**
	 * 宽度为默认情况下子组件最右边位置。
	 * 
	 * @see com.github.blovemaple.mj.cli.framework.component.CliComponent#getDefaultWidth(int)
	 */
	@Override
	public int getDefaultWidth(int height) {
		return layout.layout(this, PREF_VALUE, height).values().stream().map(CliBound::right).max(naturalOrder()).orElse(0);
	}

	/**
	 * 高度为默认情况下子组件最底部位置。
	 * 
	 * @see com.github.blovemaple.mj.cli.framework.component.CliComponent#getDefaultWidth(int)
	 */
	@Override
	public int getDefaultHeight(int width) {
		return layout.layout(this, width, PREF_VALUE).values().stream().map(CliBound::bottom).max(naturalOrder()).orElse(0);
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
		CliCell bgCell = new CliCell(this);
		bgCell.setBackground(background);
		for (int rowIndex = 0; rowIndex < height; rowIndex++) {
			for (int columnIndex = 0; columnIndex < width; columnIndex++) {
				cellGroup.setCellAt(rowIndex, columnIndex, bgCell);
			}
		}

		// 依次画所有子组件
		Map<CliComponent, CliBound> boundsOfChildren = layout.layout(this, width, height);
		for (CliComponent child : getChildren()) {
			CliBound bound = boundsOfChildren.get(child);
			if (bound == null)
				continue;

			// 按照实际大小画子组件，遍历子组件所有单元格
			CliCellGroup childCells = child.paint(bound.width(), bound.height());
			for (int rowIndex = bound.top(); rowIndex < height; rowIndex++) {
				for (int columnIndex = bound.left(); columnIndex < width; columnIndex++) {
					CliCell childCell = childCells.cellAt(rowIndex - bound.top(), columnIndex - bound.left());

					// 忽略不存在的单元格
					if (childCell == null)
						continue;

					// 如果单元格背景透明，则填充为原背景色
					CliCell oriCell = cellGroup.cellAt(rowIndex, columnIndex);
					if (childCell.getBackground() == null) {
						if (oriCell != null) {
							childCell.setBackground(oriCell.getBackground());
						}
					}

					// 添加owner
					childCell.addOwner(this);

					// 覆盖原单元格
					cellGroup.setCellAt(rowIndex, columnIndex, childCell);
				}
			}
		}

		return cellGroup;
	}

}
