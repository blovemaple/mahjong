package com.github.blovemaple.mj.local.barbot;

import static com.github.blovemaple.mj.object.StandardTileUnitType.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileUnit;

class BarBotSimChanging {
	private BarBotCpgdChoice choice;

	private Collection<Tile> removedTiles;
	private Collection<Tile> addedTiles;

	private Set<Tile> aliveTiles;

	private Integer winPoint;
	private Double prob;

	public BarBotSimChanging(BarBotCpgdChoice choice, Collection<Tile> removedTiles, Collection<Tile> addedTiles) {
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
					.getFans( // FIXME 如果是subchoice，则这个contextview是上层的
							choice.getBaseContextView(), choice.getPlayerInfo(), getAliveTiles(), null)
					.values().stream().mapToInt(f -> f).sum();
		return winPoint;
	}

	public void setWinPoint(Integer winPoint) {
		this.winPoint = winPoint;
	}

	public double getProb() {
		if (prob == null) {
			double addedTilesProb = choice.getTask().getProb(addedTiles);
			prob = choice.getForWinTypes().stream()
					// 取choice对应的所有和牌类型解析成的所有unit集合
					.flatMap(winType -> winType.parseWinTileUnits(choice.getPlayerInfo(), getAliveTiles(), null)
							.stream())
					// 取最大的系数
					.map(this::getRatio).max(Comparator.naturalOrder())
					// 乘以概率
					.map(ratio -> ratio * addedTilesProb).orElse(0d);
		}
		return prob;
	}

	// 系数= 1 * 4^涉及的刻子数 * 2^涉及的顺子数
	private int getRatio(Collection<TileUnit> units) {
		return units.stream().filter(unit -> !Collections.disjoint(unit.getTiles(), addedTiles)).map(TileUnit::getType)
				.reduce(1, (r, t) -> r * (t == KEZI ? 4 : t == SHUNZI ? 2 : 1), Math::multiplyExact);
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
		return removedTiles.containsAll(this.removedTiles) && addedTiles.containsAll(this.addedTiles);
	}

	@Override
	public String toString() {
		return "[prob=" + prob + ", removedTiles=" + removedTiles + ", addedTiles=" + addedTiles + "]";
	}

}