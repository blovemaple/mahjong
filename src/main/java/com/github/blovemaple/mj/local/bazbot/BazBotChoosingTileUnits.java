package com.github.blovemaple.mj.local.bazbot;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.github.blovemaple.mj.local.bazbot.BazBotTileUnit.BazBotTileUnitType;
import com.github.blovemaple.mj.object.TileType;

/**
 * BazBot从自己的牌中挑选出的unit组合。最终用于计算tileTypesToWin。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
class BazBotChoosingTileUnits extends BazBotTileUnits {
	/**
	 * 是否缺少将牌。
	 */
	private boolean forJiang;
	/**
	 * 缺少的顺刻数。
	 */
	private int forShunkeCount;

	/**
	 * 构建一个初始的TileUnits，没有选择任何unit。
	 */
	public BazBotChoosingTileUnits(Collection<BazBotTileNeighborhood> neighborhoods, int crtGroupCount) {
		super(neighborhoods);
		forJiang = true;
		forShunkeCount = 4 - crtGroupCount;
	}

	/**
	 * 从指定的TileUnits和要添加的units构建实例。
	 * 
	 * @param original
	 *            原始TileUnits
	 * @param newUnits
	 *            要添加的units
	 */
	private BazBotChoosingTileUnits(BazBotChoosingTileUnits original, BazBotTileUnits newUnits) {
		super(original, newUnits);

		this.forJiang = original.forJiang;
		this.forShunkeCount = original.forShunkeCount;
		forEachUnit(unit -> {
			if (unit.type().isJiang()) {
				if (!this.forJiang)
					throw new RuntimeException("Redundant JIANG unit.");
				this.forJiang = false;
			} else {
				if (this.forShunkeCount <= 0)
					throw new RuntimeException("Redundant SHUNKE unit.");
				this.forShunkeCount--;
			}
		});
	}

	/**
	 * 从剩余的units中挑选尽量多指定类型的units，对所有可能的选择生成补充后的TileUnits并返回Stream。<br>
	 * 生成的都是新实例，不改变当前实例。
	 * 
	 * @param type
	 *            挑选的unit类型
	 * @param includeEmpty
	 *            是否包含不补充任何unit
	 * @return 新TileUnits的Stream
	 */
	public Stream<BazBotChoosingTileUnits> newToChoose(BazBotTileUnitType type, boolean includeEmpty) {
		if (type.isJiang()) {
			if (!forJiang)
				return Stream.of(this);
		} else {
			if (forShunkeCount == 0)
				return Stream.of(this);
		}

		// 在所有与当前所选不冲突的units中得出符合缺数的所有unit组合
		List<BazBotTileUnits> unitCombs = nonConflicts(type).allCombs(type.isJiang() ? 1 : forShunkeCount);

		// 包括不选择
		if (includeEmpty)
			unitCombs.add(new BazBotTileUnits(neighborhoods()));

		// 复制当前TileUnits并拼接
		return unitCombs.stream().map(comb -> new BazBotChoosingTileUnits(this, comb));
	}

	/**
	 * 计算从当前选择的units到和牌所需的牌型列表，把所有可能的牌型列表组成Stream并返回。
	 */
	public Stream<List<TileType>> tileTypesToWin() {

		// TODO
		// TODO 过滤掉缺的牌和丢弃的牌有重复的组合
		return null;
	}

}