package com.github.blovemaple.mj.local.bazbot;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;
import static com.github.blovemaple.mj.utils.MyUtils.*;

import java.util.Comparator;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.local.AbstractBot;
import com.github.blovemaple.mj.object.Tile;

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
		BazBotSimContext simContext = new BazBotSimContext(contextView);
		return
		// 所有吃/碰/杠/出牌动作 + 放弃动作（如果合法的话）
		Stream.concat(cpgdActions(contextView, actionTypes), passAction(contextView, actionTypes))
				// 并行
				.parallel()
				// 模拟动作并选出评分最高的一个
				.max(Comparator.comparing(action -> simContext.afterSimAction(action).score())).orElse(null);
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

	private Stream<Action> passAction(GameContextPlayerView contextView, Set<ActionType> actionTypes) {
		if (contextView.getMyInfo().getAliveTiles().size() % 3 == 1)
			return Stream.of((Action) null);
		else
			return Stream.empty();
	}

}
