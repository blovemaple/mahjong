package com.github.blovemaple.mj.action.standard;

import static com.github.blovemaple.mj.object.TileGroupType.*;
import static com.github.blovemaple.mj.utils.MyUtils.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import com.github.blovemaple.mj.action.AbstractActionType;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroup;

/**
 * 动作类型“补杠”。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class BugangActionType extends AbstractActionType {

	@Override
	public boolean canPass(GameContext context, PlayerLocation location) {
		return true;
	}

	@Override
	protected Predicate<Integer> getAliveTileSizePrecondition() {
		return size -> size % 3 == 2;
	}

	@Override
	protected int getActionTilesSize() {
		return 1;
	}

	@Override
	protected boolean isLegalActionWithPreconition(GameContextPlayerView context,
			Set<Tile> tiles) {
		return findLegalPengGroup(context.getMyInfo(), tiles).isPresent();
	}

	@Override
	protected void doLegalAction(GameContext context, PlayerLocation location,
			Set<Tile> tiles) {
		PlayerInfo playerInfo = context.getPlayerInfoByLocation(location);

		TileGroup group = findLegalPengGroup(playerInfo, tiles).orElse(null);
		if (group == null)
			// tiles不合法，抛异常，因为调用此方法时应该确保是合法的
			throw new IllegalArgumentException(
					"Illegal bugang tiles: " + tiles);

		// 在aliveTiles中去掉动作牌
		playerInfo.getAliveTiles().removeAll(tiles);

		// 把碰组改为补杠组，并加上动作牌
		TileGroup newGroup = new TileGroup(BUGANG_GROUP, group.getGotTile(),
				group.getFromRelation(), mergedSet(group.getTiles(), tiles));
		List<TileGroup> groups = playerInfo.getTileGroups();
		int groupIndex = groups.indexOf(group);
		groups.remove(groupIndex);
		groups.add(groupIndex, newGroup);
	}

	/**
	 * 返回在玩家的牌中能与动作牌组成补杠的碰组（Optional）。
	 */
	private Optional<TileGroup> findLegalPengGroup(PlayerInfo playerInfo,
			Set<Tile> actionTiles) {
		return playerInfo.getTileGroups()
				// 过滤出该玩家的所有碰组
				.stream().filter(group -> group.getType() == PENG_GROUP)
				// 过滤出能与动作相关牌组成合法补杠的
				.filter(group -> {
					// 取出碰组的牌，并加上动作中的tiles（应该只有一个tile）
					Set<Tile> gangTiles = mergedSet(group.getTiles(),
							actionTiles);
					// 只留下合法的（补）杠
					return BUGANG_GROUP.isLegalTiles(gangTiles);
				})
				// 取任何一个（有的话肯定只有一个）
				.findAny();
	}

}
