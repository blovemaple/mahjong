package com.github.blovemaple.mj.game.rule;

import java.util.Set;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;

/**
 * 和牌类型。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface WinType {
	/**
	 * 判断指定条件下是否可和牌。如果aliveTiles非null，则用于替换playerInfo中的信息做出判断，
	 * 否则利用playerInfo中的aliveTiles做出判断。
	 * 
	 * @param playerInfo
	 *            玩家信息
	 * @param aliveTiles
	 *            玩家手中的牌
	 * @return 是否可以和牌
	 */
	public boolean canWin(PlayerInfo playerInfo, Set<Tile> aliveTiles);
}
