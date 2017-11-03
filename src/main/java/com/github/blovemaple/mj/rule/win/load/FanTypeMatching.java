package com.github.blovemaple.mj.rule.win.load;

import java.util.List;
import java.util.Map;

/**
 * 用于定义番种匹配逻辑中的一个对象。包括Unit和Tile等。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface FanTypeMatching {
	public enum MatchingType {
		TILE, UNIT
	}

	/**
	 * 返回匹配tile还是unit。
	 */
	public MatchingType matchingType();

	/**
	 * 尝试匹配指定的对象，并返回匹配成功时使用的新变量值。
	 * 
	 * @param object
	 *            匹配的对象，tile为TileType，unit为TileUnit
	 * @param vars
	 *            已定变量值
	 * @return 列表的每个元素表示一组新变量值，应用每组新变量值都可以完成匹配。不匹配返回null。
	 */
	public List<Map<Character, Object>> match(Object object, Map<Character, Object> vars);
}
