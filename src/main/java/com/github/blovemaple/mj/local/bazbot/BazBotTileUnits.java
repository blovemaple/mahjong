package com.github.blovemaple.mj.local.bazbot;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.github.blovemaple.mj.local.bazbot.BazBotTileUnit.BazBotTileUnitType;

/**
 * BazBotTileUnit组合。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
class BazBotTileUnits {
	/**
	 * neighborbood - unit列表。
	 */
	private Map<BazBotTileNeighborhood, List<BazBotTileUnit>> unitsByNeighborhood;

	public BazBotTileUnits(Collection<BazBotTileNeighborhood> neighborhoods) {
		unitsByNeighborhood = new LinkedHashMap<>();
		neighborhoods.forEach(hood -> unitsByNeighborhood.put(hood, new ArrayList<>()));
	}

	public BazBotTileUnits(BazBotTileUnits original, BazBotTileUnits newUnits) {
		// 拷贝original的unitsByNeighborhood
		this.unitsByNeighborhood = new LinkedHashMap<>();
		original.unitsByNeighborhood
				.forEach((hood, units) -> this.unitsByNeighborhood.put(hood, new ArrayList<>(units)));

		// 把newUnits填入unitsByNeighborhood
		if (newUnits != null)
			newUnits.unitsByNeighborhood.forEach((hood, units) -> this.unitsByNeighborhood.get(hood).addAll(units));
	}

	private BazBotTileUnits(Map<BazBotTileNeighborhood, List<BazBotTileUnit>> unitsByNeighborhood) {
		this.unitsByNeighborhood = unitsByNeighborhood;
	}

	protected Collection<BazBotTileNeighborhood> neighborhoods() {
		return unitsByNeighborhood.keySet();
	}

	public void forEachUnit(Consumer<? super BazBotTileUnit> action) {
		unitsByNeighborhood.values().forEach(units -> units.forEach(action));
	}

	public List<BazBotTileUnit> unitsOfHood(BazBotTileNeighborhood hood) {
		return unitsByNeighborhood.get(hood);
	}

	public BazBotTileUnits nonConflicts(BazBotTileUnitType type) {
		return new BazBotTileUnits( //
				neighborhoods().stream()
						.collect(toMap(identity(), hood -> hood.getNonConflictingUnits(type, unitsOfHood(hood)))) //
		);
	}

	/**
	 * 挑选符合数量要求的unit组合，返回所有的可能。<br>
	 * 先找到第一个unit，然后把数量要求减1、去除与第一个unit冲突者，并递归调用，最后把第一个unit和递归调用的结果分别拼接并返回。
	 * 
	 * @param forUnitCount
	 *            需要返回的组合中的unit数量
	 * @return 所有符合要求的unit组合列表
	 */
	public List<BazBotTileUnits> allCombs(int forUnitCount) {
		if (forUnitCount <= 0) {
			// 数量要求为0，选择一个空组合
			return List.of(new BazBotTileUnits(neighborhoods()));
		}

		List<BazBotTileUnits> res = new ArrayList<>();

		unitsByNeighborhood.entrySet().stream()
				// 找第一个候选unit
				.filter(hoodAndUnits -> !hoodAndUnits.getValue().isEmpty()).findFirst()
				.ifPresentOrElse(firstHoodAndUnits -> {
					// 找到了第一个候选unit
					BazBotTileNeighborhood hood = firstHoodAndUnits.getKey();
					List<BazBotTileUnit> unitsOfHood = firstHoodAndUnits.getValue();
					BazBotTileUnit firstUnit = unitsOfHood.get(0);

					// 剩余Units
					BazBotTileUnits remains = new BazBotTileUnits(this, null);
					remains.unitsByNeighborhood.put(hood,
							unitsOfHood.stream().filter(unit -> firstUnit.conflictWith(unit)).collect(toList()));

					// 递归取剩余Units的所有组合，并拼接第一个unit
					remains.allCombs(forUnitCount - 1).stream().peek(units -> units.add(hood, firstUnit))
							.forEach(res::add);

				}, () -> {
					// 没有候选units，无法选择。下面直接返回空列表。
				});

		return res;
	}

	private void add(BazBotTileNeighborhood hood, BazBotTileUnit newUnit) {
		unitsByNeighborhood.get(hood).add(newUnit);
	}
}