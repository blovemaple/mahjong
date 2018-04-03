package com.github.blovemaple.mj.cli.v2;

import static com.github.blovemaple.mj.cli.v2.CliTile.*;
import static com.github.blovemaple.mj.cli.v2.CliViewDirection.*;

import java.util.List;

import com.github.blovemaple.mj.object.TileWallPilePlayerView;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliTileWallSide extends CliDirectionalPanel {

	public CliTileWallSide(CliViewDirection direction) {
		super(direction);

		if (direction == LOWER || direction == UPPER)
			getLayout().setColumnGap(1);
	}

	public synchronized void view(List<? extends TileWallPilePlayerView> piles) {
		clearChildren();
		piles.stream().map(pile -> pile.getSize() == 2 ? TILE_BACK : pile.getSize() == 1 ? TILE_HALF : TILE_NONE)
				.forEach(this::addChild);
	}

}
