package com.github.blovemaple.mj.local.bazbot;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.tuple.Pair;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.action.IllegalActionException;
import com.github.blovemaple.mj.action.PlayerAction;
import com.github.blovemaple.mj.action.PlayerActionType;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.local.AbstractBot;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class BazBot extends AbstractBot {
	private static final Logger logger = Logger.getLogger(BazBot.class.getSimpleName());

	/**
	 * 由于Stream中不能用null作为元素（reduce后在创建Optional时出错），所以用这个对象表示放弃动作。<br>
	 * XXX 后续应用到其他表示放弃动作地方代替原来的null。
	 */
	private static final Action PASS_ACTION = new Action(new ActionType() {

		@Override
		public boolean isLegalAction(GameContext context, Action action) {
			return true;
		}

		@Override
		public void doAction(GameContext context, Action action)
				throws IllegalActionException {
		}

		@Override
		public String toString() {
			return "[PASS]";
		}
	});

	public BazBot(String name) {
		super(name);
	}

	public BazBot() {
		this("BazBot");
	}

	@Override
	protected PlayerAction chooseCpgdAction(GameContextPlayerView contextView, Set<PlayerActionType> actionTypes,
			List<PlayerAction> actions) throws InterruptedException {
		BazBotSimContext simContext = new BazBotSimContext(contextView);
		List<Pair<PlayerAction, Double>> actionAndScores = actions.stream()
				// 并行
				.parallel()
				// 模拟动作并计算评分（不能直接用max否则会重复计算）
				.map(action -> Pair.of(action, simContext.afterSimAction(action).score()))
				// 按评分从高到底排序方便看日志
				.sorted(comparing(actionAndScore -> -actionAndScore.getRight())).collect(toList());

		actionAndScores.forEach(actionAndScore -> logger.info(
				() -> "BOT Action candidate " + actionAndScore.getLeft() + " score " + actionAndScore.getRight()));

		// 选评分最高的一个
		PlayerAction chosenAction = actionAndScores.get(0).getKey();
		return chosenAction == PASS_ACTION ? null : chosenAction;
	}

}
