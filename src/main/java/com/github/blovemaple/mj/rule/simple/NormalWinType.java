package com.github.blovemaple.mj.rule.simple;

import static com.github.blovemaple.mj.object.StandardTileUnitType.*;
import static com.github.blovemaple.mj.utils.MyUtils.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
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
		return combinationSetStream(aliveTiles, JIANG.size()).filter(JIANG::isLegalTiles)
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
		combinationSetStream(tiles.subList(1, tiles.size()), 2).peek(halfUnit -> halfUnit.add(fixedTile))
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
			.synchronizedMap(new HashMap<>()); // TODO delete

	// @Override
	public Stream<ChangingForWin> changingsForWin_old(PlayerInfo playerInfo, int changeCount,
			Collection<Tile> candidates) {
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
			Map<TileType, List<Tile>> candidatesByType = candidates.stream().collect(Collectors.groupingBy(Tile::type));
			Map<TileSuit, List<Tile>> candidatesBySuit = candidates.stream()
					.collect(Collectors.groupingBy(tile -> tile.type().getSuit()));
			changingsForWin(new ArrayList<>(aliveTiles), new HashMap<>(), false, new HashMap<>(), changings,
					candidatesByType, candidatesBySuit);
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
	 * @param candidatesByType
	 *            候选换牌集合，按照牌型分组
	 * @param candidatesBySuit
	 *            候选换牌集合，按照花色分组
	 */
	private void changingsForWin(List<Tile> aliveTiles, Map<List<Tile>, List<TileType>> preUnitsAndLacks,
			boolean hasJiang, Map<TileType, Integer> tileTypeAndCrtUnitSize,
			Map<Integer, List<ChangingForWin>> changings, Map<TileType, List<Tile>> candidatesByType,
			Map<TileSuit, List<Tile>> candidatesBySuit) {
		if (aliveTiles.isEmpty()) {
			// 生成结果
			genChangings(aliveTiles, preUnitsAndLacks, changings, candidatesByType);
			return;
		}

		if (!hasJiang) {
			// 找到每种可能的的将牌/半将牌，在剩下的手牌里继续找（顺子/刻子）
			Map<TileType, List<Tile>> aliveTilesByType = aliveTiles.stream().collect(Collectors.groupingBy(Tile::type));
			aliveTilesByType.forEach((type, tiles) -> {
				// 将牌
				if (tiles.size() > 1) {
					List<Tile> preUnit = tiles.subList(0, 2);
					preUnitsAndLacks.put(preUnit, Collections.emptyList());
					List<Tile> remainAliveTiles = newRemainColl(ArrayList<Tile>::new, aliveTiles, tiles.subList(0, 2));
					changingsForWin(remainAliveTiles, preUnitsAndLacks, true, tileTypeAndCrtUnitSize, changings,
							candidatesByType, candidatesBySuit); // ->
					preUnitsAndLacks.remove(preUnit);
				}
				// 差一张牌的将牌
				if (candidatesByType.containsKey(type)) {
					List<Tile> preUnit = Collections.singletonList(tiles.get(0));
					preUnitsAndLacks.put(preUnit, Collections.singletonList(type));
					List<Tile> remainAliveTiles = newRemainColl(ArrayList<Tile>::new, aliveTiles, tiles.get(0));
					changingsForWin(remainAliveTiles, preUnitsAndLacks, true, tileTypeAndCrtUnitSize, changings,
							candidatesByType, candidatesBySuit); // ->
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
					List<Tile> candidatesForSuit = candidatesBySuit.get(suit);
					// 顺刻
					if (lastUnitSize >= 3 && otherTiles.size() >= 2) {
						distinctCollBy(combinationListStream(otherTiles, 2), Tile::type)
								.peek(halfUnit -> halfUnit.add(fixedTile))
								.filter(unit -> SHUNZI.isLegalTiles(unit) || KEZI.isLegalTiles(unit)) //
								.forEach(unit -> {
									List<Tile> remainTiles = newRemainColl(ArrayList<Tile>::new, aliveTiles, unit);
									preUnitsAndLacks.put(unit, Collections.emptyList());
									changingsForWin(remainTiles, preUnitsAndLacks, true, tileTypeAndCrtUnitSize,
											changings, candidatesByType, candidatesBySuit);
									preUnitsAndLacks.remove(unit);
								});
					}
					// 差一张牌的顺刻
					if (lastUnitSize >= 2 && otherTiles.size() >= 1 && candidatesForSuit.size() >= 1) {
						distinctBy(otherTiles.stream(), Tile::type) //
								.map(tile -> Arrays.asList(fixedTile, tile)) //
								.forEach(preUnit -> {
									distinctBy(candidatesForSuit.stream(), Tile::type) //
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
														tileTypeAndCrtUnitSize, changings, candidatesByType,
														candidatesBySuit);
												preUnitsAndLacks.remove(preUnit);
												tileTypeAndCrtUnitSize.put(fixedTile.type(), lastUnitSize);
											});
								});
					}
					// 差两张牌的顺刻
					if (lastUnitSize >= 1 && candidatesForSuit.size() >= 2) {
						List<Tile> preUnit = Collections.singletonList(fixedTile);
						distinctCollBy(combinationListStream(candidatesForSuit, 2), Tile::type) //
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
											changings, candidatesByType, candidatesBySuit);
									preUnitsAndLacks.remove(preUnit);
									tileTypeAndCrtUnitSize.put(fixedTile.type(), lastUnitSize);
								});
					}
				});
			});
		}
	}

	private void genChangings(List<Tile> aliveTiles, Map<List<Tile>, List<TileType>> preUnitsAndLacks,
			Map<Integer, List<ChangingForWin>> changings, Map<TileType, List<Tile>> candidatesByType) {
		List<Map.Entry<List<Tile>, List<TileType>>> preUnitsList = new ArrayList<>(preUnitsAndLacks.entrySet());
		IntStream.rangeClosed(1, preUnitsList.size()).forEach(size -> {
			combinationListStream(preUnitsList, size).map(entryList -> {
				try {
					Set<Tile> added = new HashSet<>(), removed = new HashSet<>();
					preUnitsList.stream().forEach(preUnit -> {
						if (entryList.contains(preUnit))
							added.addAll(getTileFromCandidates(preUnit.getValue(), candidatesByType, added));
						else
							removed.addAll(preUnit.getKey());
					});
					if (!disjointBy(added, removed, Tile::type))
						// 只留下增加牌和减去牌没有牌型重复的
						return null;
					if (added.size() != removed.size() + 1)
						// 只留下增加牌数比减去牌数多1的
						return null;
					return new ChangingForWin(removed, added);
				} catch (NoSuchElementException e) {
					// 过滤掉候选牌不够的
					return null;
				}
			}).filter(Objects::nonNull).forEach(changing -> {
				List<ChangingForWin> list = changings.get(changing.removedTiles.size());
				if (list == null) {
					list = new ArrayList<>();
					changings.put(changing.removedTiles.size(), list);
				}
				list.add(changing);
			});
		});
	}

	/**
	 * @throws NoSuchElementException
	 *             候选牌不够
	 */
	private List<Tile> getTileFromCandidates(Collection<TileType> types, Map<TileType, List<Tile>> candidatesByType,
			Set<Tile> excludes) throws NoSuchElementException {
		// 不够的时候会在subList处抛出IndexOutOfBoundsException
		return types.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet()
				.stream().flatMap(entry -> {
					TileType type = entry.getKey();
					int count = entry.getValue().intValue();
					List<Tile> typeTiles = candidatesByType.getOrDefault(type, Collections.emptyList()).stream()
							.filter(tile -> !excludes.contains(tile)).limit(count).collect(Collectors.toList());
					if (typeTiles.size() < count)
						throw new NoSuchElementException();
					return typeTiles.stream();
				}).collect(Collectors.toList());
	}

	@Override
	public List<Tile> getDiscardCandidates(Set<Tile> aliveTiles, Collection<Tile> candidates) {
		// 先保证顺子刻子的定义都是3张牌、将牌是2张牌
		if (SHUNZI.size() != 3 || KEZI.size() != 3)
			throw new RuntimeException();
		if (JIANG.size() != 2)
			throw new RuntimeException();

		List<Tile> aliveTileList = new ArrayList<>(aliveTiles);

		Map<TileSuit, List<Tile>> candidatesBySuit = candidates.stream()
				.collect(Collectors.groupingBy(tile -> tile.type().getSuit()));
		List<PreUnit> preShunkes = parsePreShunkes(aliveTileList, candidatesBySuit);

		preShunkes.sort(Comparator.<PreUnit, Integer> comparing(preUnit -> preUnit.tiles.size())
				.thenComparing(preUnit -> preUnit.lackedTypesList.size()).reversed());

		List<Tile> removedTiles = new ArrayList<>();
		List<Tile> sortedTiles = new ArrayList<>();
		int unitSize = 0, lackedKinds = 0;
		for (PreUnit preUnit : preShunkes) {
			if (unitSize == 0) {
				unitSize = preUnit.tiles.size();
				lackedKinds = preUnit.lackedTypesList.size();
			}
			if (preUnit.tiles.size() == unitSize && preUnit.lackedTypesList.size() == lackedKinds) {
				removedTiles.addAll(preUnit.tiles);
			}
			preUnit.tiles.stream().filter(tile -> !sortedTiles.contains(tile)).forEach(sortedTiles::add);
		}

		if (removedTiles.size() < aliveTiles.size())
			sortedTiles.removeAll(removedTiles);
		reverse(sortedTiles);
		return sortedTiles;
	}

	@Override
	public Stream<ChangingForWin> changingsForWin(PlayerInfo playerInfo, int changeCount, Collection<Tile> candidates) {
		Set<Tile> aliveTiles = playerInfo.getAliveTiles();
		int hash = aliveTiles.hashCode();
		hash = 31 * hash + candidates.hashCode();
		Map<Integer, List<ChangingForWin>> changings = CHANGINGS_CACHE.get(hash);
		if (changings == null) {
			changings = parseChangings(new ArrayList<>(aliveTiles), candidates);
			System.out.println(changings.values().stream().mapToLong(List::size).sum());
			changings.forEach((cc, cs) -> {
				System.out.println("Changes: " + cc);
				// cs.forEach(System.out::println);
			});
			CHANGINGS_CACHE.put(hash, changings);
		}

		List<ChangingForWin> changingsForCount = changings.get(changeCount);
		return changingsForCount != null ? changingsForCount.stream() : Stream.empty();
	}

	private Map<Integer, List<ChangingForWin>> parseChangings(List<Tile> aliveTiles, Collection<Tile> candidates) {
		// 先保证顺子刻子的定义都是3张牌、将牌是2张牌
		if (SHUNZI.size() != 3 || KEZI.size() != 3)
			throw new RuntimeException();
		if (JIANG.size() != 2)
			throw new RuntimeException();

		Map<TileType, List<Tile>> candidatesByType = candidates.stream().collect(Collectors.groupingBy(Tile::type));
		Map<TileSuit, List<Tile>> candidatesBySuit = candidates.stream()
				.collect(Collectors.groupingBy(tile -> tile.type().getSuit()));

		List<PreUnit> preJiangs = parsePreJiangs(aliveTiles, candidatesByType);
		List<PreUnit> preShunkes = parsePreShunkes(aliveTiles, candidatesBySuit);
		preJiangs.forEach(System.out::println);
		preShunkes.forEach(System.out::println);
		cleanPreUnits(preJiangs, preShunkes);
		System.out.println("Clean:");
		preJiangs.forEach(System.out::println);
		preShunkes.forEach(System.out::println);
		System.out.println();
		Map<Integer, List<ChangingForWin>> result = genChangings(aliveTiles, preJiangs, preShunkes, candidatesByType);
		return result;
	}

	private List<PreUnit> parsePreJiangs(List<Tile> aliveTiles, Map<TileType, List<Tile>> candidatesByType) {
		List<PreUnit> result = new ArrayList<>();

		// 差一张牌的将牌
		aliveTiles.stream().filter(tile -> candidatesByType.containsKey(tile.type()))
				.forEach(tile -> result.add(new PreUnit(true, tile, tile.type())));

		// 将牌
		Map<TileType, List<Tile>> aliveTilesByType = aliveTiles.stream().collect(Collectors.groupingBy(Tile::type));
		aliveTilesByType.forEach((type, tiles) -> combinationListStream(tiles, 2).map(jiang -> new PreUnit(true, jiang))
				.forEach(result::add));

		return result;
	}

	private List<PreUnit> parsePreShunkes(List<Tile> aliveTiles, Map<TileSuit, List<Tile>> candidatesBySuit) {
		List<PreUnit> result = new ArrayList<>();

		// 差两张牌的顺刻
		aliveTiles.forEach(tile -> {
			List<List<TileType>> lackedTypesList = new ArrayList<>();
			lackedTypesList.addAll(SHUNZI.getLackedTypesForTiles(Collections.singletonList(tile)));
			lackedTypesList.addAll(KEZI.getLackedTypesForTiles(Collections.singletonList(tile)));
			if (!lackedTypesList.isEmpty())
				result.add(new PreUnit(false, tile, lackedTypesList, 2));
		});

		Map<TileSuit, List<Tile>> aliveTilesBySuit = aliveTiles.stream()
				.collect(Collectors.groupingBy(tile -> tile.type().getSuit()));

		aliveTilesBySuit.forEach((suit, tiles) -> {
			// 差一张牌的顺刻
			combinationListStream(tiles, 2).forEach(testUnit -> {
				List<List<TileType>> lackedTypesList = new ArrayList<>();
				lackedTypesList.addAll(SHUNZI.getLackedTypesForTiles(testUnit));
				lackedTypesList.addAll(KEZI.getLackedTypesForTiles(testUnit));
				if (!lackedTypesList.isEmpty())
					result.add(new PreUnit(false, testUnit, lackedTypesList, 1));
			});
			// 顺刻
			combinationListStream(tiles, 3)
					.filter(testUnit -> SHUNZI.isLegalTiles(testUnit) || KEZI.isLegalTiles(testUnit))
					.forEach(unitTiles -> result.add(new PreUnit(false, unitTiles)));
		});

		return result;
	}

	private void cleanPreUnits(List<PreUnit> preJiangs, List<PreUnit> preShunkes) {
		List<PreUnit> allPreUnits = merged(ArrayList::new, preJiangs, preShunkes);
		// 过滤出孤立的preUnit（所有与之有交集的preUnit都是它的子集）
		List<PreUnit> lonelyUnits = allPreUnits.stream()
				.filter(preUnit -> allPreUnits.stream().filter(otherUnit -> !disjoint(preUnit.tiles, otherUnit.tiles))
						.allMatch(otherUnit -> preUnit.tiles.containsAll(otherUnit.tiles)))
				.collect(Collectors.toList());
		// 检查是否有孤立的完整将牌
		boolean hasLonelyJiang = lonelyUnits.stream().filter(unit -> unit.isJiang)
				.anyMatch(unit -> unit.lackedTypesList.isEmpty());
		// 在preJiang和preShunkes中进行清理：
		// 1. 如果存在孤立的完整将牌，则删除所有不完整的将牌
		// 2. 对于孤立的完整顺刻，删除它的所有真子集
		// 3. 对于孤立的不完整顺刻，删除它的真子集中的顺刻
		Arrays.asList(preJiangs, preShunkes).forEach(preUnits -> {
			Iterator<PreUnit> preUnitItr = preUnits.iterator();
			while (preUnitItr.hasNext()) {
				PreUnit preUnit = preUnitItr.next();
				if (preUnit.isJiang && hasLonelyJiang && !preUnit.lackedTypesList.isEmpty())
					preUnitItr.remove();
				else if (lonelyUnits.stream().anyMatch(lonelyUnit ->
				// 孤立顺刻
				!lonelyUnit.isJiang &&
				// 真子集
				lonelyUnit.tiles.size() > preUnit.tiles.size() && lonelyUnit.tiles.containsAll(preUnit.tiles) &&
				// 如果孤立顺刻不完整，则检查的unit必须是顺刻
				(lonelyUnit.lackedTypesList.isEmpty() ? true : !preUnit.isJiang))) {
					preUnitItr.remove();
				}
			}
		});

		// 将所有孤立的完整顺刻，或合并后是孤立的n个顺刻，它本身和子集都删除
		// TODO

	}

	private Map<Integer, List<ChangingForWin>> genChangings(List<Tile> aliveTiles, List<PreUnit> preJiangs,
			List<PreUnit> preShunkes, Map<TileType, List<Tile>> candidatesByType) {
		long startTime = System.currentTimeMillis();

		// 先选任意一个preJiang
		Map<Integer, List<ChangingForWin>> result = distinctBy(preJiangs.stream(), PreUnit::tilesTypeHash)
				.flatMap(preJiang -> {
					// 然后在preShunke里遍历(aliveTiles.size()/3)个元素的组合
					// (aliveTiles.size()/3)是aliveTiles换完牌后应该具有的顺刻数
					List<PreUnit> legalPreShunkes = preShunkes.stream()
							.filter(preShunke -> disjoint(preJiang.tiles, preShunke.tiles))
							.collect(Collectors.toList());
					int shunKeCount = aliveTiles.size() / 3;
					List<PreUnit> legalPreShunkesWithoutSingle = legalPreShunkes.stream()
							.filter(preUnit -> preUnit.tiles.size() > 1).collect(Collectors.toList());
					// 如果2个以上牌的legalPreShunkes已经足够，则只用2个以上牌的
					if (legalPreShunkesWithoutSingle.size() >= shunKeCount)
						legalPreShunkes = legalPreShunkesWithoutSingle;
					Stream<ChangingForWin> changings = preShunkesStream(legalPreShunkes, shunKeCount)
							// 合并上preJiang
							.peek(preUnits -> preUnits.add(preJiang))
							// 组成选定的preUnits，可以生成若干个changing
							.flatMap(preUnits -> {
								Set<Tile> removed = new HashSet<>(aliveTiles);
								preUnits.stream().map(PreUnit::tiles).forEach(removed::removeAll);

								Set<TileType> removedTypeSet = removed.stream().map(Tile::type)
										.collect(Collectors.toSet());

								// 从每个非完整的preUnits中选择任意一个lackedTypes，合并成总共的lackedTypes
								// lackedTypes要过滤掉与removedTiles有牌型重复的
								List<List<List<TileType>>> allLackedTypes = preUnits.stream()
										.map(PreUnit::lackedTypeList)
										// 非完整
										.filter(lackedTypesList -> !lackedTypesList.isEmpty())
										// 过滤掉与removedTiles有牌型重复的
										.map(lackedTypesList -> lackedTypesList.stream()
												.filter(lackedTypes -> lackedTypes.stream()
														.noneMatch(removedTypeSet::contains))
												.collect(Collectors.toList()))
										.collect(Collectors.toList());
								return selectStream(allLackedTypes).map(
										select -> select.stream().flatMap(List::stream).collect(Collectors.toList()))
										.map(lackedTypes -> {
											List<Tile> added;
											try {
												added = getTileFromCandidates(lackedTypes, candidatesByType);
											} catch (NoSuchElementException e) {
												return null;
											}
											// 生成ChangingForWin
											return new ChangingForWin(removed, new HashSet<>(added));
										}).filter(Objects::nonNull);
							}).filter(Objects::nonNull);
					return changings;
				}).distinct()
				// 按removedTiles个数归类
				.collect(Collectors.groupingBy(c -> c.removedTiles.size()));

		System.out.println("total " + (System.currentTimeMillis() - startTime));
		return result;
	}

	private Stream<List<PreUnit>> preShunkesStream(List<PreUnit> preShunkes, int shunkeCount) {
		return combinationStream(preShunkes, shunkeCount, ArrayList<PreUnit>::new, null,
				(preUnit1, preUnit2) -> disjoint(preUnit1.tiles, preUnit2.tiles));
	}

	/**
	 * @throws NoSuchElementException
	 *             候选牌不够
	 */
	private List<Tile> getTileFromCandidates(Collection<TileType> types, Map<TileType, List<Tile>> candidatesByType)
			throws NoSuchElementException {
		List<Tile> result = new ArrayList<>();
		types.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
				.forEach((type, count) -> {
					List<Tile> tilesOfType = candidatesByType.get(type);
					if (tilesOfType == null || tilesOfType.size() < count)
						throw new NoSuchElementException();
					result.addAll(tilesOfType.subList(0, count.intValue()));
				});
		return result;
	}

	private static class PreUnit {
		boolean isJiang;
		List<Tile> tiles;
		List<List<TileType>> lackedTypesList;
		int lackedCount;

		private int tilesTypeHash = 0;

		PreUnit(boolean isJiang, List<Tile> tiles, List<List<TileType>> lackedTypesList, int lackedCount) {
			this.isJiang = isJiang;
			this.tiles = tiles;
			this.lackedTypesList = lackedTypesList;
			this.lackedCount = lackedCount;
		}

		PreUnit(boolean isJiang, List<Tile> tiles) {
			this(isJiang, tiles, Collections.emptyList(), 0);
		}

		PreUnit(boolean isJiang, Tile tile, List<List<TileType>> lackedTypeList, int lackedCount) {
			this(isJiang, Collections.singletonList(tile), lackedTypeList, lackedCount);
		}

		PreUnit(boolean isJiang, Tile tile, TileType lackedType) {
			this(isJiang, Collections.singletonList(tile), singletonList(singletonList(lackedType)), 1);
		}

		List<List<TileType>> lackedTypeList() {
			return lackedTypesList;
		}

		int lackedCount() {
			return lackedCount;
		}

		List<Tile> tiles() {
			return tiles;
		}

		int tilesTypeHash() {
			if (tilesTypeHash == 0)
				tilesTypeHash = tiles.stream().map(Tile::type).mapToInt(TileType::hashCode).sum();
			return tilesTypeHash;
		}

		@Override
		public String toString() {
			return "[isJiang=" + isJiang + ", tiles=" + tiles + ", lackedTypesList=" + lackedTypesList + "]";
		}

	}

}
