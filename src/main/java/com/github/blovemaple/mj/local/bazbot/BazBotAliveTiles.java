package com.github.blovemaple.mj.local.bazbot;

import static com.github.blovemaple.mj.local.bazbot.BazBotTileUnit.BazBotTileUnitType.*;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileType;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * BazBot手中的活牌。用于计算{@link #tileTypesToWin()}。有缓存。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
class BazBotAliveTiles {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BazBotAliveTiles.class.getSimpleName());

	private static final Cache<Set<Tile>, BazBotAliveTiles> cache = //
			CacheBuilder.newBuilder().maximumSize(20).build();

	public static BazBotAliveTiles of(Set<Tile> aliveTiles) {
		try {
			return cache.get(aliveTiles, () -> new BazBotAliveTiles(aliveTiles));
		} catch (ExecutionException e) {
			// not possible
			throw new RuntimeException(e);
		}
	}

	private final Set<Tile> aliveTiles;
	private List<BazBotTileNeighborhood> neighborhoods; // lazy
	private List<List<TileType>> tileTypesToWin; // lazy

	private BazBotAliveTiles(Set<Tile> aliveTiles) {
		this.aliveTiles = aliveTiles;
	}

	public List<BazBotTileNeighborhood> neighborhoods() {
		return neighborhoods;
	}

	public List<List<TileType>> tileTypesToWin() {
		if (tileTypesToWin != null)
			return tileTypesToWin;

		synchronized (this) {
			if (tileTypesToWin != null)
				return tileTypesToWin;

			neighborhoods = BazBotTileNeighborhood.parse(aliveTiles);
			int forShunkeCount = aliveTiles.size() / 3;

			tileTypesToWin = Stream.of(new BazBotChoosingTileUnits(neighborhoods, forShunkeCount)) // 一个初始units，为flatmap做准备
					.flatMap(units -> units.newToChoose(COMPLETE_JIANG, true)) // 选所有完整将牌，以及不选完整将牌
					.flatMap(units -> units.newToChoose(COMPLETE_SHUNKE, false)) // 选所有合适的完整顺刻组合
					.flatMap(units -> units.newToChoose(UNCOMPLETE_SHUNKE_FOR_ONE, false)) // 选所有合适的不完整顺刻组合（缺一张的）
					.flatMap(units -> units.newToChoose(UNCOMPLETE_SHUNKE_FOR_TWO, false)) // 选所有合适的不完整顺刻组合（缺两张的）
					.flatMap(units -> units.newToChoose(UNCOMPLETE_JIANG, false)) // 选所有合适的不完整将牌
//					.peek(System.out::println)
					.flatMap(BazBotChoosingTileUnits::tileTypesToWin) // 计算tileUnits和牌所需牌型
//					.peek(System.out::println)
					.peek(tileTypes -> tileTypes.sort(naturalOrder())) // 每组tileType内部排序，准备去重
					.distinct() // 去重
					.collect(toList()) // 收集结果
			;

			return tileTypesToWin;
		}
	}
}
