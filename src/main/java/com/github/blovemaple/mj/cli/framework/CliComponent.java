package com.github.blovemaple.mj.cli.framework;

import static com.github.blovemaple.mj.cli.framework.CliLayoutSettingType.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntSupplier;

import com.google.common.base.Objects;

/**
 * 命令行界面组件。一个组件属于一个父组件，也可以有0至多个子组件。每个组件定义相对其父组件的位置和绝对尺寸。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public abstract class CliComponent {
	private CliComponent parent;
	private final List<CliComponent> children = new ArrayList<>();
	private final boolean leaf;

	private final Map<CliLayoutSettingType, IntSupplier> settingBySelf = new HashMap<>();
	private final Map<CliLayoutSettingType, Function<CliComponent, Integer>> settingWithParent = new HashMap<>();

	public CliComponent(boolean leaf) {
		this.leaf = leaf;
	}

	public final CliComponent getParent() {
		return parent;
	}

	private void setParent(CliComponent parent) {
		if (getClass() == CliRootPane.class)
			throw new UnsupportedOperationException();
		if (parent != null && this.parent != null)
			throw new IllegalStateException();
		this.parent = parent;
	}

	public final List<CliComponent> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public final void addChild(CliComponent child) {
		if (leaf)
			throw new UnsupportedOperationException();
		if (!children.contains(child)) {
			children.add(child);
			child.setParent(this);
		}
	}

	public final void removeChild(CliComponent child) {
		if (children.remove(child))
			child.setParent(null);
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
		OptionalInt width = get(WIDTH, parent, RIGHT, LEFT, (right, left) -> right - left + 1);
		if (width.isPresent())
			return width;
		return getDefaultWidth();
	}

	protected OptionalInt getDefaultWidth() {
		return OptionalInt.empty();
	}

	/**
	 * 返回绝对高度。
	 * 
	 * @param 父组件。null表示不可依赖父组件。
	 */
	public OptionalInt getHeight(CliComponent parent) {
		OptionalInt height = get(HEIGHT, parent, BOTTOM, TOP, (bottom, top) -> bottom - top + 1);
		if (height.isPresent())
			return height;
		return getDefaultHeight();
	}

	protected OptionalInt getDefaultHeight() {
		return OptionalInt.empty();
	}

	/**
	 * 返回左侧与父组件左侧的相对位置。
	 * 
	 * @param 父组件。null表示不可依赖父组件。
	 */
	public OptionalInt getLeft(CliComponent parent) {
		return get(LEFT, parent, RIGHT, WIDTH, (right, width) -> right - width + 1);
	}

	/**
	 * 返回右侧与父组件左侧的相对位置。
	 * 
	 * @param 父组件。null表示不可依赖父组件。
	 */
	public OptionalInt getRight(CliComponent parent) {
		return get(RIGHT, parent, LEFT, WIDTH, (left, width) -> left + width - 1);
	}

	/**
	 * 返回顶端与父组件顶端的相对位置。
	 * 
	 * @param 父组件。null表示不可依赖父组件。
	 */
	public OptionalInt getTop(CliComponent parent) {
		return get(TOP, parent, BOTTOM, HEIGHT, (bottom, height) -> bottom - height + 1);
	}

	/**
	 * 返回底端与父组件顶端的相对位置。
	 * 
	 * @param 父组件。null表示不可依赖父组件。
	 */
	public OptionalInt getBottom(CliComponent parent) {
		return get(BOTTOM, parent, TOP, HEIGHT, (top, height) -> top + height - 1);
	}

	private OptionalInt get(CliLayoutSettingType type, CliComponent parent, CliLayoutSettingType calcParamType1,
			CliLayoutSettingType calcParamType2, IntBinaryOperator calculator) {
		// with parent or by self
		OptionalInt specified = getSpecified(type, parent);
		if (specified.isPresent())
			return specified;

		// by calculation
		OptionalInt specified1 = getSpecified(calcParamType1, parent);
		if (!specified1.isPresent())
			return OptionalInt.empty();
		OptionalInt specified2 = getSpecified(calcParamType2, parent);
		if (!specified2.isPresent())
			return OptionalInt.empty();
		return OptionalInt.of(calculator.applyAsInt(specified1.getAsInt(), specified2.getAsInt()));
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

	private int lastLeft, lastTop, lastWidth, lastHeight;
	private List<List<CliCell>> lastCells;

	public synchronized final void repaint() {
		// 判断尺寸、位置有没有变化
		boolean sizePosChanged = false;
		int crtLeft = getLeft(getParent()).orElse(-1);
		if (crtLeft != lastLeft) {
			sizePosChanged = true;
			lastLeft = crtLeft;
		}
		int crtTop = getTop(getParent()).orElse(-1);
		if (crtTop != lastTop) {
			sizePosChanged = true;
			lastTop = crtTop;
		}
		int crtWidth = getWidth(getParent()).orElse(-1);
		if (crtWidth != lastWidth) {
			sizePosChanged = true;
			lastWidth = crtWidth;
		}
		int crtHeight = getHeight(getParent()).orElse(-1);
		if (crtHeight != lastHeight) {
			sizePosChanged = true;
			lastHeight = crtHeight;
		}

		// 重画当前组件和所有子组件
		List<List<CliCell>> crtCells = paintTree();
		if (crtCells.size() != crtHeight)
			throw new RuntimeException("Incorrect height " + crtCells.size() + " with expected " + crtHeight);
		for (List<CliCell> row : crtCells)
			if (row.size() != crtWidth)
				throw new RuntimeException("Incorrect width " + row.size() + " with expected " + crtWidth);

		// 比较是否有变化，如果有变化则提交
		if (sizePosChanged || !isSameCells(crtCells, lastCells)) {
			lastCells = crtCells;
			submitPaint(sizePosChanged);
		}
	}

	protected final List<List<CliCell>> paintTree() {
		// 画当前组件
		List<List<CliCell>> crtCells = paintSelf();

		// 画所有子组件
		for (CliComponent child : getChildren())
			fillCells(crtCells, child.paintTree(), child.getLeft(this).orElse(0), child.getTop(this).orElse(0));
		return crtCells;
	}

	private void fillCells(List<List<CliCell>> allCells, List<List<CliCell>> newCells, int left, int top) {
		for (int rowIndex = top; rowIndex < top + newCells.size() && rowIndex < allCells.size(); rowIndex++) {
			List<CliCell> row = newCells.get(rowIndex - top);
			List<CliCell> targetRow = allCells.get(rowIndex);
			for (int columnIndex = left; columnIndex < left + row.size()
					&& columnIndex < targetRow.size(); columnIndex++) {
				CliCell cell = row.get(columnIndex - left);
				if (cell.getBackground() == null) {
					cell = (CliCell) cell.clone();
					cell.setBackground(targetRow.get(columnIndex).getBackground());
				}
				targetRow.set(columnIndex, cell);
			}
		}
	}

	private boolean isSameCells(List<List<CliCell>> cells1, List<List<CliCell>> cells2) {
		if (cells1 == null || cells2 == null)
			return false;
		if (cells1.size() != cells2.size())
			return false;
		for (int rowIndex = 0; rowIndex < cells1.size(); rowIndex++) {
			if (!Objects.equal(cells1.get(rowIndex), cells2.get(rowIndex)))
				return false;
		}
		return true;
	}

	protected abstract List<List<CliCell>> paintSelf();

	protected void submitPaint(boolean sizePosChanged) {
		getParent().paintChild(this, sizePosChanged);
	}

	protected final void paintChild(CliComponent child, boolean sizePosChanged) {
		int childIndex = getChildren().indexOf(child);
		if (childIndex < 0)
			throw new RuntimeException("Unexisted child: " + child);

		// 如果指定子组件尺寸位置已变化，则重画当前组件并获取所有子组件上次画完的结果；如果没有变化则拿当前组件上次画完的结果，覆盖上指定子组件及其上面的所有组件上次画完的结果
		List<CliComponent> involvedChildren = sizePosChanged ? getChildren()
				: getChildren().subList(childIndex, getChildren().size());

		List<List<CliCell>> crtCells = sizePosChanged ? paintSelf() : getCells();
		for (CliComponent aChild : involvedChildren) {
			fillCells(crtCells, aChild.getCells(), aChild.getLeft(this).orElse(0), aChild.getTop(this).orElse(0));
		}

		// 比较是否有变化，如果有变化则提交
		if (sizePosChanged || !isSameCells(crtCells, lastCells)) {
			lastCells = crtCells;
			submitPaint(sizePosChanged);
		}
	}

	protected List<List<CliCell>> getCells() {
		return lastCells;
	}

}
