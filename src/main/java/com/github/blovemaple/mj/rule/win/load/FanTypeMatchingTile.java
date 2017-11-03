package com.github.blovemaple.mj.rule.win.load;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.blovemaple.mj.object.TileRank;
import com.github.blovemaple.mj.object.TileSuit;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.object.TileRank.NumberRank;

/**
 * 番种匹配逻辑中使用的Tile对象，用于匹配一种Tile。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class FanTypeMatchingTile implements FanTypeMatching {
	private TileSuit suit;
	private char suitVar;
	private TileRank<?> rank;
	private int rankVarOffset;
	private char rankVar;

	public static final char VAR_ANY = '?';

	public enum DynamicTileRank implements TileRank<DynamicTileRank> {
		QUANFENG, MENFENG
	}

	public FanTypeMatchingTile(TileSuit suit, char suitVar, TileRank<?> rank, int rankVarOffset, char rankVar) {
		this.suit = suit;
		this.suitVar = suitVar;
		this.rank = rank;
		this.rankVarOffset = rankVarOffset;
		this.rankVar = rankVar;
	}

	@Override
	public MatchingType matchingType() {
		return MatchingType.TILE;
	}

	@Override
	public List<Map<Character, Object>> match(Object object, Map<Character, Object> vars) {
		if (!(object instanceof TileType))
			throw new IllegalArgumentException("Illegal object type: " + object.getClass());

		Object newSuitValue = null, newRankValue = null;

		TileType tileType = (TileType) object;

		if (suit != null) {
			if (suit != tileType.suit())
				return null;
		} else {
			if (suitVar != VAR_ANY) {
				Object existValue = vars.get(suitVar);
				if (existValue != null) {
					if (tileType.suit() != existValue)
						return null;
				} else {
					if (varConflicts(vars, suitVar, tileType.suit()))
						return null;
					newSuitValue = tileType.suit();
				}
			}
		}

		if (rank != null) {
			if (rank != tileType.rank())
				return null;
		} else {
			if (rankVar != VAR_ANY) {
				Object existValue = vars.get(rankVar);
				TileRank<?> needRankVarValue = getRankVarValue(tileType.rank());
				if (needRankVarValue == null)
					return null;
				if (existValue != null) {
					if (needRankVarValue != existValue)
						return null;
				} else {
					if (varConflicts(vars, rankVar, needRankVarValue))
						return null;
					newRankValue = needRankVarValue;
				}
			}
		}

		if (newSuitValue == null && newRankValue == null)
			return Collections.singletonList(Collections.emptyMap());
		else {
			Map<Character, Object> newVars = new HashMap<>();
			if (newSuitValue != null)
				newVars.put(suitVar, newSuitValue);
			if (newRankValue != null)
				newVars.put(rankVar, newRankValue);
			return Collections.singletonList(newVars);
		}
	}

	private boolean varConflicts(Map<Character, Object> vars, char newVar, Object newValue) {
		if (Character.isLowerCase(newVar))
			return false;
		return vars.entrySet().stream() //
				.filter(entry -> Character.isUpperCase(entry.getKey()))
				.anyMatch(entry -> entry.getValue().equals(newValue));
	}

	private TileRank<?> getRankVarValue(TileRank<?> realRank) {
		if (rankVarOffset == 0) {
			return realRank;
		} else {
			if (!(realRank instanceof NumberRank))
				return null;
			int realVarNumber = ((NumberRank) realRank).number() - rankVarOffset;
			if (realVarNumber < 1 || realVarNumber > 9)
				return null;
			return NumberRank.ofNumber(realVarNumber);
		}
	}

	public TileSuit getSuit() {
		return suit;
	}

	public boolean isAnySuit() {
		return suit == null && suitVar == VAR_ANY;
	}

	public char getSuitVar() {
		return suitVar;
	}

	public TileRank<?> getRank() {
		return rank;
	}

	public boolean isAnyRank() {
		return rank == null && rankVar == VAR_ANY;
	}

	public int getRankVarOffset() {
		return rankVarOffset;
	}

	public char getRankVar() {
		return rankVar;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rank == null) ? 0 : rank.hashCode());
		result = prime * result + rankVar;
		result = prime * result + rankVarOffset;
		result = prime * result + ((suit == null) ? 0 : suit.hashCode());
		result = prime * result + suitVar;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FanTypeMatchingTile))
			return false;
		FanTypeMatchingTile other = (FanTypeMatchingTile) obj;
		if (rank == null) {
			if (other.rank != null)
				return false;
		} else if (!rank.equals(other.rank))
			return false;
		if (rankVar != other.rankVar)
			return false;
		if (rankVarOffset != other.rankVarOffset)
			return false;
		if (suit != other.suit)
			return false;
		if (suitVar != other.suitVar)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "" + (suit != null ? suit : suitVar) + (rank != null ? rank : "" + rankVarOffset + rankVar);
	}

}
