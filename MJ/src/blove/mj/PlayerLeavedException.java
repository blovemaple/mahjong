package blove.mj;

import blove.mj.board.GameBoard;

/**
 * 因玩家已离开游戏桌，而不能进行某些操作时，抛出此异常。
 * 
 * @author 陈通
 */
public class PlayerLeavedException extends IllegalStateException {
	private static final long serialVersionUID = 1L;

	private final Player player;
	private final GameBoard board;

	/**
	 * 新建一个实例。
	 * 
	 * @param player
	 *            玩家
	 * @param board
	 *            游戏桌
	 */
	public PlayerLeavedException(Player player, GameBoard board) {
		this("玩家" + player + "已离开游戏桌" + board, player, board);
	}

	/**
	 * 新建一个实例。
	 * 
	 * @param message
	 *            消息
	 * @param player
	 *            玩家
	 * @param board
	 *            游戏桌
	 */
	public PlayerLeavedException(String message, Player player, GameBoard board) {
		super(message);
		this.player = player;
		this.board = board;
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
	 * 返回游戏桌。
	 * 
	 * @return 游戏桌
	 */
	public GameBoard getGameBoard() {
		return board;
	}

}
