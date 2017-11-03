package com.github.blovemaple.mj.rule.win.load;

import static com.github.blovemaple.mj.utils.MyUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
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
public class IncludingFanTypeMatcher implements FanTypeMatcher {
	private static final Logger LOGGER = Logger.getLogger(IncludingFanTypeMatcher.class.getSimpleName());

	private final Map<FanTypeMatching, Long> objects;
	private final int objectSize;
	private final MatchingType matchingType;

	public IncludingFanTypeMatcher(List<FanTypeMatching> objects) {
		if (objects.isEmpty())
			throw new RuntimeException("Matching objects is empty for IncludingFanTypeMatcher.");

		this.objects = objects.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		objectSize = objects.size();

		MatchingType matchingType = null;
		for (FanTypeMatching object : objects) {
			if (matchingType == null)
				matchingType = object.matchingType();
			else if (object.matchingType() != matchingType)
				throw new IllegalArgumentException("Matching types must be same: " + objects);
		}
		this.matchingType = matchingType;
	}

	@Override
	public int matchCount(WinInfo winInfo) {
		int match;
		switch (matchingType) {
		case TILE:
			match = match(winInfo.getTileTypes());
			break;
		case UNIT:
			// 只能针对NormalWinType的units
			List<List<TileUnit>> unitsList = winInfo.getUnits().get(NormalWinType.get());
			if (unitsList == null || unitsList.isEmpty())
				return 0;
			// 对每一组units进行匹配，取最大的匹配数量
			match = unitsList.stream().mapToInt(units -> match(units)).max().getAsInt();
			break;
		default:
			throw new RuntimeException("Unimplemented matching type: " + matchingType);
		}

		return match;
	}

	private int match(List<?> objectsToCheck) {
		LOGGER.finer(() -> "Start Matching " + objectsToCheck + " to " + this);
		int match = match(objectsToCheck, new HashMap<>(objects), objectSize, new HashMap<>());
		LOGGER.finer(() -> (match > 0 ? match + " " : "Not ") + "Matched.");
		return match;
	}

	/**
	 * 将指定objects进行匹配，返回是否匹配。匹配到一个对象之后递归调用，匹配剩余对象，直到没有匹配对象时则成功。
	 * 
	 * @param crtObjectsToCheck
	 *            被匹配的对象
	 * @param crtMatchingObjects
	 *            进行中的匹配对象（对象->数目）
	 * @param crtMatchingObjectSize
	 *            进行中的匹配对象的总数（matchingObjects的数目之和，设置这个参数避免重复计算）
	 * @param vars
	 *            已经产生的变量值
	 * @return 匹配次数
	 */
	private <O> int match(List<O> crtObjectsToCheck, Map<FanTypeMatching, Long> crtMatchingObjects,
			int crtMatchingObjectSize, Map<Character, Object> vars) {
		LOGGER.finest(() -> "Now Matching " + crtObjectsToCheck + " to " + crtMatchingObjects + " vars " + vars);

		if (crtMatchingObjectSize == 0)
			// 没有匹配对象可用，容错
			return 0;

		if (crtObjectsToCheck.size() < crtMatchingObjectSize)
			// 被匹配对象数目不足，则一定不能匹配成功
			return 0;

		// 拿出第一个匹配对象尝试匹配
		Map.Entry<FanTypeMatching, Long> matchingObjectEntry = crtMatchingObjects.entrySet().iterator().next();
		FanTypeMatching matchingObject = matchingObjectEntry.getKey();
		Long matchingObjectCount = matchingObjectEntry.getValue();

		// 将每个被匹配对象和它尝试匹配，并递归完成后续匹配，取最大的匹配数量
		int matchCount = crtObjectsToCheck.stream().distinct().mapToInt(objectToCheck -> {
			LOGGER.finest(() -> "Matching " + objectToCheck + " to " + matchingObject + " vars " + vars);

			// 进行匹配
			List<Map<Character, Object>> newVarsList = matchingObject.match(objectToCheck, vars);
			if (newVarsList == null)
				// 没匹配上
				return 0;

			// 匹配上了
			LOGGER.finest(() -> "Matched " + objectToCheck + " to " + matchingObject + " newvars " + newVarsList);

			if (crtMatchingObjectSize > 1) {
				// 这不是最后一个匹配对象，继续进行本组匹配

				// 得出剩余被匹配对象和匹配对象（为了减少创建对象，复用matchingObjects）
				List<O> newObjectsToCheck = remainColl(ArrayList<O>::new, crtObjectsToCheck, objectToCheck);
				if (matchingObjectCount.longValue() == 1L)
					crtMatchingObjects.remove(matchingObject);
				else
					crtMatchingObjects.put(matchingObject, matchingObjectCount - 1);

				// 对匹配成功的每组变量值，递归匹配剩余对象，取最大的匹配数量
				int match0 = newVarsList.stream().mapToInt(newVars -> {
					// 把新变量合并到vars（为了减少创建对象，复用vars）
					vars.putAll(newVars);
					int matchRemain = match(newObjectsToCheck, crtMatchingObjects, crtMatchingObjectSize - 1, vars);
					// 恢复vars
					newVars.keySet().forEach(vars::remove);
					return matchRemain;
				}).max().getAsInt();

				// 恢复matchingObjects参数
				crtMatchingObjects.put(matchingObject, matchingObjectCount);
				return match0;
			} else {
				// 这是最后一个匹配对象，即本组匹配成功，看剩余被匹配对象还能不能再匹配
				if (crtObjectsToCheck.size() - 1 >= objectSize) {
					// 剩余被匹配对象还够，继续匹配下一组
					List<O> remainObjectsToCheck = remainColl(ArrayList<O>::new, crtObjectsToCheck, objectToCheck);
					return 1 + match(remainObjectsToCheck, new HashMap<>(objects), objectSize, new HashMap<>());
				} else
					// 剩余被匹配对象不够了，不继续下一组了
					return 1;

			}

		}).max().getAsInt();
		return matchCount;
	}

	@Override
	public String toString() {
		return "INCLUDING" + objects;
	}

}
