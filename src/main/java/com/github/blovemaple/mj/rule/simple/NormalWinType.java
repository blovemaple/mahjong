package com.github.blovemaple.mj.rule.simple;

import static com.github.blovemaple.mj.object.StandardTileUnitType.*;
import static com.github.blovemaple.mj.utils.MyUtils.*;
import static java.util.Collections.*;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

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
import java.util.Queue;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroup;
import com.github.blovemaple.mj.object.TileRank;
import com.github.blovemaple.mj.object.TileRank.NumberRank;
import com.github.blovemaple.mj.rule.win.WinInfo;
import com.github.blovemaple.mj.rule.win.WinType;
import com.github.blovemaple.mj.object.TileSuit;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.object.TileUnit;
import com.github.blovemaple.mj.object.TileUnitType;

/**
 * 普通和牌（相对于七对等特殊和牌类型而言）。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class NormalWinType implements WinType {

	private static final NormalWinType INSTANCE = new NormalWinType();

	public static NormalWinType get() {
		return INSTANCE;
	}

	private NormalWinType() {
	}

	private static class ParseBranch {
		List<TileUnit> units;
		boolean hasJiang;
		List<Tile> remainTiles;

		ParseBranch(List<TileUnit> units, boolean hasJiang, List<Tile> remainTiles) {
			this.units = units;
			this.hasJiang = hasJiang;
			this.remainTiles = remainTiles;
		}

		ParseBranch(List<Tile> aliveTileList) {
			this(emptyList(), false, aliveTileList);
		}
	}

	@Override
	public List<List<TileUnit>> parseWinTileUnits(WinInfo winInfo) {
		if (winInfo.getUnits() != null) {
			List<List<TileUnit>> units = winInfo.getUnits().get(this);
			if (units != null)
				return units;
		}

		Tile winTile = winInfo.getWinTile();
		Boolean ziMo = winInfo.getZiMo();

		if (winInfo.getAliveTiles().size() % 3 != 2) {
			winInfo.setUnits(this, Collections.emptyList());
			return Collections.emptyList();
		}

		// 根据牌组生成units
		List<TileUnit> groupUnits = genGroupUnits(winInfo.getTileGroups());

		// 将手牌按type排序，开始parse
		List<Tile> aliveTileList = winInfo.getAliveTiles().stream().sorted(comparing(Tile::type)).collect(toList());

		List<List<TileUnit>> parseResults = new LinkedList<>();
		Queue<ParseBranch> branchQueue = new LinkedList<>();
		branchQueue.add(new ParseBranch(aliveTileList));
		ParseBranch crtBranch;
		while ((crtBranch = branchQueue.poll()) != null) {
			List<Tile> jiangTiles = null, keTiles = null, shunTiles = null;
			TileType firstType = null;
			TileSuit suit = null;
			int lastNumber = -1;
			boolean first = true;
			for (Tile tile : crtBranch.remainTiles) {
				if (first) {
					firstType = tile.type();
					suit = firstType.suit();
					// 将
					if (!crtBranch.hasJiang) {
						jiangTiles = new ArrayList<>();
						jiangTiles.add(tile);
					}
					// 刻
					keTiles = new ArrayList<>();
					keTiles.add(tile);
					// 顺
					TileRank<?> crtRank = tile.type().rank();
					if (crtRank instanceof NumberRank && (lastNumber = ((NumberRank) crtRank).number()) <= 7) {
						shunTiles = new ArrayList<>();
						shunTiles.add(tile);
					}
					first = false;
				} else {
					TileType crtType = tile.type();
					// 将
					if (jiangTiles != null) {
						if (crtType == firstType) {
							jiangTiles.add(tile);
							newBranchOrResult(crtBranch, jiangTiles, JIANG, branchQueue, parseResults, groupUnits,
									winTile, ziMo);
							jiangTiles = null;
						} else
							jiangTiles = null;
					}
					// 刻
					if (keTiles != null) {
						if (crtType == firstType) {
							keTiles.add(tile);
							if (keTiles.size() == 3) {
								newBranchOrResult(crtBranch, keTiles, KEZI, branchQueue, parseResults, groupUnits,
										winTile, ziMo);
								keTiles = null;
							}
						} else
							keTiles = null;

					}
					// 顺
					if (shunTiles != null) {
						TileSuit crtSuit = crtType.suit();
						if (crtSuit == suit) {
							int crtNumber = ((NumberRank) crtType.rank()).number();
							switch (crtNumber - lastNumber) {
							case 1:
								lastNumber++;
								shunTiles.add(tile);
								if (shunTiles.size() == 3) {
									newBranchOrResult(crtBranch, shunTiles, SHUNZI, branchQueue, parseResults,
											groupUnits, winTile, ziMo);
									shunTiles = null;
								}
								break;
							case 0:
								break;
							default:
								shunTiles = null;
								break;
							}
						} else
							shunTiles = null;
					}
				}
				if (jiangTiles == null && keTiles == null && shunTiles == null)
					break;
			}
		}

		winInfo.setUnits(this, parseResults);
		return parseResults;
	}

	/**
	 * 从玩家的所有牌组生成单元集合。
	 */
	protected List<TileUnit> genGroupUnits(List<TileGroup> tileGroups) {
		return tileGroups.stream()
				.map(group -> TileUnit.got(group.getType().getUnitType(), group.getTiles(), group.getGotTile()))
				.collect(toList());
	}

	private void newBranchOrResult(ParseBranch baseBranch, List<Tile> selectedTiles, TileUnitType newUnitType,
			Queue<ParseBranch> branchQueue, List<List<TileUnit>> parseResults, List<TileUnit> groupUnits, Tile winTile,
			Boolean ziMo) {
		boolean hasJiang = baseBranch.hasJiang || newUnitType == JIANG;
		TileUnit newUnit = winTile != null && selectedTiles.contains(winTile) && (ziMo != null && !ziMo)
				? TileUnit.got(newUnitType, selectedTiles, winTile) : TileUnit.self(newUnitType, selectedTiles);
		List<TileUnit> units = merged(ArrayList::new, baseBranch.units, newUnit);
		if (baseBranch.remainTiles.size() != selectedTiles.size()) {
			// 牌没选完，生成一个分支
			List<Tile> remainTiles = remainColl(ArrayList<Tile>::new, baseBranch.remainTiles, selectedTiles);
			branchQueue.add(new ParseBranch(units, hasJiang, remainTiles));
		} else {
			// 牌都选完了，生成一个结果
			if (hasJiang)
				parseResults.add(merged(ArrayList::new, groupUnits, units));
		}
	}

	@Override
	public List<Tile> getDiscardCandidates(Set<Tile> aliveTiles, Collection<Tile> candidates) {
		// 先保证顺子刻子的定义都是3张牌、将牌是2张牌
		if (SHUNZI.size() != 3 || KEZI.size() != 3)
			throw new RuntimeException();
		if (JIANG.size() != 2)
			throw new RuntimeException();

		if (aliveTiles.isEmpty())
			return emptyList();

		List<Tile> aliveTileList = new ArrayList<>(aliveTiles);

		// 解析出全部的preShunke
		Map<TileSuit, List<Tile>> candidatesBySuit = candidates.stream()
				.collect(groupingBy(tile -> tile.type().suit()));
		List<PreUnit> preShunkes = parsePreShunkes(aliveTileList, candidatesBySuit);

		// 按照从好到差排序（牌数越多越好，牌数相同的，lackedTypes种类越多越好）
		preShunkes.sort(Comparator.<PreUnit, Integer> comparing(preUnit -> preUnit.tiles.size())
				.thenComparing(preUnit -> preUnit.lackedTypesList.size()).reversed());

		// 找出与最差preShunke情况相同的units中的牌
		Set<Tile> remainTiles = new HashSet<>(aliveTiles);
		List<Tile> tilesFromWorstUnits = new ArrayList<>();
		@SuppressWarnings("unused")
		int crtUnitSize = Integer.MAX_VALUE, crtLackedKinds = Integer.MAX_VALUE;
		for (PreUnit shunke : preShunkes) {
			if (remainTiles.isEmpty())
				break;
			if (crtUnitSize > shunke.tiles.size()
			/*
			 * || (crtUnitSize == shunke.tiles.size() && crtLackedKinds >
			 * shunke.lackedTypesList.size())
			 */) {
				tilesFromWorstUnits.clear();
				crtUnitSize = shunke.tiles.size();
				crtLackedKinds = shunke.lackedTypesList.size();
			}
			shunke.tiles.stream().filter(remainTiles::contains).forEach(tile -> {
				tilesFromWorstUnits.add(tile);
				remainTiles.remove(tile);
			});
		}

		reverse(tilesFromWorstUnits);
		return tilesFromWorstUnits;
	}

	// 缓存的key是aliveTiles+candidates的hashcode
	private final Map<Integer, Map<Integer, List<ChangingForWin>>> CHANGINGS_CACHE = Collections
			.synchronizedMap(new HashMap<>()); // TODO 改成全部取出，不缓存

	@Override
	public Stream<ChangingForWin> changingsForWin(PlayerInfo playerInfo, int changeCount, Collection<Tile> candidates) {
		Set<Tile> aliveTiles = playerInfo.getAliveTiles();
		int hash = aliveTiles.hashCode();
		hash = 31 * hash + candidates.hashCode();
		Map<Integer, List<ChangingForWin>> changings = CHANGINGS_CACHE.get(hash);
		if (changings == null) {
			changings = parseChangings(new ArrayList<>(aliveTiles), candidates);
			// changings.forEach((count, cs) -> {
			// System.out.println("count " + count);
			// cs.forEach(System.out::println);
			// });
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

		Map<TileType, List<Tile>> candidatesByType = candidates.stream().collect(groupingBy(Tile::type));
		Map<TileSuit, List<Tile>> candidatesBySuit = candidates.stream()
				.collect(groupingBy(tile -> tile.type().suit()));

		List<PreUnit> preJiangs = parsePreJiangs(aliveTiles, candidatesByType);
		List<PreUnit> preShunkes = parsePreShunkes(aliveTiles, candidatesBySuit);
		cleanPreUnits(preJiangs, preShunkes);
		Map<Integer, List<ChangingForWin>> result = genChangings(aliveTiles, preJiangs, preShunkes, candidatesByType);
		return result;
	}

	private List<PreUnit> parsePreJiangs(List<Tile> aliveTiles, Map<TileType, List<Tile>> candidatesByType) {
		List<PreUnit> result = new ArrayList<>();

		// 将牌
		Map<TileType, List<Tile>> aliveTilesByType = aliveTiles.stream().collect(groupingBy(Tile::type));
		aliveTilesByType.forEach(
				(type, tiles) -> combListStream(tiles, 2).map(jiang -> new PreUnit(true, jiang)).forEach(result::add));

		// 差一张牌的将牌
		aliveTiles.stream().filter(tile -> candidatesByType.containsKey(tile.type()))
				.forEach(tile -> result.add(new PreUnit(true, tile, tile.type())));

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
				.collect(groupingBy(tile -> tile.type().suit()));

		aliveTilesBySuit.forEach((suit, tiles) -> {
			// 差一张牌的顺刻
			combListStream(tiles, 2).forEach(testUnit -> {
				List<List<TileType>> lackedTypesList = new ArrayList<>();
				lackedTypesList.addAll(SHUNZI.getLackedTypesForTiles(testUnit));
				lackedTypesList.addAll(KEZI.getLackedTypesForTiles(testUnit));
				if (!lackedTypesList.isEmpty())
					result.add(new PreUnit(false, testUnit, lackedTypesList, 1));
			});
			// 顺刻
			combListStream(tiles, 3).filter(testUnit -> SHUNZI.isLegalTiles(testUnit) || KEZI.isLegalTiles(testUnit))
					.forEach(unitTiles -> result.add(new PreUnit(false, unitTiles)));
		});

		return result;
	}

	private void cleanPreUnits(List<PreUnit> preJiangs, List<PreUnit> preShunkes) {
		List<PreUnit> allPreUnits = merged(ArrayList::new, preJiangs, preShunkes);
		// 过滤出孤立的多余1张牌的preUnit（孤立：所有与之有交集的preUnit都是它的子集）
		List<PreUnit> lonelyUnits = allPreUnits.stream()
				// 多余1张牌
				.filter(preUnit -> preUnit.tiles.size() > 1)
				// 孤立的
				.filter(preUnit -> allPreUnits.stream().filter(otherUnit -> !disjoint(preUnit.tiles, otherUnit.tiles))
						.allMatch(otherUnit -> preUnit.tiles.containsAll(otherUnit.tiles)))
				.collect(toList());
		// 在preJiang和preShunkes中进行清理：
		// 对于孤立的完整unit（完整将牌或完整顺刻），删除它的所有真子集
		// 对于孤立的不完整unit（不完整顺刻），删除它的真子集中的顺刻
		Arrays.asList(preJiangs, preShunkes).forEach(preUnits -> {
			Iterator<PreUnit> preUnitItr = preUnits.iterator();
			while (preUnitItr.hasNext()) {
				PreUnit preUnit = preUnitItr.next();
				if (lonelyUnits.stream().anyMatch(lonelyUnit ->
				// 【孤立的完整unit】 或 【（孤立的不完整顺刻，且）当前preUnit也是顺刻】
				(lonelyUnit.lackedTypesList.isEmpty() || !preUnit.isJiang) &&
				// 真子集
				lonelyUnit.tiles.size() > preUnit.tiles.size() && lonelyUnit.tiles.containsAll(preUnit.tiles))) {
					preUnitItr.remove();
				}
			}
		});
	}

	private Map<Integer, List<ChangingForWin>> genChangings(List<Tile> aliveTiles, List<PreUnit> preJiangs,
			List<PreUnit> preShunkes, Map<TileType, List<Tile>> candidatesByType) {
		// 过滤出完整将牌和不完整将牌
		List<PreUnit> fullJiangs = preJiangs.stream().filter(preUnit -> preUnit.lackedTypesList.isEmpty())
				.collect(toList());
		List<PreUnit> halfJiangs = remainColl(ArrayList<PreUnit>::new, preJiangs, fullJiangs);

		// 没用过的完整将牌记在这里
		Set<PreUnit> unusedFullJiangs = new HashSet<>(fullJiangs);

		List<ChangingForWin> changings = new ArrayList<>();

		// 生成第一部分：在preShunkes里按照牌数从大到小，依次取尽量多个，一共最多(aliveTiles.size()/3)个
		int shunkeCount = aliveTiles.size() / 3;
		preShunkesStreamGreedy(preShunkes, shunkeCount).flatMap(shunkes -> {
			// 对于每种顺刻组合，在剩余牌中选择可能的将牌（选择完整将牌，如果没有完整的就选择不完整的）
			Set<Tile> shunkeTiles = shunkes.stream().map(PreUnit::tiles).flatMap(List::stream).collect(toSet());
			List<PreUnit> legalJiangs = fullJiangs.stream().filter(jiang -> disjoint(jiang.tiles, shunkeTiles))
					.peek(unusedFullJiangs::remove) // 在没用过的完整将牌里删除
					.collect(toList());
			if (legalJiangs.isEmpty())
				legalJiangs = halfJiangs.stream().filter(jiang -> disjoint(jiang.tiles, shunkeTiles)) //
						.limit(1) // 如果必须要选择不完整将牌，则只选一个
						.collect(toList());
			// 把每种可能的将牌加在组合中，组成一种选择，生成若干个changing
			return legalJiangs.stream().map(jiang -> merged(ArrayList<PreUnit>::new, shunkes, jiang))
					.flatMap(preUnits -> genChangingsBySelectedUnits(preUnits, aliveTiles, candidatesByType));
		}).forEach(changings::add);

		// 生成第二部分：对于每一个没用过的完整将牌，先选择它，再从剩余牌中选择顺刻，组成changings
		unusedFullJiangs.stream().flatMap(jiang -> {
			List<PreUnit> legalShunkes = preShunkes.stream().filter(shunke -> disjoint(shunke.tiles, jiang.tiles))
					.collect(toList());
			return preShunkesStreamGreedy(legalShunkes, shunkeCount).peek(shunkes -> shunkes.add(jiang))
					.flatMap(preUnits -> genChangingsBySelectedUnits(preUnits, aliveTiles, candidatesByType));
		}).forEach(changings::add);

		// 把两部分changings合起来，去重，按removedTiles个数归类
		Map<Integer, List<ChangingForWin>> result = changings.stream().distinct()
				.collect(groupingBy(c -> c.removedTiles.size()));
		return result;
	}

	private Stream<List<PreUnit>> preShunkesStreamGreedy(List<PreUnit> preShunkes, int forCount) {
		if (forCount == 0)
			return Stream.of(new ArrayList<>());

		Stream<List<PreUnit>> result;

		Map<Integer, List<PreUnit>> preShunkesBySize = preShunkes.stream()
				.collect(groupingBy(preUnit -> preUnit.tiles.size()));
		BiPredicate<PreUnit, PreUnit> combCondition = (preUnit1, preUnit2) -> disjoint(preUnit1.tiles, preUnit2.tiles);

		// 在3张牌（完整）的顺刻中尽量选择，最多forCount个
		List<PreUnit> shunkes3 = preShunkesBySize.getOrDefault(3, emptyList());
		result = combStreamGreedy(shunkes3, ArrayList<PreUnit>::new, combCondition, 3);

		// 如果不够，在2张牌的顺刻中尽量选择，合起来最多forCount个
		List<PreUnit> shunkes2 = preShunkesBySize.get(2);
		if (shunkes2 != null)
			result = result.flatMap(shunkes -> {
				if (shunkes.size() == forCount)
					return Stream.of(shunkes);

				Set<Tile> selectedTiles = shunkes.stream().flatMap(shunke -> shunke.tiles.stream()).collect(toSet());
				List<PreUnit> legalShunkes2 = shunkes2.stream().filter(shunke -> disjoint(selectedTiles, shunke.tiles))
						.collect(toList());
				return combStreamGreedy(legalShunkes2, ArrayList<PreUnit>::new, combCondition,
						forCount - shunkes.size()).peek(newShunkes -> newShunkes.addAll(shunkes));
			});

		// 如果还不够，在1张牌的顺刻中补足剩余数量
		List<PreUnit> shunkes1 = preShunkesBySize.getOrDefault(1, emptyList());
		result = result.flatMap(shunkes -> {
			if (shunkes.size() == forCount)
				return Stream.of(shunkes);

			Set<Tile> selectedTiles = shunkes.stream().flatMap(shunke -> shunke.tiles.stream()).collect(toSet());
			List<PreUnit> legalShunkes1 = shunkes1.stream().filter(shunke -> disjoint(selectedTiles, shunke.tiles))
					.collect(toList());
			return combStream(legalShunkes1, forCount - shunkes.size(), ArrayList<PreUnit>::new, null, combCondition)
					.peek(newShunkes -> newShunkes.addAll(shunkes));
		});

		return result;
	}

	private Stream<ChangingForWin> genChangingsBySelectedUnits(List<PreUnit> preUnits, List<Tile> aliveTiles,
			Map<TileType, List<Tile>> candidatesByType) {
		Set<Tile> removed = new HashSet<>(aliveTiles);
		preUnits.stream().map(PreUnit::tiles).forEach(removed::removeAll);

		Set<TileType> removedTypeSet = removed.stream().map(Tile::type).collect(toSet());

		// 从每个非完整的preUnits中选择任意一个lackedTypes，合并成总共的lackedTypes
		// lackedTypes要过滤掉与removedTiles有牌型重复的
		// []每个preUnit[]一个preUnit的每种lackedTypes[]一种lackedTypes的每个TileType
		List<List<List<TileType>>> allLackedTypes = preUnits.stream().map(PreUnit::lackedTypeList)
				// 非完整
				.filter(lackedTypesList -> !lackedTypesList.isEmpty())
				// 过滤掉与removedTiles有牌型重复的
				.map(lackedTypesList -> lackedTypesList.stream()
						.filter(lackedTypes -> lackedTypes.stream().noneMatch(removedTypeSet::contains))
						.collect(toList()))
				.collect(toList());
		return selectStream(allLackedTypes).map(select -> select.stream().flatMap(List::stream).collect(toList()))
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
	}

	/**
	 * @throws NoSuchElementException
	 *             候选牌不够
	 */
	private List<Tile> getTileFromCandidates(Collection<TileType> types, Map<TileType, List<Tile>> candidatesByType)
			throws NoSuchElementException {
		List<Tile> result = new ArrayList<>();
		types.stream().collect(groupingBy(Function.identity(), counting())).forEach((type, count) -> {
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
		@SuppressWarnings("unused")
		int lackedCount;

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

		List<Tile> tiles() {
			return tiles;
		}

		@Override
		public String toString() {
			return "[isJiang=" + isJiang + ", tiles=" + tiles + ", lackedTypesList=" + lackedTypesList + "]";
		}

	}

}
