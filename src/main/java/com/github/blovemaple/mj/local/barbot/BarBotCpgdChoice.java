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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionAndLocation;
import com.github.blovemaple.mj.action.IllegalActionException;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.rule.win.WinType;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
@Deprecated
class BarBotCpgdChoice {
	private final BarBotCpgdSelectTask task;
	private final List<? extends WinType> forWinTypes;

	private final GameContextPlayerView baseContextView;
	private final Action action;
	private final PlayerInfo playerInfo; // 执行动作之后的
	// 后续动作（如吃后出牌）。有后续动作时，和牌概率为后续动作的和牌概率最大者。
	private final List<BarBotCpgdChoice> subChoices;
	private final BarBotCpgdChoice superChoice;

	// 无后续动作时：
	private Map<Integer, List<BarBotSimChanging>> winChangingByChangeCount = new HashMap<>();
	private Map<Integer, Double> winProbByChangeCount = new HashMap<>();

	// 有后续动作时：
	private BarBotCpgdChoice bestSubChoice; // 计算finalWinProb时生成

	private Double finalWinProb; // 只计算一次，延迟生成

	public BarBotCpgdChoice(GameContextPlayerView baseContextView, PlayerInfo baseInfo, Action action,
			BarBotCpgdSelectTask task, List<? extends WinType> forWinTypes) {
		this(baseContextView, baseInfo, action, task, forWinTypes, null);
	}

	private BarBotCpgdChoice(GameContextPlayerView baseContextView, PlayerInfo baseInfo, Action action,
			BarBotCpgdSelectTask task, List<? extends WinType> forWinTypes, BarBotCpgdChoice superChoice) {
		this.baseContextView = baseContextView;
		this.action = action;
		this.task = task;
		this.superChoice = superChoice;
		this.forWinTypes = forWinTypes;
		if (action == null)
			playerInfo = baseInfo;
		else
			playerInfo = doAction(baseContextView, baseInfo, action);
		if (playerInfo.getAliveTiles().size() % 3 == 2)
			subChoices = genSubChoices();
		else
			subChoices = null;
	}

	public List<? extends WinType> getForWinTypes() {
		return forWinTypes;
	}

	private PlayerInfo doAction(GameContextPlayerView baseContextView, PlayerInfo baseInfo, Action action) {
		PlayerInfo playerInfo = baseInfo.clone();
		GameContext simContext = new BarBotSimContext(baseContextView, superChoice == null ? null
				: new ActionAndLocation(superChoice.getAction(), baseContextView.getMyLocation()), playerInfo);
		try {
			action.getType().doAction(simContext, baseContextView.getMyLocation(), action);
		} catch (IllegalActionException e) {
			// 动作不可能非法，因为之前只取的合法动作
			throw new RuntimeException(e);
		}
		return playerInfo;
	}

	private List<BarBotCpgdChoice> genSubChoices() {
		return playerInfo.getAliveTiles().stream().map(tile -> new Action(DISCARD, tile))
				.map(action -> new BarBotCpgdChoice(baseContextView, playerInfo, action, task, forWinTypes, this))
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
	 * @throws InterruptedException
	 */
	public boolean testWinProb(int changeCount) throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();

		if (playerInfo.isTing() && changeCount > 0)
			return false; // 写死了听牌后不能换牌

		if (subChoices == null) {
			// 没有后续动作，收集结果为和牌的换牌，并计算和牌概率（将其发生概率相加）
			List<BarBotSimChanging> winChangings = Collections.synchronizedList(new ArrayList<>());
			// testTime("changings " + changeCount, () ->
			// changings(changeCount).count());
			double winProb = winChangings(changeCount).parallel()
					// 收集
					.peek(winChangings::add)
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
			boolean canWin = false;
			for (BarBotCpgdChoice subChoice : subChoices) {
				if (subChoice.testWinProb(changeCount))
					canWin = true;
			}
			return canWin;
		}
	}

	private Stream<BarBotSimChanging> winChangings(int changeCount) {
		return forWinTypes.stream()
				.flatMap(winType -> winType.changingsForWin(playerInfo, changeCount, task.remainTiles()))
				.map(wc -> new BarBotSimChanging(this, wc.removedTiles, wc.addedTiles));
	}

