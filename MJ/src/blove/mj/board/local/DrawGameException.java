package blove.mj.board.local;

/**
 * 因为流局而无法进行某些操作时，抛出此异常。
 * 
 * @author 陈通
 */
public class DrawGameException extends Exception {
	private static final long serialVersionUID = 1L;

	public DrawGameException() {
		super("已流局");
	}
}
