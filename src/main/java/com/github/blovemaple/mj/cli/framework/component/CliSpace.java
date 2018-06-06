package com.github.blovemaple.mj.cli.framework.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.blovemaple.mj.cli.framework.CliCell;
import com.github.blovemaple.mj.cli.framework.CliCellGroup;

/**
 * 命令行界面空白占位区域。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliSpace extends CliComponent {
	public static Map<List<Integer>, CliSpace> INSTANCES = new HashMap<>();

	public static CliSpace of(int width, int height) {
		CliSpace i = INSTANCES.get(List.of(width, height));
		if (i == null)
			INSTANCES.put(List.of(width, height), i = new CliSpace(width, height));
		return i;
	}

	private final int width, height;

	private CliSpace(int width, int height) {
		super();
		this.width = width;
		this.height = height;
		setMinWidth(()->getWidth());
		setMaxWidth(()->getWidth());
		setMinHeight(()->getHeight());
		setMaxHeight(()->getHeight());
	}

	@Override
	public int getDefaultWidth(int height) {
		return getWidth();
	}

	@Override
	public int getDefaultHeight(int width) {
		return getHeight();
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public CliCellGroup paint(int width, int height) {
		CliCellGroup group = new CliCellGroup();
		CliCell cell = new CliCell(this);
		for (int w = 0; w < width; w++)
			for (int h = 0; h < height; h++)
				group.setCellAt(h, w, cell);
		return group;
	}

}
