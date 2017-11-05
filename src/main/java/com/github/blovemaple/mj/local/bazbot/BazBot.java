package com.github.blovemaple.mj.local.bazbot;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.action.IllegalActionException;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.local.AbstractBot;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileType;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class BazBot extends AbstractBot {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BazBot.class.getSimpleName());

	public BazBot(String name) {
		super(name);
	}

	public BazBot() {
		this("BazBot");
	}

	@Override
	protected Action chooseCpgdAction(GameContextPlayerView contextView, Set<ActionType> actionTypes)
			throws InterruptedException {
		return
		// 所有吃/碰/杠/出牌动作 + 放弃动作（如果合法的话）
		Stream.concat(cpgdActions(contextView, actionTypes), passAction(contextView, actionTypes))
				// 并行
				.parallel()
				// 选出评分最高的一个
				.max(Comparator.comparing(action -> score(contextView, action))).orElse(null);
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
						.filter(new ByTileTypesDistinctor())
						// 构造Action
						.map(tiles -> new Action(actionType, tiles)));
	}

	private Stream<Action> passAction(GameContextPlayerView contextView, Set<ActionType> actionTypes) {
		if (contextView.getMyInfo().getAliveTiles().size() % 3 == 1)
			return Stream.of((Action) null);
		else
			return Stream.empty();
	}

	private static class ByTileTypesDistinctor implements Predicate<Set<Tile>> {
		private Set<List<TileType>> distinctTypes = new HashSet<>();

		@Override
		public boolean test(Set<Tile> tiles) {
			return distinctTypes.add(tiles.stream().map(Tile::type).sorted().collect(Collectors.toList()));
		}

	}

	/**
	 * 根据指定的contextView条件，对指定的action进行评分并返回分数。
	 */
	private double score(GameContextPlayerView contextView, Action action) {
		// 动作模拟
		GameContextPlayerView contextViewAfterAction = simAction(contextView, action);

		if (contextViewAfterAction.getMyInfo().getAliveTiles().size() % 3 == 2) {
			// 待出牌状态，从出每一张牌后的状态中选最高评分
			return contextViewAfterAction.getMyInfo().getAliveTiles().stream()
					// 生成、模拟出牌动作
					.map(aliveTile -> new Action(DISCARD, aliveTile))
					.map(discard -> simAction(contextViewAfterAction, discard))
					// 评分并选出最高
					.map(contextViewAfterDiscard -> new BazBotScoreCalculator(contextViewAfterDiscard).calcScore())
					.max(Comparator.naturalOrder()).orElse(0d);
		} else {
			// 不是待出牌状态，直接评分
			return new BazBotScoreCalculator(contextViewAfterAction).calcScore();
		}

	}

	/**
	 * 在指定contextView条件下模拟指定动作，并返回完成动作后的GameContextPlayerView。参数中的contextView不被修改。
	 */
	private GameContextPlayerView simAction(GameContextPlayerView contextView, Action action) {
		if (action == null)
			return contextView;

		try {
			GameContext simContext = new BazBotSimContext(contextView);
			action.getType().doAction(simContext, contextView.getMyLocation(), action);
			return simContext.getPlayerView(contextView.getMyLocation());
		} catch (IllegalActionException e) {
			// 非法动作，不可能发生，因为选择动作的时候就是只选的合法的
			throw new RuntimeException("BazBot is simming illegal action: " + action, e);
		}
	}

}
