package com.github.blovemaple.mj.local.foobot;

import java.util.Collection;
import java.util.Set;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.action.IllegalActionException;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContext.PlayerView;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * 什么都不做的动作类型。对一个context进行模拟时，如果非流局，必须选择一个动作供gameTool返回，则默认选择这个动作。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class FooSimEmptyActionType implements ActionType {

	private static final FooSimEmptyActionType i = new FooSimEmptyActionType();

	/**
	 * 单例。
	 */
	public static FooSimEmptyActionType type() {
		return i;
	}

	private static final Action EMPTY_ACTION = new Action(
			FooSimEmptyActionType.type());

	public static Action action() {
		return EMPTY_ACTION;
	}

	private FooSimEmptyActionType() {
	}

	@Override
	public boolean canDo(GameContext context, PlayerLocation location) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean canPass(GameContext context, PlayerLocation location) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Set<Tile>> getLegalActionTiles(PlayerView context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isLegalAction(GameContext context, PlayerLocation location,
			Action action) {
		return true;
	}

	@Override
	public void doAction(GameContext context, PlayerLocation location,
			Action action) throws IllegalActionException {
		return;
	}

}
