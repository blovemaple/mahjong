package com.github.blovemaple.mj.local.bazbot;

import static com.github.blovemaple.mj.local.bazbot.BazBotTileUnit.BazBotTileUnitType.*;
import static java.util.Comparator.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.blovemaple.mj.object.PlayerTiles;
import com.github.blovemaple.mj.object.TileType;

/**
 * {@link BazBot}的一手牌，提供计算和牌所需牌型的功能。<br>
 * 不缓存，一次性使用。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class BazBotPlayerTiles {

	private PlayerTiles playerTiles;

	private List<BazBotTileNeighborhood> neighborhoods;

	/**
	 * 构建实例。
	 * 
	 * @param playerTiles
	 */
	public BazBotPlayerTiles(PlayerTiles playerTiles) {
		this.playerTiles = playerTiles;
	}

	public List<List<TileType>> tileTypesToWin() {
		neighborhoods = BazBotTileNeighborhood.parse(playerTiles.getAliveTiles());

		return Stream.of(new BazBotChoosingTileUnits(neighborhoods, playerTiles.getTileGroups().size())) // 一个初始units，为flatmap做准备
				.flatMap(units -> units.newToChoose(COMPLETE_JIANG, true)) // 选所有完整将牌，以及不选完整将牌
				.flatMap(units -> units.newToChoose(COMPLETE_SHUNKE, false)) // 选所有合适的完整顺刻组合
				.flatMap(units -> units.newToChoose(UNCOMPLETE_SHUNKE_FOR_ONE, false)) // 选所有合适的不完整顺刻组合（缺一张的）
				.flatMap(units -> units.newToChoose(UNCOMPLETE_SHUNKE_FOR_TWO, false)) // 选所有合适的不完整顺刻组合（缺两张的）
				.flatMap(units -> units.newToChoose(UNCOMPLETE_JIANG, false)) // 选所有合适的不完整将牌
				.flatMap(BazBotChoosingTileUnits::tileTypesToWin) // 计算tileUnits和牌所需牌型
				.peek(tileTypes -> tileTypes.sort(naturalOrder())) // 每组tileType内部排序，准备去重
				.distinct() // 去重
				.collect(Collectors.toList()) // 收集结果
		;
	}

}
