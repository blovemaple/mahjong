package com.github.blovemaple.mj.local.bazbot;

import java.util.Set;

import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileUnitType;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class BazBotTileUnit {
	@SuppressWarnings("unused")
	private boolean completed;
	@SuppressWarnings("unused")
	private TileUnitType unitType;
	@SuppressWarnings("unused")
	private Set<Tile> tiles;

	public static BazBotTileUnit completed(TileUnitType unitType, Set<Tile> tiles) {
		return new BazBotTileUnit(true, unitType, tiles);
	}

	public static BazBotTileUnit uncompleted(TileUnitType unitType, Set<Tile> tiles) {
		return new BazBotTileUnit(false, unitType, tiles);
	}

	public BazBotTileUnit(boolean isCompleted, TileUnitType unitType, Set<Tile> tiles) {
		this.completed = isCompleted;
		this.unitType = unitType;
		this.tiles = tiles;
	}

}
