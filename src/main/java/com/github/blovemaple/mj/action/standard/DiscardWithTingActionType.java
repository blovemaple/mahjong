package com.github.blovemaple.mj.action.standard;

import static com.github.blovemaple.mj.utils.MyUtils.*;

import java.util.HashSet;
import java.util.Set;

import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.rule.GameStrategy;
import com.github.blovemaple.mj.rule.win.WinInfo;

/**
 * 动作类型“打牌的同时听牌”。与打牌动作的区别是：
 * <li>合法性要判断打出后是否可以听；
 * <li>执行动作时要设置听牌状态。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class DiscardWithTingActionType extends DiscardActionType {

	protected DiscardWithTingActionType() {
	}

	@Override
	protected boolean isLegalActionWithPreconition(GameContextPlayerView context, Set<Tile> tiles) {
		GameStrategy strategy = context.getGameStrategy();
		PlayerInfo playerInfo = context.getMyInfo();
		Set<Tile> remainAliveTiles = new HashSet<>(playerInfo.getAliveTiles());
		remainAliveTiles.removeAll(tiles);

		return
		// 获取所有牌的流
		strategy.getAllTiles().stream()
				// 只留下id==0的牌
				.filter(tileToGet -> tileToGet.id() == 0)
				// 与打出动作牌后的aliveTiles合并，看任何一种合并后的aliveTiles能否和牌
				.anyMatch(tileToGet -> {
					WinInfo winInfo = WinInfo.fromPlayerTiles(playerInfo, tileToGet, false);
					winInfo.setAliveTiles(mergedSet(remainAliveTiles, tileToGet));
					winInfo.setContextView(context);
					return strategy.canWin(winInfo);
				});
	}

	@Override
	protected void doLegalAction(GameContext context, PlayerLocation location, Set<Tile> tiles) {
		super.doLegalAction(context, location, tiles);
		context.getPlayerInfoByLocation(location).setTing(true);
	}

}
