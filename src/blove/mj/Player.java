package blove.mj;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 玩家。
 * 
 * @author blovemaple
 */
public abstract class Player {
	private static AtomicInteger nextId = new AtomicInteger();

	private int id = nextId.getAndIncrement();
	private final String name;

	/**
	 * 新建一个实例。
	 * 
	 * @param name
	 *            名字
	 */
	public Player(String name) {
		this.name = name;
	}

	/**
	 * 返回名字。
	 * 
	 * @return 名字
	 */
	public String getName() {
		return name;
	}

	/**
	 * 等待玩家准备时调用。
	 * 
	 * @param playerView
	 *            玩家视图
	 */
	public abstract void forReady(PlayerView playerView);

	/**
	 * 从机会中选择吃/碰/杠/和牌。<br>
	 * 注意：如果选择已无必要，比如优先级更高的吃/碰/杠/和牌被别的玩家确定，则此方法所在的线程将被中断。所以此方法中应该随时检测线程是否被中断，
	 * 若被中断则抛出 {@link InterruptedException}。
	 * 
	 * @param playerView
	 *            玩家视图
	 * @param cpkwChances
	 *            吃/碰/杠/和机会
	 * @param newTile
	 *            其他玩家打出的牌，或自己刚摸的牌
	 * @param drawed
	 *            newTile是否是自己刚摸的牌
	 * @return 选择。如果放弃，返回null。
	 * @throws InterruptedException
	 *             被中断
	 */
	public abstract CpkwChoice chooseCpkw(PlayerView playerView,
			Set<CpkwChoice> cpkwChances, Tile newTile, boolean drawed)
			throws InterruptedException;

	/**
	 * 吃/碰/杠/和选择。
	 * 
	 * @author blovemaple
	 */
	public static class CpkwChoice {
		public final Cpk cpk;
		public final boolean win;

		/**
		 * 新建一个吃/碰/杠选择。
		 * 
		 * @param cpk
		 *            吃/碰/杠
		 * @return 选择
		 * @throws NullPointerException
		 *             cpk是null
		 */
		public static CpkwChoice chooseCpk(Cpk cpk) {
			if (cpk == null)
				throw new NullPointerException();
			return new CpkwChoice(cpk, false);
		}

		/**
		 * 新建一个和牌选择。
		 * 
		 * @return 选择
		 */
		public static CpkwChoice chooseWin() {
			return new CpkwChoice(null, true);
		}

		private CpkwChoice(Cpk cpk, boolean win) {
			this.cpk = cpk;
			this.win = win;
		}
	}

	/**
	 * 选择一张牌打出。如果要听牌，则调用{@link #readyHand()}方法。<br>
	 * 注意：如果选择已无必要，则此方法所在的线程将被中断。所以此方法中应该随时检测线程是否被中断， 若被中断则抛出
	 * {@link InterruptedException}。
	 * 
	 * @param playerView
	 *            玩家视图
	 * @param readyHandTypes
	 *            打出后可以听牌的牌型
	 * @param drawedTile
	 *            刚摸的牌。null表示吃/碰之后。
	 * @return 选择
	 * @throws InterruptedException
	 *             被中断
	 */
	public abstract DiscardChoice chooseDiscard(PlayerView playerView,
			Set<TileType> readyHandTypes, Tile drawedTile)
			throws InterruptedException;

	/**
	 * 出牌选择。
	 * 
	 * @author blovemaple
	 */
	public static class DiscardChoice {
		public final Tile discardTile;
		public final boolean readyHand;

		/**
		 * 新建一个选择。
		 * 
		 * @param discardTile
		 *            打出的牌
		 * @param readyHand
		 *            是否听牌
		 * @throws NullPointerException
		 *             discardTile是null
		 */
		public DiscardChoice(Tile discardTile, boolean readyHand) {
			if (discardTile == null)
				throw new NullPointerException();
			this.discardTile = discardTile;
			this.readyHand = readyHand;
		}

	}

	/**
	 * 玩家即将离开游戏桌时调用。
	 * 
	 * @param playerView
	 *            玩家视图
	 */
	public abstract void forLeaving(PlayerView playerView);

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Player))
			return false;
		Player other = (Player) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
