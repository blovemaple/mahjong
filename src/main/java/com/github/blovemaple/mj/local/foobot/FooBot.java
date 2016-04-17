package com.github.blovemaple.mj.local.foobot;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.game.GameContext.PlayerView;
import com.github.blovemaple.mj.game.MahjongGame;
import com.github.blovemaple.mj.object.Player;
import com.github.blovemaple.mj.object.PlayerLocation;

/**
 * 机器人。注意：chooseAction方法上加了锁，不能同时进行两个选择过程。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class FooBot implements Player {
	private static final Logger logger = Logger.getLogger(FooBot.class.getSimpleName());

	private String name;

	public FooBot(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	// 下面两个集合分别存放 目前等待被确定结果（和牌或流局）或拆分的 和 已经确定结果的 context。

	// 进行模拟时，将被拆分的context从doingContexts移除，并将新拆分出的context加到doneContexts；
	// 计算结果时，从doneContexts中所有context按层数从大到小合并结果，即可逐层合并到到最顶层的context。

	// 主选择线程需要以doingContexts的清空作为模拟结束的标志，所以主线程在lock上加锁，在simStopCondition上等待，
	// 每个模拟线程处理完后，或者模拟过程被中断后，应该在simStopCondition上notify，确保主线程及时知道doingContexts的清空。

	// 当模拟过程被中断时，应该尽快清空doingContexts。这样，如果某个模拟任务没有及时中止，也可以发现处理的context不在doingContexts中而及时中止。
	private final Set<FooSimContext> doingContexts = Collections.synchronizedSet(new HashSet<>());
	private final List<FooSimContext> doneContexts = Collections.synchronizedList(new LinkedList<>());
	private final Lock lock = new ReentrantLock();
	private final Condition simStopCondition = lock.newCondition();

	/**
	 * 模拟任务运行在这个executor上。<br>
	 * 引用是Executor而不是ExecutorService，是为了限制只能用execute方法提交任务，原因是：<br>
	 * 需要用level作为优先级进行队列排序，而队列只支持用execute提交的Runnable。
	 */
	private Executor simExecutor;

	@Override
	public synchronized Action chooseAction(PlayerView contextView, Set<ActionType> actionTypes)
			throws InterruptedException {
		try {
			// 创建顶层的context
			MahjongGame gameTool = new MahjongGame(new FooSimGameStrategy(contextView.getGameStrategy()),
					contextView.getTimeLimitStrategy());
			FooSimContext topContext = new FooSimContext(contextView, gameTool, this::submitSplitContextForSim,
					this::submitDoneContext);

			// 新建simExecutor并提交顶层context，开始模拟
			int processorCount = Runtime.getRuntime().availableProcessors();
			ExecutorService simExecutor = new ThreadPoolExecutor(processorCount, processorCount, 0L,
					TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>(11,
							Comparator.comparing(task -> ((FooSimContext) task).getLevel())));
			this.simExecutor = simExecutor;
			submitContextForSim(topContext);

			// TODO 如果只有一个选择，就不用模拟了

			// 等待doingContexts清空（模拟完毕或中止）
			try {
				// 在simStopCondition上等待，直到doingContexts清空
				lock.lockInterruptibly();
				try {
					while (!doingContexts.isEmpty())
						simStopCondition.await();
				} finally {
					lock.unlock();
				}
			} finally {
				// 一定要关闭simExecutor，并且中断还没执行完的模拟任务
				simExecutor.shutdownNow();
			}

			// 计算结果得出选择
			return computeFinalAction();
		} finally {
			// 一定要clear，避免浪费资源
			clear();
		}
	}

	/**
	 * 提交模拟任务。任务目的是将指定的context进行一步动作，确定结果或者拆分为若干context。<br>
	 * 具体做法是将指定的context用gameTool选择一次动作，此时gameTool就会调用模拟玩家，由模拟玩家确定结果或拆分context。
	 */
	private void submitContextForSim(FooSimContext context) {
		doingContexts.add(context);
		simExecutor.execute(context);
	}

	private void submitSplitContextForSim(FooSimContext oriContext, Collection<FooSimContext> newContexts) {
		// TODO

		doingContexts.addAll(newContexts);
		newContexts.forEach(simExecutor::execute);
	}

	private void submitDoneContext(FooSimContext oriContext) {
		// TODO

	}

	private Action computeFinalAction() {
		// TODO
		return null;
	}

	private void clear() {
		doingContexts.clear();
		doneContexts.clear();
		simExecutor = null;
		// TODO
	}

	@Override
	public Action chooseAction(PlayerView contextView, Set<ActionType> actionTypes, Action illegalAction)
			throws InterruptedException {
		logger.severe("Selected illegal action: " + illegalAction);
		return null;
	}

	@Override
	public void actionDone(PlayerView contextView, PlayerLocation actionLocation, Action action) {
	}

	@Override
	public void timeLimit(PlayerView contextView, Integer secondsToGo) {
		// TODO 机器人对限时的处理
	}

}
