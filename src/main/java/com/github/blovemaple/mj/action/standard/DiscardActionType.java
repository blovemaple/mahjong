package com.github.blovemaple.mj.action.standard;

import java.util.Set;
import java.util.function.Predicate;

import com.github.blovemaple.mj.action.AbstractActionType;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * 动作类型“打牌”。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class DiscardActionType extends AbstractActionType {

	@Override
	public boolean canPass(GameContext context, PlayerLocation location) {
		return false;
	}

	@Override
	protected Predicate<Integer> getAliveTileSizePrecondition() {
		return size -> size % 3 == 2;
	}

	@Override
	protected int getActionTilesSize() {
		return 1;
	}

	@Override
	protected boolean isLegalActionWithPreconition(GameContextPlayerView context, Set<Tile> tiles) {
		if (!context.getMyInfo().isTing()) {
			// 没听牌时，所有aliveTiles都可以打出
			return true;
		} else {
			// 听牌后只允许打出最后摸的牌
			Tile justDrawed = context.getJustDrawedTile();
			return justDrawed != null
					&& justDrawed.equals(tiles.iterator().next());
		}
	}

	@Override
	protected void doLegalAction(GameContext context, PlayerLocation location, Set<Tile> tiles) {
		PlayerInfo playerInfo = context.getPlayerInfoByLocation(location);
		playerInfo.removeAliveTiles(tiles);
		playerInfo.addDiscardedTiles(tiles);
	}

}
