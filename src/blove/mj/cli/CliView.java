package blove.mj.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.Thread.State;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jline.console.ConsoleReader;

/**
 * 命令行界面。提供信息显示以及最下方的状态栏显示，以及接受用户单字符无回显输入。
 * 
 * @author blovemaple
 */
class CliView {
	private final ConsoleReader consoleReader;
	private final PrintStream out;

	private static final char SPECIAL_MSG_LINE_CHAR = '*';
	private static final int SPECIAL_MSG_LINE_CHAR_NUM = 50;

	private String status = "";
	private List<CharHandler> charHandlers = new CopyOnWriteArrayList<>();
	private CharHandler monoHandler;

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
	public CliView(PrintStream out, InputStream in) throws IOException,
			InterruptedException {
		consoleReader = new ConsoleReader(null, in, out, null);
		this.out = out;
		consoleReader.getTerminal().setEchoEnabled(false);
		startCharReading();
	}

	/**
	 * 初始化显示。
	 * 
	 * @throws IOException
	 */
	public synchronized void init() throws IOException {
		consoleReader.clearScreen();
		for (int i = 0; i < consoleReader.getTerminal().getHeight(); i++)
			out.println();
		out.print(status);
	}

	/**
	 * 显示信息。
	 * 
	 * @param message
	 *            信息
	 * @throws IOException
	 */
	public synchronized void printMessage(String message) throws IOException {
		clearStatus();
		out.println(message);
		out.print(status);
	}

	/**
	 * 显示特殊信息。特殊信息将被包含在醒目的头尾之间。
	 * 
	 * @param name
	 *            名称
	 * @param message
	 *            信息
	 * @throws IOException
	 */
	public synchronized void printSpecialMessage(String name, String message)
			throws IOException {
		StringBuilder headLine = new StringBuilder();
		if (name == null || name.length() == 0)
			for (int i = 0; i < SPECIAL_MSG_LINE_CHAR_NUM; i++)
				headLine.append(SPECIAL_MSG_LINE_CHAR);
		else if (name.length() >= SPECIAL_MSG_LINE_CHAR_NUM - 2)
			headLine.append(name);
		else {
			double halfLineLength = (SPECIAL_MSG_LINE_CHAR_NUM - name.length() - 2) / 2d;
			for (int i = 0; i < Math.floor(halfLineLength); i++)
				headLine.append(SPECIAL_MSG_LINE_CHAR);
			headLine.append(' ');
			headLine.append(name);
			headLine.append(' ');
			for (int i = 0; i < Math.ceil(halfLineLength); i++)
				headLine.append(SPECIAL_MSG_LINE_CHAR);
		}
		printMessage(headLine.toString());
		printMessage(message);
		StringBuilder tailLine = new StringBuilder();
		for (int i = 0; i < SPECIAL_MSG_LINE_CHAR_NUM; i++)
			tailLine.append(SPECIAL_MSG_LINE_CHAR);
		printMessage(tailLine.toString());
	}

	/**
	 * 更新状态栏显示。
	 * 
	 * @param status
	 *            状态栏
	 */
	public synchronized void updateStatus(String status) {
		clearStatus();
		this.status = status;
		out.print(status.substring(0, Math.min(status.length(), consoleReader
				.getTerminal().getWidth())));
	}

	private void clearStatus() {
		out.print('\r');
		for (int i = 0; i < status.length(); i++)
			out.print(" ");
		out.print('\r');
	}

	/**
	 * 返回状态栏字符串。
	 * 
	 * @return 状态栏
	 */
	public synchronized String getStatus() {
		return status;
	}

	/**
	 * 添加一个字符处理器。添加后，读取的所有字符都将交给此字符处理器处理。
	 * 
	 * @param handler
	 *            字符处理器
	 * @param wait
	 *            是否等待，直到监听方法返回false停止监听，此方法才返回
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
	public synchronized String readInputLine() throws IOException,
			InterruptedException {
		stopCharReading();

		consoleReader.getTerminal().setEchoEnabled(true);
		// XXX - 读取一行输入时，输入太多会超出状态栏显示
		String line = consoleReader.readLine();
		consoleReader.getTerminal().setEchoEnabled(false);

		startCharReading();

		return line;
	}

	/**
	 * 字符处理器。
	 * 
	 * @author blovemaple
	 */
	public static interface CharHandler {
		/**
		 * 处理结果。由处理方法返回。
		 * 
		 * @author blovemaple
		 */
		enum HandlingResult {
			/**
			 * 未处理。
			 */
			IGNORE,
			/**
			 * 已处理。
			 */
			ACCEPT,
			/**
			 * 已处理，并且将独占下一个字符的处理。返回此结果后，下一个读入的字符将仅交给此处理器处理。
			 */
			ACCEPT_FOR_MONOPOLIZATION,
			/**
			 * 已处理，并且不再处理以后的字符。返回此结果后，此处理器将会被移除。
			 */
			ACCEPT_FOR_QUITING,
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
				stopCharReading();
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
				int cInt;

				while ((cInt = consoleReader.readCharacter()) >= 0) {
					c = (char) cInt;
					if (monoHandler != null) {
						CharHandler handler = monoHandler;
						monoHandler = null;
						handle(handler, c);
					} else {
						for (CharHandler handler : charHandlers)
							handle(handler, c);
					}
				}
			} catch (IOException e) {
			}
		}

		private void handle(CharHandler handler, char c) {
			switch (handler.handle(c)) {
			case ACCEPT_FOR_MONOPOLIZATION:
				monoHandler = handler;
				break;
			case ACCEPT_FOR_QUITING:
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
