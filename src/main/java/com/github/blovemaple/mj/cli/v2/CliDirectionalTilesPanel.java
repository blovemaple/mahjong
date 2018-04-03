package com.github.blovemaple.mj.cli.v2;

import java.util.Collection;

import com.github.blovemaple.mj.cli.framework.layout.CliFlowLayout;
import com.github.blovemaple.mj.object.Tile;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliDirectionalTilesPanel extends CliDirectionalPanel {
	public CliDirectionalTilesPanel(CliViewDirection direction) {
		super(direction);

		CliFlowLayout layout = getLayout();
		switch (direction) {
		case LOWER:
		case UPPER:
			layout.setColumnGap(1);
			layout.setChildWidth(2);
			layout.setChildHeight(2);
			break;
		case LEFT:
		case RIGHT:
			layout.setRowGap(1);
			layout.setChildWidth(4);
			layout.setChildHeight(1);
			break;
		default:
			throw new RuntimeException();
		}
	}

	public void view(Collection<Tile> tiles) {
		clearChildren();
		tiles.stream().map(CliTile::of).forEach(this::addChild);
	}
}
