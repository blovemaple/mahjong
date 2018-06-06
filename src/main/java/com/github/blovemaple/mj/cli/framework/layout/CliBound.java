package com.github.blovemaple.mj.cli.framework.layout;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliBound {
	private int left, top, width, height;
	private boolean widthSet = false, heightSet = false;

	public void set(CliBoundField field, int value) {
		switch (field) {
		case LEFT:
			left = value;
			break;
		case RIGHT:
			if (!widthSet)
				throw new IllegalStateException("Width has not set yet while setting right.");
			left = value - width;
			break;
		case TOP:
			top = value;
			break;
		case BOTTOM:
			if (!heightSet)
				throw new IllegalStateException("Height has not set yet while setting bottom.");
			top = value - height;
			break;
		case WIDTH:
			width = value;
			widthSet = true;
			break;
		case HEIGHT:
			height = value;
			heightSet = true;
			break;
		}
	}

	public void moveRight(int distance) {
		left += distance;
	}

	public void moveDown(int distance) {
		top += distance;
	}

	public int left() {
		return left;
	}

	public int right() {
		return left + width;
	}

	public int top() {
		return top;
	}

	public int bottom() {
		return top + height;
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}

}
