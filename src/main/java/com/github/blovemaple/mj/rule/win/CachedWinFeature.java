package com.github.blovemaple.mj.rule.win;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;

/**
 * 带缓存的WinFeature。默认仅使用手牌作为识别条件。识别条件相同的会优先使用缓存结果。
 * 
 * @deprecated
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public abstract class CachedWinFeature {
	private final Map<Integer, Boolean> cache = Collections.synchronizedMap(new WeakHashMap<>());

	private boolean useAliveTiles = true;
	private final List<Function<PlayerInfo, ?>> otherCacheKeys = new ArrayList<>();

	protected void setUseAliveTiles(boolean useAliveTiles) {
		this.useAliveTiles = useAliveTiles;
	}

	protected void addCacheKey(Function<PlayerInfo, ?> value) {
		otherCacheKeys.add(value);
	}

	public boolean match(PlayerInfo playerInfo, Set<Tile> aliveTiles) {
		Set<Tile> realAliveTiles = aliveTiles != null ? aliveTiles : playerInfo.getAliveTiles();

		int hash = hash(playerInfo, realAliveTiles);
		Boolean result = cache.get(hash);
		if (result == null) {
			result = matchWithoutCache(playerInfo, realAliveTiles);
			cache.put(hash, result);
		}
		return result;
	}

	private int hash(PlayerInfo playerInfo, Set<Tile> realAliveTiles) {
		final int prime = 31;
		int result = 1;
		if (useAliveTiles) {
			result = prime * result + ((realAliveTiles == null) ? 0 : realAliveTiles.hashCode());
		}
		for (Function<PlayerInfo, ?> function : otherCacheKeys) {
			Object value = function.apply(playerInfo);
			result = prime * result + ((value == null) ? 0 : value.hashCode());
		}
		return result;
	}

	/**
	 * 判断是否符合。未中缓存时调用。
	 * 
	 * @param playerInfo
	 *            除手牌之外的信息
	 * @param realAliveTiles
	 *            手牌
	 * @return 是否符合
	 */
	public abstract boolean matchWithoutCache(PlayerInfo playerInfo, Set<Tile> realAliveTiles);

}
