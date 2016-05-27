package com.github.blovemaple.mj.rule.win;

import java.util.List;
import java.util.Map;

import com.github.blovemaple.mj.action.ActionAndLocation;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.object.TileUnit;

/**
 * TODO
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class WinInfo {
	private PlayerLocation location;
	private List<ActionAndLocation> allActions;
	private List<TileType> tileTypes;
	private Map<WinType, List<List<TileUnit>>> units;
	private Tile winTile;
	private boolean ziMo;

	public PlayerLocation getLocation() {
		return location;
	}

	public void setLocation(PlayerLocation location) {
		this.location = location;
	}

	public List<ActionAndLocation> getAllActions() {
		return allActions;
	}

	public void setAllActions(List<ActionAndLocation> allActions) {
		this.allActions = allActions;
	}

	public List<TileType> getTileTypes() {
		return tileTypes;
	}

	public void setTileTypes(List<TileType> tileTypes) {
		this.tileTypes = tileTypes;
	}

	public Map<WinType, List<List<TileUnit>>> getUnits() {
		return units;
	}

	public void setUnits(Map<WinType, List<List<TileUnit>>> units) {
		this.units = units;
	}

	public Tile getWinTile() {
		return winTile;
	}

	public void setWinTile(Tile winTile) {
		this.winTile = winTile;
	}

	public boolean isZiMo() {
		return ziMo;
	}

	public void setZiMo(boolean ziMo) {
		this.ziMo = ziMo;
	}
}