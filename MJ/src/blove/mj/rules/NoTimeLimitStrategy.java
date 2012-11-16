package blove.mj.rules;

/**
 * 没有时间限制的时间策略。
 * 
 * @author blovemaple
 */
public class NoTimeLimitStrategy implements TimeLimitStrategy {

	@Override
	public long discardLimit() {
		return -1;
	}

	@Override
	public long cpkLimit() {
		return -1;
	}

}
