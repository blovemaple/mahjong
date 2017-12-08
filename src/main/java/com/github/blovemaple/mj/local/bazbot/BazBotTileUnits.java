package com.github.blovemaple.mj.local.bazbot;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

import java.util.ArrayList;
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

	private int size;

	public BazBotTileUnits(Collection<BazBotTileNeighborhood> neighborhoods) {
		unitsByNeighborhood = new LinkedHashMap<>();
		neighborhoods.forEach(hood -> unitsByNeighborhood.put(hood, new ArrayList<>()));
		size = 0;
	}

	public BazBotTileUnits(BazBotTileUnits original, BazBotTileUnits newUnits) {
		// 拷贝original的unitsByNeighborhood
		this.unitsByNeighborhood = new LinkedHashMap<>();
		original.unitsByNeighborhood
				.forEach((hood, units) -> this.unitsByNeighborhood.put(hood, new ArrayList<>(units)));

		// 把newUnits填入unitsByNeighborhood
		if (newUnits != null)
			newUnits.unitsByNeighborhood.forEach((hood, units) -> this.unitsByNeighborhood.get(hood).addAll(units));

		size = original.size() + newUnits.size();
	}

	private BazBotTileUnits(Map<BazBotTileNeighborhood, List<BazBotTileUnit>> unitsByNeighborhood) {
		this.unitsByNeighborhood = unitsByNeighborhood;
		size = unitsByNeighborhood.values().stream().mapToInt(List::size).sum();
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
	 * @param maxUnitCount
	 *            返回的组合中的最多unit数量
	 * @param includeEmpty
	 *            是否一定包含空组合，false表示只有没有unit可选或maxUnitCount==0时才包含空组合
	 * @return 所有符合要求的unit组合列表
	 */
	public List<BazBotTileUnits> allCombs(int maxUnitCount, boolean includeEmpty) {
		if (maxUnitCount <= 0)
			// 数量要求为0，选择一个空组合
			return List.of(new BazBotTileUnits(neighborhoods()));

		List<BazBotTileUnit> allUnits = unitsByNeighborhood.values().stream().flatMap(List::stream).collect(toList());
		if (allUnits.isEmpty())
			// 没有unit可选
			return includeEmpty ? List.of(new BazBotTileUnits(neighborhoods())) : List.of();

		List<BazBotTileUnits> res = allCombs(allUnits, 0, List.of(), List.of(), true, maxUnitCount);

		if (includeEmpty) {
			res = new ArrayList<>(res);
			res.add(new BazBotTileUnits(neighborhoods()));
		}

		return res;
	}

	private List<BazBotTileUnits> allCombs(List<BazBotTileUnit> allUnits, int startIndex,
			List<BazBotTileUnit> selecteds, List<BazBotTileUnit> droppeds, boolean satisfiedWithDroppeds,
			int forUnitCount) {
		if (forUnitCount <= 0)
			// 数量要求为0，选择一个空组合
			return List.of(new BazBotTileUnits(neighborhoods()));
		if (allUnits.isEmpty() || startIndex >= allUnits.size())
			// 没有unit可选
			// 当satisfiedWithDroppeds时选择一个空组合，否则不选择
			return satisfiedWithDroppeds ? List.of(new BazBotTileUnits(neighborhoods())) : List.of();

		// 从startIndex开始，找到第一个与已选units不冲突的unit
		int chosenIndex = IntStream.range(startIndex, allUnits.size())
				.dropWhile(index -> selecteds.stream().anyMatch(conflict -> conflict.conflictWith(allUnits.get(index))))
				.findFirst().orElse(-1);
		if (chosenIndex < 0)
			// 没有unit可选（都冲突）
			// 当satisfiedWithDroppeds时选择一个空组合，否则不选择
			return satisfiedWithDroppeds ? List.of(new BazBotTileUnits(neighborhoods())) : List.of();
		BazBotTileUnit chosenUnit = allUnits.get(chosenIndex);

		List<BazBotTileUnits> res = new ArrayList<>();

		// 选择此unit，递归调用并与此unit组合
		List<BazBotTileUnit> crtSelecteds = new ArrayList<>(selecteds);
		crtSelecteds.add(chosenUnit);
		boolean crtSatisfiedWithDroppeds = satisfiedWithDroppeds ? true
				: droppeds.stream().allMatch(dropped -> dropped.conflictWith(chosenUnit));
		if (!crtSatisfiedWithDroppeds) {
			if (allUnits.size() - chosenIndex < forUnitCount)
				return res;
		}
		allCombs(allUnits, chosenIndex + 1, crtSelecteds, droppeds, crtSatisfiedWithDroppeds, forUnitCount - 1).stream()
				.peek(units -> units.add(chosenUnit.hood(), chosenUnit)) //
				.filter(units -> crtSatisfiedWithDroppeds ? true : units.size() == forUnitCount) //
				.forEach(res::add);

		// 不选此unit，递归调用
		List<BazBotTileUnit> crtDroppeds = new ArrayList<>(droppeds);
		crtDroppeds.add(chosenUnit);
		allCombs(allUnits, chosenIndex + 1, selecteds, crtDroppeds, false, forUnitCount).stream()
				.filter(units -> crtSatisfiedWithDroppeds ? true : units.size() == forUnitCount) //
				.forEach(res::add);

		return res;
	}

	private void add(BazBotTileNeighborhood hood, BazBotTileUnit newUnit) {
		unitsByNeighborhood.get(hood).add(newUnit);
		size += 1;
	}

	public int size() {
		return size;
	}

	@Override
	public String toString() {
		return units().collect(toList()).toString();
	}
}