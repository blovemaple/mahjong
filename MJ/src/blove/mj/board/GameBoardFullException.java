package blove.mj.board;

/**
 * 因游戏桌已满而无法进行某些操作时，抛出此异常。
 * 
 * @author blovemaple
 */
public class GameBoardFullException extends Exception {
	private static final long serialVersionUID = 1L;
	private final GameBoard board;

	/**
	 * 新建一个实例。
	 * 
	 * @param board
	 *            游戏桌
	 */
	public GameBoardFullException(GameBoard board) {
		this.board = board;
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
