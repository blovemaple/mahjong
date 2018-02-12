package com.github.blovemaple.mj.cli.framework.component;

import static com.github.blovemaple.mj.cli.framework.layout.CliLayoutSettingType.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntSupplier;

import com.github.blovemaple.mj.cli.framework.CliPaintable;
import com.github.blovemaple.mj.cli.framework.layout.CliLayoutSettingType;

/**
 * 命令行界面组件。一个组件属于一个父组件，也可以有0至多个子组件。每个组件定义相对其父组件的位置和绝对尺寸。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public abstract class CliComponent extends CliPaintable {
	private final List<CliComponent> children = new ArrayList<>();
	private final boolean leaf;

	private final Map<CliLayoutSettingType, IntSupplier> settingBySelf = new HashMap<>();
	private final Map<CliLayoutSettingType, Function<CliComponent, Integer>> settingWithParent = new HashMap<>();

	public CliComponent(boolean leaf) {
		this.leaf = leaf;

		set(LEFT, () -> 0);
		set(TOP, () -> 0);
		set(WIDTH, () -> 0);
		set(HEIGHT, () -> 0);
	}

	public final List<CliComponent> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public final void addChild(CliComponent child) {
		if (leaf)
			throw new UnsupportedOperationException();
		children.add(child);
	}

	public final void removeChild(CliComponent child) {
		children.remove(child);
	}

	public void setWidthBySelf(IntSupplier widthBySelf) {
		set(WIDTH, widthBySelf);
	}

	public void setHeightBySelf(IntSupplier heightBySelf) {
		set(HEIGHT, heightBySelf);
	}

	public void setLeftBySelf(IntSupplier leftBySelf) {
		set(LEFT, leftBySelf);
	}

	public void setRightBySelf(IntSupplier rightBySelf) {
		set(RIGHT, rightBySelf);
	}

	public void setTopBySelf(IntSupplier topBySelf) {
		set(TOP, topBySelf);
	}

	public void setBottomBySelf(IntSupplier bottomBySelf) {
		set(BOTTOM, bottomBySelf);
	}

	public void setWidthWithParent(Function<CliComponent, Integer> widthWithParent) {
		set(WIDTH, widthWithParent);
	}

	public void setHeightWithParent(Function<CliComponent, Integer> heightWithParent) {
		set(HEIGHT, heightWithParent);
	}

	public void setLeftWithParent(Function<CliComponent, Integer> leftWithParent) {
		set(LEFT, leftWithParent);
	}

	public void setRightWithParent(Function<CliComponent, Integer> rightWithParent) {
		set(RIGHT, rightWithParent);
	}

	public void setTopWithParent(Function<CliComponent, Integer> topWithParent) {
		set(TOP, topWithParent);
	}

	public void setBottomWithParent(Function<CliComponent, Integer> bottomWithParent) {
		set(BOTTOM, bottomWithParent);
	}

	private void set(CliLayoutSettingType type, IntSupplier bySelf) {
		if (bySelf != null) {
			checkConflict(type);
			settingBySelf.put(type, bySelf);
		} else
			settingBySelf.remove(type);
	}

	private void set(CliLayoutSettingType type, Function<CliComponent, Integer> withParent) {
		if (withParent != null) {
			checkConflict(type);
			settingWithParent.put(type, withParent);
		} else
			settingWithParent.remove(type);
	}

	private void checkConflict(CliLayoutSettingType type) {
		List<CliLayoutSettingType> conflictTypes;
		switch (type) {
		case WIDTH:
			conflictTypes = List.of(LEFT, RIGHT);
			break;
		case HEIGHT:
			conflictTypes = List.of(TOP, BOTTOM);
			break;
		case LEFT:
			conflictTypes = List.of(WIDTH, RIGHT);
			break;
		case RIGHT:
			conflictTypes = List.of(LEFT, WIDTH);
			break;
		case TOP:
			conflictTypes = List.of(HEIGHT, BOTTOM);
			break;
		case BOTTOM:
			conflictTypes = List.of(TOP, HEIGHT);
			break;
		default:
			throw new RuntimeException();
		}
		if (conflictTypes.stream().allMatch(t -> settingBySelf.containsKey(t) || settingWithParent.containsKey(t)))
			throw new IllegalStateException("Cannot set " + type + " with " + conflictTypes + " set.");
	}

	/**
	 * 返回绝对宽度。
	 * 
	 * @param 父组件。null表示不可依赖父组件。
	 */
	public OptionalInt getWidth(CliComponent parent) {
		return get(WIDTH, parent);
	}

	/**
	 * 返回绝对高度。
	 * 
	 * @param 父组件。null表示不可依赖父组件。
	 */
	public OptionalInt getHeight(CliComponent parent) {
		return get(HEIGHT, parent);
	}

	/**
	 * 返回左侧与父组件左侧的相对位置。
	 * 
	 * @param 父组件。null表示不可依赖父组件。
	 */
	public OptionalInt getLeft(CliComponent parent) {
		return get(LEFT, parent);
	}

	/**
	 * 返回右侧与父组件左侧的相对位置。
	 * 
	 * @param 父组件。null表示不可依赖父组件。
	 */
	public OptionalInt getRight(CliComponent parent) {
		return get(RIGHT, parent);
	}

	/**
	 * 返回顶端与父组件顶端的相对位置。
	 * 
	 * @param 父组件。null表示不可依赖父组件。
	 */
	public OptionalInt getTop(CliComponent parent) {
		return get(TOP, parent);
	}

	/**
	 * 返回底端与父组件顶端的相对位置。
	 * 
	 * @param 父组件。null表示不可依赖父组件。
	 */
	public OptionalInt getBottom(CliComponent parent) {
		return get(BOTTOM, parent);
	}

	public OptionalInt get(CliLayoutSettingType type, CliComponent parent) {
		// with parent or by self
		OptionalInt specified = getSpecified(type, parent);
		if (specified.isPresent())
			return specified;

		// by calculation
		OptionalInt specified1 = getSpecified(type.getCalcParamType1(), parent);
		if (!specified1.isPresent())
			return OptionalInt.empty();
		OptionalInt specified2 = getSpecified(type.getCalcParamType2(), parent);
		if (!specified2.isPresent())
			return OptionalInt.empty();
		return OptionalInt.of(type.getCalculator().applyAsInt(specified1.getAsInt(), specified2.getAsInt()));
	}

	private OptionalInt getSpecified(CliLayoutSettingType type, CliComponent parent) {
		if (parent != null && settingWithParent.containsKey(type)) {
			Integer width = settingWithParent.get(type).apply(parent);
			if (width != null)
				return OptionalInt.of(width);
		}
		if (settingBySelf.containsKey(type))
			return OptionalInt.of(settingBySelf.get(type).getAsInt());
		return OptionalInt.empty();
	}

}
