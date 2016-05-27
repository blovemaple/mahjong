package com.github.blovemaple.mj.action.standard;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;
import static com.github.blovemaple.mj.utils.MyUtils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

import com.github.blovemaple.mj.action.AbstractActionType;
import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionAndLocation;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContext.PlayerView;
import com.github.blovemaple.mj.game.GameResult;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * 动作类型“和牌”。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class WinActionType extends AbstractActionType {

	@Override
	public boolean canPass(GameContext context, PlayerLocation location) {
		return true;
	}

	@Override
	protected BiPredicate<ActionAndLocation, PlayerLocation> getLastActionPrecondition() {
		// 必须是发牌、自己摸牌，或别人打牌后
		return (al, location) -> DEAL.matchBy(al.getActionType()) || //
				(al.getLocation() == location ? DRAW.matchBy(al.getActionType()) : DISCARD.matchBy(al.getActionType()));
	}

	@Override
	protected int getActionTilesSize() {
		return 0;
	}

	@Override
	public boolean isLegalActionWithPreconition(PlayerView context, Set<Tile> tiles) {
		PlayerInfo playerInfo = context.getMyInfo();
		Action lastAction = context.getLastAction();
		Set<Tile> aliveTiles = DISCARD.matchBy(lastAction.getType())
				? mergedSet(playerInfo.getAliveTiles(), lastAction.getTile()) : null;
		return context.getGameStrategy().canWin(playerInfo, aliveTiles, lastAction.getTile());
	}

	@Override
	protected void doLegalAction(GameContext context, PlayerLocation location, Set<Tile> tiles) {
		Action lastAction = context.getLastAction();

		GameResult result = new GameResult(context.getTable().getPlayerInfos(), context.getZhuangLocation());
		result.setWinnerLocation(location);
		if (DRAW.matchBy(lastAction.getType())) {
			result.setWinTile(context.getPlayerView(location).getJustDrawedTile());
		} else {
			result.setPaoerLocation(context.getLastActionLocation());
			result.setWinTile(lastAction.getTile());
		}

		// 算番
		PlayerInfo playerInfo = context.getPlayerInfoByLocation(location);
		List<Tile> aliveTiles = merged(ArrayList::new, playerInfo.getAliveTiles(), result.getWinTile());
		result.setFans(context.getGameStrategy().getFans(context.getPlayerView(location), playerInfo, aliveTiles,
				result.getWinTile()));

		context.setGameResult(result);
	}

}
