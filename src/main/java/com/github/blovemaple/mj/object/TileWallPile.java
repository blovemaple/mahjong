package com.github.blovemaple.mj.object;

/**
 * 牌墙里的牌堆。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class TileWallPile implements TileWallPilePlayerView {
	private Tile upper, lower;

	public Tile getUpper() {
		return upper;
	}

	public void setUpper(Tile upper) {
		if (upper != null && getLower() == null)
			throw new IllegalStateException();
		this.upper = upper;
	}

	public Tile getLower() {
		return lower;
	}

	public void setLower(Tile lower) {
		if (lower == null && getUpper() != null)
			throw new IllegalStateException();
		this.lower = lower;
	}

	@Override
	public int getSize() {
		return lower == null ? 0 : upper == null ? 1 : 2;
	}

	public Tile draw() {
		Tile drawed;
		if (upper != null) {
			drawed = upper;
			upper = null;
		} else if (lower != null) {
			drawed = lower;
			lower = null;
		} else {
			throw new IllegalStateException("No remaining tiles.");
		}
		return drawed;
	}
}