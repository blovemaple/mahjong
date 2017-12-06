package com.github.blovemaple.mj.local.bazbot;

import static com.github.blovemaple.mj.object.StandardTileUnitType.*;
import static java.util.stream.Collectors.*;
import static com.github.blovemaple.mj.utils.LambdaUtils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.github.blovemaple.mj.local.bazbot.BazBotTileUnit.BazBotTileUnitType;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileRank.NumberRank;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 两两可组成{@link BazBotTileUnit}（每张牌至少能和另一张牌组成一个{@link BazBotTileUnit}）的一组牌。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
class BazBotTileNeighborhood {

	private static final Cache<List<Tile>, BazBotTileNeighborhood> cache = //
			CacheBuilder.newBuilder().maximumSize(200).build();

	/**
	 * 把指定的Tile集合解析为若干个neiborhood并返回。
	 */
	public static List<BazBotTileNeighborhood> parse(Set<Tile> tiles) {
		List<Tile> tileList = tiles.stream().sorted().collect(Collectors.toList());

		List<List<Tile>> neighborhoodTilesList = new ArrayList<>();
		List<Tile> crtNeighbors = null;
		Tile lastTile = null;
		for (Tile tile : tileList) {
			if (crtNeighbors == null || !isNeighbors(lastTile, tile)) {
				crtNeighbors = new ArrayList<>();
				neighborhoodTilesList.add(crtNeighbors);
			}
			crtNeighbors.add(tile);
			lastTile = tile;
		}
		try {
			return neighborhoodTilesList.stream()
					.map(rethrowFunction(
							hoodTiles -> cache.get(hoodTiles, () -> new BazBotTileNeighborhood(hoodTiles))))
					.collect(Collectors.toList());
		} catch (ExecutionException e) {
			// not possible
			throw new RuntimeException(e);
		}
	}

	private static boolean isNeighbors(Tile tile1, Tile tile2) {
		if (tile1.type() == tile2.type())
			return true;

		if (tile1.type().suit() != tile2.type().suit())
			return false;
		if (tile1.type().suit().getRankClass() != NumberRank.class)
			return false;
		int number1 = ((NumberRank) tile1.type().rank()).number();
		int number2 = ((NumberRank) tile2.type().rank()).number();
		if (number2 - number1 > 2)
			return false;
		return true;
	}

	private List<Tile> tiles;

	// 完整将牌、完整顺刻、不完整将牌、不完整顺刻（缺一张）、不完整顺刻（缺两张）
	private boolean parsed = false;
	private List<BazBotTileUnit> completedJiangs = new ArrayList<>();
	private List<BazBotTileUnit> completedShunKes = new ArrayList<>();
	private List<BazBotTileUnit> uncompletedJiangs = new ArrayList<>();
	private List<BazBotTileUnit> uncompletedShunKesForOne = new ArrayList<>();
	private List<BazBotTileUnit> uncompletedShunKesForTwo = new ArrayList<>();
	private List<BazBotTileUnit> allUnits = new ArrayList<>();

	/**
	 * @param tiles
	 *            注意：传入后不会做校验，调用者必须保证tiles是排好序的，并且是一个neighborhood中的牌。
	 */
	private BazBotTileNeighborhood(List<Tile> tiles) {
		this.tiles = tiles;
	}

	private void initParseUnits() {
		if (parsed)
			return;

		synchronized (this) {
			parseUnits(true, tiles.stream().toArray(Tile[]::new));
			allUnits.addAll(completedJiangs);
			allUnits.addAll(completedShunKes);
			allUnits.addAll(uncompletedJiangs);
			allUnits.addAll(uncompletedShunKesForOne);
			allUnits.addAll(uncompletedShunKesForTwo);
			parsed = true;
		}
	}

