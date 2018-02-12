package com.github.blovemaple.mj.cli.framework;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliCellGroup {
	private final List<List<CliCell>> cells;

	private int width, height;

	public CliCellGroup() {
		cells = new ArrayList<List<CliCell>>();
		width = 0;
		height = 0;
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}

	public CliCell cellAt(int rowIndex, int columnIndex) {
		try {
			return cells.get(rowIndex).get(columnIndex);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	public void setCellAt(int rowIndex, int columnIndex, CliCell cell) {
		if (rowIndex < 0)
			throw new IndexOutOfBoundsException("rowIndex: " + rowIndex);
		if (columnIndex < 0)
			throw new IndexOutOfBoundsException("columnIndex: " + columnIndex);
		if (cell == null)
			return;

		while (rowIndex >= cells.size())
			cells.add(new ArrayList<CliCell>());
		this.height = cells.size();
		List<CliCell> row = cells.get(rowIndex);

		while (columnIndex >= row.size())
			row.add(null);
		if (row.size() > this.width)
			this.width = row.size();
		row.set(columnIndex, cell);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
