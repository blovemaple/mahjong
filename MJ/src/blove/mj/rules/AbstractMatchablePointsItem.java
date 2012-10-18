package blove.mj.rules;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import blove.mj.PointsItem;

/**
 * 简化的可判断匹配得分项目接口。
 * 
 * @author blovemaple
 */
public abstract class AbstractMatchablePointsItem implements
		MatchablePointsItem {
	private final String name;
	private final int points;
	private final boolean isSpecialWinType;
	private final Set<PointsItem> coverItems;

	/**
	 * 新建一个实例。
	 * 
	 * @param name
	 *            名称
	 * @param points
	 *            得分
	 * @param isSpecialWinType
	 *            是否是特殊和牌类型
	 * @param coverItems
	 *            覆盖的得分项目
	 */
	public AbstractMatchablePointsItem(String name, int points,
			boolean isSpecialWinType, PointsItem... coverItems) {
		this.name = name;
		this.points = points;
		this.isSpecialWinType = isSpecialWinType;
		this.coverItems = new HashSet<>();
		this.coverItems.addAll(Arrays.asList(coverItems));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getPoints() {
		return points;
	}

	@Override
	public boolean isSpecialWinType() {
		return isSpecialWinType;
	}

	@Override
	public Set<PointsItem> coverItems() {
		return coverItems;
	}

}
