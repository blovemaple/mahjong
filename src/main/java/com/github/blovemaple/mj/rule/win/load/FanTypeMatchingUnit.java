package com.github.blovemaple.mj.rule.win.load;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.blovemaple.mj.object.TileUnit;
import com.github.blovemaple.mj.object.TileUnit.TileUnitSource;
import com.github.blovemaple.mj.object.TileUnitType;

/**
 * 番种匹配逻辑中使用的TileUnit对象，用于匹配一种TileUnit。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class FanTypeMatchingUnit implements FanTypeMatching {
	private TileUnitType unitType = null;
	private Boolean isHidden = null; // null不限，true暗，false明
	private FanTypeMatching tile = null;

	public FanTypeMatchingUnit(TileUnitType unitType, Boolean isHidden, FanTypeMatching tile) {
		this.unitType = unitType;
		this.isHidden = isHidden;
		this.tile = tile;
	}

	@Override
	public MatchingType matchingType() {
		return MatchingType.UNIT;
	}

	@Override
	public List<Map<Character, Object>> match(Object object, Map<Character, Object> vars) {
		if (!(object instanceof TileUnit))
			throw new IllegalArgumentException("Illegal object type: " + object.getClass());

		TileUnit tileUnit = (TileUnit) object;
		if (tileUnit.getType() != unitType)
			return null;

		if (isHidden != null) {
			boolean isUnitHidden = tileUnit.getSource() == TileUnitSource.SELF;
			if (isHidden.booleanValue() != isUnitHidden)
				return null;
		}

		if (tile == null)
			return Collections.singletonList(Collections.emptyMap());
		else
			return tile.match(tileUnit.getFirstTileType(), vars);
	}

	public TileUnitType getUnitType() {
		return unitType;
	}

	public Boolean getIsHidden() {
		return isHidden;
	}

	public FanTypeMatching getTile() {
		return tile;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((isHidden == null) ? 0 : isHidden.hashCode());
		result = prime * result + ((tile == null) ? 0 : tile.hashCode());
		result = prime * result + ((unitType == null) ? 0 : unitType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FanTypeMatchingUnit))
			return false;
		FanTypeMatchingUnit other = (FanTypeMatchingUnit) obj;
		if (isHidden == null) {
			if (other.isHidden != null)
				return false;
		} else if (!isHidden.equals(other.isHidden))
			return false;
		if (tile == null) {
			if (other.tile != null)
				return false;
		} else if (!tile.equals(other.tile))
			return false;
		if (unitType == null) {
			if (other.unitType != null)
				return false;
		} else if (!unitType.equals(other.unitType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return unitType + (isHidden == null ? "" : isHidden ? "_A" : "_M") + (tile == null ? "" : "_" + tile);
	}

}
