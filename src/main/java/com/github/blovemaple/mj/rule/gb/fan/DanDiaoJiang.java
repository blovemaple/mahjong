package com.github.blovemaple.mj.rule.gb.fan;

import java.util.List;

import com.github.blovemaple.mj.object.StandardTileUnitType;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.object.TileUnit;
import com.github.blovemaple.mj.rule.simple.NormalWinType;
import com.github.blovemaple.mj.rule.win.FanTypeMatcher;
import com.github.blovemaple.mj.rule.win.WinInfo;

/**
 * 单钓将。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class DanDiaoJiang implements FanTypeMatcher {

	@Override
	public int matchCount(WinInfo winInfo) {
		Tile winTile = winInfo.getWinTile();
		if (winTile == null)
			// 没有winTile
			return 0;

		List<List<TileUnit>> unitsList = winInfo.getUnits().get(NormalWinType.get());
		if (unitsList == null || unitsList.isEmpty())
			// 不是NormalWinType
			return 0;

		TileType winTileType = winTile.type();
		boolean winJiang = false;
		for (List<TileUnit> units : unitsList) {
			for (TileUnit unit : units) {
				if (unit.getType() != StandardTileUnitType.JIANG) {
					if (unit.getTiles().contains(winTile))
						// 和牌在非将牌中
						return 0;
				} else {
					if (!winJiang && unit.getFirstTileType() == winTileType)
						winJiang = true;
				}
			}
		}
		if (!winJiang)
			// 和牌不在将牌中
			return 0;
		return 1;
	}

}
