package com.github.blovemaple.mj.rule;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;

/**
 * 带缓存的PlayerTileType。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public abstract class CachedPlayerTileType implements PlayerTileType {
	private final Map<Integer, Boolean> cache = Collections.synchronizedMap(new WeakHashMap<>());

	private final boolean useAliveTiles;
	private final Function<PlayerInfo, ?>[] otherValues;

	/**
	 * 新建实例，使用手牌作为识别条件。识别条件相同的会优先使用缓存结果。
	 */
	public CachedPlayerTileType() {
		this(true, (Function<PlayerInfo, ?>[]) null);
	}

	/**
	 * 新建实例，使用指定函数返回的值作为识别条件。识别条件相同的会优先使用缓存结果。
	 * 
	 * @param useAliveTiles
	 *            是否使用手牌作为识别条件
	 * @param otherValues
	 *            返回除手牌外用于识别条件的值的函数
	 */
	@SuppressWarnings("unchecked")
	public CachedPlayerTileType(boolean useAliveTiles, Function<PlayerInfo, ?>... otherValues) {
		this.useAliveTiles = useAliveTiles;
		this.otherValues = otherValues != null ? otherValues : new Function[0];
	}

	@Override
	public boolean match(PlayerInfo playerInfo, Set<Tile> aliveTiles) {
		int hash = hash(playerInfo, aliveTiles);
		Boolean result = cache.get(hash);
		if (result == null) {
			result = matchWithoutCache(playerInfo, aliveTiles);
			cache.put(hash, result);
		}
		return result;
	}

	private int hash(PlayerInfo playerInfo, Set<Tile> aliveTiles) {
		final int prime = 31;
		int result = 1;
		if (useAliveTiles) {
			Set<Tile> testAliveTiles = aliveTiles != null ? aliveTiles : playerInfo.getAliveTiles();
			result = prime * result + ((testAliveTiles == null) ? 0 : testAliveTiles.hashCode());
		}
		for (Function<PlayerInfo, ?> function : otherValues) {
			Object value = function.apply(playerInfo);
			result = prime * result + ((value == null) ? 0 : value.hashCode());
		}
		return result;
	}

	public abstract boolean matchWithoutCache(PlayerInfo playerInfo, Set<Tile> aliveTiles);

}
