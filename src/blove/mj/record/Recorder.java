package blove.mj.record;

import blove.mj.GameResult;

/**
 * 记录管理器。
 * 
 * @author blovemaple
 */
public interface Recorder {

	/**
	 * 返回指定玩家的分数。
	 * 
	 * @param playerName
	 *            玩家名称
	 * @return 分数。如果玩家不存在，则返回0。
	 */
	public int getPoints(String playerName);

	/**
	 * 添加一个新记录。
	 * 
	 * @param result
	 *            新记录
	 */
	public void addResult(GameResult result);

}