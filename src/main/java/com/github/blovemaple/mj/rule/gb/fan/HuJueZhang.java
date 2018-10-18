package com.github.blovemaple.mj.rule.gb.fan;

import static com.github.blovemaple.mj.action.standard.PlayerActionTypes.*;
import static com.github.blovemaple.mj.object.TileGroupType.*;

import java.util.stream.Stream;

import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.object.PlayerInfoPlayerView;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroupPlayerView;
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

		GameContextPlayerView contextView = winInfo.getContextView();
		if (contextView == null)
			// 没有contextView
			return 0;

		Stream<Tile> showTiles = Stream.empty();
		for (PlayerInfoPlayerView playerInfoView : contextView.getTableView().getPlayerInfoView().values()) {
			// 打出的牌
			showTiles = Stream.concat(showTiles, playerInfoView.getDiscardedTiles().stream());
			for (TileGroupPlayerView group : playerInfoView.getTileGroups()) {
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
