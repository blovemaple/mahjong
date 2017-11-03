package com.github.blovemaple.mj.rule.gb.fan;

import com.github.blovemaple.mj.object.TileGroup;
import com.github.blovemaple.mj.rule.win.FanTypeMatcher;
import com.github.blovemaple.mj.rule.win.WinInfo;
import static com.github.blovemaple.mj.object.TileGroupType.*;

/**
 * 门前清。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class MenQianQing implements FanTypeMatcher {

	@Override
	public int matchCount(WinInfo winInfo) {
		return winInfo.getTileGroups().stream().map(TileGroup::getType) //
				.filter(type -> type != ANGANG_GROUP).findAny().isPresent() ? 0 : 1;
	}

}
