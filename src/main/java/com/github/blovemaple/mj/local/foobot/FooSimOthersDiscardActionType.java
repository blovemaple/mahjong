package com.github.blovemaple.mj.local.foobot;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;
import static com.github.blovemaple.mj.object.PlayerLocation.Relation.*;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionAndLocation;
import com.github.blovemaple.mj.action.standard.DiscardActionType;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContext.PlayerView;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * 模拟非本家的出牌动作。只用于非本家。本家忽略此动作。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class FooSimOthersDiscardActionType extends DiscardActionType {

	private static final FooSimOthersDiscardActionType i = new FooSimOthersDiscardActionType();

	/**
	 * 单例。
	 */
	public static FooSimOthersDiscardActionType type() {
		return i;
	}

	private FooSimOthersDiscardActionType() {
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
		// 上一个玩家出牌后即可进行模拟出牌，免去无意义的摸牌动作
		return (al, location) ->
		// 上一个玩家
		location.getRelationOf(al.getLocation()) == PREVIOUS
				// 出牌
				&& DISCARD.matchBy(al.getActionType());
	}

	@Override
	protected Set<Tile> getActionTilesRange(PlayerView context,
			PlayerLocation location) {
		// 牌数为0，返回null。
		return null;
	}

	@Override
	protected int getActionTilesSize() {
		// 牌的数量设为0，只要符合前提条件就可选，让context处理打出各种牌的可能性。
		return 0;
	}

	@Override
	protected boolean isLegalActionWithPreconition(PlayerView context,
			Set<Tile> tiles) {
		// 因为隐含了一个摸牌动作，因此牌墙中有牌才行，没有就要流局
		return super.isLegalActionWithPreconition(context, tiles);
	}

	@Override
	protected void doLegalAction(GameContext context, PlayerLocation location, Set<Tile> tiles) {
		super.doLegalAction(context, location, tiles);
		// 虽然免去了一个摸牌动作，但还是要在牌墙中减去一个
		((FooSimContext) context).decreaseWallSize();
	}
	
	@Override
	public boolean isLegalAction(GameContext context, PlayerLocation location, Action action) {
		return super.isLegalAction(context, location, action);
	}

}
