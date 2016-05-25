package com.github.blovemaple.mj.rule;

import java.util.Set;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;

/**
 * 番种。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface FanType {
	/**
	 * 返回番种的名称，作为标识。<br>
	 * 默认为类的SimpleName，全大写。
	 */
	public default String name() {
		return this.getClass().getSimpleName().toUpperCase();
	}

	/**
	 * 检查和牌是否符合此番种。如果aliveTiles非null，则用于替换playerInfo中的信息做出判断，
	 * 否则利用playerInfo中的aliveTiles做出判断。
	 */
	public boolean match(PlayerInfo playerInfo, Set<Tile> aliveTiles);
}
