package com.github.blovemaple.mj.cli.v2;

import java.util.EnumMap;
import java.util.Map;

import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.TileWall;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliTileWallGroup {
	private final Map<CliViewDirection, CliTileWallSide> walls;
	private PlayerLocation selfLocation;

	public CliTileWallGroup() {
		walls = new EnumMap<>(CliViewDirection.class);
		for (var direction : CliViewDirection.values())
			walls.put(direction, new CliTileWallSide(direction));

		setSelfLocation(PlayerLocation.EAST);
	}

	public PlayerLocation getSelfLocation() {
		return selfLocation;
	}

	public void setSelfLocation(PlayerLocation selfLocation) {
		this.selfLocation = selfLocation;
	}

	public void view(TileWall wall) {
		for (var location : PlayerLocation.values())
			walls.get(CliViewDirection.UNDER.getLocationOf(selfLocation.getRelationOf(location)))
					.view(wall.getPiles(location));
	}
}