	@SuppressWarnings("unused")
	private Stream<BarBotSimChanging> winChangings_old(int changeCount) {
		// testTime("changings "+changeCount+" 1", ()->{
		// typeDistinctStream(playerInfo.getAliveTiles(), changeCount).count();
		// });
		//
		// testTime("changings "+changeCount+" 2", ()->{
		// typeDistinctStream(task.remainTiles(), changeCount + 1)
		// .flatMap(removedTiles -> {
		// return
		// // 取count+1个剩余牌，按type去重，组成流
		// combinationStream(playerInfo.getAliveTiles(), changeCount)
		// .map(addedTiles -> null);
		// }
		// //
		// ).count();
		// });
		//
		// testTime("changings "+changeCount+" 3", ()->{
		// typeDistinctStream(playerInfo.getAliveTiles(), changeCount)
		// .flatMap(removedTiles -> {
		// return
		// // 取count+1个剩余牌，按type去重，组成流
		// typeDistinctStream(task.remainTiles(), changeCount + 1)
		// // 过滤掉添加与删除的手牌有重复牌型的（这种的相当于少换了）
		// .filter(addedTiles -> disjointBy(addedTiles, removedTiles,
		// Tile::type))
		// // 生成changing对象
		// .map(addedTiles -> null);
		//
		// }
		// //
		// ).count();
		// });
		//
		// testTime("changings "+changeCount+" 4", ()->{
		// typeDistinctStream(playerInfo.getAliveTiles(), changeCount)
		// .flatMap(removedTiles -> {
		// return
		// // 取count+1个剩余牌，按type去重，组成流
		// typeDistinctStream(task.remainTiles(), changeCount + 1)
		// // 过滤掉添加与删除的手牌有重复牌型的（这种的相当于少换了）
		// .filter(addedTiles -> disjointBy(addedTiles, removedTiles,
		// Tile::type))
		// // 过滤掉少次换牌已和牌的情况（这种的概率已计算在内）
		// .filter(addedTiles -> !this.isCoveredByWin(removedTiles,
		// addedTiles))
		// // 生成changing对象
		// .map(addedTiles -> null);
		//
		// }
		// //
		// ).count();
		// });
		//
		// testTime("changings "+changeCount+" 5", ()->{
		// typeDistinctStream(playerInfo.getAliveTiles(), changeCount)
		// .flatMap(removedTiles -> {
		// return
		// // 取count+1个剩余牌，按type去重，组成流
		// typeDistinctStream(task.remainTiles(), changeCount + 1)
		// // 过滤掉添加与删除的手牌有重复牌型的（这种的相当于少换了）
		// .filter(addedTiles -> disjointBy(addedTiles, removedTiles,
		// Tile::type))
		// // 过滤掉少次换牌已和牌的情况（这种的概率已计算在内）
		// .filter(addedTiles -> !this.isCoveredByWin(removedTiles,
		// addedTiles))
		// // 生成changing对象
		// .map(addedTiles -> new BarBotSimChanging(this, removedTiles,
		// addedTiles));
		//
		// }
		// //
		// ).count();
		// });

		// start 0
		// end 0 38
		// start 1
		// end 1 154
		// start 2
		// end 2 1155
		// start 3
		// end 3 19217

		List<List<Tile>> playerTiles = typeDistinctStream(playerInfo.getAliveTiles(), changeCount)
				.collect(Collectors.toList());
		// 取count个剩余牌，按type去重，组成流
		return typeDistinctStream(task.remainTiles(), changeCount + 1).flatMap(removedTiles -> {
			return
			// 取count+1个手牌，按type去重，组成流
			playerTiles.stream()
					// 过滤掉添加与删除的手牌有重复牌型的（这种的相当于少换了）
					.filter(addedTiles -> disjointBy(addedTiles, removedTiles, Tile::type))
					// 过滤掉少次换牌已和牌的情况（这种的概率已计算在内）
					.filter(addedTiles -> !this.isCoveredByWin(removedTiles, addedTiles))
					// 生成changing对象
					.map(addedTiles -> new BarBotSimChanging(this, removedTiles, addedTiles));
		}
		//
		)
				// 过滤出和牌的
				.filter(BarBotSimChanging::isWin);
	}

	private static Stream<List<Tile>> typeDistinctStream(Collection<Tile> tiles, int size) {
		return distinctCollBy(combListStream(tiles, size), Tile::type);
	}

	/**
	 * 判断指定的换牌是否被已经判断和牌的换牌所覆盖。
	 */
	private boolean isCoveredByWin(Collection<Tile> removedTiles, Collection<Tile> addedTiles) {
		return winChangingByChangeCount.values().stream().flatMap(List::stream)
				.anyMatch(winChanging -> winChanging.isCovered(removedTiles, addedTiles));
	}

	public BarBotCpgdSelectTask getTask() {
		return task;
	}

	public GameContextPlayerView getBaseContextView() {
		return baseContextView;
	}

	public PlayerInfo getPlayerInfo() {
		return playerInfo;
	}

	protected Map<Integer, List<BarBotSimChanging>> getWinChangingByChangeCount() {
		return winChangingByChangeCount;
	}

	protected Map<Integer, Double> getWinProbByChangeCount() {
		return winProbByChangeCount;
	}

	public Double getFinalWinProb() {
		if (finalWinProb == null) {
			if (subChoices == null) {
				// 没有后续动作，把所有winChanging的概率加起来
				finalWinProb = winProbByChangeCount.values().stream().mapToDouble(prob -> prob).sum();
			} else {
				// 有后续动作，取后续动作中概率最大者
				bestSubChoice = subChoices.stream().max(Comparator.comparing(BarBotCpgdChoice::getFinalWinProb))
						.orElse(null);
				if (bestSubChoice != null)
					finalWinProb = bestSubChoice.getFinalWinProb();
			}
		}
		return finalWinProb;
	}

	@Override
	public String toString() {
		return "BarBotCpgdChoice [\n\taction=" + action + "\n\twinProbByChangeCount=" + winProbByChangeCount
				+ "\n\tfinalWinProb=" + finalWinProb + "\n]";
	}

}
