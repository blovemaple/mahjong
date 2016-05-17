package com.github.blovemaple.mj.local.barbot;

import static com.github.blovemaple.mj.object.StandardTileUnitType.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.object.TileUnit;

class BarBotSimChanging {
	private BarBotCpgdChoice choice;

	private Collection<Tile> removedTiles;
	private Collection<Tile> addedTiles;

	private Set<Tile> aliveTiles;

	private Integer winPoint;
	private Double prob;

	public BarBotSimChanging(BarBotCpgdChoice choice, Collection<Tile> removedTiles,
			Collection<Tile> addedTiles) {
		this.choice = choice;
		this.removedTiles = removedTiles;
		this.addedTiles = addedTiles;
	}

	protected Collection<Tile> getRemovedTiles() {
		return removedTiles;
	}

	protected Collection<Tile> getAddedTiles() {
		return addedTiles;
	}

	public boolean isWin() {
		return getWinPoint() > 0;
	}

	public Integer getWinPoint() {
		if (winPoint == null)
			winPoint = choice.getBaseContextView().getGameStrategy()
					.getFans(choice.getPlayerInfo(), getAliveTiles()).values()
					.stream().mapToInt(f -> f).sum();
		return winPoint;
	}

	public void setWinPoint(Integer winPoint) {
		this.winPoint = winPoint;
	}

	public double getProb() {
		if (prob == null) {
			prob = choice.getBaseContextView().getGameStrategy()
					// 取所有和牌类型解析成的所有unit集合
					.getAllWinTypes().stream()
					.flatMap(winType -> winType.parseWinTileUnits(
							choice.getPlayerInfo(), getAliveTiles()))
					// 取系数最大的一个
					.max(Comparator.comparing(this::getRatio))
					// 算可能性
					.map(this::getProb).orElse(0d);
		}
		return prob;
	}

	// 系数= 1 * 4^涉及的刻子数 * 2^涉及的顺子数
	private int getRatio(Set<TileUnit> units) {
		return units.stream()
				.filter(unit -> !Collections.disjoint(unit.getTiles(),
						addedTiles))
				.map(TileUnit::getType).reduce(1, (r, t) -> r * (t == KEZI ? 4
						: t == SHUNZI ? 2 : 1), Math::multiplyExact);
	}

	// addedTiles出现的可能性*系数
	// addedTiles出现的可能性=(在剩余牌中选到addedTiles的组合数)/(在剩余牌中选addedTiles个牌的组合数)
	private double getProb(Set<TileUnit> units) {
		Map<TileType, List<Tile>> addedByType = addedTiles.stream()
				.collect(Collectors.groupingBy(Tile::type));
		Map<TileType, Long> remainTiles = choice.getTask()
				.remainTileCountByType();

		AtomicInteger addedComb = new AtomicInteger(1);
		addedByType.forEach((type, added) -> addedComb.getAndAccumulate(
				combCount(remainTiles.get(type), added.size()),
				Math::multiplyExact));

		return (double) addedComb.get()
				/ combCount(choice.getTask().remainTileCount(),
						addedTiles.size())
				* getRatio(units);
	}

	private int combCount(long total, int select) {
		int result = 1;
		int limit = (int) total - select;
		for (int i = (int) total; i > limit; i--)
			result *= i;
		return result;
	}

	private Set<Tile> getAliveTiles() {
		if (aliveTiles == null) {
			aliveTiles = new HashSet<>(choice.getPlayerInfo().getAliveTiles());
			aliveTiles.removeAll(removedTiles);
			aliveTiles.addAll(addedTiles);
		}
		return aliveTiles;
	}

	public boolean isCovered(Collection<Tile> removedTiles, Collection<Tile> addedTiles) {
		if (removedTiles.size() < this.removedTiles.size())
			return false;
		if (addedTiles.size() < this.addedTiles.size())
			return false;
		return removedTiles.containsAll(this.removedTiles)
				&& addedTiles.containsAll(this.addedTiles);
	}

	@Override
	public String toString() {
		return "[prob=" + prob + ", removedTiles=" + removedTiles + ", addedTiles=" + addedTiles + "]";
	}

}