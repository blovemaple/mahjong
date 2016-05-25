package com.github.blovemaple.mj.rule.guobiao;

import static com.github.blovemaple.mj.object.TileRank.NumberRank.*;
import static com.github.blovemaple.mj.object.TileRank.ZiRank.*;
import static com.github.blovemaple.mj.object.TileSuit.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.object.TileUnit;
import com.github.blovemaple.mj.rule.AbstractWinFanType;

/**
 * 十三幺。由万、筒、条的1和9牌，及7张不同字牌组成，见其中任意张和牌。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class ShiSanYao extends AbstractWinFanType {

	public ShiSanYao() {
	}

	private static final Set<TileType> TILE_TYPES = new HashSet<>();
	static {
		TILE_TYPES.add(TileType.of(WAN, YI));
		TILE_TYPES.add(TileType.of(WAN, JIU));
		TILE_TYPES.add(TileType.of(TIAO, YI));
		TILE_TYPES.add(TileType.of(TIAO, JIU));
		TILE_TYPES.add(TileType.of(BING, YI));
		TILE_TYPES.add(TileType.of(BING, JIU));
		TILE_TYPES.add(TileType.of(ZI, DONG_FENG));
		TILE_TYPES.add(TileType.of(ZI, NAN));
		TILE_TYPES.add(TileType.of(ZI, XI));
		TILE_TYPES.add(TileType.of(ZI, BEI));
		TILE_TYPES.add(TileType.of(ZI, ZHONG));
		TILE_TYPES.add(TileType.of(ZI, FA));
		TILE_TYPES.add(TileType.of(ZI, BAI));
	}

	@Override
	public boolean matchWithoutCache(PlayerInfo playerInfo, Set<Tile> realAliveTiles) {
		// 没有牌组
		if (!playerInfo.getTileGroups().isEmpty())
			return false;

		// 符合的type种类全有（同时也就保证了数量也正确，因为手牌肯定是14个）
		return realAliveTiles.stream().filter(tile -> TILE_TYPES.contains(tile.type())).distinct().count() == 13;
	}

	@Override
	public Stream<Set<TileUnit>> parseWinTileUnits(PlayerInfo playerInfo, Set<Tile> readAliveTiles) {
		// TODO
		return null;
	}

	@Override
	public List<Tile> getDiscardCandidates(Set<Tile> aliveTiles, Collection<Tile> candidates) {
		// TODO
		return null;
	}

	@Override
	public Stream<ChangingForWin> changingsForWin(PlayerInfo playerInfo, int changeCount, Collection<Tile> candidates) {
		// TODO
		return null;
	}

}
