package blove.mj.local;

import java.util.HashSet;
import java.util.LinkedList;

import blove.mj.Cpk;
import blove.mj.CpkType;
import blove.mj.PlayerLocation.Relation;
import blove.mj.Tile;
import blove.mj.board.PlayerTiles;

/**
 * 本地游戏桌上一个玩家的牌。
 * 
 * @author blovemaple
 */
class LocalPlayerTiles extends PlayerTiles {
	/**
	 * 新建一个实例。
	 */
	public LocalPlayerTiles() {
		aliveTiles = new HashSet<>();
		cpks = new LinkedList<>();
	}

	/**
	 * 初始化（清空）。
	 */
	public void init() {
		aliveTiles.clear();
		cpks.clear();
		readyHandDiscardTile = null;
	}

	/**
	 * 添加一张牌。
	 * 
	 * @param tile
	 *            牌
	 * @throws IllegalArgumentException
	 *             已存在
	 */
	public void addTile(Tile tile) {
		boolean added = aliveTiles.add(tile);
		if (!added)
			throw new IllegalArgumentException("已存在：" + tile);
	}

	/**
	 * 移除一张牌。如果指定了听牌，则此牌将被记录到听牌时打出的牌。
	 * 
	 * @param tile
	 *            牌
	 * @param readyHand
	 *            是否听牌
	 * @throws IllegalArgumentException
	 *             不存在
	 */
	public void removeTile(Tile tile, boolean readyHand) {
		if (!aliveTiles.contains(tile))
			throw new IllegalArgumentException("不存在：" + tile);
		aliveTiles.remove(tile);
		if (readyHand)
			readyHandDiscardTile = tile;
	}

	/**
	 * 检查此时是否可以进行指定的吃/碰/杠。<br>
	 * 对于暗杠，此方法检查暗杠牌组中的牌是否都在本玩家的aliveTiles中；对于自摸后的明杠，此方法检查其余牌是否是本玩家手中的杠牌；对于其他，
	 * 此方法检查其余牌是否是此玩家手中非吃/碰/杠牌组中的牌。
	 * 
	 * @param cpk
	 *            吃/碰/杠
	 * @return 如果可以，返回true；否则返回false。
	 */
	private boolean isCpkValid(Cpk cpk) {
		if (cpk.getType() == CpkType.EXPOSED_KONG
				&& cpk.getFromRelation() == Relation.SELF) {
			for (Cpk myCpk : cpks) {
				if (myCpk.getType() == CpkType.PONG
						&& myCpk.getForTile().getType()
								.equals(cpk.getForTile().getType())
						&& cpk.getTiles().containsAll(myCpk.getTiles())) {
					return true;
				}
			}
			return false;
		} else {
			for (Tile tile : cpk.getTiles())
				if (!tile.equals(cpk.getForTile())
						&& !aliveTiles.contains(tile))
					return false;
			return true;
		}
	}

	/**
	 * 添加一个吃/碰/杠。此方法自动管理aliveTiles，移除吃/碰/杠中的牌。
	 * 
	 * @param cpk
	 *            吃/碰/杠
	 * @throws IllegalArgumentException
	 *             当前不可以进行指定的吃/碰/杠动作
	 */
	public void importCpk(Cpk cpk) {
		if (!isCpkValid(cpk))
			throw new IllegalArgumentException("当前不可以进行动作：" + cpk);
		if (cpk.getType() == CpkType.EXPOSED_KONG
				&& cpk.getFromRelation() == Relation.SELF) {
			for (Cpk myCpk : cpks) {
				if (myCpk.getType() == CpkType.PONG
						&& myCpk.getForTile().getType()
								.equals(cpk.getForTile().getType())
						&& cpk.getTiles().containsAll(myCpk.getTiles())) {
					cpks.set(cpks.indexOf(myCpk), cpk);
					break;
				}
			}
			aliveTiles.remove(cpk.getForTile());
		} else {
			for (Tile tile : cpk.getTiles())
				aliveTiles.remove(tile);
			cpks.add(cpk);
		}
	}

}
