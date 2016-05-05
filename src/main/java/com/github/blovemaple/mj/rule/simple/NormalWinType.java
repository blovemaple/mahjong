package com.github.blovemaple.mj.rule.simple;

import static com.github.blovemaple.mj.object.StandardTileUnitType.*;
import static com.github.blovemaple.mj.utils.MyUtils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileSuit;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.object.TileUnit;
import com.github.blovemaple.mj.rule.AbstractWinType;

/**
 * 普通和牌（相对于七对等特殊和牌类型而言）。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class NormalWinType extends AbstractWinType {

	/**
	 * 全部解析成完整的TileUnit集合的流，包括将牌和若干个顺子/刻子，失败返回空集合。
	 * 
	 * @param aliveTiles
	 * @param anyOne
	 *            true表示最多只解析一种可能的集合，用于快速判断是否可解析
	 * @return 完整的TileUnit集合的流
	 */
	public Stream<Set<TileUnit>> parseWinTileUnits(PlayerInfo playerInfo, Set<Tile> aliveTiles) {
		Set<TileUnit> groupUnits = genGroupUnits(playerInfo);

		// 所有可能的将牌
		return combinationStream(aliveTiles, JIANG.size()).filter(JIANG::isLegalTiles)
				// 针对每一种可能的将牌，寻找剩下的牌全部解析成顺子/刻子的所有可能
				.flatMap(jiang -> {
					TileUnit jiangUnit = new TileUnit(JIANG, jiang);
					List<Tile> otherTiles = new ArrayList<Tile>(aliveTiles);
					otherTiles.removeAll(jiang);
					return parseShunKes(otherTiles).peek(shunKes -> shunKes.add(jiangUnit));
				})
				// 加上牌组生成的单元
				.peek(set -> set.addAll(groupUnits));
	}

	/**
	 * 从玩家的所有牌组生成单元集合。
	 */
	protected Set<TileUnit> genGroupUnits(PlayerInfo playerInfo) {
		return playerInfo.getTileGroups().stream()
				.map(group -> new TileUnit(group.getType().getUnitType(), group.getTiles()))
				.collect(Collectors.toSet());
	}

	/**
	 * 全部解析成顺子/刻子集合的可行情况组成的流，失败返回空流。
	 */
	private Stream<Set<TileUnit>> parseShunKes(List<Tile> tiles) {
		if (tiles.isEmpty())
			return Stream.of(new HashSet<>());

		// 先保证顺子刻子的定义都是3张牌
		if (SHUNZI.size() != 3 || KEZI.size() != 3)
			throw new RuntimeException();

		// 取出第一张牌
		Tile fixedTile = tiles.get(0);
		// （整体逻辑：先找一个顺子或刻子单元，再递归解析剩下的牌）
		return
		// 从第二张开始，取任意两张牌，与第一张牌组成一个单元
		combinationStream(tiles.subList(1, tiles.size()), 2).peek(halfUnit -> halfUnit.add(fixedTile))
				// 过滤出合法的顺子或刻子单元
				.filter(unit -> SHUNZI.isLegalTiles(unit) || KEZI.isLegalTiles(unit))
				// 在剩下的牌中递归解析顺子或刻子单元集合，并添加上面这个单元
				.flatMap(unit -> {
					TileUnit shunUnit = new TileUnit(SHUNZI, unit);
					List<Tile> otherTiles = new LinkedList<>(tiles);
					otherTiles.removeAll(unit);
					return parseShunKes(otherTiles).peek(shunKes -> shunKes.add(shunUnit));
				});
	}

	// 缓存的key是aliveTiles+candidates的hashcode
	private final Map<Integer, Map<Integer, List<ChangingForWin>>> CHANGINGS_CACHE = Collections
			.synchronizedMap(new WeakHashMap<>());

	@Override
	public Stream<ChangingForWin> changingsForWin(PlayerInfo playerInfo, int changeCount, Collection<Tile> candidates) {
		// 先保证顺子刻子的定义都是3张牌、将牌是2张牌
		if (SHUNZI.size() != 3 || KEZI.size() != 3)
			throw new RuntimeException();
		if (JIANG.size() != 2)
			throw new RuntimeException();

		Set<Tile> aliveTiles = playerInfo.getAliveTiles();
		int hash = aliveTiles.hashCode();
		hash = 31 * hash + candidates.hashCode();
		Map<Integer, List<ChangingForWin>> changings = CHANGINGS_CACHE.get(hash);
		if (changings == null) {
			changings = new HashMap<>();
			changingsForWin(new ArrayList<>(aliveTiles), new HashMap<>(), false, new HashMap<>(), changings,
					candidates);
			CHANGINGS_CACHE.put(hash, changings);
		}

		List<ChangingForWin> changingsForCount = changings.get(changeCount);
		return changingsForCount != null ? changingsForCount.stream() : Stream.empty();
	}

	/**
	 * @param aliveTiles
	 * @param preUnitsAndLacks
	 *            已经找到的组合/半组合 和 所缺的牌型
	 * @param hasJiang
	 *            是否已找到将牌/半将牌
	 * @param tileTypeAndCrtUnitSize
	 *            当前分支已经处理的牌型 和 已经处理的最小顺子/刻子单元大小<br>
	 *            后续处理相同牌型时，单元大小不得超过已经处理的最小大小，以避免重复
	 * @param changings
	 *            存放结果
	 * @param 候选换牌集合
	 */
	private void changingsForWin(List<Tile> aliveTiles, Map<List<Tile>, List<TileType>> preUnitsAndLacks,
			boolean hasJiang, Map<TileType, Integer> tileTypeAndCrtUnitSize,
			Map<Integer, List<ChangingForWin>> changings, Collection<Tile> candidates) {
		if (aliveTiles.isEmpty()) {
			// 生成结果
			genChangings(preUnitsAndLacks, changings);
			return;
		}

		if (!hasJiang) {
			// 找到每种可能的的将牌/半将牌，在剩下的手牌里继续找（顺子/刻子）
			Map<TileType, List<Tile>> aliveTilesByType = aliveTiles.stream().collect(Collectors.groupingBy(Tile::type));
			aliveTilesByType.forEach((type, tiles) -> {
				// 差一张牌的将牌
				if (candidates.stream().map(Tile::type).anyMatch(t -> t == type)) {
					List<Tile> preUnit = Collections.singletonList(tiles.get(0));
					preUnitsAndLacks.put(preUnit, Collections.singletonList(type));
					List<Tile> remainAliveTiles = newRemainColl(ArrayList<Tile>::new, aliveTiles, tiles.get(0));
					changingsForWin(remainAliveTiles, preUnitsAndLacks, true, tileTypeAndCrtUnitSize, changings,
							candidates); // ->
					preUnitsAndLacks.remove(preUnit);
				}
				// 将牌
				if (tiles.size() > 1) {
					List<Tile> preUnit = tiles.subList(0, 2);
					preUnitsAndLacks.put(preUnit, Collections.emptyList());
					List<Tile> remainAliveTiles = newRemainColl(ArrayList<Tile>::new, aliveTiles, tiles.subList(0, 2));
					changingsForWin(remainAliveTiles, preUnitsAndLacks, true, tileTypeAndCrtUnitSize, changings,
							candidates); // ->
					preUnitsAndLacks.remove(preUnit);
				}
			});
		} else {
			// 找到每种可能的的顺刻/半顺刻，在剩下的手牌里继续找（顺子/刻子）
			Map<TileSuit, List<Tile>> aliveTilesBySuit = aliveTiles.stream()
					.collect(Collectors.groupingBy(tile -> tile.type().getSuit()));
			aliveTilesBySuit.forEach((suit, tiles) -> {
				IntStream.range(0, tiles.size()).forEach(index -> {
					Tile fixedTile = tiles.get(index);
					List<Tile> otherTiles = tiles.subList(index + 1, tiles.size());
					int lastUnitSize = tileTypeAndCrtUnitSize.getOrDefault(fixedTile.type(), 3);
					// 顺刻
					if (lastUnitSize >= 3 && otherTiles.size() >= 2) {
						distinctCollBy(combinationListStream(otherTiles, 2), Tile::type)
								.peek(halfUnit -> halfUnit.add(fixedTile))
								.filter(unit -> SHUNZI.isLegalTiles(unit) || KEZI.isLegalTiles(unit)) //
								.forEach(unit -> {
									List<Tile> remainTiles = newRemainColl(ArrayList<Tile>::new, aliveTiles, unit);
									preUnitsAndLacks.put(unit, Collections.emptyList());
									changingsForWin(remainTiles, preUnitsAndLacks, true, tileTypeAndCrtUnitSize,
											changings, candidates);
									preUnitsAndLacks.remove(unit);
								});
					}
					// 差一张牌的顺刻
					if (lastUnitSize >= 2 && otherTiles.size() >= 1 && candidates.size() >= 1) {
						distinctBy(otherTiles.stream(), Tile::type) //
								.map(tile -> Arrays.asList(fixedTile, tile)) //
								.forEach(preUnit -> {
									distinctBy(candidates.stream(), Tile::type) //
											.filter(candTile -> {
												List<Tile> unit = new ArrayList<>(preUnit);
												unit.add(candTile);
												return SHUNZI.isLegalTiles(unit) || KEZI.isLegalTiles(unit);
											}).forEach(candTile -> {
												List<Tile> remainTiles = newRemainColl(ArrayList<Tile>::new, aliveTiles,
														preUnit);
												preUnitsAndLacks.put(preUnit,
														Collections.singletonList(candTile.type()));
												tileTypeAndCrtUnitSize.put(fixedTile.type(), 2);
												changingsForWin(remainTiles, preUnitsAndLacks, true,
														tileTypeAndCrtUnitSize, changings, candidates);
												preUnitsAndLacks.remove(preUnit);
												tileTypeAndCrtUnitSize.put(fixedTile.type(), lastUnitSize);
											});
								});
					}
					// 差两张牌的顺刻
					if (lastUnitSize >= 1 && candidates.size() >= 2) {
						List<Tile> preUnit = Collections.singletonList(fixedTile);
						distinctCollBy(combinationListStream(candidates, 2), Tile::type) //
								.filter(candTiles -> {
									List<Tile> unit = new ArrayList<>(candTiles);
									unit.add(fixedTile);
									return SHUNZI.isLegalTiles(unit) || KEZI.isLegalTiles(unit);
								}).forEach(candTiles -> {
									List<Tile> remainTiles = newRemainColl(ArrayList<Tile>::new, aliveTiles, fixedTile);
									preUnitsAndLacks.put(preUnit,
											candTiles.stream().map(Tile::type).collect(Collectors.toList()));
									tileTypeAndCrtUnitSize.put(fixedTile.type(), 1);
									changingsForWin(remainTiles, preUnitsAndLacks, true, tileTypeAndCrtUnitSize,
											changings, candidates);
									preUnitsAndLacks.remove(preUnit);
									tileTypeAndCrtUnitSize.put(fixedTile.type(), lastUnitSize);
								});
					}
				});
			});
		}
	}

	private void genChangings(Map<List<Tile>, List<TileType>> preUnitsAndLacks,
			Map<Integer, List<ChangingForWin>> changings) {
		// TODO
	}

}
