package com.github.blovemaple.mj.action.standard;

import static com.github.blovemaple.mj.utils.MyUtils.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContext.PlayerView;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.rule.GameStrategy;

/**
 * 动作类型“打牌的同时听牌”。与打牌动作的区别是：
 * <li>合法性要判断打出后是否可以听；
 * <li>执行动作时要设置听牌状态。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class DiscardWithTingActionType extends DiscardActionType {

	@Override
	protected boolean isLegalActionWithPreconition(PlayerView context,
			Set<Tile> tiles) {
		GameStrategy strategy = context.getGameStrategy();
		PlayerInfo playerInfo = context.getMyInfo();
		Set<Tile> remainAliveTiles = new HashSet<>(playerInfo.getAliveTiles());
		remainAliveTiles.removeAll(tiles);

		return
		// 获取所有牌的流
		strategy.getAllTiles().stream()
				// 过滤掉已经有的牌（tile=type+id）
				.filter(tileToGet -> !remainAliveTiles.contains(tileToGet))
				// 按照牌的id排序，这样能先把所有type分别检查一张，最快找到anyMatch
				.sorted(Comparator.comparing(Tile::id))
				// 与打出动作牌后的aliveTiles合并
				.map(tileToGet -> newMergedSet(remainAliveTiles, tileToGet))
				// 看任何一种合并后的aliveTiles能否符合任何一种wintype
				.anyMatch(testAliveTiles -> strategy.canWin(playerInfo,
						testAliveTiles));
	}

	@Override
	protected void doLegalAction(GameContext context, PlayerLocation location, Set<Tile> tiles) {
		super.doLegalAction(context, location, tiles);
		context.getPlayerInfoByLocation(location).setTing(true);
	}

}
