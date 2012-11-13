package blove.mj.cli;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import blove.mj.board.GameBoard;
import blove.mj.board.GameBoardFullException;
import blove.mj.bot.FooBot;
import blove.mj.local.LocalGameBoard;
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
				gameBoard.newPlayer(new FooBot("Foo" + i));
			CliView cliView = new CliView(System.out, System.in);
			CliGame cliGame = new CliGame(args.length == 0 ? "Tom" : args[0],
					cliView);
			cliGame.join(gameBoard);
		} catch (GameBoardFullException e) {
			throw new RuntimeException(e);// 不可能
		} catch (InterruptedException e) {
		} catch (IOException e) {
			System.err.println("I/O Error:" + e.getMessage());
		}
	}

}
