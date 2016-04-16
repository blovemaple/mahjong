package com.github.blovemaple.mj.action;

import java.util.Collection;
import java.util.Set;

import com.github.blovemaple.mj.action.standard.CpgActionType;
import com.github.blovemaple.mj.action.standard.StandardActionType;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * 做出动作的类型。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface ActionType {

	/**
	 * 名称，用作唯一标识。<br>
	 * 默认实现为{@code this.getClass().getSimpleName()}。<br>
	 * 注意：使用{@link StandardActionType}时，name为枚举值的{@code name(})。
	 * 
	 * @see com.github.blovemaple.mj.action.ActionType#name()
	 */
	public default String name() {
		return this.getClass().getSimpleName();
	}

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
	public Collection<Set<Tile>> getLegalActionTiles(
			GameContext.PlayerView context);

	/**
	 * 判断指定动作是否合法。
	 */
	public boolean isLegalAction(GameContext context, PlayerLocation location,
			Action action);

	/**
	 * 执行指定动作。
	 * 
	 * @throws IllegalActionException
	 *             动作非法
	 */
	public void doAction(GameContext context, PlayerLocation location,
			Action action) throws IllegalActionException;

	/**
	 * 返回指定的动作类型是否是此类表示的动作。<br>
	 * 有些动作有上下从属关系，比如“摸底牌”是“摸牌”，但反过来不是。<br>
	 * 默认实现为判断真正类的从属关系。如果不是这样（例如{@link StandardActionType}和{@link CpgActionType}
	 * ）则需要重写此方法。
	 */
	public default boolean matchBy(ActionType testType) {
		return getRealTypeClass().isAssignableFrom(testType.getRealTypeClass());
	}

	/**
	 * 返回真正的动作类型类。<br>
	 * 默认实现为返回此对象的类。如果不是这样（例如{@link StandardActionType}）则需要重写此方法。
	 */
	public default Class<? extends ActionType> getRealTypeClass() {
		return this.getClass();
	}

	/**
	 * 返回真正的动作类型对象。<br>
	 * 默认实现为返回此对象。如果不是这样（例如{@link StandardActionType}）则需要重写此方法。
	 */
	public default ActionType getRealTypeObject() {
		return this;
	}

}
