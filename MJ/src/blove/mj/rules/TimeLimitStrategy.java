package blove.mj.rules;

/**
 * 游戏限时策略。
 * 
 * @author blovemaple
 */
public interface TimeLimitStrategy {
	/**
	 * 返回打牌限时。单位：秒。
	 * 
	 * @return 限时，若不限制则返回0。
	 */
	long discardLimit();

	/**
	 * 返回吃/碰/杠限时。单位：秒。
	 * 
	 * @return 限时，若不限制则返回0。
	 */
	long cpkLimit();
}
