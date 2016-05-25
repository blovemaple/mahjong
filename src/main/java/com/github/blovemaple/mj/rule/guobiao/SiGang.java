package com.github.blovemaple.mj.rule.guobiao;

import static com.github.blovemaple.mj.object.TileGroupType.*;

import java.util.EnumSet;
import java.util.Set;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroup;
import com.github.blovemaple.mj.object.TileGroupType;
import com.github.blovemaple.mj.rule.AbstractFanType;

/**
 * 四杠。有四副杠牌。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class SiGang extends AbstractFanType {

	public SiGang() {
		setUseAliveTiles(false);
		addCacheKey(PlayerInfo::getTileGroups);
	}

	private static final Set<TileGroupType> GANG_TYPES = EnumSet.of(ZHIGANG_GROUP, ANGANG_GROUP, BUGANG_GROUP);

	@Override
	public boolean matchWithoutCache(PlayerInfo playerInfo, Set<Tile> realAliveTiles) {
		if (playerInfo.getTileGroups().size() != 4)
			return false;
		return playerInfo.getTileGroups().stream().map(TileGroup::getType).allMatch(GANG_TYPES::contains);
	}

}
