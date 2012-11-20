package blove.mj.record;

import java.io.IOException;

import blove.mj.GameResult;
import blove.mj.PlayerLocation;

/**
 * 记录管理器。
 * 
 * @author blovemaple
 */
public interface Recorder {

	/**
	 * 返回最近一局的庄家位置。
	 * 
	 * @return 庄家位置。如果没有记录，则返回null。
	 */
	public PlayerLocation getLastDealerLocation();

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
	 * @throws IOException
	 */
	public void addResult(GameResult result) throws IOException;

}