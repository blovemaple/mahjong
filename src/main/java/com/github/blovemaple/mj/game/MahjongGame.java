package com.github.blovemaple.mj.game;

import static com.github.blovemaple.mj.utils.LambdaUtils.*;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionAndLocation;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.action.ActionTypeAndLocation;
import com.github.blovemaple.mj.action.IllegalActionException;
import com.github.blovemaple.mj.event.GameEventListener;
import com.github.blovemaple.mj.game.rule.GameStrategy;
import com.github.blovemaple.mj.game.rule.TimeLimitStrategy;
import com.github.blovemaple.mj.object.MahjongTable;
import com.github.blovemaple.mj.object.Player;
import com.github.blovemaple.mj.object.PlayerLocation;

/**
 * 麻将游戏。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class MahjongGame {
	private static final Logger logger = Logger
			.getLogger(MahjongGame.class.getSimpleName());

	private GameStrategy gameStrategy;
	private TimeLimitStrategy timeStrategy;

	public MahjongGame(GameStrategy gameStrategy,
			TimeLimitStrategy timeStrategy) {
		this.gameStrategy = gameStrategy;
		this.timeStrategy = timeStrategy;
	}

	/**
	 * 进行一局麻将游戏，结束后记录并返回结果。
	 * 
	 * @return 结果
	 * @throws InterruptedException
	 *             等待玩家做出选择的时候当前线程被中断
	 */
	public GameResult play(MahjongTable table) throws InterruptedException {
		// 检查有效性
		if (!gameStrategy.checkReady(table)) {
			throw new IllegalStateException("Table is not ready for play.");
		}

		// 生成上下文
		GameContext context = new GameContext(table, gameStrategy);

		// 初始化麻将桌
		table.init(gameStrategy.getAllTiles());

		// 初始化上下文
		gameStrategy.readyContext(context);

		// 收集所有玩家的监听器
		Map<PlayerLocation, GameEventListener> listeners = new EnumMap<>(
				PlayerLocation.class);
		table.getPlayerInfos().forEach((location, playerInfo) -> {
			Player player = playerInfo.getPlayer();
			if (player != null) {
				GameEventListener listener = player.getEventListener();
				if (listener != null)
					listeners.put(location, listener);
			}
		});

		logger.info("init done.");

		// 发牌
		Action dealAction = gameStrategy.getDealAction(context);
		try {
			doAction(context, null, dealAction, listeners);
		} catch (IllegalActionException e) {
			// 策略返回的发牌动作是非法动作
			throw new RuntimeException("Illegal deal action: " + dealAction);
		}

		logger.info("deal done.");

		// 循环执行动作，直到结束
		while (true) {

			// 决定一个要执行的动作
			logger.info("Start choose action...");
			ActionAndLocation finalAction = chooseAction(context, table,
					listeners);
			logger.info("End choose action");
			if (finalAction != null) {
				// 如果有动作要执行，则执行动作
				try {
					doAction(context, finalAction.getLocation(),
							finalAction.getAction(), listeners);
				} catch (IllegalActionException e) {
					// 玩家默认动作或默认动作不合法（玩家选择的动作已经经过了合法性判断）
					throw new RuntimeException(
							"Illegal action: " + finalAction.getLocation()
									+ ", " + finalAction.getAction());
				}
			}

			// 从策略判断游戏是否结束
			boolean end = gameStrategy.tryEndGame(context);
			if (end) {
				// 策略判断游戏已结束
				return context.getGameResult();
			}

			if (finalAction == null && !end) {
				// 如果既没有动作可执行，游戏又没有结束，则是异常状态
				throw new RuntimeException(
						"Nothing to do. Context: " + context);
			}
		}
	}

	/**
	 * 决定要执行的动作。先让玩家选择，如果玩家无可选动作或者都选择不做动作，则从策略获取默认动作。
	 */
	private ActionAndLocation chooseAction(GameContext context,
			MahjongTable table,
			Map<PlayerLocation, GameEventListener> listeners)
			throws InterruptedException {
		// 查找所有玩家可以做的动作类型
		Map<PlayerLocation, Set<ActionType>> choisesByLocation = new HashMap<>();
		table.getPlayerInfos().forEach((location, playerInfo) -> {
			// 从策略获取所有动作类型
			Set<ActionType> choises = (playerInfo.isTing()
					? gameStrategy.getAllActionTypesInTing()
					: gameStrategy.getAllActionTypesInGame()).stream()
							// 过滤出可以做的动作类型
							.filter(actionType -> actionType.canDo(context,
									location))
							.collect(Collectors.<ActionType> toSet());
			if (!choises.isEmpty())
				choisesByLocation.put(location, choises);
		});

		logger.info(() -> "Action choises: " + choisesByLocation);

		if (!choisesByLocation.isEmpty()) {

			// 存在玩家可以做的动作

			// 询问这些玩家想要做的动作
			// 玩家做出动作之后判断合法性，如果不合法则继续询问

			ScheduledExecutorService executor = Executors
					.newScheduledThreadPool(choisesByLocation.size() * 2 + 1);
			// （这个map必须支持null value，因为需要用null value表示选择不做动作）
			Map<PlayerLocation, Action> choseActionByLocation = new EnumMap<>(
					PlayerLocation.class);
			Map<PlayerLocation, CompletableFuture<Action>> chooseFutures = new EnumMap<>(
					PlayerLocation.class);
			choisesByLocation.forEach((location, choisesAndPriority) -> {
				CompletableFuture<Action> chooseFuture = playerChooseActionAsync(
						context, table, location, choisesAndPriority, executor,
						choseActionByLocation);
				chooseFutures.put(location, chooseFuture);
			});

			// 用限时策略获取最长等待时间，如果超时则从策略获取每个玩家的默认动作
			Integer timeLimit = timeStrategy.getLimit(context,
					choisesByLocation);
			if (timeLimit != null) {
				AtomicInteger secondsToGo = new AtomicInteger(timeLimit);
				executor.scheduleAtFixedRate(() -> {
					int crtSecondsToGo = secondsToGo.getAndDecrement();
					if (crtSecondsToGo < 0)
						return;
					listeners.forEach((listenerLocation, listener) -> listener
							.timeLimit(context.getPlayerView(listenerLocation),
									crtSecondsToGo));
					if (crtSecondsToGo == 0) {
						logger.info("Time limit!");
						chooseFutures.forEach((location, chooseFuture) -> {
							if (!chooseFuture.isDone()) {
								logger.info(
										() -> "Start get player def action...: "
												+ location);
								Action defAction = gameStrategy
										.getPlayerDefaultAction(context,
												location, choisesByLocation
														.get(location));
								chooseFuture.complete(defAction);
								logger.info(() -> "End get player def action: "
										+ location + defAction);
							}
						});
					}
				}, 0, 1, TimeUnit.SECONDS);
			}

			// 在map上等待
			// 如果出现目前等待的玩家中优先级最高的动作，或者所有玩家都做出了动作，则进行做出的优先级最高的动作
			synchronized (choseActionByLocation) {
				while (true) {
					ActionAndLocation action = determineAction(
							choseActionByLocation, choisesByLocation, context);
					if (action != null) {
						// 动作已决定
						// 中断未作出选择的玩家的选择逻辑并返回
						logger.info("Action determined: " + action);
						executor.shutdownNow();
						return action;
					} else if (choseActionByLocation.size() == choisesByLocation
							.size()) {
						// 所有玩家都做出选择仍不能决定（即所有玩家都选择不做动作）
						logger.info("Action not determined at final.");
						break;
					}
					choseActionByLocation.wait();
				}
			}
			// 所有玩家都做出决定了，关掉executor
			executor.shutdownNow();
		}

		// 如果上面没有产生要做的动作，则从策略获取默认动作
		logger.info("Start get def action...");
		ActionAndLocation defAction = gameStrategy.getDefaultAction(context,
				choisesByLocation);
		logger.info("End get def action: " + defAction);
		return defAction;
	}

	/**
	 * 异步让一个玩家选择动作。
	 * 
	 * @param context
	 * @param table
	 * @param location
	 * @param choises
	 * @param executor
	 *            使用这个executor，以便主线程可以中断玩家的选择过程
	 * @param choseActionByLocation
	 *            存放玩家的选择结果。<br>
	 *            玩家选择结果后需要put到此map中，并且在此map上notifyAll，通知主线程map已更新。
	 * @return 选择任务的Future
	 */
	private CompletableFuture<Action> playerChooseActionAsync(
			GameContext context, MahjongTable table, PlayerLocation location,
			Set<ActionType> choises, Executor executor,
			Map<PlayerLocation, Action> choseActionByLocation) {
		Player player = table.getPlayerByLocation(location);

		boolean canPass = choises.stream()
				.allMatch(actionType -> actionType.canPass(context, location));

		try {
			CompletableFuture<Action> chooseFuture = CompletableFuture
					.supplyAsync(rethrowSupplier(() -> {
						// 让玩家选择动作
						logger.info("Start choose action...: " + location);
						Action testedAction = player.chooseAction(
								context.getPlayerView(location), choises);
						logger.info("End choose action: " + location
								+ testedAction);
						// 检查选择的动作合法性，如果不合法则循环重新选择
						checkInterrupted();
						while (!(testedAction == null ? canPass
								: testedAction.getType().isLegalAction(context,
										location, testedAction))) {
							checkInterrupted();
							logger.info("Start choose action again...: "
									+ location);
							testedAction = player.chooseAction(
									context.getPlayerView(location), choises,
									testedAction);
							logger.info("End choose action again: " + location
									+ testedAction);
						}
						logger.info("Legal chose action: " + location
								+ testedAction);
						return testedAction;
					}), executor);

			// 当选择结束时，把选择的结果放入map
			chooseFuture.whenCompleteAsync(rethrowBiConsumer((action, e) -> {
				if (e != null) {
					logger.log(Level.SEVERE, ((Throwable) e).toString(),
							(Throwable) e);
					return;
				}
				checkInterrupted();
				synchronized (choseActionByLocation) {
					choseActionByLocation.put(location, (Action) action);
					// notify一下，让主线程知道选择已更新
					choseActionByLocation.notifyAll();
				}
			}), executor);

			return chooseFuture;
		} catch (InterruptedException e) {
			return null;
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
			return null;
		}
	}

	private void checkInterrupted() throws InterruptedException {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
	}

	private ActionAndLocation determineAction(
			Map<PlayerLocation, Action> choseActionByLocation,
			Map<PlayerLocation, Set<ActionType>> choisesByLocation,
			GameContext context) {
		logger.info("determining...");
		logger.info("chosed:" + choseActionByLocation);
		logger.info("choises:" + choisesByLocation);

		if (choseActionByLocation.isEmpty())
			return null;

		Comparator<ActionTypeAndLocation> actionPriorityComparator = gameStrategy
				.getActionPriorityComparator();

		// 得出已选动作类型中优先级最高的
		ActionAndLocation chosePriorAction = choseActionByLocation.entrySet()
				.stream()
				// 过滤掉放弃的
				.filter(entry -> entry.getValue() != null)
				// 选优先级最高的一个
				.min(Comparator
						.comparing(
								entry -> new ActionTypeAndLocation(
										entry.getValue().getType(),
										entry.getKey(), context),
								actionPriorityComparator))
				// 创建成ActionAndLocation
				.map(entry -> new ActionAndLocation(entry.getValue(),
						entry.getKey()))
				.orElse(null);

		if (chosePriorAction == null)
			return null;

		if (choseActionByLocation.size() == choisesByLocation.size()) {
			// 所有玩家都做出了选择，直接返回已选的优先级最高的动作
			return chosePriorAction;
		}

		// 还有玩家没做出选择，看没做出选择的里面是否可能有优先级更高的动作类型

		// 得出未选动作类型中优先级最高的
		ActionTypeAndLocation priorAction = choisesByLocation.entrySet()
				.stream()
				// 过滤掉已选择的玩家
				.filter(entry -> !choseActionByLocation
						.containsKey(entry.getKey()))
				// 在剩下的所有选择中取优先级最高的
				.flatMap(entry -> entry.getValue().stream()
						.map(actionType -> new ActionTypeAndLocation(actionType,
								entry.getKey(), context)))
				.min(actionPriorityComparator).orElse(null);

		if (priorAction == null) {
			// 选不出优先级最高的动作类型，这是不可能的
			return null;
		}

		// 如果未选动作中没有超过已选动作优先级的，则确定做最高优先级的已选动作，
		// 否则返回null继续等待选择
		if (actionPriorityComparator.compare(
				new ActionTypeAndLocation(
						chosePriorAction.getAction().getType(),
						chosePriorAction.getLocation(), context),
				priorAction) <= 0)
			return chosePriorAction;
		else
			return null;

	}

	private void doAction(GameContext context, PlayerLocation location,
			Action action, Map<PlayerLocation, GameEventListener> listeners)
			throws IllegalActionException {
		logger.info(() -> "Start action " + location + action);

		action.getType().doAction(context, location, action);
		context.actionDone(action, location);
		listeners.forEach((locationOfListener, listener) -> listener.actionDone(
				context.getPlayerView(locationOfListener), location, action));

		logger.info(() -> "Done action " + location + action);
	}

	public GameStrategy getGameStrategy() {
		return gameStrategy;
	}

}
