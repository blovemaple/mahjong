package com.github.blovemaple.mj.action.standard;

import static com.github.blovemaple.mj.object.TileGroupType.*;

import java.util.Set;
import java.util.function.Predicate;

import com.github.blovemaple.mj.action.AbstractActionType;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContext.PlayerView;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroup;

/**
 * 动作类型“补花”。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class BuhuaActionType extends AbstractActionType {

	@Override
	public boolean canPass(GameContext context, PlayerLocation location) {
		return true;
	}

	@Override
	protected Predicate<Integer> getAliveTileSizePrecondition() {
		return size -> size % 3 == 2;
	}

	@Override
	protected int getActionTilesSize() {
		return BUHUA_GROUP.size();
	}

	@Override
	protected boolean isLegalActionWithPreconition(PlayerView context,
			Set<Tile> tiles) {
		return BUHUA_GROUP.isLegalTiles(tiles);
	}

	@Override
	protected void doLegalAction(GameContext context, PlayerLocation location,
			Set<Tile> tiles) {
		PlayerInfo playerInfo = context.getPlayerInfoByLocation(location);
		playerInfo.getAliveTiles().removeAll(tiles);
		playerInfo.getTileGroups().add(new TileGroup(BUHUA_GROUP, tiles));
	}

}
