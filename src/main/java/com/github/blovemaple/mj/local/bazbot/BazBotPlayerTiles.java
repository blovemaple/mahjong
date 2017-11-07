package com.github.blovemaple.mj.local.bazbot;

import java.util.List;
import java.util.Set;

import com.github.blovemaple.mj.object.PlayerTiles;
import com.github.blovemaple.mj.object.TileType;

/**
 * {@link BazBot}的一手牌，提供计算和牌所需牌型的功能。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class BazBotPlayerTiles {

	private PlayerTiles playerTiles;

	/**
	 * 构建实例。
	 * 
	 * @param playerTiles
	 */
	public BazBotPlayerTiles(PlayerTiles playerTiles) {
		this.playerTiles = playerTiles;
	}

	public Set<List<TileType>> tileTypesToWin() {
		@SuppressWarnings("unused")
		List<BazBotTileNeighborhood> neighborhoods = BazBotTileNeighborhood.parse(playerTiles.getAliveTiles());

		// TODO
		// 先选尽量多的完整顺刻，再选完整将牌。
		// 在选择完整顺刻的过程中，如果某一组完整将牌一直因为与完整顺刻相冲突而没有选中，则把所有选中的组合复制一份，去掉冲突的完整顺刻，把将牌替换成这组将牌
		// 顺刻不够（注意所需的顺刻数要减去已有的groups）的组合尽量在缺一张的不完整顺刻中选，再不够就在缺两张的不完整顺刻中选
		// 没有将牌的组合在不完整将牌中选
		// 过滤掉缺的牌和丢弃的牌有重复的组合
		// 计算所有组合缺的牌，转换成牌型list，list内部排序后添加进set去重

		return null;
	}

}
