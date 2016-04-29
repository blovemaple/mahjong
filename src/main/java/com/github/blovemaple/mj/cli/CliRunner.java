package com.github.blovemaple.mj.cli;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.github.blovemaple.mj.cli.CliView.CharHandler;
import com.github.blovemaple.mj.local.LocalGame;

import static com.github.blovemaple.mj.cli.CliView.CharHandler.HandlingResult.*;
import static com.github.blovemaple.mj.utils.LanguageManager.ExtraMessage.*;
import static com.github.blovemaple.mj.utils.LambdaUtils.*;

/**
 * 命令行入口。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliRunner {
	private static final Logger logger = Logger.getLogger(CliRunner.class.getSimpleName());

	public static void main(String[] args) throws IOException, URISyntaxException {
		// 让日志输出到文件，不要在控制台显示
		LogManager.getLogManager().readConfiguration(CliRunner.class.getResource("/logging.properties").openStream());

		logger.info("Started");

		try {
			new CliRunner().run();
		} catch (InterruptedException e) {
		}

		System.out.println();
		System.exit(0);
	}

	private final CliView cliView;
	private final String myName;

	private LocalGame localGame;

	private static final char NEW_GAME_YES = 'y', NEW_GAME_NO = 'n';

	public CliRunner() throws IOException, InterruptedException {
		cliView = new CliView(System.out, System.in);
		myName = System.getProperty("user.name", "Player");
	}

	public void run() throws InterruptedException {
		try {
			cliView.init();
			printHead();
			logger.info("start setup.");
			setup();
			logger.info("end setup.");
			play();
		} catch (Exception e) {
			try {
				logger.log(Level.SEVERE, e.toString(), e);
				cliView.printMessage("[ERROR] " + e.toString());
			} catch (IOException e1) {
				logger.log(Level.SEVERE, e.toString(), e);
			}
		}
	}

	private void printHead() throws IOException {
		cliView.printSplitLine("MAHJONG", 50);

		StringBuilder head = new StringBuilder();
		head.append("Welcome, ").append(myName).append("!");
		cliView.printMessage(head.toString());

		cliView.printSplitLine(null, 50);
	}

	private void setup() throws InterruptedException {
		localGame = new LocalGame(new CliPlayer(myName, cliView), rethrowSupplier(() -> {
			cliView.updateStatus(NEW_GAME_QUESTION.str());
			cliView.addCharHandler(NEW_GAME_CHAR_HANDLER, true);
			return newGame;
		}));
	}

	private void play() throws IOException, InterruptedException {
		logger.info("start play.");
		localGame.play();
		logger.info("end play.");
	}

	@SuppressWarnings("unused")
	private class SettingCharHandler implements CharHandler {

		@Override
		public HandlingResult handle(char c) {
			return QUIT;
		}

	}

	private boolean newGame;

	private final CharHandler NEW_GAME_CHAR_HANDLER = c -> {
		switch (c) {
		case NEW_GAME_YES:
			newGame = true;
			return QUIT;
		case NEW_GAME_NO:
			newGame = false;
			return QUIT;
		default:
			return IGNORE;
		}
	};

}
