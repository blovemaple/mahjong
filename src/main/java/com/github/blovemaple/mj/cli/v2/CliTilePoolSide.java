package com.github.blovemaple.mj.cli.v2;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliTilePoolSide extends CliDirectionalTilesPanel {
	private static final int HORIZONTAL_WIDTH = 39, HORIZONTAL_HEIGHT = 6;
	private static final int VERTICAL_WIDTH = 15, VERTICAL_HEIGHT = 12;

	public CliTilePoolSide(CliViewDirection direction) {
		super(direction);

		switch (direction) {
		case UPPER:
		case UNDER:
			setWidthBySelf(() -> HORIZONTAL_WIDTH);
			setHeightBySelf(() -> HORIZONTAL_HEIGHT);
			break;
		case LEFT:
		case RIGHT:
			setWidthBySelf(() -> VERTICAL_WIDTH);
			setHeightBySelf(() -> VERTICAL_HEIGHT);
			break;
		default:
			throw new RuntimeException();
		}
	}

}
