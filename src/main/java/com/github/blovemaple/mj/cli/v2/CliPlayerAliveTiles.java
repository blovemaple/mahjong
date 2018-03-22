package com.github.blovemaple.mj.cli.v2;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import com.github.blovemaple.mj.object.Tile;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliPlayerAliveTiles extends CliDirectionalTilesPanel {

	public CliPlayerAliveTiles(CliViewDirection direction) {
		super(direction);
	}

	public void view(int hiddenCount) {
		clearChildren();
		IntStream.range(0, hiddenCount).forEach(i -> addChild(CliTile.TILE_BACK));
	}

	public void view(Collection<Tile> aliveTiles, Tile lastDrawedTile) {
		List<Tile> tileList = aliveTiles.stream().sorted(comparing(Tile::type)).collect(toList());
		if (tileList.remove(lastDrawedTile))
			tileList.add(lastDrawedTile);
		view(tileList);
	}

}
