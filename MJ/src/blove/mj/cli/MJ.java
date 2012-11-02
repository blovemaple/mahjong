package blove.mj.cli;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import blove.mj.board.GameBoard;
import blove.mj.board.GameBoardFullException;
import blove.mj.board.local.LocalGameBoard;
import blove.mj.bot.foo.FooBot;
import blove.mj.rules.DefTimeLimitStrategy;

/**
 * 主方法所在的类。
 * 
 * @author blovemaple
 */
public class MJ {

	public static void main(String[] args) {
		try {
			GameBoard gameBoard = new LocalGameBoard(new DefTimeLimitStrategy(
					99, 99, TimeUnit.SECONDS));
			for (int i = 1; i <= 3; i++)
				new FooBot("Foo" + i).join(gameBoard);
			CliView cliView = new CliView(System.out, System.in);
			CliGame cliGame = new CliGame(cliView);
			cliGame.play(gameBoard, args.length == 0 ? "Tom" : args[0]);
		} catch (GameBoardFullException e) {
			throw new RuntimeException(e);// 不可能
		} catch (InterruptedException e) {
		} catch (IOException e) {
			System.err.println("I/O Error:" + e.getMessage());
		}
	}

}
