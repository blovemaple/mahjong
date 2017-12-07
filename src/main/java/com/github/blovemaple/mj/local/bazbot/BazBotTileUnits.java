package com.github.blovemaple.mj.local.bazbot;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

	public Stream<BazBotTileUnit> units() {
		return unitsByNeighborhood.values().stream().flatMap(List::stream);
	}

	public void forEachHoodAndUnits(BiConsumer<BazBotTileNeighborhood, List<BazBotTileUnit>> action) {
		unitsByNeighborhood.forEach(action);
	}

	public List<BazBotTileUnit> unitsOfHood(BazBotTileNeighborhood hood) {
		return unitsByNeighborhood.get(hood);
	}

	public BazBotTileUnits nonConflictsInHoods(BazBotTileUnitType type) {
		return new BazBotTileUnits( //
				neighborhoods().stream()
						.collect(toMap(identity(), hood -> hood.getNonConflictingUnits(type, unitsOfHood(hood)))) //
		);
	}

	/**
	 * 挑选符合数量要求的unit组合，返回所有的可能。<br>
	 * 先找到第一个unit，然后返回以下结果：
	 * <li>把数量要求减1、去除与第一个unit冲突者，并递归调用，最后把第一个unit和递归调用的结果分别拼接；
	 * <li>按照原数量要求，从所有剩余unit中递归获取结果。
	 * 
	 * @param forUnitCount
	 *            需要返回的组合中的unit数量
	 * @return 所有符合要求的unit组合列表
	 */
	public List<BazBotTileUnits> allCombs(int forUnitCount) {
		if (forUnitCount <= 0)
			// 数量要求为0，选择一个空组合
			return new ArrayList<>(Arrays.asList(new BazBotTileUnits(neighborhoods())));

		List<BazBotTileUnit> allUnits = unitsByNeighborhood.values().stream().flatMap(List::stream).collect(toList());
		if (allUnits.isEmpty())
			// 没有unit可选
			return new ArrayList<>();

		return allCombs(allUnits, 0, List.of(), forUnitCount);
	}

	private List<BazBotTileUnits> allCombs(List<BazBotTileUnit> allUnits, int startIndex,
			List<BazBotTileUnit> conflicts, int forUnitCount) {
		if (forUnitCount <= 0)
			// 数量要求为0，选择一个空组合
			return List.of(new BazBotTileUnits(neighborhoods()));
		if (allUnits.isEmpty() || startIndex >= allUnits.size())
			// 没有unit可选
			return List.of();

		// 从startIndex开始，找到第一个与已选units不冲突的unit
		int chosenIndex = IntStream.range(startIndex, allUnits.size())
				.dropWhile(
						index -> conflicts.stream().anyMatch(conflict -> conflict.conflictWith(allUnits.get(index))))
				.findFirst().orElse(-1);
		if (chosenIndex < 0)
			// 没有unit可选（都冲突）
			return List.of();
		BazBotTileUnit chosenUnit = allUnits.get(chosenIndex);

		List<BazBotTileUnits> res = new ArrayList<>();

		// 选择此unit，递归调用并与此unit组合
		List<BazBotTileUnit> newConflicts = new ArrayList<>(conflicts);
		newConflicts.add(chosenUnit);
		allCombs(allUnits, chosenIndex + 1, newConflicts, forUnitCount - 1).stream()
				.peek(units -> units.add(chosenUnit.hood(), chosenUnit)).forEach(res::add);

		// 不选此unit，递归调用
		allCombs(allUnits, chosenIndex + 1, conflicts, forUnitCount).forEach(res::add);

		return res;
	}

	private void add(BazBotTileNeighborhood hood, BazBotTileUnit newUnit) {
		unitsByNeighborhood.get(hood).add(newUnit);
	}

	@Override
	public String toString() {
		return units().collect(toList()).toString();
	}
}