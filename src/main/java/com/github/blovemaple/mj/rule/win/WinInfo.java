package com.github.blovemaple.mj.rule.win;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.object.PlayerTiles;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroup;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.object.TileUnit;

/**
 * TODO comment
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class WinInfo extends PlayerTiles {
	/**
	 * 从PlayerTiles及额外信息组建WinInfo。
	 * 
	 * @param playerTiles
	 *            玩家的牌，必须
	 * @param winTile
	 *            和牌时得到的牌，可选
	 * @param ziMo
	 *            是否自摸，可选
	 * @return WinInfo对象
	 */
	public static WinInfo fromPlayerTiles(PlayerTiles playerTiles, Tile winTile, Boolean ziMo) {
		WinInfo winInfo = new WinInfo();
		winInfo.setAliveTiles(playerTiles.getAliveTiles());
		winInfo.setTileGroups(playerTiles.getTileGroups());
		winInfo.setWinTile(winTile);
		winInfo.setZiMo(ziMo);
		return winInfo;
	}

	// 基类PlayerTiles的字段必须有
	// 以下三个字段是选填的额外信息，某些和牌类型和特殊的番种才可能会用到
	private Tile winTile;
	private Boolean ziMo;
	private GameContext.PlayerView contextView;

	// 玩家手中所有牌，排序之后的。调用getTileTypes()时自动填入。
	private List<TileType> tileTypes;

	// 检查WinType和FanType的时候填入的结果，WinType解析的units和FanType计入次数，用于：
	// (1)检查FanType时利用WinType的parse结果
	// (2)检查前先看是否已经有结果，避免重复检查
	private final Map<WinType, List<List<TileUnit>>> units = new HashMap<>();
	private final Map<FanType, Integer> fans = new HashMap<>();

	/**
	 * 这个很丑的东西是给幺九刻准备的，<br>
	 * 因为有几个番种有特殊规定，算了番的刻子不能再算幺九刻，所以把算了番的刻子都记录在这里，在判断幺九刻时排除这些刻子。
	 */
	private final Set<TileUnit> noYaoJiuKeUnits = new HashSet<>();

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

	public void setUnits(WinType winType, List<List<TileUnit>> units) {
		this.units.put(winType, units);
	}

	public Map<FanType, Integer> getFans() {
		return fans;
	}

	public void setFans(FanType fanType, Integer fans) {
		this.fans.put(fanType, fans);
	}

	public Set<TileUnit> getNoYaoJiuKeUnits() {
		return noYaoJiuKeUnits;
	}

	public void addNoYaoJiuKeUnits(Collection<TileUnit> units) {
		noYaoJiuKeUnits.addAll(units);
	}

	@Override
	public String toString() {
		return "WinInfo [\nwinTile=" + winTile + ",\nziMo=" + ziMo + ",\ncontextView=" + contextView + ",\naliveTiles="
				+ aliveTiles + ",\ntileGroups=" + tileGroups + "\n]\n";
	}

}