package com.github.blovemaple.mj.cli.v2;

import static com.github.blovemaple.mj.object.TileGroupType.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.github.blovemaple.mj.object.PlayerTiles;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroupPlayerView;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliPlayerTiles extends CliDirectionalPanel {
	private CliPlayerAliveTiles aliveTilesPanel;

	public CliPlayerTiles(CliViewDirection direction) {
		super(direction);
		getLayout().setColumnGap(1);

		aliveTilesPanel = new CliPlayerAliveTiles(direction);
		addChild(aliveTilesPanel);
	}

	public void view(PlayerTiles playerTiles, Tile lastDrawedTile) {
		clearChildren();
		viewGroups(playerTiles.getTileGroups());
		addChild(aliveTilesPanel);
		aliveTilesPanel.view(playerTiles.getAliveTiles(), lastDrawedTile);
	}

	public void view(List<? extends TileGroupPlayerView> tileGroups, int aliveTileCount) {
		clearChildren();
		viewGroups(tileGroups);
		addChild(aliveTilesPanel);
		aliveTilesPanel.view(aliveTileCount);
	}
	
	private void viewGroups(List<? extends TileGroupPlayerView> tileGroups){
		List<Tile> huas = tileGroups.stream() //
				.filter(tileGroup -> tileGroup.getType() == BUHUA_GROUP) //
				.map(TileGroupPlayerView::getTilesView).flatMap(Collection::stream) //
				.collect(Collectors.toList());
		if (!huas.isEmpty()) {
			CliTileGroup groupPanel = new CliTileGroup(getDirection());
			addChild(groupPanel);
			groupPanel.view(huas);
		}

		tileGroups.stream() //
				.filter(tileGroup -> tileGroup.getType() != BUHUA_GROUP) //
				.forEach(tileGroup -> {
					CliTileGroup groupPanel = new CliTileGroup(getDirection());
					addChild(groupPanel);
					groupPanel.view(tileGroup);
				});
	}

}
