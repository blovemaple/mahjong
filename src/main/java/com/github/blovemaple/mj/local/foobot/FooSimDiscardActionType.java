package com.github.blovemaple.mj.local.foobot;

import static com.github.blovemaple.mj.object.PlayerLocation.Relation.*;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.github.blovemaple.mj.action.AbstractActionType;
import com.github.blovemaple.mj.action.ActionAndLocation;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContext.PlayerView;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * TODO 模拟非本家的出牌动作。只用于非本家。本家忽略此动作。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class FooSimDiscardActionType extends AbstractActionType {

	private static final FooSimDiscardActionType i = new FooSimDiscardActionType();

	/**
	 * 单例。
	 */
	public static FooSimDiscardActionType type() {
		return i;
	}

	private FooSimDiscardActionType() {
	}

	@Override
	public boolean canPass(GameContext context, PlayerLocation location) {
		// 非本家执行，本家不执行（本家出牌执行正常的出牌动作）
		return true;
	}

	@Override
	protected Predicate<Integer> getAliveTileSizePrecondition() {
		// 对aliveTiles数量没有限制（模拟时其他玩家没有）
		return s -> true;
	}

	@Override
	protected BiPredicate<ActionAndLocation, PlayerLocation> getLastActionPrecondition() {
		// 上一个玩家出牌后即可进行模拟出牌，免去无意义的摸牌的动作
		return (al, location) -> location.getRelationOf(al.getLocation()) == PREVIOUS;
	}

	@Override
	protected Set<Tile> getActionTilesRange(PlayerView context, PlayerLocation location) {
		// TODO 可以模拟所有未出现的牌
		return super.getActionTilesRange(context, location);
	}

	@Override
	protected int getActionTilesSize() {
		return 1;
	}

	@Override
	protected boolean isLegalActionWithPreconition(PlayerView context, Set<Tile> tiles) {
		return true;
	}

	@Override
	protected void doLegalAction(GameContext context, PlayerLocation location, Set<Tile> tiles) {
		// TODO 模拟出牌
	}

}
