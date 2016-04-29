package com.github.blovemaple.mj.local.barbot;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;
import static com.github.blovemaple.mj.utils.MyUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.IllegalActionException;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;

class BarBotCpgdChoice {
	private final BarBotCpgdSelectTask task;

	private final GameContext.PlayerView baseContextView;
	private final Action action;
	private final PlayerInfo playerInfo; // 执行动作之后的
	// 后续动作（如吃后出牌）。有后续动作时，和牌概率为后续动作的和牌概率最大者。
	private final List<BarBotCpgdChoice> subChoices;

	// 无后续动作时：
	private Map<Integer, List<BarBotSimChanging>> winChangingByChangeCount = new HashMap<>();
	private Map<Integer, Double> winProbByChangeCount = new HashMap<>();

	// 有后续动作时：
	private BarBotCpgdChoice bestSubChoice; // 计算finalWinProb时生成

	private Double finalWinProb; // 只计算一次，延迟生成

	public BarBotCpgdChoice(GameContext.PlayerView baseContextView,
			PlayerInfo baseInfo, Action action, BarBotCpgdSelectTask task) {
		this.baseContextView = baseContextView;
		this.action = action;
		this.task = task;
		if (action == null)
			playerInfo = baseInfo;
		else
			playerInfo = doAction(baseContextView, baseInfo, action);
		if (playerInfo.getAliveTiles().size() % 3 == 2)
			subChoices = genSubChoices();
		else
			subChoices = null;
	}

	private PlayerInfo doAction(GameContext.PlayerView baseContextView,
			PlayerInfo baseInfo, Action action) {
		GameContext simContext = new BarBotSimContext(baseContextView);
		PlayerInfo playerInfo = baseInfo.clone();
		try {
			action.getType().doAction(simContext,
					baseContextView.getMyLocation(), action);
		} catch (IllegalActionException e) {
			// 动作不可能非法，因为之前只取的合法动作
			throw new RuntimeException(e);
		}
		return playerInfo;
	}

	private List<BarBotCpgdChoice> genSubChoices() {
		return playerInfo.getAliveTiles().stream()
				.map(tile -> new Action(DISCARD, tile))
				.map(action -> new BarBotCpgdChoice(baseContextView, playerInfo,
						action, task))
				.collect(Collectors.toList());
	}

	public Action getAction() {
		return action;
	}

	/**
	 * 测试指定次换牌后的和牌可能性，并记录。
	 * 
	 * @param changeCount
	 *            换牌次数（另加一张牌）
	 * @return 是否有和牌可能
	 */
	public boolean testWinProb(int changeCount) {
		if (playerInfo.isTing() && changeCount > 0)
			return false; // XXX - 写死了听牌后不能换牌

		if (subChoices == null) {
			// 没有后续动作，收集结果为和牌的换牌，并计算和牌概率（将其发生概率相加）
			List<BarBotSimChanging> winChangings = Collections
					.synchronizedList(new ArrayList<>());
			double winProb = changings(changeCount).parallel()
					// 过滤出和牌的，收集
					.filter(BarBotSimChanging::isWin).peek(winChangings::add)
					// 统计番数期望值（和牌番数×发生概率）
					.mapToDouble(c -> c.getWinPoint() * c.getProb()).sum();
			if (winProb > 0) {
				winChangingByChangeCount.put(changeCount, winChangings);
				winProbByChangeCount.put(changeCount, winProb);
				return true;
			} else
				return false;
		} else {
			// 有后续动作，让后续动作都测试一遍
			Set<Boolean> canWins = subChoices.parallelStream()
					.map(choice -> choice.testWinProb(changeCount))
					.collect(Collectors.toSet());
			return canWins.contains(Boolean.TRUE);
		}
	}

	private Stream<BarBotSimChanging> changings(int changeCount) {
		// 取count个手牌，按type去重，组成流
		return typeDistinctStream(playerInfo.getAliveTiles(), changeCount)
				.flatMap(removedTiles ->
		// 取count+1个剩余牌，按type去重，组成流
		typeDistinctStream(task.remainTiles(), changeCount + 1)
				// 过滤掉添加与删除的手牌有重复牌型的（这种的相当于少换了）
				.filter(addedTiles -> disjointBy(addedTiles, removedTiles,
						Tile::type))
				// 过滤掉少次换牌已和牌的情况（这种的概率已计算在内）
				.filter(addedTiles -> this.isCoveredByWin(removedTiles,
						addedTiles))
				// 生成changing对象
				.map(addedTiles -> new BarBotSimChanging(this, removedTiles,
						addedTiles))
		//
		);
	}

	private Stream<Set<Tile>> typeDistinctStream(Collection<Tile> tiles,
			int size) {
		return distinctBy(combinationStream(playerInfo.getAliveTiles(), size),
				Tile::type);
	}

	/**
	 * 判断指定的换牌是否被已经判断和牌的换牌所覆盖。
	 */
	public boolean isCoveredByWin(Set<Tile> removedTiles,
			Set<Tile> addedTiles) {
		return winChangingByChangeCount.values().stream().flatMap(List::stream)
				.anyMatch(winChanging -> winChanging.isCovered(removedTiles,
						addedTiles));
	}

	public GameContext.PlayerView getBaseContextView() {
		return baseContextView;
	}

	public PlayerInfo getPlayerInfo() {
		return playerInfo;
	}

	public Double getFinalWinProb() {
		if (finalWinProb == null) {
			if (subChoices == null) {
				// 没有后续动作，把所有winChanging的概率加起来
				finalWinProb = winProbByChangeCount.values().stream()
						.mapToDouble(prob -> prob).sum();
			} else {
				// 有后续动作，取后续动作中概率最大者
				bestSubChoice = subChoices.stream()
						.max(Comparator
								.comparing(BarBotCpgdChoice::getFinalWinProb))
						.orElse(null);
				if (bestSubChoice != null)
					finalWinProb = bestSubChoice.getFinalWinProb();
			}
		}
		return finalWinProb;
	}

}
