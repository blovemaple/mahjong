package com.github.blovemaple.mj.rule.win.load;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileUnit;
import com.github.blovemaple.mj.rule.win.FanTypeMatcher;
import com.github.blovemaple.mj.rule.win.WinInfo;
import com.github.blovemaple.mj.rule.win.WinType;

/**
 * 动态载入的同时作为和牌类型的番种。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class LoadedWinFanType extends LoadedFanType implements WinType {

	public LoadedWinFanType(String name, FanTypeMatcher matcher, int score) {
		super(name, matcher, score);
	}

	@Override
	public int matchCount(WinInfo winInfo) {
		return parseWinTileUnits(winInfo).isEmpty() ? 0 : 1;
	}

	@Override
	public List<List<TileUnit>> parseWinTileUnits(WinInfo winInfo) {
		List<List<TileUnit>> unitList = winInfo.getUnits().get(this);
		if (unitList == null) {
			if (super.matchCount(winInfo) > 0)
				// 符合的，往winInfo塞一个空的units，反正也用不到
				unitList = Collections.singletonList(Collections.emptyList());
			else
				unitList = Collections.emptyList();
			winInfo.getUnits().put(this, unitList);
		}
		return unitList;
	}

	@Override
	public List<Tile> getDiscardCandidates(Set<Tile> aliveTiles, Collection<Tile> candidates) {
		// TODO 载入的winType参与AI
		return Collections.emptyList();
	}

	@Override
	public Stream<ChangingForWin> changingsForWin(PlayerInfo playerInfo, int changeCount, Collection<Tile> candidates) {
		// TODO 载入的winType参与AI
		return Stream.empty();
	}

}
