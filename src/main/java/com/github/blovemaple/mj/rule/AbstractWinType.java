package com.github.blovemaple.mj.rule;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileUnit;

/**
 * WinType的共同逻辑。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public abstract class AbstractWinType extends CachedPlayerTileType implements WinType {

	public AbstractWinType() {
		super();
	}

	@SafeVarargs
	public AbstractWinType(boolean useAliveTiles, Function<PlayerInfo, ?>... otherValues) {
		super(useAliveTiles, otherValues);
	}

	@Override
	public boolean matchWithoutCache(PlayerInfo playerInfo, Set<Tile> aliveTiles) {
		return parseWinTileUnits(playerInfo, aliveTiles != null ? aliveTiles : playerInfo.getAliveTiles()).findAny()
				.isPresent();
	}

	/**
	 * 全部解析成可以和牌的完整的TileUnit集合的流，失败返回空集合。
	 * 
	 * @param aliveTiles
	 * @param anyOne
	 *            true表示最多只解析一种可能的集合，用于快速判断是否可解析
	 * @return 完整的TileUnit集合的流
	 */
	public abstract Stream<Set<TileUnit>> parseWinTileUnits(PlayerInfo playerInfo, Set<Tile> aliveTiles);

}
