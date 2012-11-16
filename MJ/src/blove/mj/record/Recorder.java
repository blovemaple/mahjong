package blove.mj.record;

import blove.mj.GameResult;

/**
 * 记录管理器。
 * 
 * @author blovemaple
 */
public class Recorder {
	private static Recorder instance = new Recorder();

	/**
	 * 返回唯一实例。
	 * 
	 * @return 实例
	 */
	public static Recorder getRecorder() {
		return instance;
	}

	/**
	 * 返回指定玩家的分数。
	 * 
	 * @param playerName
	 *            玩家名称
	 * @return 分数。如果玩家不存在，则返回0。
	 */
	public int getPoints(String playerName) {
		// TODO
		return 1000;
	}

	/**
	 * 添加一个新记录。
	 * 
	 * @param result
	 *            新记录
	 */
	public void addResult(GameResult result) {
		// TODO
	}
}