	private void parseUnits(boolean recursive, Tile... neighbors) {
		switch (neighbors.length) {
		case 1:
			// 一张牌，是不完整的将牌、顺子、刻子
			uncompletedJiangs.add(BazBotTileUnit.uncompleted(JIANG, Set.of(neighbors[0]), this));
			uncompletedShunKesForTwo.add(BazBotTileUnit.uncompleted(SHUNZI, Set.of(neighbors[0]), this));
			uncompletedShunKesForTwo.add(BazBotTileUnit.uncompleted(KEZI, Set.of(neighbors[0]), this));
			break;
		case 2:
			if (neighbors[0].type() == neighbors[1].type()) {
				// 牌型相同的两张牌，是完整的将牌、不完整的刻子
				completedJiangs.add(BazBotTileUnit.completed(JIANG, Set.of(neighbors[0], neighbors[1]), this));
				uncompletedShunKesForOne
						.add(BazBotTileUnit.uncompleted(KEZI, Set.of(neighbors[0], neighbors[1]), this));
			} else {
				// 牌型不同的两张牌，是不完整的顺子
				uncompletedShunKesForOne
						.add(BazBotTileUnit.uncompleted(SHUNZI, Set.of(neighbors[0], neighbors[1]), this));
			}
			break;
		case 3:
			if (neighbors[0].type() == neighbors[1].type() && neighbors[0].type() == neighbors[2].type()) {
				// 牌型相同的三张牌，是完整的刻子
				completedShunKes
						.add(BazBotTileUnit.uncompleted(KEZI, Set.of(neighbors[0], neighbors[1], neighbors[2]), this));
				break;
			}
			if (neighbors[0].type().rank() instanceof NumberRank) {
				int number0 = ((NumberRank) neighbors[0].type().rank()).number();
				int number1 = ((NumberRank) neighbors[1].type().rank()).number();
				int number2 = ((NumberRank) neighbors[2].type().rank()).number();
				if (number0 + 1 == number1 && number1 + 1 == number2) {
					// NumberRank连续的三张牌，是完整的顺子
					completedShunKes.add(
							BazBotTileUnit.uncompleted(SHUNZI, Set.of(neighbors[0], neighbors[1], neighbors[2]), this));
					break;
				}
			}
			// 非顺子/刻子的三张牌，走default逻辑
			if (!recursive)
				break; // 四张和以上的由default分支递归解析三张牌时会走到这，不再走default逻辑，因为一张和两张的已经在default解析过了
		default:
			// default逻辑：解析全部一张、两张、三张的组合，其中三张不再重复递归，不然会无限递归
			for (int i0 = 0; i0 < neighbors.length; i0++) {
				if (i0 >= 1 && neighbors[i0].type() == neighbors[i0 - 1].type())
					continue; // 牌型相同不再重复解析
				parseUnits(false, neighbors[i0]);

				for (int i1 = i0 + 1; i1 < neighbors.length && isNeighbors(neighbors[i0], neighbors[i1]); i1++) {
					if (i1 >= i0 + 2 && neighbors[i1].type() == neighbors[i1 - 1].type())
						continue; // 牌型相同不再重复解析
					parseUnits(false, neighbors[i0], neighbors[i1]);

					if (neighbors.length == 3)
						continue; // 三张的在上面的case分支已经走过了
					for (int i2 = i1 + 1; i2 < neighbors.length && isNeighbors(neighbors[i0], neighbors[i2]); i2++) {
						if (i2 >= i1 + 2 && neighbors[i2].type() == neighbors[i2 - 1].type())
							continue; // 牌型相同不再重复解析
						parseUnits(false, neighbors[i0], neighbors[i1], neighbors[i2]);
					}
				}
			}
		}

	}

	public List<BazBotTileUnit> getNonConflictingUnits(BazBotTileUnitType type, List<BazBotTileUnit> conflictings) {
		initParseUnits();

		List<BazBotTileUnit> allUnits;
		switch (type) {
		case COMPLETE_JIANG:
			allUnits = completedJiangs;
			break;
		case COMPLETE_SHUNKE:
			allUnits = completedShunKes;
			break;
		case UNCOMPLETE_JIANG:
			allUnits = uncompletedJiangs;
			break;
		case UNCOMPLETE_SHUNKE_FOR_ONE:
			allUnits = uncompletedShunKesForOne;
			break;
		case UNCOMPLETE_SHUNKE_FOR_TWO:
			allUnits = uncompletedShunKesForTwo;
			break;
		default:
			throw new RuntimeException("Unrecignized BazBotTileUnitType: " + type);
		}
		if (allUnits.isEmpty())
			return List.of();

		Set<Tile> conflictTiles = conflictings.stream().map(BazBotTileUnit::tiles).flatMap(Set::stream)
				.collect(toSet());
		return allUnits.stream().filter(unit -> !unit.conflictWith(conflictTiles)).collect(toList());
	}

	public List<Tile> getRemainingTiles(List<BazBotTileUnit> chosenUnits) {
		if (chosenUnits.isEmpty())
			return new ArrayList<>(tiles);
		return allUnits.stream().filter(unit -> !chosenUnits.contains(unit)).map(BazBotTileUnit::tiles)
				.flatMap(Set::stream).collect(toList());
	}
}
