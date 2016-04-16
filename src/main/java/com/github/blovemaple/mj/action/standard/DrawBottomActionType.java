package com.github.blovemaple.mj.action.standard;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import com.github.blovemaple.mj.action.ActionTypeAndLocation;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * 动作类型“摸底牌”。与摸牌动作的区别是，前提条件为自己补花或杠之后，并且是从牌墙底部摸。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class DrawBottomActionType extends DrawActionType {

	@Override
	protected Collection<ActionTypeAndLocation> getLastActionPrecondition(
			PlayerLocation location) {
		// 必须是自己补花或杠之后
		return Arrays.asList( //
				new ActionTypeAndLocation(BUHUA, location),
				new ActionTypeAndLocation(ANGANG, location),
				new ActionTypeAndLocation(ZHIGANG, location),
				new ActionTypeAndLocation(BUGANG, location));
	}

	@Override
	protected void doLegalAction(GameContext context, PlayerLocation location,
			Set<Tile> tiles) {
		Tile tile = context.getTable().drawBottom(1).get(0);
		context.getPlayerInfoByLocation(location).getAliveTiles().add(tile);
		context.getPlayerInfoByLocation(location).setLastDrawedTile(tile);
	}

}
