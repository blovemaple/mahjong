package com.github.blovemaple.mj.rule.gb.fan;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;
import static com.github.blovemaple.mj.object.TileGroupType.*;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroup;
import com.github.blovemaple.mj.object.TileGroupType;
import com.github.blovemaple.mj.rule.win.FanTypeMatcher;
import com.github.blovemaple.mj.rule.win.WinInfo;

import static java.util.stream.Collectors.*;

/**
 * 四归一。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class SiGuiYi implements FanTypeMatcher {

	private static final Set<TileGroupType> GANG_GROUPS = EnumSet.of(ANGANG_GROUP, BUGANG_GROUP, ZHIGANG_GROUP);

	@Override
	public int matchCount(WinInfo winInfo) {
		Stream<Tile> tilesWithoutGang = winInfo.getAliveTiles().stream();
		for (TileGroup group : winInfo.getTileGroups()) {
			if (GANG_GROUPS.contains(group.getType()))
				continue;
			tilesWithoutGang = Stream.concat(tilesWithoutGang, group.getTiles().stream());
		}

		return tilesWithoutGang.collect(groupingBy(Tile::type, counting())) //
				.values().contains(4L) ? 1 : 0;
	}

}
