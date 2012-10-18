package blove.mj.event;

import blove.mj.Cpk;
import blove.mj.GameBoardView;
import blove.mj.PlayerLocation;
import blove.mj.Tile;
import blove.mj.board.GameBoard;

/**
 * 发牌结束，以及任何玩家摸牌、打牌、吃/碰/杠牌的事件。
 * 
 * @author blovemaple
 */
public class PlayerActionEvent extends GameEvent {
	private static final long serialVersionUID = 1L;

	/**
	 * 事件类型。
	 * 
	 * @author blovemaple
	 */
	public enum ActionType {
		DEAL_OVER, DRAW, DISCARD, CPK
	}

	private final ActionType type;
	private final PlayerLocation playerLocation;
	private final Tile tile;
	private final Cpk cpk;
	private final boolean newReadyHand;
	private final boolean forTimeOut;

	public static PlayerActionEvent newForDealOver(GameBoard board) {
		return new PlayerActionEvent(board, ActionType.DEAL_OVER, null, null,
				null, false, false);
	}

	public static PlayerActionEvent newForDraw(GameBoard board,
			PlayerLocation playerLocation, Tile tile) {
		return new PlayerActionEvent(board, ActionType.DRAW, playerLocation,
				tile, null, false, false);
	}

	public static PlayerActionEvent newForDiscard(GameBoard board,
			PlayerLocation playerLocation, Tile tile, boolean newReadyHand,
			boolean forTimeOut) {
		return new PlayerActionEvent(board, ActionType.DISCARD, playerLocation,
				tile, null, newReadyHand, forTimeOut);
	}

	public static PlayerActionEvent newForCpk(GameBoard board,
			PlayerLocation playerLocation, Cpk cpk) {
		return new PlayerActionEvent(board, ActionType.CPK, playerLocation,
				null, cpk, false, false);
	}

	private PlayerActionEvent(GameBoard source, ActionType type,
			PlayerLocation playerLocation, Tile tile, Cpk cpk,
			boolean newReadyHand, boolean forTimeOut) {
		super(source);
		this.type = type;
		this.playerLocation = playerLocation;
		this.tile = tile;
		this.cpk = cpk;
		this.newReadyHand = newReadyHand;
		this.forTimeOut = forTimeOut;
	}

	/**
	 * 返回类型。
	 * 
	 * @return 类型
	 */
	public ActionType getType() {
		return type;
	}

	/**
	 * 返回玩家。
	 * 
	 * @return 玩家
	 */
	public PlayerLocation getPlayerLocation() {
		return playerLocation;
	}

	/**
	 * 返回牌。
	 * 
	 * @param boardView
	 *            游戏桌视图。用来识别玩家位置。
	 * @return 牌
	 */
	public Tile getTile(GameBoardView boardView) {
		if (type == ActionType.DRAW
				&& !playerLocation.equals(boardView.getMyLocation()))
			// 摸牌动作，如果不是自己摸的牌，则返回null
			return null;
		return tile;
	}

	/**
	 * 返回当前的吃/碰/杠。
	 * 
	 * @return 吃/碰/杠。若返回null，则表示当前动作为放弃吃/碰/杠。
	 */
	public Cpk getCpk() {
		return cpk;
	}

	/**
	 * 返回是否是刚刚听牌。
	 * 
	 * @return 是否刚刚听牌
	 */
	public boolean isNewReadyHand() {
		return newReadyHand;
	}

	/**
	 * 返回是否是因为超时而引发此动作。
	 * 
	 * @return 如果因为超时，则返回true；否则返回false。
	 */
	public boolean isForTimeOut() {
		return forTimeOut;
	}

}
