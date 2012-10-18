package blove.mj.board.local;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.event.EventListenerList;

/**
 * 以独立线程响应事件的监听器列表。若将监听器加入此列表，则在随后取出并调用任一方法时，方法将被放入一个独立线程执行，并立即返回null，
 * 且方法抛出的任何异常会被忽略（但会送至系统标准错误输出）。<br/>
 * <br/>
 * 注意： <li>
 * 此列表中所有监听器的所有方法总是立即返回null，因此监听器方法原本的返回类型不能为基本类型。否则，调用虽然能够执行监听器相应方法的操作，但会随后抛出
 * {@link NullPointerException}。客户程序也可以通过捕获并忽略此异常来解决这个问题。 <li>
 * 如果从此列表中取出的监听器在从此列表中移除后被调用，则所有方法均抛出{@link IllegalStateException}
 * 。调用原监听器（加入此列表前的监听器）则会正常执行操作。
 * 
 * @author 陈通
 */
public class AsyncEventListenerList extends EventListenerList {
	private static final long serialVersionUID = 1L;

	private static final int MAX_THREAD_NUM = 3;

	private final Map<Object, EventListener> proxyToListener = new HashMap<>();

	/**
	 * 线程池。
	 */
	private final ExecutorService executor = new ThreadPoolExecutor(1,
			MAX_THREAD_NUM, 30, TimeUnit.SECONDS,
			new SynchronousQueue<Runnable>());

	private final InvocationHandler methodHandler = new ListenerMethodHandler();

	@Override
	public synchronized <T extends EventListener> void add(Class<T> t, T l) {
		checkShutdown();

		Proxy proxy = (Proxy) Proxy.newProxyInstance(t.getClassLoader(),
				new Class<?>[] { t }, methodHandler);
		proxyToListener.put(proxy, l);
		super.add(t, t.cast(proxy));
	}

	@Override
	public synchronized <T extends EventListener> void remove(Class<T> t, T l) {
		proxyToListener.values().remove(l);
		super.remove(t, l);
	}

	/**
	 * 关闭线程池。此方法调用后，此列表将不允许加入新的监听器，而且也不允许调用从此列表取出的监听器的方法。
	 * 已经调用但未执行结束的监听器方法将继续执行到结束。
	 */
	public void close() {
		executor.shutdown();
	}

	private void checkShutdown() {
		if (executor.isShutdown())
			throw new IllegalStateException("线程池已关闭");
	}

	/**
	 * 调用监听器方法的处理器。此处理器将所有方法调用放入线程池执行。
	 * 
	 * @author 陈通
	 */
	private class ListenerMethodHandler implements InvocationHandler {
		@Override
		public Object invoke(final Object proxy, final Method method,
				final Object[] args) throws Throwable {
			checkShutdown();
			if (!proxyToListener.containsKey(proxy))
				throw new IllegalStateException("监听器不在此列表中");

			executor.execute(new Runnable() {

				@Override
				public void run() {
					try {
						method.invoke(proxyToListener.get(proxy), args);
					} catch (IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
						e.printStackTrace();
					}
				}

			});
			return null;
		}
	}
}
