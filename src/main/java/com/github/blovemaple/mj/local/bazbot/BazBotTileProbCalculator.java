package com.github.blovemaple.mj.local.bazbot;

import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroupPlayerView;
import com.github.blovemaple.mj.object.TileType;

/**
 * 计算指定tiles在一个{@link GameContextPlayerView}的当前状态下产生概率的工具。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
class BazBotTileProbCalculator {
	/**
	 * 返回指定contextView当前状态下的一个BazBotTileProbCalculator。<br>
	 * TODO 缓存。BazBot的一次计算中可见牌都相同，为了共用同一个calculator，<br>
	 * 新建BazBot专用的GameContextPlayerView（另写一个of方法传入），并在一次请求中的view都记录成一个id，用于在缓存中检索。
	 */
	public static BazBotTileProbCalculator of(GameContextPlayerView contextView) {
		return new BazBotTileProbCalculator(contextView);
	}

	private GameContextPlayerView contextView;

	// 所有不可见的牌按牌型统计数量
	private long invisibleTotleCount;
	private Map<TileType, Long> invisibleCountByTileType;

	private BazBotTileProbCalculator(GameContextPlayerView contextView) {
		this.contextView = contextView;
	}

	private void initInvisibleCount() {
		if (invisibleCountByTileType != null)
			return; // already inited

		synchronized (this) {
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
	}

	public double calcProb(Collection<TileType> tileTypes) {
		initInvisibleCount();

		Map<TileType, Integer> removedInvisibleCountByType = new HashMap<>();
		AtomicInteger removedTotle = new AtomicInteger(0);
		// 每个tileType的概率相乘
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
				.reduce((prob1, prob2) -> prob1 * prob2).orElse(1d);
	}

}
