package com.github.blovemaple.mj.action.standard;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;

import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

import com.github.blovemaple.mj.action.AbstractActionType;
import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionAndLocation;
import com.github.blovemaple.mj.action.IllegalActionException;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContext.PlayerView;
import com.github.blovemaple.mj.game.GameResult;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.rule.fan.FanType;
import com.github.blovemaple.mj.rule.win.WinInfo;

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
		WinInfo winInfo = new WinInfo();
		winInfo.setContextView(context);
		return context.getGameStrategy().canWin(winInfo);
	}

	@Override
	// XXX - 为了避免验证legal和算番时重复判断和牌，doAction时不进行legal验证，需要此方法的调用方保证legal（目前已保证）。
	public void doAction(GameContext context, PlayerLocation location, Action action) throws IllegalActionException {
		Action lastAction = context.getLastAction();

		GameResult result = new GameResult(context.getTable().getPlayerInfos(), context.getZhuangLocation());
		result.setWinnerLocation(location);
		if (DRAW.matchBy(lastAction.getType())) {
			result.setWinTile(context.getPlayerView(location).getJustDrawedTile());
		} else {
			result.setPaoerLocation(context.getLastActionLocation());
			result.setWinTile(lastAction.getTile());
		}

		// 和牌parse units、算番
		WinInfo winInfo = new WinInfo();
		winInfo.setContextView(context.getPlayerView(location));

		Map<FanType, Integer> fans = context.getGameStrategy().getFans(winInfo);
		if (fans.isEmpty() && (winInfo.getUnits() == null || winInfo.getUnits().isEmpty()))
			throw new IllegalActionException(context, location, action);
		result.setFans(fans);

		context.setGameResult(result);
	}

	@Override
	protected void doLegalAction(GameContext context, PlayerLocation location, Set<Tile> tiles) {
		throw new UnsupportedOperationException();
	}

}
