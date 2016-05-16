package com.github.blovemaple.mj.action.standard;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;
import static com.github.blovemaple.mj.utils.MyUtils.*;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.blovemaple.mj.action.AbstractActionType;
import com.github.blovemaple.mj.action.ActionAndLocation;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContext.PlayerView;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.PlayerLocation.Relation;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroup;
import com.github.blovemaple.mj.object.TileGroupType;

/**
 * 吃、碰、直杠动作类型的统一逻辑。<br>
 * 这类动作的共同点是：
 * <li>都可以放弃；
 * <li>前提条件都是别的玩家出牌后；
 * <li>都是从特定关系的玩家的出牌中得牌，并组成一种group。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CpgActionType extends AbstractActionType {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(CpgActionType.class.getSimpleName());

	private TileGroupType groupType;
	private Collection<PlayerLocation.Relation> lastActionRelations;

	/**
	 * 新建实例。
	 * 
	 * @param groupType
	 *            组成的牌组类型
	 * @param lastActionRelations
	 *            限制上一个动作的玩家（出牌者）与当前玩家的位置关系
	 */
	public CpgActionType(TileGroupType groupType,
			Collection<Relation> lastActionRelations) {
		Objects.requireNonNull(groupType);
		this.groupType = groupType;
		this.lastActionRelations = lastActionRelations != null
				? lastActionRelations
				: Stream.of(Relation.values()).filter(Relation::isOther)
						.collect(Collectors.toList());
	}

	/**
	 * 新建实例。上一个动作的玩家（出牌者）与当前玩家的位置关系是所有其他人。
	 * 
	 * @param groupType
	 *            组成的牌组类型
	 */
	public CpgActionType(TileGroupType groupType) {
		this(groupType, null);
	}

	@Override
	public boolean canPass(GameContext context, PlayerLocation location) {
		return true;
	}

	@Override
	protected BiPredicate<ActionAndLocation, PlayerLocation> getLastActionPrecondition() {
		// 必须是指定关系的人出牌后
		return (al, location) -> DISCARD.matchBy(al.getActionType())
				&& lastActionRelations
						.contains(location.getRelationOf(al.getLocation()));
	}

	@Override
	protected int getActionTilesSize() {
		return groupType.size() - 1;
	}

	@Override
	protected boolean isLegalActionWithPreconition(PlayerView context,
			Set<Tile> tiles) {
		Set<Tile> testTiles = mergedSet(tiles,
				context.getLastAction().getTile());
		boolean legal = groupType.isLegalTiles(testTiles);
		return legal;
	}

	@Override
	protected void doLegalAction(GameContext context, PlayerLocation location,
			Set<Tile> tiles) {
		PlayerInfo playerInfo = context.getPlayerInfoByLocation(location);

		playerInfo.getAliveTiles().removeAll(tiles);

		Tile gotTile = context.getLastAction().getTile();
		TileGroup group = new TileGroup(groupType, gotTile,
				location.getRelationOf(context.getLastActionLocation()),
				mergedSet(tiles, gotTile));
		playerInfo.getTileGroups().add(group);
	}

	/**
	 * 如果此类与testType的真正类是从属关系，并且testType的groupType与此对象相同，则视为match。
	 * 
	 * @see com.github.blovemaple.mj.action.AbstractActionType#matchBy(com.github.blovemaple.mj.action.ActionType)
	 */
	@Override
	public boolean matchBy(ActionType testType) {
		if (!CpgActionType.class.isAssignableFrom(testType.getRealTypeClass()))
			return false;
		if (!groupType.equals(
				((CpgActionType) testType.getRealTypeObject()).groupType))
			return false;
		return true;
	}

}
