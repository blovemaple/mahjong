package com.github.blovemaple.mj.rule.gb.fan;

import static com.github.blovemaple.mj.object.TileGroupType.*;
import com.github.blovemaple.mj.object.TileGroup;
import com.github.blovemaple.mj.rule.win.FanTypeMatcher;
import com.github.blovemaple.mj.rule.win.WinInfo;

/**
 * 花牌。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class HuaPai implements FanTypeMatcher {

	@Override
	public int matchCount(WinInfo winInfo) {
		return (int) winInfo.getTileGroups().stream() //
				.map(TileGroup::getType).filter(type -> type == BUHUA_GROUP).count();
	}

}
