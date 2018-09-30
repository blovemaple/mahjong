package com.github.blovemaple.mj.action.standard;

import static com.github.blovemaple.mj.object.TileGroupType.*;

import java.util.Set;

import com.github.blovemaple.mj.action.AbstractPlayerActionType;
import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.action.PlayerAction;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroup;
import com.github.blovemaple.mj.rule.simple.BeforePlayingStage;
import com.github.blovemaple.mj.rule.simple.PlayingStage;
import com.google.common.collect.Streams;

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
		switch (context.getStageName()) {
		case BeforePlayingStage.NAME:
			// 如果在BEFORE_PLAYING阶段，须满足上一个动作非补花
			ActionType lastActionType = Streams
					.findLast(context.getDoneActions().stream()
							.filter(action -> (action instanceof PlayerAction)
									&& ((PlayerAction) action).getLocation() == context.getMyLocation()))
					.map(Action::getType).orElse(null);
			if (this.matchBy(lastActionType))
				return false;
			break;
		case PlayingStage.NAME:
			// 如果在PLAYING阶段，须满足aliveTiles为待出牌的状态
			int aliveTileSize = context.getMyInfo().getAliveTiles().size();
			if (aliveTileSize % 3 != 2)
				return false;
			break;
		default:
			return false;
		}
		return true;
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
