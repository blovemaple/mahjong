package com.github.blovemaple.mj.rule.gb.fan;

import java.util.List;

import com.github.blovemaple.mj.object.StandardTileUnitType;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileRank;
import com.github.blovemaple.mj.object.TileRank.NumberRank;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.object.TileUnit;
import com.github.blovemaple.mj.rule.simple.NormalWinType;
import com.github.blovemaple.mj.rule.win.FanTypeMatcher;
import com.github.blovemaple.mj.rule.win.WinInfo;

/**
 * 边张。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class BianZhang implements FanTypeMatcher {

	@Override
	public int matchCount(WinInfo winInfo) {
		Tile winTile = winInfo.getWinTile();
		if (winTile == null)
			// 没有winTile
			return 0;

		TileType winTileType = winTile.type();
		TileRank<?> winTileRank = winTileType.rank();
		if (winTileRank != NumberRank.SAN && winTileRank != NumberRank.QI)
			// 和牌不是三或七
			return 0;

		List<List<TileUnit>> unitsList = winInfo.getUnits().get(NormalWinType.get());
		if (unitsList == null || unitsList.isEmpty())
			// 不是NormalWinType
			return 0;

		TileType requiredType, forbiddenType;
		switch ((NumberRank) winTileRank) {
		case SAN:
			// 和3，需要123，禁止345
			requiredType = TileType.of(winTileType.suit(), NumberRank.YI);
			forbiddenType = winTileType;
			break;
		case QI:
			// 和7，需要789，禁止567
			requiredType = winTileType;
			forbiddenType = TileType.of(winTileType.suit(), NumberRank.WU);
			break;
		default:
			throw new RuntimeException();
		}

		boolean requiredExists = false;
		for (List<TileUnit> units : unitsList) {
			for (TileUnit unit : units) {
				if (unit.getType() != StandardTileUnitType.SHUNZI)
					continue;
				if (!requiredExists && unit.getFirstTileType() == requiredType)
					requiredExists = true;
				if (unit.getFirstTileType() == forbiddenType)
					// 存在345或567顺子
					return 0;
			}
		}
		if (!requiredExists)
			// 不存在123或789顺子
			return 0;
		return 1;
	}

}
