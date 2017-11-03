package com.github.blovemaple.mj.rule.win.load;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.blovemaple.mj.object.TileUnit;
import com.github.blovemaple.mj.rule.simple.NormalWinType;
import com.github.blovemaple.mj.rule.win.FanTypeMatcher;
import com.github.blovemaple.mj.rule.win.WinInfo;
import com.github.blovemaple.mj.rule.win.load.FanTypeMatching.MatchingType;

/**
 * TODO comment
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class AllMatchingFanTypeMatcher implements FanTypeMatcher {

	private final List<FanTypeMatching> objects;
	private final MatchingType matchingType;
	private final boolean negate; // true：全都不符合时为匹配

	public AllMatchingFanTypeMatcher(List<FanTypeMatching> objects, boolean negate) {
		if (objects.isEmpty())
			throw new RuntimeException("Matching objects is empty for IncludingFanTypeMatcher.");

		this.objects = objects;
		MatchingType matchingType = null;
		for (FanTypeMatching object : objects) {
			if (matchingType == null)
				matchingType = object.matchingType();
			else if (object.matchingType() != matchingType)
				throw new IllegalArgumentException("Matching types must be same: " + objects);
		}
		this.matchingType = matchingType;
		this.negate = negate;
	}

	@Override
	public int matchCount(WinInfo winInfo) {
		boolean match;
		switch (matchingType) {
		case TILE:
			match = match(winInfo.getTileTypes().stream().distinct().collect(Collectors.toList()));
			break;
		case UNIT:
			// 只能针对NormalWinType的units
			List<List<TileUnit>> unitsList = winInfo.getUnits().get(NormalWinType.get());
			if (unitsList == null)
				return 0;
			// 任何一组units符合条件就算符合
			match = unitsList.stream().anyMatch(units -> match(units));
			break;
		default:
			throw new RuntimeException("Unsupported matching type: " + matchingType);
		}
		return match ? 1 : 0;
	}

	private boolean match(List<?> objectsToCheck) {
		Deque<MatchingBranch> extraBranches = null; // lazy created
		MatchingBranch crtBranch = new MatchingBranch();
		BRANCH: while (true) {
			for (int index = crtBranch.firstTileIndex; index < objectsToCheck.size(); index++) {
				Object objectToCheck = objectsToCheck.get(index);

				FanTypeMatching matching = null;
				List<Map<Character, Object>> newVarsList = null;
				for (FanTypeMatching crtMatching : objects) {
					newVarsList = crtMatching.match(objectToCheck, crtBranch.vars);
					if (newVarsList != null) {
						matching = crtMatching;
						break;
					}
				}
				if (negate)
					newVarsList = newVarsList != null ? null : Collections.singletonList(Collections.emptyMap());
				if (newVarsList == null) {
					// 不匹配，尝试下一个branch。如果没有branch则失败。
					if (extraBranches == null || extraBranches.isEmpty())
						return false;
					else {
						crtBranch = extraBranches.remove();
						continue BRANCH;
					}
				}

				// 匹配了
				crtBranch.matchedObjects.add(matching);

				if (index == objectsToCheck.size() - 1) {
					// 最后一个tileType匹配了
					if (crtBranch.matchedObjects.size() == objects.size())
						// 所有objects都有，成功
						return true;
					else
						// 没有匹配到所有objects，失败
						return false;
				}

				// 非最后一个tileType匹配了

				if (newVarsList.size() > 1) {
					// 多个匹配，非首个匹配生成新的branch
					List<Map<Character, Object>> extraNewVars = newVarsList.subList(1, newVarsList.size());
					for (Map<Character, Object> newVars : extraNewVars) {
						MatchingBranch newBranch = new MatchingBranch();
						newBranch.firstTileIndex = index + 1;
						newBranch.vars = new HashMap<>(crtBranch.vars);
						newBranch.vars.putAll(newVars);
						if (extraBranches == null)
							extraBranches = new LinkedList<>();
						extraBranches.add(newBranch);
					}
				}

				// 首个匹配复用当前branch
				crtBranch.vars.putAll(newVarsList.get(0));
			}
		}
	}

	private class MatchingBranch {
		int firstTileIndex;
		Map<Character, Object> vars;
		Set<FanTypeMatching> matchedObjects;

		public MatchingBranch() {
			firstTileIndex = 0;
			vars = new HashMap<>();
			matchedObjects = new HashSet<>();
		}

	}

	@Override
	public String toString() {
		return "ALL_MATCHING[" + objects + "]" + (negate ? "(negate)" : "");
	}

}
