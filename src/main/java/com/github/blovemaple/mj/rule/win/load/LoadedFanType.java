package com.github.blovemaple.mj.rule.win.load;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.github.blovemaple.mj.rule.win.FanType;
import com.github.blovemaple.mj.rule.win.FanTypeMatcher;
import com.github.blovemaple.mj.rule.win.WinInfo;

/**
 * 动态载入的番种。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class LoadedFanType implements FanType {
	private static final Logger LOGGER = Logger.getLogger(LoadedFanType.class.getSimpleName());

	private final String name;
	private final FanTypeMatcher matcher;
	private final int score;
	private final Set<FanType> covered = new HashSet<>();
	private boolean unitsNoYaoJiuKe;

	public LoadedFanType(String name, FanTypeMatcher matcher, int score) {
		this.name = name;
		this.matcher = matcher;
		this.score = score;
	}

	@Override
	public int matchCount(WinInfo winInfo) {
		try {
			int count = matcher.matchCount(winInfo);
			LOGGER.fine(() -> (count > 0 ? "" : "Not ") + "Matched " + name + " count " + count);
			return count;
		} catch (Exception e) {
			throw new RuntimeException("Error match fan type " + name, e);
		}
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public int score() {
		return score;
	}

	@Override
	public Set<FanType> covered() {
		return covered;
	}

	void setCovered(Set<FanType> coveredTypes) {
		this.covered.addAll(coveredTypes);
	}

	public boolean isUnitsNoYaoJiuKe() {
		return unitsNoYaoJiuKe;
	}

	public void setUnitsNoYaoJiuKe(boolean unitsNoYaoJiuKe) {
		this.unitsNoYaoJiuKe = unitsNoYaoJiuKe;
	}

	@Override
	public String toString() {
		return "LoadedFanType [\nname=" + name + ",\nmatcher=" + matcher + ",\nscore=" + score + ",\ncovered="
				+ covered.stream().map(FanType::name).collect(Collectors.toList()) + "\n]\n";
	}

}
