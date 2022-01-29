package com.github.blovemaple.mj.rule.load;

import com.github.blovemaple.mj.object.TileUnitType;

public class TileUnitExpression implements TileItemExpression {
	private TileUnitType unitType;
	private TileUnitSource unitSource;
	private TileTypeExpression tileType;

	private enum TileUnitSource {
		SELF, GOT, BUGANG, WIN
	}

	@Override
	public Boolean getValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
