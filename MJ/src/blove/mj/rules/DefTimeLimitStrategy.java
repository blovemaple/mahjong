package blove.mj.rules;

import java.util.concurrent.TimeUnit;

/**
 * 默认的限时策略，采用固定限时。
 * 
 * @author blovemaple
 */
public class DefTimeLimitStrategy implements TimeLimitStrategy {
	private final long discardLimit, cpkLimit;

	/**
	 * 新建一个实例。
	 * 
	 * @param discardLimit
	 *            打牌限时
	 * @param cpkLimit
	 *            吃/碰/杠限时
	 * @param timeUnit
	 *            时间单位
	 */
	public DefTimeLimitStrategy(int discardLimit, int cpkLimit,
			TimeUnit timeUnit) {
		this.discardLimit = TimeUnit.SECONDS.convert(discardLimit, timeUnit);
		this.cpkLimit = TimeUnit.SECONDS.convert(cpkLimit, timeUnit);
	}

	@Override
	public long discardLimit() {
		return discardLimit;
	}

	@Override
	public long cpkLimit() {
		return cpkLimit;
	}

}
