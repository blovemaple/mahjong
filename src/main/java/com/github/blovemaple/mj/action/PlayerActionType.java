package com.github.blovemaple.mj.action;

import java.util.Collection;
import java.util.Set;

import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * 玩家动作类型。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface PlayerActionType extends ActionType {

	/**
	 * 判断指定状态下指定位置的玩家可否做此种类型的动作。
	 * 
	 * @return 能做返回true；否则返回false。
	 */
	public boolean canDo(GameContext context, PlayerLocation location);

	/**
	 * 返回此动作是否可以放弃。
	 * 
	 * @return 可以放弃返回true；否则返回false。
	 */
	public boolean canPass(GameContext context, PlayerLocation location);

	/**
	 * 返回一个集合，包含指定状态下指定玩家可作出的此类型的所有合法动作的相关牌集合。
	 */
	Collection<Set<Tile>> getLegalActionTiles(GameContextPlayerView context);
}
