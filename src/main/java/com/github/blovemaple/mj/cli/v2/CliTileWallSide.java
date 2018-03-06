package com.github.blovemaple.mj.cli.v2;

import static com.github.blovemaple.mj.cli.v2.CliViewDirection.*;

import java.util.List;

import com.github.blovemaple.mj.cli.framework.component.CliLabel;
import com.github.blovemaple.mj.cli.framework.component.CliPanel;
import com.github.blovemaple.mj.cli.framework.layout.CliFlowLayout;
import com.github.blovemaple.mj.cli.framework.layout.CliFlowLayout.CliFlowLayoutDirection;
import com.github.blovemaple.mj.object.TileWall.TilePile;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliTileWallSide extends CliPanel {
	private static CliLabel PILE_FULL = new CliLabel("▇▇");
	private static CliLabel PILE_ONE = new CliLabel("▄▄");
	private static CliLabel PILE_EMPTY = new CliLabel("  ");

	private final CliViewDirection direction;

	public CliTileWallSide(CliViewDirection direction) {
		this.direction = direction;
		initLayout();
	}

	private void initLayout() {
		CliFlowLayoutDirection layoutDirection;
		switch (direction) {
		case DOWN:
			layoutDirection = CliFlowLayoutDirection.TOP_LEFT_HORIZONTAL;
			break;
		case LEFT:
			layoutDirection = CliFlowLayoutDirection.TOP_LEFT_VERTICAL;
			break;
		case RIGHT:
			layoutDirection = CliFlowLayoutDirection.BOTTOM_LEFT_VERTICAL;
			break;
		case UP:
			layoutDirection = CliFlowLayoutDirection.TOP_RIGHT_HORIZONTAL;
			break;
		default:
			throw new RuntimeException();
		}

		CliFlowLayout layout = new CliFlowLayout(layoutDirection);
		if (direction == UP || direction == DOWN)
			layout.setColumnGap(1);

		setLayout(layout);
	}

	public synchronized void view(List<TilePile> piles) {
		clearChildren();
		piles.forEach(pile -> {
			addChild(pile.getSize() == 2 ? PILE_FULL : pile.getSize() == 1 ? PILE_ONE : PILE_EMPTY);
		});
	}

}
