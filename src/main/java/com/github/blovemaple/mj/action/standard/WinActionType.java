package com.github.blovemaple.mj.action.standard;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;
import static com.github.blovemaple.mj.utils.MyUtils.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.blovemaple.mj.action.AbstractActionType;
import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionTypeAndLocation;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContext.PlayerView;
import com.github.blovemaple.mj.game.GameResult;
import com.github.blovemaple.mj.game.rule.FanType;
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
	protected Collection<ActionTypeAndLocation> getLastActionPrecondition(
			PlayerLocation location) {
		// 必须是发牌、自己摸牌，或别人打牌后
		List<ActionTypeAndLocation> otherDiscard = Stream
				.of(PlayerLocation.values())
				.filter(l -> location.getRelationOf(l).isOther())
				.map(otherLocation -> new ActionTypeAndLocation(DISCARD,
						otherLocation))
				.collect(Collectors.toList());
		return newMergedSet(otherDiscard, //
				new ActionTypeAndLocation(DEAL, null),
				new ActionTypeAndLocation(DRAW, location));
	}

	@Override
	protected int getActionTilesSize() {
		return 0;
	}

	@Override
	protected boolean isLegalActionWithPreconition(PlayerView context,
			Set<Tile> tiles) {
		PlayerInfo playerInfo = context.getMyInfo();
		Action lastAction = context.getLastAction();
		Set<Tile> aliveTiles = DISCARD.matchBy(lastAction.getType())
				? newMergedSet(playerInfo.getAliveTiles(), lastAction.getTile())
				: null;
		return context.getGameStrategy().getAllWinTypes().stream()
				.anyMatch(winType -> winType.canWin(playerInfo, aliveTiles));
	}

	@Override
	protected void doLegalAction(GameContext context, PlayerLocation location,
			Set<Tile> tiles) {
		Action lastAction = context.getLastAction();

		GameResult result = new GameResult(context.getTable().getPlayerInfos(),
				context.getZhuangLocation());
		result.setWinnerLocation(location);
		if (DRAW.matchBy(lastAction.getType())) {
			result.setWinTile(
					context.getPlayerView(location).getJustDrawedTile());
		} else {
			result.setPaoerLocation(context.getLastActionLocation());
			result.setWinTile(lastAction.getTile());
		}

		// 算番

		PlayerInfo playerInfo = context.getPlayerInfoByLocation(location);
		Map<? extends FanType, Integer> allFanTypes = context.getGameStrategy()
				.getAllFanTypes();
		// 在所有番种中过滤出所有符合的番种
		Set<FanType> fanTypes = allFanTypes.keySet().stream()
				.filter(fanType -> fanType.match(playerInfo))
				.collect(Collectors.toSet());
		// 去除被覆盖的番种
		Map<? extends FanType, Set<? extends FanType>> coveredFanTypes = context
				.getGameStrategy().getAllCoveredFanTypes();
		if (coveredFanTypes != null)
			coveredFanTypes.forEach((type, covered) -> {
				if (fanTypes.contains(type))
					fanTypes.removeAll(covered);
			});
		// 查询番数组成map
		Map<FanType, Integer> fans = fanTypes.stream().collect(
				Collectors.toMap(Function.identity(), allFanTypes::get));
		result.setFans(fans);

		context.setGameResult(result);
	}

}
