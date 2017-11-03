package com.github.blovemaple.mj.rule.win;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 番种。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface FanType extends FanTypeMatcher {
	/**
	 * 返回番种的名称，作为标识。
	 */
	public String name();

	/**
	 * 返回计入一次的番数。
	 */
	public int score();

	/**
	 * 返回被此番种覆盖的番种。如果此番种已计入，则不计被覆盖的番种。
	 */
	public Set<FanType> covered();

	/**
	 * 算番。
	 * 
	 * @param winInfo
	 *            被检查的牌
	 * @param fanTypes
	 *            使用的番种
	 * @param winTypes
	 *            使用的和牌类型
	 * @return 符合的番种和番数
	 */
	public static Map<FanType, Integer> getFans(WinInfo winInfo, Collection<FanType> fanTypes,
			Collection<WinType> winTypes) {
		// 先parse和牌units
		winTypes.forEach(winType -> winType.parseWinTileUnits(winInfo));
		// 如果没parse出来，说明不和牌，直接返回空map
		if (winInfo.getUnits() == null || winInfo.getUnits().isEmpty())
			return Collections.emptyMap();

		// 算番
		Map<FanType, Integer> fans = new HashMap<>();
		Set<FanType> coveredFanTypes = new HashSet<>();
		fanTypes.forEach(crtFanType -> {
			if (coveredFanTypes.contains(crtFanType))
				return;

			Integer matchCount;

			matchCount = winInfo.getFans().get(crtFanType);
			if (matchCount == null) {
				matchCount = crtFanType.matchCount(winInfo);
				if (matchCount > 0)
					winInfo.getFans().put(crtFanType, matchCount);
			}

			if (matchCount > 0) {
				fans.put(crtFanType, crtFanType.score() * matchCount);
				Set<? extends FanType> covered = crtFanType.covered();
				if (covered != null && !covered.isEmpty())
					coveredFanTypes.addAll(covered);
			}
		});

		return fans;
	}
}
