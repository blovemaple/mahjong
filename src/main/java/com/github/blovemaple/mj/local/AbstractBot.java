package com.github.blovemaple.mj.local;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;
import static com.github.blovemaple.mj.utils.MyUtils.*;
import static java.util.stream.Collectors.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.cli.CliGameView;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.object.Player;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public abstract class AbstractBot implements Player {
	private static final Logger logger = Logger.getLogger(AbstractBot.class.getSimpleName());

	private String name;
	private int minThinkingMs = 1000, maxThinkingMs = 3000;

	private long costSum;
	private int invokeCount;

	public AbstractBot(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setThinkingTime(int min, int max) {
		if (min > max)
			throw new IllegalArgumentException("Invalid thinking time: [" + min + "," + max + "]");
		this.minThinkingMs = min;
		this.maxThinkingMs = max;
	}

	public void resetCostStat() {
		costSum = 0;
		invokeCount = 0;
	}

	public long getCostSum() {
		return costSum;
	}

	public int getInvokeCount() {
		return invokeCount;
	}

	@Override
	public Action chooseAction(GameContextPlayerView contextView, Set<ActionType> actionTypes)
			throws InterruptedException {
		long startTime = System.nanoTime();

		logger.info(() -> "BOT Alive tiles: " + aliveTilesStr(contextView));
		Action action = chooseAction0(contextView, actionTypes);
		logger.info(() -> "BOT Chosed action:" + action);

		long endTime = System.nanoTime();
		long nanoCost = endTime - startTime;
		long delayMillis = minThinkingMs - TimeUnit.MILLISECONDS.convert(nanoCost, TimeUnit.NANOSECONDS);
		TimeUnit.MILLISECONDS.sleep(delayMillis);

		return action;
	}

	private Action chooseAction0(GameContextPlayerView contextView, Set<ActionType> actionTypes)
			throws InterruptedException {
		// 如果可以和，就和
		if (actionTypes.contains(WIN))
			return new Action(WIN);

		// 如果可以补花，就补花
		if (actionTypes.contains(BUHUA)) {
			Collection<Set<Tile>> buhuas = BUHUA.getLegalActionTiles(contextView);
			if (!buhuas.isEmpty()) {
				return new Action(BUHUA, buhuas.iterator().next());
			}
		}

		// 如果可以吃/碰/杠/出牌，就选择
		List<Action> cpgdActions = Stream.concat(cpgdActions(contextView, actionTypes), passAction(contextView))
				.collect(toList());
		Action action = cpgdActions.isEmpty() ? null
				: cpgdActions.size() == 1 ? cpgdActions.get(0)
						: chooseCpgdActionWithTimer(contextView, actionTypes, cpgdActions);
		if (action != null)
			return action;

		// 如果可以摸牌，就摸牌
		for (ActionType drawType : Arrays.asList(DRAW, DRAW_BOTTOM))
			if (actionTypes.contains(drawType)) {
				return new Action(drawType);
			}

		// 啥都没选择，放弃了
		return null;
	}

	private String aliveTilesStr(GameContextPlayerView contextView) {
		StringBuilder aliveTilesStr = new StringBuilder();
		Tile justDrawed = contextView.getMyInfo().getLastDrawedTile();
		CliGameView.appendAliveTiles(aliveTilesStr, contextView.getMyInfo().getAliveTiles(), null,
				justDrawed != null ? Set.of(contextView.getMyInfo().getLastDrawedTile()) : null);
		return aliveTilesStr.toString();
	}

	private Stream<Action> cpgdActions(GameContextPlayerView contextView, Set<ActionType> actionTypes) {
		return
		// 吃/碰/杠/出牌动作类型
		Stream.of(CHI, PENG, ZHIGANG, BUGANG, ANGANG, DISCARD, DISCARD_WITH_TING)
				// 过滤出actionTypes有的
				.filter(actionTypes::contains)
				// 生成合法动作，并按照牌型去重
				.flatMap(actionType -> actionType
						// 生成合法tiles
						.getLegalActionTiles(contextView).stream()
						// 按牌型去重
						.filter(distinctorBy(
								tiles -> tiles.stream().map(Tile::type).sorted().collect(Collectors.toList())))
						// 构造Action
						.map(tiles -> new Action(actionType, tiles)));
	}

	private Stream<Action> passAction(GameContextPlayerView contextView) {
		if (contextView.getMyInfo().getAliveTiles().size() % 3 == 1)
			return Stream.of((Action) null);
		else
			return Stream.empty();
	}

	private Action chooseCpgdActionWithTimer(GameContextPlayerView contextView, Set<ActionType> actionTypes,
			List<Action> actions) throws InterruptedException {
		long startTime = System.nanoTime();
		try {
			return chooseCpgdAction(contextView, actionTypes, actions);
		} finally {
			long endTime = System.nanoTime();
			long nanoCost = endTime - startTime;
			costSum += nanoCost;
			invokeCount++;
			logger.info("BOT Time cost(millis): " + Math.round(nanoCost / 1_000_000D));

			// 没到目标时间的话假装再想一会儿
			int targetThinkingTime = minThinkingMs + (int) (Math.random() * (maxThinkingMs - minThinkingMs));
			long delayMillis = targetThinkingTime - TimeUnit.MILLISECONDS.convert(nanoCost, TimeUnit.NANOSECONDS);
			TimeUnit.MILLISECONDS.sleep(delayMillis);
		}
	}

	protected abstract Action chooseCpgdAction(GameContextPlayerView contextView, Set<ActionType> actionTypes,
			List<Action> actions) throws InterruptedException;

	@Override
	public Action chooseAction(GameContextPlayerView contextView, Set<ActionType> actionTypes, Action illegalAction)
			throws InterruptedException {
		logger.severe("Selected illegal action: " + illegalAction);
		return null;
	}

	@Override
	public void actionDone(GameContextPlayerView contextView, PlayerLocation actionLocation, Action action) {
	}

	@Override
	public void timeLimit(GameContextPlayerView contextView, Integer secondsToGo) {
	}

}
