package blove.mj;

import java.io.Serializable;
import java.util.Comparator;

/**
 * 得分项目。
 * 
 * @author blovemaple
 */
public interface PointItem extends Serializable {
	/**
	 * 返回项目名称。
	 * 
	 * @return 名称
	 */
	String getName();

	/**
	 * 返回得分。
	 * 
	 * @return 分数
	 */
	int getPoints();

	/**
	 * 按照得分多少比较项目大小的比较器。得分越高，大小越小。
	 * 
	 * @author blovemaple
	 */
	static Comparator<PointItem> pointsComparator = new Comparator<PointItem>() {

		@Override
		public int compare(PointItem o1, PointItem o2) {
			return o1.getPoints() > o2.getPoints() ? -1 : o1.getPoints() < o2
					.getPoints() ? 1 : 0;
		}
	};
}
