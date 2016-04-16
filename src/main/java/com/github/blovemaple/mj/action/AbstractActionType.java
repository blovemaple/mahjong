package com.github.blovemaple.mj.action;

import static com.github.blovemaple.mj.utils.MyUtils.*;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameContext.PlayerView;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * 各种ActionType的共同逻辑。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public abstract class AbstractActionType implements ActionType {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(AbstractActionType.class.getSimpleName());

	/**
	 * 先使用{@link #meetPrecondition}检查前提条件，如果满足再调用{@link #canDoWithPrecondition}
	 * 。
	 * 
	 * @see com.github.blovemaple.mj.action.ActionType#canDo(com.github.blovemaple.mj.game.GameContext,
	 *      com.github.blovemaple.mj.object.PlayerLocation)
	 */
	@Override
	public boolean canDo(GameContext context, PlayerLocation location) {
		if (!meetPrecondition(context.getPlayerView(location)))
			return false;
		return canDoWithPrecondition(context, location);
	}

	/**
	 * 判断当前状态下指定玩家是否符合做出此类型动作的前提条件（比如“碰”的前提条件是别人刚出牌）。<br>
	 * 默认实现调用相应方法对上一个动作和活牌数量进行限制，进行判断。
	 */
	protected boolean meetPrecondition(GameContext.PlayerView context) {
		// aliveTiles数量
		Predicate<Integer> aliveTileSizeCondition = getAliveTileSizePrecondition();
		if (aliveTileSizeCondition != null)
			if (!aliveTileSizeCondition
					.test(context.getMyInfo().getAliveTiles().size()))
				return false;

		// 上一个动作
		Collection<ActionTypeAndLocation> lastActionPrecondition = getLastActionPrecondition(
				context.getMyLocation());
		if (lastActionPrecondition != null
				&& !lastActionPrecondition.isEmpty()) {
			Action lastAction = context.getLastAction();
			PlayerLocation lastLocation = context.getLastActionLocation();
			if (lastAction == null)
				return false;
			if (lastActionPrecondition.stream()
					.noneMatch(atl -> atl.getActionType()
							.matchBy(lastAction.getType())
							&& (atl.getLocation() == null
									|| atl.getLocation() == lastLocation)))
				return false;
		}

		return true;
	}

	/**
	 * 返回进行此类型动作时对上一个动作的限制条件。即：上一个动作符合返回的集合中任意一种ActionType和PlayerLocation的组合时，
	 * 才算满足做出此类型动作的前提条件。<br>
	 * 返回的ActionType是实际type的super类也可以。<br>
	 * 返回的PlayerLocation为null者表示不限制玩家位置。<br>
	 * 此方法用于{@link #meetPrecondition}的默认实现。
	 */
	protected Collection<ActionTypeAndLocation> getLastActionPrecondition(
			PlayerLocation location) {
		return null;
	}

	/**
	 * 返回进行此类型动作时对对活牌数量的限制条件。<br>
	 * 返回null表示不限制。<br>
	 * 此方法用于{@link #meetPrecondition}的默认实现。
	 */
	protected Predicate<Integer> getAliveTileSizePrecondition() {
		return null;
	}

	/**
	 * 判断指定状态下指定位置的玩家可否做此种类型的动作。调用此方法之前已经使用{@link #meetPrecondition}判断过符合前提条件。
	 * <br>
	 * 默认实现为：判断{@link #legalActionTilesStream}返回的流不为空。
	 */
	protected boolean canDoWithPrecondition(GameContext context,
			PlayerLocation location) {
		return legalActionTilesStream(context.getPlayerView(location)).findAny()
				.isPresent();
	}

	/**
	 * 调用{@link #legalActionTilesStream}并收集为Set返回。
	 * 
	 * @see com.github.blovemaple.mj.action.ActionType#getLegalActionTiles(com.github.blovemaple.mj.game.GameContext)
	 */
	@Override
	public Collection<Set<Tile>> getLegalActionTiles(
			GameContext.PlayerView context) {
		return legalActionTilesStream(context).collect(Collectors.toSet());
	}

	/**
	 * {@inheritDoc}<br>
	 * 默认实现为将action为null的和动作类型不符合的报异常，然后用{@link #isLegalActionTiles}检查是否合法。
	 * 
	 * @see com.github.blovemaple.mj.action.ActionType#isLegalAction(com.github.blovemaple.mj.game.GameContext,
	 *      com.github.blovemaple.mj.object.PlayerLocation,
	 *      com.github.blovemaple.mj.action.Action)
	 */
	@Override
	public boolean isLegalAction(GameContext context, PlayerLocation location,
			Action action) {
		Objects.requireNonNull(action);
		if (!matchBy(action.getType()))
			throw new IllegalArgumentException(
					action.getType().getRealTypeClass().getSimpleName()
							+ " is not " + getRealTypeClass());
		if (!isLegalActionTiles(context.getPlayerView(location),
				action.getTiles()))
			return false;
		return true;
	}

	/**
	 * {@inheritDoc}<br>
	 * 默认实现为用{@link #isLegalAction}检查是否合法，如果合法则调用{@link #doLegalAction}执行动作。
	 * 
	 * @see com.github.blovemaple.mj.action.ActionType#doAction(com.github.blovemaple.mj.game.GameContext,
	 *      com.github.blovemaple.mj.object.PlayerLocation,
	 *      com.github.blovemaple.mj.action.Action)
	 */
	@Override
	public void doAction(GameContext context, PlayerLocation location,
			Action action) throws IllegalActionException {
		if (!isLegalAction(context, location, action)) {
			throw new IllegalActionException();
		}

		doLegalAction(context, location, action.getTiles());
	}

	/**
	 * 返回一个流，流中包含指定状态下指定玩家可作出的此类型的所有合法动作的相关牌集合。<br>
	 * 默认实现为：在玩家手中的牌中选取所有合法数量个牌的组合，并使用{@link #isLegalActionTiles}过滤出合法的组合。<br>
	 * 如果合法的相关牌不限于手中的牌，则需要子类重写此方法。
	 */
	protected Stream<Set<Tile>> legalActionTilesStream(
			GameContext.PlayerView context) {
		PlayerInfo playerInfo = context.getMyInfo();
		if (playerInfo == null)
			return Stream.empty();
		return combinationStream(
				getActionTilesRange(context, context.getMyLocation()),
				getActionTilesSize())
						.filter(tiles -> isLegalActionTiles(context, tiles));
	}

	/**
	 * 返回合法动作中相关牌的可选范围。<br>
	 * 默认实现为指定玩家的aliveTiles。
	 */
	protected Set<Tile> getActionTilesRange(GameContext.PlayerView context,
			PlayerLocation location) {
		return context.getMyInfo().getAliveTiles();
	}

	/**
	 * 返回合法动作中相关牌的数量。可以为0。
	 */
	protected abstract int getActionTilesSize();

	/**
	 * 判断动作是否合法。<br>
	 * 默认实现为：先检查前提条件、相关牌数量、相关牌范围，如果满足再调用{@link #isLegalActionWithPreconition}。
	 */
	protected boolean isLegalActionTiles(GameContext.PlayerView context,
			Set<Tile> tiles) {
		PlayerLocation location = context.getMyLocation();
		if (!meetPrecondition(context)) {
			return false;
		}

		int legalTilesSize = getActionTilesSize();
		if (legalTilesSize > 0
				&& (tiles == null || tiles.size() != legalTilesSize)) {
			return false;
		}

		Set<Tile> ligalTilesRange = getActionTilesRange(context, location);
		if (tiles != null && ligalTilesRange != null
				&& !ligalTilesRange.containsAll(tiles)) {
			return false;
		}

		boolean legal = isLegalActionWithPreconition(context, tiles);
		return legal;
	}

	/**
	 * 判断动作是否合法。调用此方法之前已经判断确保符合前提条件、相关牌数量、相关牌范围。
	 */
	protected abstract boolean isLegalActionWithPreconition(PlayerView context,
			Set<Tile> tiles);

	/**
	 * 执行动作。调用此方法之前已经确保符合动作类型，并使用{@link #isLegalActionTiles}判断过动作的合法性。
	 */
	protected abstract void doLegalAction(GameContext context,
			PlayerLocation location, Set<Tile> tiles);

}
