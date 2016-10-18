package com.github.blovemaple.mj.rule.win;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroup;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.object.TileUnit;

/**
 * TODO comment
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class WinInfo {
	private GameContext.PlayerView contextView;

	private PlayerInfo playerInfo;
	private Set<Tile> aliveTiles;
	private List<TileType> tileTypes; // 玩家手中所有牌，排序
	private Tile winTile;
	private Boolean ziMo;

	private Map<Class<? extends WinType>, List<List<TileUnit>>> units;

	public void setContextView(GameContext.PlayerView contextView) {
		this.contextView = contextView;
	}

	public PlayerInfo getPlayerInfo() {
		if (playerInfo == null)
			playerInfo = fromContext(GameContext.PlayerView::getMyInfo);
		return playerInfo;
	}

	public void setPlayerInfo(PlayerInfo playerInfo) {
		this.playerInfo = playerInfo;
	}

	public Set<Tile> getAliveTiles() {
		if (aliveTiles == null)
			aliveTiles = from(playerInfo, PlayerInfo::getAliveTiles);
		return aliveTiles;
	}

	public void setAliveTiles(Set<Tile> aliveTiles) {
		this.aliveTiles = aliveTiles;
	}

	public List<TileType> getTileTypes() {
		if (tileTypes == null)
			tileTypes = fromContext(contextView -> {
				PlayerInfo playerInfo = contextView.getMyInfo();
				Stream<Tile> tiles = playerInfo.getAliveTiles().stream();
				if (playerInfo != null)
					for (TileGroup group : playerInfo.getTileGroups())
						tiles = Stream.concat(tiles, group.getTiles().stream());
				return tiles.map(Tile::type).sorted().collect(Collectors.toList());
			});
		return tileTypes;
	}

	public void setTileTypes(List<TileType> tileTypes) {
		this.tileTypes = tileTypes;
	}

	public Map<Class<? extends WinType>, List<List<TileUnit>>> getUnits() {
		return units;
	}

	public void setUnits(Map<Class<? extends WinType>, List<List<TileUnit>>> units) {
		this.units = units;
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

	private <R> R fromContext(Function<GameContext.PlayerView, R> function) {
		return from(contextView, function);
	}

	private <S, R> R from(S source, Function<S, R> function) {
		if (source == null)
			return null;
		return function.apply(source);
	}
}