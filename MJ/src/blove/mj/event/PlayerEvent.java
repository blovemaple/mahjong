package blove.mj.event;

import blove.mj.Player;
import blove.mj.PlayerLocation;
import blove.mj.board.GameBoard;

/**
 * 玩家进入、离开游戏，或者准备好开始游戏的事件。
 * 
 * @author blovemaple
 */
public class PlayerEvent extends GameEvent {
	private static final long serialVersionUID = 1L;

	/**
	 * 玩家事件类型。
	 * 
	 * @author blovemaple
	 */
	public enum PlayerEventType {
		IN, OUT, READY
	}

	private final Player player;
	private final PlayerLocation location;
	private final PlayerEventType type;

	/**
	 * 新建一个实例。
	 * 
	 * @param source
	 *            事件源
	 * @param type
	 *            类型
	 * @param player
	 *            玩家
	 * @param location
	 *            位置
	 */
	public PlayerEvent(GameBoard source, PlayerEventType type,
			Player player, PlayerLocation location) {
		super(source);
		this.player = player;
		this.location = location;
		this.type = type;
	}

	/**
	 * 返回类型。
	 * 
	 * @return type
	 */
	public PlayerEventType getType() {
		return type;
	}

	/**
	 * 返回玩家。
	 * 
	 * @return 玩家
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * 返回位置。
	 * 
	 * @return 位置
	 */
	public PlayerLocation getLocation() {
		return location;
	}

}
