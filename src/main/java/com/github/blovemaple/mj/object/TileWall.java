package com.github.blovemaple.mj.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.github.blovemaple.mj.object.PlayerLocation.Relation;

/**
 * 牌墙。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class TileWall implements TileWallPlayerView {
	private final Map<PlayerLocation, List<TileWallPile>> piles;
	private int remainTileCount;
	private PlayerLocation headLocation, bottomLocation;
	private int headPileIndex, bottomPileIndex;
	private boolean initial;

	public TileWall() {
		piles = new EnumMap<>(PlayerLocation.class);
		for (PlayerLocation location : PlayerLocation.values())
			piles.put(location, new ArrayList<>());
	}

	public synchronized void init(Collection<Tile> tiles) {
		if (tiles.size() < 7)
			throw new IllegalArgumentException("tiles are too few.");

		piles.values().forEach(List::clear);

		TileWallPile pile = null;
		int index = 0;
		for (Tile tile : tiles) {
			PlayerLocation location = PlayerLocation.values()[Math.min(4, index / (tiles.size() / 4))];

			if (pile == null) {
				pile = new TileWallPile();
				piles.get(location).add(pile);
			}

			if (pile.getLower() == null) {
				pile.setLower(tile);
			} else {
				pile.setUpper(tile);
				pile = null;
			}

			index++;
		}

		remainTileCount = tiles.size();
		headLocation = PlayerLocation.EAST;
		headPileIndex = 0;
		bottomLocation = PlayerLocation.SOUTH;
		bottomPileIndex = piles.get(bottomLocation).size() - 1;
		initial = true;
	}

	public synchronized void setDrawPosition(PlayerLocation location, int pileIndex) {
		if (!initial)
			throw new IllegalStateException();
		if (pileIndex < 0 || pileIndex >= piles.get(location).size())
			throw new IllegalArgumentException("Illegal pileIndex: " + pileIndex);

		headLocation = location;
		headPileIndex = pileIndex;

		if (headPileIndex > 0) {
			bottomLocation = headLocation;
			bottomPileIndex = headPileIndex - 1;
		} else {
			bottomLocation = headLocation.getLocationOf(Relation.PREVIOUS);
			bottomPileIndex = piles.get(bottomLocation).size() - 1;
		}
	}

	public synchronized List<Tile> draw(int count) {
		if (count <= 0)
			throw new IllegalArgumentException("Illegal draw count: " + count);
		if (count > remainTileCount)
			throw new IllegalArgumentException(
					"No enough tiles to draw: remain=" + remainTileCount + " required=" + count);

		List<Tile> drawed = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			TileWallPile pile = piles.get(headLocation).get(headPileIndex);
			drawed.add(pile.draw());
			if (pile.getSize() == 0) {
				if (headPileIndex < piles.get(headLocation).size() - 1) {
					headPileIndex++;
				} else {
					headLocation = headLocation.getLocationOf(Relation.NEXT);
					headPileIndex = 0;
				}
			}
		}

		remainTileCount -= count;
		initial = false;
		return drawed;
	}

	public synchronized List<Tile> drawBottom(int count) {
		if (count <= 0)
			throw new IllegalArgumentException("Illegal draw count: " + count);
		if (count > remainTileCount)
			throw new IllegalArgumentException(
					"No enough tiles to draw: remain=" + remainTileCount + " required=" + count);

		List<Tile> drawed = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			TileWallPile pile = piles.get(bottomLocation).get(bottomPileIndex);
			drawed.add(pile.draw());
			if (pile.getSize() == 0) {
				if (bottomPileIndex > 0) {
					bottomPileIndex--;
				} else {
					bottomLocation = bottomLocation.getLocationOf(Relation.PREVIOUS);
					bottomPileIndex = piles.get(bottomLocation).size() - 1;
				}
			}
		}

		remainTileCount -= count;
		initial = false;
		return drawed;
	}

	@Override
	public int getRemainTileCount() {
		return remainTileCount;
	}

	@Override
	public List<TileWallPile> getPiles(PlayerLocation location) {
		return Collections.unmodifiableList(piles.get(location));
	}
}
