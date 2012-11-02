package blove.mj.board.local;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.event.EventListenerList;

/**
 * 为每个监听器分配单一线程的监听器列表。若将监听器加入此列表，则在随后取出并调用监听器接口的任一方法时，方法将被放入一个属于此监听器的单一线程执行，
 * 并立即返回null， 且方法抛出的任何异常会被忽略（但会送至系统标准错误输出）。<br/>
 * <br/>
 * 注意： <li>
 * 此列表中所有监听器的所有方法总是立即返回null，因此监听器方法原本的返回类型不能为基本类型。否则，调用虽然能够执行监听器相应方法的操作，但会随后抛出
 * {@link NullPointerException}。客户程序也可以通过捕获并忽略此异常来解决这个问题。 <li>
 * 如果从此列表中取出的监听器在从此列表中移除后被调用，则所有方法均抛出{@link IllegalStateException}
 * 。调用原监听器（加入此列表前的监听器）则会正常执行操作。
 * 
 * @author blovemaple
 */
public class AsyncEventListenerList extends EventListenerList {
	private static final long serialVersionUID = 1L;

	private final List<Object> proxies = new LinkedList<>();
	private final List<EventListener> listeners = new LinkedList<>();
	private final List<ExecutorService> executors = new LinkedList<>();
	private final List<Set<Method>> methodsIgnore = new LinkedList<>();

	private final InvocationHandler methodHandler = new ListenerMethodHandler();

	private boolean isShutdown = false;

	@Override
	public synchronized <T extends EventListener> void add(Class<T> t, T l) {
		add(t, l, new Method[] {});
	}

	/**
	 * 添加监听器。
	 * 
	 * @param t
	 *            监听器接口
	 * @param l
	 *            监听器
	 * @param methodsIgnore
	 *            不在单一线程中执行的方法
	 */
	public synchronized <T extends EventListener> void add(Class<T> t, T l,
			Method... methodsIgnore) {
		checkShutdown();

		Proxy proxy = (Proxy) Proxy.newProxyInstance(t.getClassLoader(),
				new Class<?>[] { t }, methodHandler);
		proxies.add(proxy);
		listeners.add(l);
		executors.add(Executors.newSingleThreadExecutor());
		this.methodsIgnore.add(new HashSet<>(Arrays.asList(methodsIgnore)));
		super.add(t, t.cast(proxy));
	}

	@Override
	public synchronized <T extends EventListener> void remove(Class<T> t, T l) {
		int index = listeners.indexOf(l);
		proxies.remove(index);
		listeners.remove(index);
		executors.remove(index);
		methodsIgnore.remove(index);
		super.remove(t, l);
	}

	/**
	 * 关闭线程池。此方法调用后，此列表将不允许加入新的监听器，而且也不允许调用从此列表取出的监听器的方法。
	 * 已经调用但未执行结束的监听器方法将继续执行到结束。
	 */
	public synchronized void close() {
		for (ExecutorService executor : executors)
			executor.shutdown();
		isShutdown = true;
	}

	private void checkShutdown() {
		if (isShutdown)
			throw new IllegalStateException("线程池已关闭");
	}

	/**
	 * 调用监听器方法的处理器。此处理器将所有方法调用放入线程池执行。
	 * 
	 * @author blovemaple
	 */
	private class ListenerMethodHandler implements InvocationHandler {
		@Override
		public Object invoke(final Object proxy, final Method method,
				final Object[] args) throws Throwable {
			// 要保证此方法内不会调用proxy的方法，否则将会出现无限递归！
			synchronized (AsyncEventListenerList.this) {// 要保证index不会变，所以同步
				checkShutdown();

				final int index = getIndex(proxies, proxy);
				final EventListener listener = listeners.get(index);

				if (index < 0)
					throw new IllegalStateException("监听器不在此列表中");

				boolean isListenerMethod = false;
				for (Class<?> interfaceClass : listener.getClass()
						.getInterfaces()) {
					if (Arrays.asList(interfaceClass.getInterfaces()).contains(
							EventListener.class)) {
						if (Arrays.asList(interfaceClass.getMethods())
								.contains(method)) {
							isListenerMethod = true;
							break;
						}
					}
				}

				final Set<Method> methodsIgnore = AsyncEventListenerList.this.methodsIgnore
						.get(index);

				if (isListenerMethod && !methodsIgnore.contains(method)) {
					ExecutorService executor = executors.get(index);
					executor.execute(new Runnable() {

						@Override
						public void run() {
							try {
								method.invoke(listener, args);
							} catch (IllegalAccessException
									| IllegalArgumentException
									| InvocationTargetException e) {
								e.printStackTrace();
							}
						}

					});
					return null;
				} else {
					return method.invoke(listener, args);
				}
			}
		}
	}

	/**
	 * 用等号的方法查找元素在数组中的位置，避免调用equals方法。
	 * 
	 * @param list
	 *            数组
	 * @param object
	 *            查找的元素
	 * @return 找到返回索引，找不到返回-1。
	 */
	private <T> int getIndex(List<T> list, T object) {
		int index = 0;
		Iterator<T> itr = list.iterator();
		while (itr.hasNext()) {
			if (itr.next() == object)
				return index;
			else
				index++;
		}
		return -1;
	}
}
