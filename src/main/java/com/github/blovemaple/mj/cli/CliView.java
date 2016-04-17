package com.github.blovemaple.mj.cli;

import static com.github.blovemaple.mj.utils.LanguageManager.ExtraMessage.*;
import static com.github.blovemaple.mj.utils.MyUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.Thread.State;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import jline.console.ConsoleReader;

/**
 * 基于jline2实现的命令行界面。提供信息显示以及最下方的状态栏显示，以及接受用户单字符无回显输入。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
class CliView {
	private static final Logger logger = Logger
			.getLogger(CliView.class.getSimpleName());

	private final ConsoleReader console;
	private final PrintStream out;

	// 分割线字符
	private static final char SPLIT_LINE_CHAR = '*';

	private String status = "";
	private List<CharHandler> charHandlers = new CopyOnWriteArrayList<>();
	private CharHandler monoHandler; // 目前独占字符处理的处理器

	/**
	 * 新建一个实例。
	 * 
	 * @param out
	 *            输出流
	 * @param in
	 *            输入流
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public CliView(PrintStream out, InputStream in)
			throws IOException, InterruptedException {
		console = new ConsoleReader(null, in, out, null);
		this.out = out;
		console.getTerminal().setEchoEnabled(false);
		console.setHandleUserInterrupt(true);
		logger.info("terminal width: " + console.getTerminal().getWidth());
		startCharReading();
	}

	/**
	 * 初始化显示。
	 * 
	 * @throws IOException
	 */
	public synchronized void init() throws IOException {
		console.clearScreen();
		for (int i = 0; i < console.getTerminal().getHeight(); i++)
			out.println();
		updateStatus("");
	}

	/**
	 * 显示信息。
	 * 
	 * @param message
	 *            信息
	 * @throws IOException
	 */
	public synchronized void printMessage(String message) throws IOException {
		String status = this.status;
		updateStatus(message); // TODO message过长会导致显示窗口太窄
		out.println();
		out.print(status);
		this.status = status;
	}

	/**
	 * 显示分割线。
	 * 
	 * @param message
	 *            分割线中间显示的信息，null表示没有信息
	 * @param width
	 *            宽度
	 * @throws IOException
	 */
	public synchronized void printSplitLine(String message, int width)
			throws IOException {
		int messageLength = 0;
		if (message != null)
			messageLength = strWidth(message);

		StringBuilder line = new StringBuilder();
		if (messageLength == 0)
			for (int i = 0; i < width; i++)
				line.append(SPLIT_LINE_CHAR);
		else if (messageLength >= width - 2)
			line.append(message);
		else {
			double halfLineLength = (width - messageLength - 2) / 2d;
			for (int i = 0; i < Math.floor(halfLineLength); i++)
				line.append(SPLIT_LINE_CHAR);
			line.append(' ');
			line.append(message);
			line.append(' ');
			for (int i = 0; i < Math.ceil(halfLineLength); i++)
				line.append(SPLIT_LINE_CHAR);
		}
		printMessage(line.toString());
	}

	/**
	 * 更新状态栏信息。
	 * 
	 * @param status
	 *            状态栏信息
	 */
	public synchronized void updateStatus(String status) {
		int strWidth = strWidth(status);
		int terminalWidth = console.getTerminal().getWidth();
		if (strWidth > terminalWidth)
			status = WINDOW_TOO_NARROW.str();// TODO 窗口太窄

		int narrowed = strWidth(this.status) - strWidth(status);
		out.print('\r');
		this.status = status;
		out.print(status);
		if (narrowed > 0) {
			for (int i = 0; i < narrowed; i++)
				out.print(' ');
			for (int i = 0; i < narrowed; i++)
				out.print('\b');
		}
	}

	/**
	 * 添加一个字符处理器。添加后，读取的所有字符都将交给此字符处理器处理。
	 * 
	 * @param handler
	 *            字符处理器
	 * @param wait
	 *            是否等待，直到此监听器停止监听（被移除），此方法才返回
	 * @throws InterruptedException
	 */
	public void addCharHandler(CharHandler handler, boolean wait)
			throws InterruptedException {
		charHandlers.add(handler);
		if (wait) {
			synchronized (handler) {
				try {
					while (charHandlers.contains(handler))
						handler.wait();
				} finally {
					charHandlers.remove(handler);
				}
			}
		}
	}

	/**
	 * 读取一行输入。读取时字符处理将暂停。
	 * 
	 * @return 输入字符串
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public synchronized String readInputLine()
			throws IOException, InterruptedException {
		stopCharReading();

		console.getTerminal().setEchoEnabled(true);
		// XXX - 读取一行输入时，输入太多会超出状态栏显示
		String line = console.readLine();
		console.getTerminal().setEchoEnabled(false);

		startCharReading();

		return line;
	}

	/**
	 * 字符处理器。
	 * 
	 * @author blovemaple <blovemaple2010(at)gmail.com>
	 */
	@FunctionalInterface
	public static interface CharHandler {
		/**
		 * 处理结果。由处理方法返回。
		 */
		enum HandlingResult {
			/**
			 * 未处理。
			 */
			IGNORE,
			/**
			 * 已处理。如果此监听器处于独占状态，将解除独占状态。
			 */
			ACCEPT,
			/**
			 * 已处理，并且将独占下一个字符的处理。返回此结果后，下一个读入的字符将仅交给此处理器处理。
			 */
			MONOPOLIZE,
			/**
			 * 已处理，并且不再处理以后的字符。返回此结果后，此处理器将会被移除。
			 */
			QUIT,
		}

		/**
		 * 处理一个字符。
		 * 
		 * @param c
		 *            字符
		 * @return 处理结果
		 */
		HandlingResult handle(char c);
	}

	private static ReadThread thread;

	private void startCharReading() throws InterruptedException {
		synchronized (ReadThread.class) {
			if (thread == null || thread.getState() == State.TERMINATED) {
				thread = new ReadThread();
				thread.start();
			}
		}
	}

	private void stopCharReading() throws InterruptedException {
		synchronized (ReadThread.class) {
			if (thread != null && thread.getState() != State.TERMINATED) {
				thread.interrupt();
				thread.join();
			}
		}
	}

	private class ReadThread extends Thread {

		private ReadThread() {
			this.setDaemon(true);
		}

		@Override
		public void run() {
			try {
				char c;

				while ((c = (char) console.readCharacter()) >= 0) {
					if (monoHandler != null) {
						CharHandler handler = monoHandler;
						monoHandler = null;
						handle(handler, c);
					} else {
						for (CharHandler handler : charHandlers)
							handle(handler, c);
					}
				}
			} catch (Exception e) {
				try {
					logger.log(Level.SEVERE, e.toString(), e);
					printMessage("[ERROR]" + e.toString());
				} catch (IOException e1) {
					logger.log(Level.SEVERE, e.toString(), e);
				}
			}
		}

		private void handle(CharHandler handler, char c) {
			switch (handler.handle(c)) {
			case MONOPOLIZE:
				monoHandler = handler;
				break;
			case QUIT:
				charHandlers.remove(handler);
				break;
			default:
				break;
			}
			synchronized (handler) {
				handler.notifyAll();
			}
		}
	}
}
