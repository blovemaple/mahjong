package com.github.blovemaple.mj.cli.v2;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.PlayerLocation.Relation;
import com.github.blovemaple.mj.object.TileWall;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliTileWall {
	private final Map<PlayerLocation, CliTileWallSide> walls; // starting from location east
	@SuppressWarnings("unused")
	private CliViewDirection eastDirection;

	public CliTileWall(CliViewDirection eastDirection) {
		walls = new EnumMap<>(PlayerLocation.class);
		init(eastDirection);
	}

	public void init(CliViewDirection eastDirection) {
		Objects.requireNonNull(eastDirection);
		this.eastDirection = eastDirection;

		PlayerLocation crtLocation = PlayerLocation.EAST;
		CliViewDirection crtDirection = eastDirection;
		for (int i = 0; i < 4; i++) {
			walls.put(crtLocation, new CliTileWallSide(crtDirection));
			crtLocation = crtLocation.getLocationOf(Relation.NEXT);
			crtDirection = crtDirection.next();
		}
	}

	public void view(TileWall wall) {
		for (PlayerLocation location : PlayerLocation.values())
			walls.get(location).view(wall.getPiles(location));
	}
}
