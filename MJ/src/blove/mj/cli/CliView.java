package blove.mj.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 命令行显示。提供信息显示以及最下方的状态栏显示，以及接受用户单字符无回显输入。
 * 
 * @author blovemaple
 */
public class CliView {
	private final PrintStream out;
	private final InputStream in;

	private String status = "";
	private List<CharHandler> charHandlers = new LinkedList<>();

	/**
	 * 新建一个实例。
	 * 
	 * @param out
	 *            输出流
	 * @param in
	 *            输入流
	 */
	public CliView(PrintStream out, InputStream in) {
		this.out = out;
		this.in = in;
		readThread.start();
	}

	/**
	 * 初始化显示。
	 */
	public void init() {
		for (int i = 0; i < 50; i++) {
			out.println();
		}
		out.print(status);
	}

	/**
	 * 显示信息。
	 * 
	 * @param message
	 *            信息
	 */
	public void showMessage(String message) {
		out.print('\r');
		out.println(message);
		out.print(status);
	}

	/**
	 * 更新状态栏显示。
	 * 
	 * @param status
	 *            状态栏
	 */
	public void updateStatus(String status) {
		out.print('\r');
		out.print(this.status = status);
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
		if (wait)
			while (charHandlers.contains(handler))
				handler.wait();
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
			Reader reader = new InputStreamReader(in, Charset.defaultCharset());
			char c;
			int cInt;
			try {
				while ((cInt = reader.read()) >= 0) {
					c = (char) cInt;
					synchronized (charHandlers) {
						Iterator<CharHandler> handlerItr = charHandlers
								.iterator();
						while (handlerItr.hasNext()) {
							CharHandler handler = handlerItr.next();
							if (!handler.handle(c)) {
								handlerItr.remove();
								handler.notifyAll();
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
