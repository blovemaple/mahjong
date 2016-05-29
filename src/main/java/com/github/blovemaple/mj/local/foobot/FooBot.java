package com.github.blovemaple.mj.local.foobot;

import static com.github.blovemaple.mj.utils.LambdaUtils.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
import java.util.stream.Collectors;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionAndLocation;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.game.GameContext.PlayerView;
import com.github.blovemaple.mj.game.MahjongGame;
import com.github.blovemaple.mj.object.Player;
import com.github.blovemaple.mj.object.PlayerLocation;

/**
 * 机器人。注意：chooseAction方法上加了锁，不能同时进行两个选择过程。
 * 
 * @deprecated 这个机器人太慢了，没用到。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class FooBot implements Player {
	private static final Logger logger = Logger
			.getLogger(FooBot.class.getSimpleName());

	private String name;

	public FooBot(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	// 下面两个集合分别存放 目前等待被确定结果（和牌或流局）或拆分的 和 已经确定结果或拆分的 context。

	// 进行模拟时，将被拆分的context从doingContexts移除，并将新拆分出的context加到doneContexts；
	// 计算结果时，从doneContexts中所有context按层数从大到小合并结果，即可逐层合并到到最顶层的context。

	// 主选择线程需要以doingContexts的清空作为模拟结束的标志，所以主线程在lock上加锁，在simStopCondition上等待，
	// 每个模拟线程处理完后，或者模拟过程被中断后，应该在simStopCondition上notify，确保主线程及时知道doingContexts的清空。

	// 当模拟过程被中断时，应该尽快清空doingContexts。这样，如果某个模拟任务没有及时中止，也可以发现处理的context不在doingContexts中而及时中止。
	private final Set<FooSimContext> doingContexts = Collections
			.synchronizedSet(new HashSet<>());
	private final List<FooSimContext> doneContexts = Collections
			.synchronizedList(new LinkedList<>());
	private final Map<Integer, FooSimContext> allContexts = Collections
			.synchronizedMap(new HashMap<>());
	private final Lock lock = new ReentrantLock();
	private final Condition simStopCondition = lock.newCondition();

	/**
	 * 模拟任务运行在这个executor上。<br>
	 * 引用是Executor而不是ExecutorService，是为了限制只能用execute方法提交任务，原因是：<br>
	 * 需要用level作为优先级进行队列排序，而队列只支持用execute提交的Runnable。
	 */
	private Executor simExecutor;

	@Override
	public synchronized Action chooseAction(PlayerView contextView,
			Set<ActionType> actionTypes) throws InterruptedException {
		try {
			// 创建顶层的context
			MahjongGame gameTool = new MahjongGame(
					new FooSimGameStrategy(contextView.getGameStrategy()),
					contextView.getTimeLimitStrategy());
			FooSimContext topContext = new FooSimContext(contextView, gameTool,
					rethrowBiFunction(this::submitSplitContextForSim),
					rethrowConsumer(this::submitDoneContext));

			// 新建simExecutor并提交顶层context，开始模拟
			int processorCount = Runtime.getRuntime().availableProcessors();
			ExecutorService simExecutor = new ThreadPoolExecutor(processorCount,
					processorCount, 0L, TimeUnit.MILLISECONDS,
					// executor按照level从低到高处理，使模拟逐层向下推进
					new PriorityBlockingQueue<Runnable>(11,
							Comparator.comparing(task -> ((FooSimContext) task)
									.getLevel())));
			this.simExecutor = simExecutor;
			submitContextForSim(topContext);

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
				// 一定要关闭simExecutor，中断还没执行完的模拟任务
				simExecutor.shutdownNow();
			}

			// 计算结果得出选择
			return selectFinalAction();
		} finally {
			// 一定要clear，避免浪费资源
			clear();
		}
	}

	/**
	 * 提交一个模拟任务。
	 */
	private void submitContextForSim(FooSimContext context) {
		doingContexts.add(context);
		allContexts.put(context.contextHash(), context);
		simExecutor.execute(context);
	}

	Map<Integer, Integer> count = Collections.synchronizedMap(new HashMap<>());

	/**
	 * 分割模拟任务。
	 * 
	 * @throws InterruptedException
	 */
	private Map<ActionAndLocation, FooSimContext> submitSplitContextForSim(
			FooSimContext oriContext,
			Map<ActionAndLocation, FooSimContext> newContexts)
			throws InterruptedException {
		lock.lockInterruptibly();
		try {
			if (doingContexts.remove(oriContext)) {
				// 把newContexts在allContexts中检查重复，去掉已经有的。重复由FooSimContext.equal()定义。
				Map<ActionAndLocation, FooSimContext> realNewContexts = new HashMap<>();
				newContexts.forEach((al, context) -> {
					FooSimContext existContext = allContexts
							.putIfAbsent(context.contextHash(), context);
					realNewContexts.put(al,
							existContext != null ? existContext : context);
					if (existContext == null) {
						doingContexts.add(context);
						simExecutor.execute(context);
						count.compute(context.getLevel(), (level,
								levelC) -> levelC == null ? 1 : levelC + 1);
					}
				});
				logger.info("CONTEXT LEVEL " + oriContext.getLevel()
						+ " SPLIT INFO: " + realNewContexts.size());
				doneContexts.add(oriContext);
				return realNewContexts;
			} else
				return null;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 模拟任务结束。
	 * 
	 * @throws InterruptedException
	 */
	private void submitDoneContext(FooSimContext oriContext)
			throws InterruptedException {
		lock.lockInterruptibly();
		try {
			if (doingContexts.remove(oriContext)) {
				doneContexts.add(oriContext);
				simStopCondition.signalAll();
			}
		} finally {
			lock.unlock();
		}
	}

	private Action selectFinalAction() {
		logger.info("CONTEXT COUNT: " + count);

		if (doneContexts.isEmpty())
			throw new RuntimeException("No done contexts!");

		Queue<FooSimContext> contexts = new PriorityBlockingQueue<FooSimContext>(
				11, Comparator.comparing(FooSimContext::getLevel).reversed());
		contexts.addAll(doneContexts);

		// 从底层向高层逐层计算胜率
		// 先串行搞，如果要改成并行，需要注意必须先算完下一层才能开始上一层
		FooSimContext context;
		while ((context = contexts.poll()) != null) {
			// 确定此context的选择动作和胜率
			determineSelfActionAndWinProb(context);

			// 如果是顶层context，返回bestaction
			if (context.getLevel() == 0)
				return context.getBestSelfAction();
		}

		throw new RuntimeException("No top context!");
	}

	private void determineSelfActionAndWinProb(FooSimContext context) {
		if (context.getSelfWinProb() != null)
			return;
		if (context.getFinalProbs().isEmpty())
			throw new RuntimeException("Final probs is empty.");

		// 算出本家每一种选择的胜率
		Map<Action, Double> selfActionAndWinProb = new HashMap<>();
		context.setSelfActionAndWinProb(selfActionAndWinProb);
		context.getFinalProbs().forEach((selfAction, actionAndProbs) -> {
			Double winProbSum = actionAndProbs.entrySet().stream()
					.collect(Collectors.summingDouble(alAndProb -> {
						Double winProb = context.getNextContexts()
								.get(alAndProb.getKey()).getSelfWinProb();
						return winProb == null ? 0d
								: winProb * alAndProb.getValue();
					}));
			selfActionAndWinProb.put(selfAction, winProbSum);
		});

		// 选择一种最好的
		Map.Entry<Action, Double> bestSelfAction = selfActionAndWinProb
				.entrySet().stream()
				.max(Comparator.comparing(Map.Entry::getValue)).get();
		context.setBestSelfAction(bestSelfAction.getKey());
		context.setSelfWinProb(bestSelfAction.getValue());
	}

	private void clear() {
		doingContexts.clear();
		doneContexts.clear();
		simExecutor = null;
	}

	@Override
	public Action chooseAction(PlayerView contextView,
			Set<ActionType> actionTypes, Action illegalAction)
			throws InterruptedException {
		logger.severe("Selected illegal action: " + illegalAction);
		return null;
	}

	@Override
	public void actionDone(PlayerView contextView,
			PlayerLocation actionLocation, Action action) {
	}

	@Override
	public void timeLimit(PlayerView contextView, Integer secondsToGo) {
		// TODO 机器人对限时的处理
	}

}
