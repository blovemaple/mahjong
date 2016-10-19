package com.github.blovemaple.mj.rule.win;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.object.PlayerTiles;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroup;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.object.TileUnit;
import com.github.blovemaple.mj.rule.fan.FanType;

/**
 * TODO comment
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class WinInfo extends PlayerTiles {
	private Tile winTile;
	private Boolean ziMo;
	private GameContext.PlayerView contextView;

	private List<TileType> tileTypes; // 玩家手中所有牌，排序

	// 检查WinType和FanType的时候填入的结果，用于：
	// (1)检查FanType时利用WinType的parse结果
	// (2)检查前先看是否已经有结果，避免重复检查
	private Map<WinType, List<List<TileUnit>>> units = new HashMap<>();
	private Map<FanType, Integer> fans = new HashMap<>();

	public static WinInfo fromPlayerTiles(PlayerTiles playerTiles, Tile winTile, Boolean ziMo) {
		WinInfo winInfo = new WinInfo();
		winInfo.setAliveTiles(playerTiles.getAliveTiles());
		winInfo.setTileGroups(playerTiles.getTileGroups());
		winInfo.setWinTile(winTile);
		winInfo.setZiMo(ziMo);
		return winInfo;
	}

	public Tile getWinTile() {
		return winTile;
	}

	public void setWinTile(Tile winTile) {
		this.winTile = winTile;
	}

	public Boolean getZiMo() {
		return ziMo;
	}

	public void setZiMo(Boolean ziMo) {
		this.ziMo = ziMo;
	}

	public void setContextView(GameContext.PlayerView contextView) {
		this.contextView = contextView;
	}

	public GameContext.PlayerView getContextView() {
		return contextView;
	}

	public List<TileType> getTileTypes() {
		if (tileTypes == null) {
			Stream<Tile> tiles = getAliveTiles().stream();
			for (TileGroup group : getTileGroups())
				tiles = Stream.concat(tiles, group.getTiles().stream());
			tileTypes = tiles.map(Tile::type).sorted().collect(Collectors.toList());
		}
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

	public void setUnits(WinType winType, List<List<TileUnit>> units) {
		this.units.put(winType, units);
	}

	public Map<FanType, Integer> getFans() {
		return fans;
	}

	public void setFans(Map<FanType, Integer> fans) {
		this.fans = fans;
	}

	public void setFans(FanType fanType, Integer fans) {
		this.fans.put(fanType, fans);
	}

}