package com.github.blovemaple.mj.local.bazbot;

import static com.github.blovemaple.mj.object.StandardTileUnitType.*;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroupPlayerView;
import com.github.blovemaple.mj.object.TileRank.NumberRank;
import com.github.blovemaple.mj.object.TileType;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class BazBotScoreCalculator {
	private final GameContextPlayerView contextView;

	// 第一步生成：完整将牌、完整顺刻、不完整将牌、不完整顺刻（缺一张）、不完整顺刻（缺两张）
	private List<BazBotTileUnit> completedJiangs = new ArrayList<>();
	private List<BazBotTileUnit> completedShunKes = new ArrayList<>();
	private List<BazBotTileUnit> uncompletedJiangs = new ArrayList<>();
	private List<BazBotTileUnit> uncompletedShunKesForOne = new ArrayList<>();
	private List<BazBotTileUnit> uncompletedShunKesForTwo = new ArrayList<>();

	// 第二步生成：和牌需要增加的牌的牌型
	private Set<List<TileType>> tileTypesToWin = new HashSet<>();

	// 第三步生成：所有不可见的牌按牌型统计数量
	private long invisibleTotleCount;
	private Map<TileType, Long> invisibleCountByTileType;

	// 第四步生成：最终评分
	private double score;

	public BazBotScoreCalculator(GameContextPlayerView contextView) {
		this.contextView = contextView;
	}

	public double calcScore() {
		// 第一步：解析完整顺刻、完整将牌、不完整顺刻、不完整将牌
		parseUnits();

		// 第二步：从完整到不完整依次选择，过滤，得出和牌需要增加的牌的牌型，去重，直到一定组数
		genTileTypesToWin();

		// 第三步：按牌型统计所有不可见的牌
		statInvisibleCount();

		// 第四步：计算概率，求和，得出最终评分
		calcFinalScore();

		return score;
	}

	private void parseUnits() {
		// 活牌按牌型排序
		List<Tile> aliveTileList = contextView.getMyInfo().getAliveTiles() //
				.stream().sorted(comparing(Tile::type)).collect(toList());
		// 同花色且相隔小于两个数的是neighbors，按neighbors分组进行解析。不同的neighbors分组之间不可能组成完整或不完整单元。
		parseNeighborsList(aliveTileList).forEach(this::parseOneNeighbors);
	}

	private List<List<Tile>> parseNeighborsList(List<Tile> tiles) {
		List<List<Tile>> res = new ArrayList<>();

		List<Tile> crtNeighbors = null;
		Tile lastTile = null;
		for (Tile tile : tiles) {
			if (crtNeighbors == null || !isNeighbors(lastTile, tile)) {
				crtNeighbors = new ArrayList<>();
				res.add(crtNeighbors);
			}
			crtNeighbors.add(tile);
			lastTile = tile;
		}
		return res;
	}

	private boolean isNeighbors(Tile tile1, Tile tile2) {
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

	private void parseOneNeighbors(List<Tile> neighbors) {
		parseOneNeighbors(true, neighbors.stream().toArray(Tile[]::new));
	}

	private void parseOneNeighbors(boolean recursive, Tile... neighbors) {
		switch (neighbors.length) {
		case 1:
			// 一张牌，是不完整的将牌、顺子、刻子
			uncompletedJiangs.add(BazBotTileUnit.uncompleted(JIANG, Set.of(neighbors[0])));
			uncompletedShunKesForTwo.add(BazBotTileUnit.uncompleted(SHUNZI, Set.of(neighbors[0])));
			uncompletedShunKesForTwo.add(BazBotTileUnit.uncompleted(KEZI, Set.of(neighbors[0])));
			break;
		case 2:
			if (neighbors[0].type() == neighbors[1].type()) {
				// 牌型相同的两张牌，是完整的将牌、不完整的刻子
				completedJiangs.add(BazBotTileUnit.completed(JIANG, Set.of(neighbors[0], neighbors[1])));
				uncompletedShunKesForOne.add(BazBotTileUnit.uncompleted(KEZI, Set.of(neighbors[0], neighbors[1])));
			} else {
				// 牌型不同的两张牌，是不完整的顺子
				uncompletedShunKesForOne.add(BazBotTileUnit.uncompleted(SHUNZI, Set.of(neighbors[0], neighbors[1])));
			}
			break;
		case 3:
			if (neighbors[0].type() == neighbors[1].type() && neighbors[0].type() == neighbors[2].type()) {
				// 牌型相同的三张牌，是完整的刻子
				completedShunKes
						.add(BazBotTileUnit.uncompleted(KEZI, Set.of(neighbors[0], neighbors[1], neighbors[2])));
				break;
			}
			if (neighbors[0].type().rank() instanceof NumberRank) {
				int number0 = ((NumberRank) neighbors[0].type().rank()).number();
				int number1 = ((NumberRank) neighbors[1].type().rank()).number();
				int number2 = ((NumberRank) neighbors[2].type().rank()).number();
				if (number0 + 1 == number1 && number1 + 1 == number2) {
					// NumberRank连续的三张牌，是完整的顺子
					completedShunKes
							.add(BazBotTileUnit.uncompleted(SHUNZI, Set.of(neighbors[0], neighbors[1], neighbors[2])));
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
				parseOneNeighbors(false, neighbors[i0]);

				for (int i1 = i0 + 1; i1 < neighbors.length && isNeighbors(neighbors[i0], neighbors[i1]); i1++) {
					if (i1 >= i0 + 2 && neighbors[i1].type() == neighbors[i1 - 1].type())
						continue; // 牌型相同不再重复解析
					parseOneNeighbors(false, neighbors[i0], neighbors[i1]);

					if (neighbors.length == 3)
						continue; // 三张的在上面的case分支已经走过了
					for (int i2 = i1 + 1; i2 < neighbors.length && isNeighbors(neighbors[i0], neighbors[i2]); i2++) {
						if (i2 >= i1 + 2 && neighbors[i2].type() == neighbors[i2 - 1].type())
							continue; // 牌型相同不再重复解析
						parseOneNeighbors(false, neighbors[i0], neighbors[i1], neighbors[i2]);
					}
				}
			}
		}

	}

	private void genTileTypesToWin() {
		// TODO
		// 先选尽量多的完整顺刻，再选完整将牌。
		// 在选择完整顺刻的过程中，如果某一组完整将牌一直因为与完整顺刻相冲突而没有选中，则把所有选中的组合复制一份，去掉冲突的完整顺刻，把将牌替换成这组将牌
		// 顺刻不够（注意所需的顺刻数要减去已有的groups）的组合尽量在缺一张的不完整顺刻中选，再不够就在缺两张的不完整顺刻中选
		// 没有将牌的组合在不完整将牌中选
		// 过滤掉缺的牌和丢弃的牌有重复的组合
		// 计算所有组合缺的牌，转换成牌型list，list内部排序后添加进set去重
	}

	private void statInvisibleCount() {
		// 在所有牌中去掉所有可见牌，留下不可见的牌
		Set<Tile> invisibleTiles = new HashSet<>(contextView.getGameStrategy().getAllTiles());
		// 去掉：自己的活牌、打出的牌、牌组中的牌
		invisibleTiles.removeAll(contextView.getMyInfo().getAliveTiles());
		invisibleTiles.removeAll(contextView.getMyInfo().getDiscardedTiles());
		contextView.getMyInfo().getTileGroups().forEach(group -> invisibleTiles.removeAll(group.getTiles()));
		// 去掉：其他玩家打出的牌、牌组中可见的牌
		contextView.getTableView().getPlayerInfoView().forEach((location, playerView) -> {
			if (location != contextView.getMyLocation()) {
				invisibleTiles.removeAll(playerView.getDiscardedTiles());
				playerView.getTileGroups().stream().map(TileGroupPlayerView::getTiles).filter(Objects::nonNull)
						.forEach(invisibleTiles::removeAll);
			}
		});

		// 统计不可见的牌
		invisibleTotleCount = invisibleTiles.size();
		invisibleCountByTileType = invisibleTiles.stream().collect(groupingBy(Tile::type, counting()));
	}

	private void calcFinalScore() {
		// tileTypesToWin中每组的概率相加
		score = tileTypesToWin.stream().mapToDouble(tileTypes -> {
			Map<TileType, Integer> removedInvisibleCountByType = new HashMap<>();
			AtomicInteger removedTotle = new AtomicInteger(0);
			// 每组中每个tileType的概率相乘
			return tileTypes.stream()
					// 取该牌型不可见的牌数
					.map(type -> {
						long oriCount = invisibleCountByTileType.getOrDefault(type, 0L);
						if (oriCount == 0L)
							return 0L;
						int removed = removedInvisibleCountByType.getOrDefault(type, 0);
						removedInvisibleCountByType.put(type, removed + 1);
						removedTotle.incrementAndGet();
						return oriCount + removed;
					})
					// 除以不可见牌的总数得出概率
					.mapToDouble(count -> count.doubleValue() / (invisibleTotleCount - removedTotle.get()))
					// 所有概率相乘
					.reduce((prob1, prob2) -> prob1 * prob2).orElse(0d);
		}).sum();
	}

}
