package com.github.blovemaple.mj.local.foobot;

import java.util.Set;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.IllegalActionException;
import com.github.blovemaple.mj.action.standard.WinActionType;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.local.foobot.FooSimContext.FooSimOthers;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * 模拟和牌动作。非本家只用来判断前提条件，只要前提条件符合就算合法，因为符合前提条件后就需要估计此家的立即和牌概率，不符合就不用估计。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class FooSimWinActionType extends WinActionType {

	private static final FooSimWinActionType i = new FooSimWinActionType();

	/**
	 * 单例。
	 */
	public static FooSimWinActionType type() {
		return i;
	}

	private FooSimWinActionType() {
	}

	@Override
	public boolean isLegalActionWithPreconition(GameContext.PlayerView context, Set<Tile> tiles) {
		if (context.getMyInfo().getPlayer() instanceof FooSimOthers)
			// 非本家只要前提条件符合就作为可选动作类型
			return true;

		return super.isLegalActionWithPreconition(context, tiles);
	}

	@Override
	public void doAction(GameContext context, PlayerLocation location, Action action) throws IllegalActionException {
		// 只用来判断前提条件，用不到执行
		throw new UnsupportedOperationException();
	}

}
