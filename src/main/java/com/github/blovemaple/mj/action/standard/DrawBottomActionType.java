package com.github.blovemaple.mj.action.standard;

import static com.github.blovemaple.mj.action.standard.PlayerActionTypes.*;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.PlayerAction;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * 动作类型“摸底牌”。与摸牌动作的区别是，前提条件为自己补花或杠之后，并且是从牌墙底部摸。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class DrawBottomActionType extends DrawActionType {

	protected DrawBottomActionType() {
	}

	@Override
	protected BiPredicate<Action, PlayerLocation> getLastActionPrecondition() {
		// 必须是自己补花或杠之后
		return (a, location) -> a instanceof PlayerAction && ((PlayerAction) a).getLocation() == location
				&& Stream.of(BUHUA, ANGANG, ZHIGANG, BUGANG).anyMatch(type -> type.matchBy(a.getType()));
	}

	@Override
	protected void doLegalAction(GameContext context, PlayerLocation location, Set<Tile> tiles) {
		Tile tile = context.getTable().drawBottom(1).get(0);
		context.getPlayerInfoByLocation(location).getAliveTiles().add(tile);
		context.getPlayerInfoByLocation(location).setLastDrawedTile(tile);
	}

}
