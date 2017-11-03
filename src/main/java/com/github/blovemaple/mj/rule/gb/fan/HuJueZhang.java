package com.github.blovemaple.mj.rule.gb.fan;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;
import static com.github.blovemaple.mj.object.TileGroupType.*;

import java.util.stream.Stream;

import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroup;
import com.github.blovemaple.mj.rule.win.FanTypeMatcher;
import com.github.blovemaple.mj.rule.win.WinInfo;

/**
 * 和绝张。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class HuJueZhang implements FanTypeMatcher {

	@Override
	public int matchCount(WinInfo winInfo) {
		Tile winTile = winInfo.getWinTile();
		if (winTile == null)
			// 没有winType
			return 0;

		GameContext.PlayerView contextView = winInfo.getContextView();
		if (contextView == null)
			// 没有contextView
			return 0;

		Stream<Tile> showTiles = Stream.empty();
		for (PlayerInfo.PlayerView playerInfoView : contextView.getTableView().getPlayerInfoView().values()) {
			// 打出的牌
			showTiles = Stream.concat(showTiles, playerInfoView.getDiscardedTiles().stream());
			for (TileGroup group : playerInfoView.getTileGroups()) {
				if (group.getType() != ANGANG_GROUP)
					// 已亮明的group的牌
					showTiles = Stream.concat(showTiles, group.getTiles().stream());
			}
		}

		int requiredCount;
		if (DISCARD.matchBy(contextView.getLastAction().getType()))
			// 和别人打出的牌，算上这张一共4张
			requiredCount = 4;
		else
			requiredCount = 3;

		if (!showTiles.map(Tile::type).filter(type -> type == winTile.type()).skip(requiredCount - 1).findAny()
				.isPresent())
			// 已亮明的牌数不够
			return 0;

		return 1;
	}

}
