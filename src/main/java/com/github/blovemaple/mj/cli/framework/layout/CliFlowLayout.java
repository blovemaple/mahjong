package com.github.blovemaple.mj.cli.framework.layout;

import static com.github.blovemaple.mj.cli.framework.layout.CliLayoutSettingType.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.github.blovemaple.mj.cli.framework.component.CliComponent;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliFlowLayout implements CliLayout {

	private CliPanelFlowLayoutDirection direction;
	private int lineSize;
	private int rowGap, columnGap;

	public enum CliPanelFlowLayoutDirection {
		TOP_LEFT_HORIZONTAL(Direction.RIGHT, Direction.DOWN), //
		TOP_LEFT_VERTICAL(Direction.DOWN, Direction.RIGHT), //
		TOP_RIGHT_HORIZONTAL(Direction.LEFT, Direction.DOWN), //
		TOP_RIGHT_VERTICAL(Direction.DOWN, Direction.LEFT), //
		BOTTOM_LEFT_HORIZONTAL(Direction.RIGHT, Direction.UP), //
		BOTTOM_LEFT_VERTICAL(Direction.UP, Direction.RIGHT), //
		BOTTOM_RIGHT_HORIZONTAL(Direction.LEFT, Direction.UP), //
		BOTTOM_RIGHT_VERTICAL(Direction.UP, Direction.LEFT), //
		;

		private final Direction firstDirection, secondDirection;

		private CliPanelFlowLayoutDirection(Direction firstDirection, Direction secondDirection) {
			this.firstDirection = firstDirection;
			this.secondDirection = secondDirection;
		}

		public Direction firstDirection() {
			return firstDirection;
		}

		public Direction secondDirection() {
			return secondDirection;
		}

		public CliLayoutSettingType logiWidthType() {
			return firstDirection().logiLengthType();
		}

		public CliLayoutSettingType logiHeightType() {
			return secondDirection().logiLengthType();
		}

		public CliLayoutSettingType logiLeftType() {
			return firstDirection().logiStartType();
		}

		public CliLayoutSettingType logiTopType() {
			return secondDirection().logiStartType();
		}

	}

	private enum Direction {
		LEFT(WIDTH, CliLayoutSettingType.RIGHT, true), //
		RIGHT(WIDTH, CliLayoutSettingType.LEFT, false), //
		UP(HEIGHT, CliLayoutSettingType.BOTTOM, true), //
		DOWN(HEIGHT, CliLayoutSettingType.TOP, false), //
		;

		private final CliLayoutSettingType logiLengthType, logiStartType;
		private final boolean reversed;

		private Direction(CliLayoutSettingType logiLengthType, CliLayoutSettingType logiStartType, boolean reversed) {
			this.logiLengthType = logiLengthType;
			this.logiStartType = logiStartType;
			this.reversed = reversed;
		}

		public CliLayoutSettingType logiLengthType() {
			return logiLengthType;
		}

		public CliLayoutSettingType logiStartType() {
			return logiStartType;
		}

		public boolean isReversed() {
			return reversed;
		}

	}

	public CliFlowLayout() {
		this.direction = CliPanelFlowLayoutDirection.TOP_LEFT_HORIZONTAL;
	}

	public CliFlowLayout(CliPanelFlowLayoutDirection direction) {
		this.direction = direction;
	}

	public CliPanelFlowLayoutDirection getDirection() {
		return direction;
	}

	public void setDirection(CliPanelFlowLayoutDirection direction) {
		Objects.requireNonNull(direction);
		this.direction = direction;
	}

	public int getLineSize() {
		return lineSize;
	}

	public void setLineSize(int lineSize) {
		this.lineSize = lineSize;
	}

	public int getRowGap() {
		return rowGap;
	}

	public void setRowGap(int rowGap) {
		if (columnGap < 0)
			throw new IllegalArgumentException("Illegal row gap: " + rowGap);
		this.rowGap = rowGap;
	}

	public int getColumnGap() {
		return columnGap;
	}

	public void setColumnGap(int columnGap) {
		if (columnGap < 0)
			throw new IllegalArgumentException("Illegal column gap: " + columnGap);
		this.columnGap = columnGap;
	}

	@Override
	public Map<CliComponent, CliLayoutSetting> layout(CliComponent parent) {
		Map<CliComponent, CliLayoutSetting> layout = new HashMap<>();

		int crtLineLength = 0; // 当前行已有子组件个数
		int crtLineTop = 0; // 当前行顶部（逻辑上）位置
		int nextLeft = 0; // 下一个子组件左边（逻辑上）位置
		int crtLineHeight = 0; // 当前行高度（逻辑上）
		for (CliComponent child : parent.getChildren()) {
			CliLayoutSetting setting = new CliLayoutSetting();
			layout.put(child, setting);

			// 取子组件的宽高（逻辑上）
			int width = child.get(getDirection().logiWidthType(), parent).orElse(0);
			int height = child.get(getDirection().logiHeightType(), parent).orElse(0);

			// 设置子组件属性
			setting.set(getDirection().logiWidthType(), width);
			setting.set(getDirection().logiHeightType(), height);
			setting.set(getDirection().logiLeftType(), nextLeft);
			setting.set(getDirection().logiTopType(), crtLineTop);

			// 调整临时变量
			crtLineLength++;
			nextLeft += (getDirection().firstDirection().isReversed() ? -1 : 1) * width;
			nextLeft += (getDirection().firstDirection().isReversed() ? -1 : 1) * getColumnGap();
			crtLineHeight = Math.max(crtLineHeight, height);

			// 当前行已满，调整临时变量
			if (getLineSize() > 0 && crtLineLength >= getLineSize()) {
				crtLineLength = 0;
				crtLineTop += (getDirection().secondDirection().isReversed() ? -1 : 1) * crtLineHeight;
				crtLineTop += (getDirection().secondDirection().isReversed() ? -1 : 1) * getRowGap();
				nextLeft = 0;
				crtLineHeight = 0;
			}
		}

		// 统一移动所有子组件位置，消除负数
		int minLeft = 0, minTop = 0;
		for (CliLayoutSetting setting : layout.values()) {
			minLeft = Math.min(minLeft, setting.get(LEFT));
			minTop = Math.min(minTop, setting.get(TOP));
		}
		if (minLeft < 0) {
			for (CliLayoutSetting setting : layout.values()) {
				setting.moveRight(-minLeft);
			}
		}
		if (minTop < 0) {
			for (CliLayoutSetting setting : layout.values()) {
				setting.moveDown(-minTop);
			}
		}

		// 校验合法性
		layout.values().forEach(CliLayoutSetting::validate);

		return layout;
	}

}
