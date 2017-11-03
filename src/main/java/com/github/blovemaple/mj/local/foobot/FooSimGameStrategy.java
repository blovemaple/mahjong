package com.github.blovemaple.mj.local.foobot;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionAndLocation;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.action.ActionTypeAndLocation;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.object.MahjongTable;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.rule.GameStrategy;
import com.github.blovemaple.mj.rule.win.FanType;
import com.github.blovemaple.mj.rule.win.WinType;

/**
 * 在原策略的基础上添加模拟出牌动作类型、将和牌动作类型替换为模拟和牌动作类型。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class FooSimGameStrategy implements GameStrategy {

	private GameStrategy realStrategy;

	private Set<ActionType> actionTypesInGame, actionTypesInTing;

	public FooSimGameStrategy(GameStrategy realStrategy) {
		this.realStrategy = realStrategy;
		actionTypesInGame = simActionTypes(
				realStrategy.getAllActionTypesInGame());
		actionTypesInTing = simActionTypes(
				realStrategy.getAllActionTypesInTing());
	}

	private Set<ActionType> simActionTypes(Set<? extends ActionType> oriTypes) {
		Set<ActionType> simTypes = new HashSet<ActionType>();
		simTypes.addAll(oriTypes);

		simTypes.add(FooSimSelfDrawActionType.type());

		if (!oriTypes.contains(DISCARD))
			throw new IllegalArgumentException("No discard type.");
		simTypes.add(FooSimOthersDiscardActionType.type());

		List<ActionType> winTypes = oriTypes.stream().filter(WIN::matchBy)
				.collect(Collectors.toList());
		if (winTypes.size() != 1)
			throw new IllegalArgumentException(
					"More than 1 win types: " + winTypes);
		ActionType oriWinType = winTypes.get(0);
		if (oriWinType != WIN)
			throw new IllegalArgumentException(
					"Unsupported win type: " + oriWinType);
		simTypes.remove(oriWinType);
		simTypes.add(FooSimWinActionType.type());

		return simTypes;
	}

	public Set<? extends ActionType> getAllActionTypesInGame() {
		return actionTypesInGame;
	}

	public Set<? extends ActionType> getAllActionTypesInTing() {
		return actionTypesInTing;
	}

	public Comparator<ActionTypeAndLocation> getActionPriorityComparator() {
		// 永远把empty action放在最低优先级，确保gametool让所有模拟玩家都完成选择逻辑
		return (o1, o2) -> {
			if (o1.getActionType() == FooSimEmptyActionType.type()
					&& o2.getActionType() == FooSimEmptyActionType.type())
				return 0;
			else if (o1.getActionType() == FooSimEmptyActionType.type())
				return 1;
			else if (o2.getActionType() == FooSimEmptyActionType.type())
				return -1;
			else
				return realStrategy.getActionPriorityComparator()
						.compare(realActionType(o1), realActionType(o2));
		};
	}

	private ActionTypeAndLocation realActionType(ActionTypeAndLocation o) {
		if (o.getActionType() == FooSimOthersDiscardActionType.type())
			// simdiscard应该当作draw，因为实际上是在应该draw的时候做，把draw跳过去
			return new ActionTypeAndLocation(DRAW, o.getLocation(),
					o.getContext());
		if (o.getActionType() == FooSimWinActionType.type())
			return new ActionTypeAndLocation(WIN, o.getLocation(),
					o.getContext());
		return o;
	}

	// 下面的都是直接委托

	public boolean checkReady(MahjongTable table) {
		return realStrategy.checkReady(table);
	}

	public List<Tile> getAllTiles() {
		return realStrategy.getAllTiles();
	}

	public void readyContext(GameContext context) {
		realStrategy.readyContext(context);
	}

	public Action getDealAction(GameContext context) {
		return realStrategy.getDealAction(context);
	}

	public Action getPlayerDefaultAction(GameContext context,
			PlayerLocation location, Set<ActionType> choises) {
		return realStrategy.getPlayerDefaultAction(context, location, choises);
	}

	public ActionAndLocation getDefaultAction(GameContext context,
			Map<PlayerLocation, Set<ActionType>> choises) {
		ActionAndLocation defAction = realStrategy.getDefaultAction(context,
				choises);
		// 原策略默认动作如果有，必须是流局
		if (defAction != null && !LIUJU.matchBy(defAction.getActionType()))
			throw new RuntimeException("Not def liuju action: " + defAction);
		return defAction;
	}

	public List<WinType> getAllWinTypes() {
		return realStrategy.getAllWinTypes();
	}

	public List<FanType> getAllFanTypes() {
		return realStrategy.getAllFanTypes();
	}

	public boolean tryEndGame(GameContext context) {
		return realStrategy.tryEndGame(context);
	}

}
