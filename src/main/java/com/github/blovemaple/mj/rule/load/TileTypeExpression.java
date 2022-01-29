package com.github.blovemaple.mj.rule.load;

import com.github.blovemaple.mj.object.TileRank;
import com.github.blovemaple.mj.object.TileSuit;

public class TileTypeExpression implements TileItemExpression {
	private TileSuit suit;
	private TileRank<?> rank;

	private char suitVar;
	private char rankVar;
	private int rankVarOffset;

	@Override
	public Boolean getValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
