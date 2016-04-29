package com.github.blovemaple.mj.action.standard;

import static com.github.blovemaple.mj.object.PlayerLocation.Relation.*;
import static com.github.blovemaple.mj.object.TileGroupType.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.action.IllegalActionException;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * 一些标准的动作类型。<br>
 * 枚举的每种动作类型包含对应Type类的单例，并委托调用其对应的方法。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public enum StandardActionType implements ActionType {
	/**
	 * 发牌
	 */
	DEAL(new DealActionType()),
	/**
	 * 吃
	 */
	CHI(new CpgActionType(CHI_GROUP, Collections.singleton(PREVIOUS))),
	/**
	 * 碰
	 */
	PENG(new CpgActionType(PENG_GROUP)),
	/**
	 * 直杠
	 */
	ZHIGANG(new CpgActionType(ZHIGANG_GROUP)),
	/**
	 * 补杠
	 */
	BUGANG(new BugangActionType()),
	/**
	 * 暗杠
	 */
	ANGANG(new AngangActionType()),
	/**
	 * 补花
	 */
	BUHUA(new BuhuaActionType()),
	/**
	 * 打牌
	 */
	DISCARD(new DiscardActionType()),
	/**
	 * 打牌的同时听牌
	 */
	DISCARD_WITH_TING(new DiscardWithTingActionType()),
	/**
	 * 摸牌
	 */
	DRAW(new DrawActionType()),
	/**
	 * 摸底牌
	 */
	DRAW_BOTTOM(new DrawBottomActionType()),
	/**
	 * 和牌
	 */
	WIN(new WinActionType()),
	/**
	 * 流局
	 */
	LIUJU(new LiujuActionType());

	private final ActionType type;

	private StandardActionType(ActionType type) {
		this.type = type;
	}

	// 以下都是委托方法，调用type的对应方法

	@Override
	public boolean canDo(GameContext context, PlayerLocation location) {
		return type.canDo(context, location);
	}

	@Override
	public boolean canPass(GameContext context, PlayerLocation location) {
		return type.canPass(context, location);
	}

	@Override
	public Collection<Set<Tile>> getLegalActionTiles(
			GameContext.PlayerView context) {
		return type.getLegalActionTiles(context);
	}

	@Override
	public void doAction(GameContext context, PlayerLocation location, Action action) throws IllegalActionException {
		type.doAction(context, location, action);
	}

	@Override
	public boolean matchBy(ActionType testType) {
		return type.matchBy(testType);
	}

	@Override
	public Class<? extends ActionType> getRealTypeClass() {
		return type.getRealTypeClass();
	}

	@Override
	public ActionType getRealTypeObject() {
		return type;
	}

	@Override
	public boolean isLegalAction(GameContext context, PlayerLocation location, Action action) {
		return type.isLegalAction(context, location, action);
	}

}
