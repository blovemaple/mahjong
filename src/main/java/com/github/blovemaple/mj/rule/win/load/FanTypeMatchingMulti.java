package com.github.blovemaple.mj.rule.win.load;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.object.TileUnit;

/**
 * 番种匹配逻辑中使用的符合对象，用于匹配一种对象的多种情况，表示匹配这些情况的其中之一。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class FanTypeMatchingMulti implements FanTypeMatching {

	private final List<FanTypeMatching> objects;
	private final MatchingType matchingType;

	public FanTypeMatchingMulti(List<FanTypeMatching> objects) {
		if (objects.isEmpty())
			throw new IllegalArgumentException("objects cannot be empty.");
		this.objects = objects;
		List<MatchingType> matchingTypes = objects.stream().map(FanTypeMatching::matchingType).distinct()
				.collect(Collectors.toList());
		if (matchingTypes.size() > 1)
			throw new IllegalArgumentException("Matching types must be same: " + objects);
		matchingType = matchingTypes.get(0);
	}

	@Override
	public MatchingType matchingType() {
		return matchingType;
	}

	@Override
	public List<Map<Character, Object>> match(Object object, Map<Character, Object> vars) {
		switch (matchingType) {
		case TILE:
			if (!(object instanceof TileType))
				throw new IllegalArgumentException("Illegal object type: " + object.getClass());
			break;
		case UNIT:
			if (!(object instanceof TileUnit))
				throw new IllegalArgumentException("Illegal object type: " + object.getClass());
			break;
		default:
			throw new RuntimeException("Unsupported matching type: " + matchingType);
		}

		List<Map<Character, Object>> newVarsList = objects.stream()
				.map(matchingObject -> matchingObject.match(object, vars)).filter(Objects::nonNull).map(list -> {
					if (list.size() != 1)
						throw new RuntimeException("Matching tile or unit return invalid groups of new vars: " + list);
					return list.get(0);
				}).distinct().collect(Collectors.toList());

		if (newVarsList.isEmpty())
			return null;
		return newVarsList;
	}

	public List<FanTypeMatching> getObjects() {
		return objects;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((objects == null) ? 0 : objects.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FanTypeMatchingMulti))
			return false;
		FanTypeMatchingMulti other = (FanTypeMatchingMulti) obj;
		if (objects == null) {
			if (other.objects != null)
				return false;
		} else if (!objects.equals(other.objects))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.join("|", objects.stream().map(Object::toString).toArray(String[]::new));
	}

}
