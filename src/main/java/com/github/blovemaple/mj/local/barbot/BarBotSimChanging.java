package com.github.blovemaple.mj.local.barbot;

import java.util.HashSet;
import java.util.Set;

import com.github.blovemaple.mj.object.Tile;

class BarBotSimChanging {
	private BarBotCpgdChoice choice;

	private Set<Tile> removedTiles;
	private Set<Tile> addedTiles;

	private Integer winPoint;

	public BarBotSimChanging(BarBotCpgdChoice choice, Set<Tile> removedTiles,
			Set<Tile> addedTiles) {
		this.choice = choice;
		this.removedTiles = removedTiles;
		this.addedTiles = addedTiles;
	}

	public boolean isWin() {
		return getWinPoint() > 0;
	}

	public Integer getWinPoint() {
		if (winPoint == null) {
			Set<Tile> aliveTiles = new HashSet<>(
					choice.getPlayerInfo().getAliveTiles());
			aliveTiles.removeAll(removedTiles);
			aliveTiles.addAll(addedTiles);
			winPoint = choice.getBaseContextView().getGameStrategy()
					.getFans(choice.getPlayerInfo(), aliveTiles).values()
					.stream().mapToInt(f -> f).sum();
		}
		return winPoint;
	}

	public void setWinPoint(Integer winPoint) {
		this.winPoint = winPoint;
	}

	public double getProb() {
		// TODO
		return 0;
	}

	public boolean isCovered(Set<Tile> removedTiles, Set<Tile> addedTiles) {
		if (removedTiles.size() < this.removedTiles.size())
			return false;
		if (addedTiles.size() < this.addedTiles.size())
			return false;
		return removedTiles.containsAll(this.removedTiles)
				&& addedTiles.containsAll(this.addedTiles);
	}

}