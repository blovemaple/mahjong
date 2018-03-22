package com.github.blovemaple.mj.cli.v2;

import static com.github.blovemaple.mj.object.TileGroupType.*;
import static java.util.Comparator.*;

import java.util.stream.IntStream;

import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroup;
import com.github.blovemaple.mj.object.TileGroupPlayerView;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliTileGroup extends CliDirectionalTilesPanel {
	public CliTileGroup(CliViewDirection direction) {
		super(direction);
		getLayout().setColumnGap(0);
	}

	public void view(TileGroupPlayerView tileGroup) {
		clearChildren();
		if (tileGroup.getType() == ANGANG_GROUP)
			IntStream.range(0, 4).forEach(i -> addChild(CliTile.TILE_BACK));
		else
			tileGroup.getTiles().stream().sorted(comparing(Tile::type)).map(CliTile::of).forEach(this::addChild);
	}

	public void view(TileGroup tileGroup) {
		clearChildren();
		tileGroup.getTiles().stream().sorted(comparing(Tile::type)).map(CliTile::of).forEach(this::addChild);
	}

}
