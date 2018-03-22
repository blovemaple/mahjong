package com.github.blovemaple.mj.cli.v2;

import static com.github.blovemaple.mj.cli.v2.CliViewDirection.*;
import static com.github.blovemaple.mj.object.PlayerLocation.*;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.github.blovemaple.mj.cli.framework.component.CliPanel;
import com.github.blovemaple.mj.cli.framework.component.CliSpace;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliTilePool extends CliPanel {
	private static final int CENTER_SPACE_WIDTH = 23, CENTER_SPACE_HEIGHT = 6;

	private final Map<CliViewDirection, CliTilePoolSide> sidesByDirection;
	private final Map<PlayerLocation, CliTilePoolSide> sidesByLocation;
	@SuppressWarnings("unused")
	private CliViewDirection eastDirection;

	public CliTilePool() {
		sidesByDirection = new EnumMap<>(CliViewDirection.class);
		sidesByLocation = new EnumMap<>(PlayerLocation.class);
		CliSpace centerSpace = CliSpace.of(CENTER_SPACE_WIDTH, CENTER_SPACE_HEIGHT);
		addChild(centerSpace);

		CliTilePoolSide underPool = new CliTilePoolSide(UNDER);
		underPool.setLeftBySelf(() -> centerSpace.getLeft(this).orElseThrow(RuntimeException::new));
		underPool.setTopBySelf(() -> centerSpace.getBottom(this).orElseThrow(RuntimeException::new) + 1);
		sidesByDirection.put(UNDER, underPool);
		addChild(underPool);

		CliTilePoolSide rightPool = new CliTilePoolSide(RIGHT);
		rightPool.setBottomBySelf(() -> centerSpace.getBottom(this).orElseThrow(RuntimeException::new));
		rightPool.setLeftBySelf(() -> centerSpace.getRight(this).orElseThrow(RuntimeException::new) + 1);
		sidesByDirection.put(RIGHT, rightPool);
		addChild(rightPool);

		CliTilePoolSide upperPool = new CliTilePoolSide(UPPER);
		upperPool.setRightBySelf(() -> centerSpace.getRight(this).orElseThrow(RuntimeException::new));
		upperPool.setBottomBySelf(() -> centerSpace.getTop(this).orElseThrow(RuntimeException::new) - 1);
		sidesByDirection.put(UPPER, upperPool);
		addChild(upperPool);

		CliTilePoolSide leftPool = new CliTilePoolSide(LEFT);
		leftPool.setTopBySelf(() -> centerSpace.getTop(this).orElseThrow(RuntimeException::new));
		leftPool.setRightBySelf(() -> centerSpace.getLeft(this).orElseThrow(RuntimeException::new) - 1);
		sidesByDirection.put(LEFT, leftPool);
		addChild(leftPool);

		init(UNDER);
	}

	public void init(CliViewDirection eastDirection) {
		this.eastDirection = eastDirection;
		sidesByDirection.forEach((direction, side) -> sidesByLocation
				.put(EAST.getLocationOf(eastDirection.getRelationOf(direction)), side));
	}

	public void view(PlayerLocation location, List<Tile> tiles) {
		sidesByLocation.get(location).view(tiles);
	}
}
