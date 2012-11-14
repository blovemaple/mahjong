package blove.mj.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jline.console.ConsoleReader;

/**
 * 命令行界面。提供信息显示以及最下方的状态栏显示，以及接受用户单字符无回显输入。
 * 
 * @author blovemaple
 */
public class CliView {
	private final ConsoleReader consoleReader;
	private final PrintStream out;

	private String status = "";
	private List<CharHandler> charHandlers = new LinkedList<>();

	/**
	 * 新建一个实例。
	 * 
	 * @param out
	 *            输出流
	 * @param in
	 *            输入流
	 * @throws IOException
	 */
	public CliView(PrintStream out, InputStream in) throws IOException {
		consoleReader = new ConsoleReader(null, in, out, null);
		this.out = out;
		consoleReader.getTerminal().setEchoEnabled(false);
		readThread.start();
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
	 */
	public synchronized void showMessage(String message) {
		clearStatus();
		out.println(message);
		out.print(status);
	}

	/**
	 * 更新状态栏显示。
	 * 
	 * @param status
	 *            状态栏
	 */
	public synchronized void updateStatus(String status) {
		clearStatus();
		out.print(this.status = status);
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
		synchronized (charHandlers) {
			charHandlers.add(handler);
		}
		if (wait) {
			synchronized (handler) {
				try {
					while (charHandlers.contains(handler))
						handler.wait();
				} catch (InterruptedException e) {
					charHandlers.remove(handler);
					throw e;
				}
			}
		}
	}

	/**
	 * 字符处理器。
	 * 
	 * @author blovemaple
	 */
	public static interface CharHandler {
		/**
		 * 处理一个字符。
		 * 
		 * @param c
		 *            字符
		 * @return 是否继续处理下次的字符。若继续处理，返回true；否则返回false。
		 */
		boolean handle(char c);
	}

	private Thread readThread = new Thread() {
		{
			this.setDaemon(true);
		}

		@Override
		public void run() {
			try {
				char c;
				int cInt;

				while ((cInt = consoleReader.readCharacter()) >= 0) {
					c = (char) cInt;
					synchronized (charHandlers) {
						Iterator<CharHandler> handlerItr = charHandlers
								.iterator();
						while (handlerItr.hasNext()) {
							CharHandler handler = handlerItr.next();
							if (!handler.handle(c)) {
								handlerItr.remove();
								synchronized (handler) {
									handler.notifyAll();
								}
							}
						}
					}
				}
			} catch (IOException e) {
				showMessage(e.toString());
			}
		};
	};
}
