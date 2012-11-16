package blove.mj.cli;

import java.io.IOException;

import blove.mj.board.GameBoard;
import blove.mj.board.GameBoardFullException;
import blove.mj.bot.FooBot;
import blove.mj.local.LocalGameBoard;
import blove.mj.record.Recorder;
import blove.mj.rules.NoTimeLimitStrategy;

/**
 * 主方法所在的类。
 * 
 * @author blovemaple
 */
public class MJ {

	public static void main(String[] args) {
		try {
			CliView view = new CliView(System.out, System.in);
			view.init();

			MJ mj = new MJ(view);
			mj.printHead();
			mj.startGame();
		} catch (IOException e) {
			System.err.println("I/O Error:" + e.getMessage());
		} catch (InterruptedException e) {
		}

	}

	private static final String BOT1_NAME = "Joe", BOT2_NAME = "John",
			BOT3_NAME = "Jack";

	private final CliView cliView;
	private final String myName = System.getProperty("user.name", "You");

	public MJ(CliView cliView) {
		this.cliView = cliView;
	}

	public void printHead() throws IOException {
		StringBuilder head = new StringBuilder();
		head.append("Welcome, ").append(myName).append("!")
				.append(System.lineSeparator());
		head.append(System.lineSeparator());
		head.append("Current points:").append(System.lineSeparator());
		Recorder recorder = Recorder.getRecorder();
		for (String playerName : new String[] { myName, BOT1_NAME, BOT2_NAME,
				BOT3_NAME }) {
			head.append(String.format("%-13s",
					playerName + ":" + recorder.getPoints(playerName)));
		}

		cliView.printSpecialMessage("MAHJONG", head.toString());
	}

	public void startGame() throws InterruptedException, IOException {
		try {
			GameBoard gameBoard = new LocalGameBoard(new NoTimeLimitStrategy());

			gameBoard.newPlayer(new FooBot(BOT1_NAME));
			gameBoard.newPlayer(new FooBot(BOT2_NAME));
			gameBoard.newPlayer(new FooBot(BOT3_NAME));

			CliGame cliGame = new CliGame(myName, cliView);
			cliGame.join(gameBoard);
			System.exit(0);
		} catch (GameBoardFullException e) {
			throw new RuntimeException(e);// 不可能
		}
	}

}
