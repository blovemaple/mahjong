package blove.mj.rules;

import blove.mj.PointItem;

/**
 * 简单的得分项目。只须提供固定的名称和得分即可实例化。
 * 
 * @author blovemaple
 */
public class SimplePointItem implements PointItem {
	private static final long serialVersionUID = -8607439914336704901L;

	private final String name;
	private final int points;

	/**
	 * 创建一个实例。
	 * 
	 * @param name
	 *            名称
	 * @param points
	 *            得分
	 */
	public SimplePointItem(String name, int points) {
		this.name = name;
		this.points = points;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getPoints() {
		return points;
	}

}
