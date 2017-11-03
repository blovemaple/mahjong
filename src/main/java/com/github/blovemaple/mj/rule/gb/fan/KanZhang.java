package com.github.blovemaple.mj.rule.gb.fan;

import static com.github.blovemaple.mj.object.TileRank.NumberRank.*;

import java.util.ArrayList;
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
 * 坎张。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class KanZhang implements FanTypeMatcher {

	@Override
	public int matchCount(WinInfo winInfo) {
		Tile winTile = winInfo.getWinTile();
		if (winTile == null)
			// 没有winTile
			return 0;

		TileType winTileType = winTile.type();
		TileRank<?> winTileRank = winTileType.rank();
		if (!(winTileRank instanceof NumberRank) || winTileRank == YI || winTileRank == JIU)
			// 和牌不是[2,8]的数字
			return 0;

		int winTileNumber = ((NumberRank) winTileRank).number();

		List<List<TileUnit>> unitsList = winInfo.getUnits().get(NormalWinType.get());
		if (unitsList == null || unitsList.isEmpty())
			// 不是NormalWinType
			return 0;

		TileType requiredType;
		List<TileType> forbiddenTypes = new ArrayList<>();
		// 和n，需要n-1顺子，禁止n-2、n顺子
		requiredType = TileType.of(winTileType.suit(), NumberRank.ofNumber(winTileNumber - 1));
		if (winTileNumber >= 3)
			forbiddenTypes.add(TileType.of(winTileType.suit(), NumberRank.ofNumber(winTileNumber - 2)));
		if (winTileNumber <= 7)
			forbiddenTypes.add(winTileType);

		boolean requiredExists = false;
		for (List<TileUnit> units : unitsList) {
			for (TileUnit unit : units) {
				if (unit.getType() != StandardTileUnitType.SHUNZI)
					continue;
				if (!requiredExists && unit.getFirstTileType() == requiredType)
					requiredExists = true;
				if (forbiddenTypes.contains(unit.getFirstTileType()))
					// 存在n-2或n顺子
					return 0;
			}
		}
		if (!requiredExists)
			// 不存在n-1顺子
			return 0;
		return 1;
	}

}
