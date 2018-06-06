package com.github.blovemaple.mj.cli.framework.component;

import static com.github.blovemaple.mj.cli.framework.layout.CliBoundField.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;

import com.github.blovemaple.mj.cli.framework.CliCellGroup;
import com.github.blovemaple.mj.cli.framework.layout.CliBoundField;

/**
 * 命令行界面组件。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public abstract class CliComponent {
	private final Map<CliBoundField, IntUnaryOperator> minSize = new EnumMap<>(CliBoundField.class);
	private final Map<CliBoundField, IntUnaryOperator> prefSize = new EnumMap<>(CliBoundField.class);
	private final Map<CliBoundField, IntUnaryOperator> maxSize = new EnumMap<>(CliBoundField.class);
	private final Map<CliBoundField, IntSupplier> freePosition = new EnumMap<>(CliBoundField.class);

	public static final int PREF_VALUE = -1;

	public CliComponent() {
		// 设置默认范围：首选尺寸为默认尺寸，不限制最小和最大尺寸，自由位置在左上角
		setMinWidth(() -> 0);
		setPrefWidth(height -> getDefaultWidth(height));
		setMaxWidth(() -> Integer.MAX_VALUE);
		setMinHeight(() -> 0);
		setPrefHeight(width -> getDefaultHeight(width));
		setMaxHeight(() -> Integer.MAX_VALUE);
		setFreeLeft(() -> 0);
		setFreeTop(() -> 0);
	}

	public void setMinWidth(IntSupplier value) {
		Objects.requireNonNull(value);
		setMinWidth(i -> value.getAsInt());
	}

	public void setMinWidth(IntUnaryOperator value) {
		Objects.requireNonNull(value);
		minSize.put(WIDTH, value);
	}

	public void setPrefWidth(IntSupplier value) {
		Objects.requireNonNull(value);
		setPrefWidth(i -> value.getAsInt());
	}

	public void setPrefWidth(IntUnaryOperator value) {
		Objects.requireNonNull(value);
		prefSize.put(WIDTH, value);
	}

	public void setMaxWidth(IntSupplier value) {
		Objects.requireNonNull(value);
		setMaxWidth(i -> value.getAsInt());
	}

	public void setMaxWidth(IntUnaryOperator value) {
		Objects.requireNonNull(value);
		maxSize.put(WIDTH, value);
	}

	public abstract int getDefaultWidth(int height);

	public void setMinHeight(IntSupplier value) {
		Objects.requireNonNull(value);
		setMinHeight(i -> value.getAsInt());
	}

	public void setMinHeight(IntUnaryOperator value) {
		Objects.requireNonNull(value);
		minSize.put(HEIGHT, value);
	}

	public void setPrefHeight(IntSupplier value) {
		Objects.requireNonNull(value);
		setPrefHeight(i -> value.getAsInt());
	}

	public void setPrefHeight(IntUnaryOperator value) {
		Objects.requireNonNull(value);
		prefSize.put(HEIGHT, value);
	}

	public void setMaxHeight(IntSupplier value) {
		Objects.requireNonNull(value);
		setMaxHeight(i -> value.getAsInt());
	}

	public void setMaxHeight(IntUnaryOperator value) {
		Objects.requireNonNull(value);
		maxSize.put(HEIGHT, value);
	}

	public abstract int getDefaultHeight(int width);

	public void setFreeLeft(IntSupplier value) {
		Objects.requireNonNull(value);
		freePosition.remove(RIGHT);
		freePosition.put(LEFT, value);
	}

	public void setFreeRight(IntSupplier value) {
		Objects.requireNonNull(value);
		freePosition.remove(LEFT);
		freePosition.put(RIGHT, value);
	}

	public void setFreeTop(IntSupplier value) {
		Objects.requireNonNull(value);
		freePosition.remove(BOTTOM);
		freePosition.put(TOP, value);
	}

	public void setFreeBottom(IntSupplier value) {
		Objects.requireNonNull(value);
		freePosition.remove(TOP);
		freePosition.put(BOTTOM, value);
	}

	public int getMinSize(CliBoundField field, int another) {
		if (!field.isSizeField())
			throw new IllegalArgumentException(field + " is not a size field.");
		return minSize.get(field).applyAsInt(another);
	}

	public int getPrefSize(CliBoundField field, int another) {
		if (!field.isSizeField())
			throw new IllegalArgumentException(field + " is not a size field.");
		return prefSize.get(field).applyAsInt(another);
	}

	public int getMaxSize(CliBoundField field, int another) {
		if (!field.isSizeField())
			throw new IllegalArgumentException(field + " is not a size field.");
		return maxSize.get(field).applyAsInt(another);
	}

	public int getFreePosition(CliBoundField field) {
		return freePosition.getOrDefault(field, () -> {
			switch (field) {
			case LEFT:
				return getFreePosition(RIGHT) - getPrefSize(WIDTH, 0);
			case RIGHT:
				return getFreePosition(LEFT) + getPrefSize(WIDTH, 0);
			case TOP:
				return getFreePosition(BOTTOM) - getPrefSize(HEIGHT, 0);
			case BOTTOM:
				return getFreePosition(TOP) + getPrefSize(HEIGHT, 0);
			default:
				throw new IllegalArgumentException(field + " is not a position field.");
			}
		}).getAsInt();
	}

	/**
	 * 画自己。
	 */
	public abstract CliCellGroup paint(int width, int height);

}
