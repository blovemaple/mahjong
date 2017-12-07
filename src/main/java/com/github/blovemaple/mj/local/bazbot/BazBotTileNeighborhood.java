package com.github.blovemaple.mj.local.bazbot;

import static com.github.blovemaple.mj.object.StandardTileUnitType.*;
import static com.github.blovemaple.mj.utils.LambdaUtils.*;
import static com.github.blovemaple.mj.utils.MyUtils.*;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
		List<Tile> tileList = tiles.stream() //
				.sorted(comparing(Tile::type).thenComparing(Tile::id)) //
				.collect(Collectors.toList());

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
	private transient List<BazBotTileUnit> allUnits = new ArrayList<>();

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
			parseUnits();
			allUnits.addAll(completedJiangs);
			allUnits.addAll(completedShunKes);
			allUnits.addAll(uncompletedJiangs);
			allUnits.addAll(uncompletedShunKesForOne);
			allUnits.addAll(uncompletedShunKesForTwo);
			parsed = true;
		}
	}

	private void parseUnits() {
		parseUnits(true, this.tiles);
	}

	private void parseUnits(boolean init, List<Tile> tiles) {
		if (init && tiles.size() == 3) {
			if (SHUNZI.isLegalTiles(tiles)) {
				// 整个neighborhood正好是一个顺子，不再拆开
				completedShunKes
						.add(BazBotTileUnit.completed(SHUNZI, Set.of(tiles.get(0), tiles.get(1), tiles.get(2)), this));
				return;
			}
			if (KEZI.isLegalTiles(tiles)) {
				// 整个neighborhood正好是一个刻子，不再拆开
				completedShunKes
						.add(BazBotTileUnit.completed(KEZI, Set.of(tiles.get(0), tiles.get(1), tiles.get(2)), this));
				return;
			}
		}

		// 第一张牌自己是不完整的将牌、顺子、刻子
		Tile tile0 = tiles.get(0);
		uncompletedJiangs.add(BazBotTileUnit.uncompleted(JIANG, Set.of(tile0), this));
		uncompletedShunKesForTwo.add(BazBotTileUnit.uncompleted(SHUNZI, Set.of(tile0), this));
		uncompletedShunKesForTwo.add(BazBotTileUnit.uncompleted(KEZI, Set.of(tile0), this));

		// 从第二张牌起，与每张neighbor去重后组成完整将牌、不完整顺子/刻子
		if (tiles.size() >= 2) {
			tiles.subList(1, tiles.size()).stream() //
					.filter(distinctorBy(Tile::type)) //
					.takeWhile(tileN -> isNeighbors(tile0, tileN)) //
					.forEach(tileN -> {
						if (tile0.type() == tileN.type()) {
							completedJiangs.add(BazBotTileUnit.completed(JIANG, Set.of(tile0, tileN), this));
							uncompletedShunKesForOne.add(BazBotTileUnit.uncompleted(KEZI, Set.of(tile0, tileN), this));
						} else {
							uncompletedShunKesForOne
									.add(BazBotTileUnit.uncompleted(SHUNZI, Set.of(tile0, tileN), this));
						}
					});
		}

		// 从第二张牌起，与每两张neighbors组成完整顺子/刻子
		if (tiles.size() >= 3) {
			if (tile0.type() == tiles.get(1).type() && tile0.type() == tiles.get(2).type())
				// 牌型相同的三张牌，是完整的刻子
				completedShunKes.add(BazBotTileUnit.completed(KEZI, Set.of(tile0, tiles.get(1), tiles.get(2)), this));
			if (tile0.type().suit().getRankClass() == NumberRank.class && tile0.type().number() <= 7) {
				int rank0 = tile0.type().number();
				Tile tile1 = null, tile2 = null;
				for (int i = 1; i < tiles.size(); i++) {
					Tile tileI = tiles.get(i);
					if (tile1 == null && tileI.type().number() == rank0 + 1) {
						tile1 = tileI;
					} else if (tile2 == null && tileI.type().number() == rank0 + 2) {
						tile2 = tileI;
						break;
					}
				}
				if (tile1 != null && tile2 != null)
					// 数字连续的三张牌，是完整的顺子
					completedShunKes.add(BazBotTileUnit.completed(SHUNZI, Set.of(tile0, tile1, tile2), this));
			}
		}

		// 递归parse从与第一张不同牌型的牌开始的子列表
		if (tiles.size() > 1)
			IntStream.range(1, tiles.size()) //
					.dropWhile(i -> tile0.type() == tiles.get(i).type()) //
					.findFirst().ifPresent( //
							firstIndexOfDiffType -> parseUnits(false,
									tiles.subList(firstIndexOfDiffType, tiles.size())));
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
		List<Tile> remainingTiles = new ArrayList<>(tiles);
		chosenUnits.stream().map(BazBotTileUnit::tiles).forEach(remainingTiles::removeAll);
		return remainingTiles;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}
