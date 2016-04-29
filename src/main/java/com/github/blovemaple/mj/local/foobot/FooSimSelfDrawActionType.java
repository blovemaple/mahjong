package com.github.blovemaple.mj.local.foobot;

import java.util.Set;

import com.github.blovemaple.mj.action.standard.DrawActionType;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContext.PlayerView;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * 模拟本家摸牌（包括摸底牌）动作。不用于gameTool选择，只用于执行，可以指定摸的牌。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class FooSimSelfDrawActionType extends DrawActionType {

	private static final FooSimSelfDrawActionType i = new FooSimSelfDrawActionType();

	/**
	 * 单例。
	 */
	public static FooSimSelfDrawActionType type() {
		return i;
	}

	private FooSimSelfDrawActionType() {
	}

	@Override
	public boolean canDo(GameContext context, PlayerLocation location) {
		// 不用于gameTool选择
		return false;
	}

	@Override
	protected Set<Tile> getActionTilesRange(PlayerView context,
			PlayerLocation location) {
		// 不规定范围
		return null;
	}

	@Override
	protected int getActionTilesSize() {
		return 1;
	}

	@Override
	protected boolean isLegalActionWithPreconition(PlayerView context,
			Set<Tile> tiles) {
		// 仍然是牌墙中有牌才能摸，没有就要流局
		return super.isLegalActionWithPreconition(context, tiles);
	}

	@Override
	protected void doLegalAction(GameContext context, PlayerLocation location, Set<Tile> tiles) {
		// 就不判断tiles数量了，但必须是1个
		((FooSimContext) context).decreaseWallSize();
		context.getPlayerInfoByLocation(location).getAliveTiles().addAll(tiles);
		context.getPlayerInfoByLocation(location)
				.setLastDrawedTile(tiles.iterator().next());
	}

}
