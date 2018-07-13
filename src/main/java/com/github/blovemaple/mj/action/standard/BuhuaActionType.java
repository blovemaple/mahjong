package com.github.blovemaple.mj.action.standard;

import static com.github.blovemaple.mj.action.standard.AutoActionTypes.*;
import static com.github.blovemaple.mj.object.TileGroupType.*;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.github.blovemaple.mj.action.AbstractPlayerActionType;
import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroup;

/**
 * 动作类型“补花”。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class BuhuaActionType extends AbstractPlayerActionType {

	protected BuhuaActionType() {
	}

	@Override
	public boolean canPass(GameContext context, PlayerLocation location) {
		return true;
	}

	protected boolean meetPrecondition(GameContextPlayerView context) {
		// 以下两个条件只需要满足其一

		// 验证aliveTiles数量条件
		Predicate<Integer> aliveTileSizeCondition = getAliveTileSizePrecondition();
		if (aliveTileSizeCondition != null)
			if (aliveTileSizeCondition.test(context.getMyInfo().getAliveTiles().size()))
				return true;

		// 验证上一个动作条件
		BiPredicate<Action, PlayerLocation> lastActionPrecondition = getLastActionPrecondition();
		if (lastActionPrecondition != null) {
			Action lastAction = context.getLastAction();
			if (lastAction != null)
				if (lastActionPrecondition.test(lastAction, context.getMyLocation()))
					return true;
		}

		return false;
	}

	@Override
	protected Predicate<Integer> getAliveTileSizePrecondition() {
		return size -> size % 3 == 2;
	}

	@Override
	protected BiPredicate<Action, PlayerLocation> getLastActionPrecondition() {
		return (a, l) -> DEAL.matchBy(a.getType());
	}

	@Override
	protected int getActionTilesSize() {
		return BUHUA_GROUP.size();
	}

	@Override
	protected boolean isLegalActionWithPreconition(GameContextPlayerView context,
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
