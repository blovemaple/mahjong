package com.github.blovemaple.mj.rule;

import static com.github.blovemaple.mj.action.standard.AutoActionTypes.*;
import static com.github.blovemaple.mj.action.standard.PlayerActionTypes.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.action.ActionTypeAndLocation;
import com.github.blovemaple.mj.action.PlayerAction;
import com.github.blovemaple.mj.action.PlayerActionType;
import com.github.blovemaple.mj.action.standard.PlayerActionTypes;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.object.MahjongTable;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.PlayerLocation.Relation;
import com.github.blovemaple.mj.object.Tile;

/**
 * 基本的常用的规则。继承此类，实现抽象方法，可以实现一种特定的游戏规则，包括坐庄规则、和牌限制、和牌类型、番种定义等。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public abstract class AbstractGameStrategy implements GameStrategy {

	/**
	 * {@inheritDoc}<br>
	 * 桌上所有位置都有玩家就返回true。
	 * 
	 * @see com.github.blovemaple.mj.rule.GameStrategy#checkReady(com.github.blovemaple.mj.object.MahjongTable)
	 */
	@Override
	public boolean checkReady(MahjongTable table) {
		return Stream.of(PlayerLocation.values())
				.map(table::getPlayerByLocation).allMatch(Objects::nonNull);
	}

	@Override
	public List<Tile> getAllTiles() {
		return Tile.all();
	}

	/**
	 * {@inheritDoc}<br>
	 * 在一局开始之前设置庄家位置。
	 * 
	 * @see com.github.blovemaple.mj.rule.GameStrategy#readyContext(com.github.blovemaple.mj.game.GameContext)
	 */
	@Override
	public void readyContext(GameContext context) {
		context.setZhuangLocation(nextZhuangLocation(context));
	}

	/**
	 * 在一局开始之前，根据context返回此局的庄家位置。
	 */
	protected abstract PlayerLocation nextZhuangLocation(GameContext context);

	@Override
	public Action getDealAction(GameContext context) {
		return new Action(DEAL);
	}

	private static final Set<PlayerActionType> ALL_ACTION_TYPES = new HashSet<>(
			Arrays.asList(PlayerActionTypes.values()));

	@Override
	public Set<PlayerActionType> getAllActionTypesInGame() {
		return ALL_ACTION_TYPES;
	}

	@Override
	public Set<PlayerActionType> getAllActionTypesInTing() {
		return Set.of(DRAW, DISCARD, BUHUA, DRAW_BOTTOM, WIN);
	}

	/**
	 * 动作类型优先级倒序，先低后高，不在列表里的为最低。<br>
	 * 进行比较时反过来用index比较，不在列表里的为-1。
	 */
	private static final List<ActionType> ACTION_TYPE_PRIORITY_LIST = Arrays
			.asList(CHI, PENG, ZHIGANG, BUHUA, WIN);

	/**
	 * 和>补花>杠>碰>吃>其他，相同的比较与上次动作的玩家位置关系。
	 * 
	 * @see com.github.blovemaple.mj.rule.GameStrategy#getActionPriorityComparator()
	 */
	@Override
	public Comparator<ActionTypeAndLocation> getActionPriorityComparator() {
		Comparator<ActionTypeAndLocation> c = Comparator.comparing(
				atl -> ACTION_TYPE_PRIORITY_LIST.indexOf(atl.getActionType()));
		c = c.reversed();
		c = c.thenComparing(a -> {
			PlayerLocation lastLocation = a.getContext()
					.getLastActionLocation();
			return lastLocation == null ? Relation.SELF
					: lastLocation.getRelationOf(a.getLocation());
		});
		return c;
	}

	@Override
	public PlayerAction getPlayerDefaultAction(GameContext context,
			PlayerLocation location, Set<PlayerActionType> choises) {
		if (choises.contains(DRAW))
			return new PlayerAction(location, DRAW);
		if (choises.contains(DRAW_BOTTOM))
			return new PlayerAction(location, DRAW_BOTTOM);
		if (choises.contains(DISCARD)) {
			Tile tileToDiscard;
			Action lastAction = context.getLastAction();
			if (context.getLastActionLocation() == location
					&& DRAW.matchBy(lastAction.getType())) {
				tileToDiscard = ((PlayerAction) lastAction).getTile();
			} else {
				tileToDiscard = context.getPlayerInfoByLocation(location)
						.getAliveTiles().iterator().next();
			}
			return new PlayerAction(location, DISCARD, tileToDiscard);
		}
		return null;
	}

	@Override
	public Action getDefaultAction(GameContext context,
			Map<PlayerLocation, Set<PlayerActionType>> choises) {
		if (context.getTable().getTileWallSize() == 0)
			return new Action(LIUJU);
		else
			return null;
	}

	@Override
	public boolean tryEndGame(GameContext context) {
		return context.getGameResult() != null;
	}

}
