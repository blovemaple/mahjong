package blove.mj;

import java.util.Map;

import blove.mj.board.DiscardedTiles;
import blove.mj.board.PlayerTiles;
import blove.mj.board.Wall;
import blove.mj.rules.WinStrategy;

/**
 * 一局游戏的结果。
 * 
 * @author blovemaple
 */
public class GameResult {
	private final Map<PlayerLocation, String> playerNames;
	private final PlayerLocation dealerLocation;
	private final WinInfo winInfo;
	private final PointsResult points;
	private final Map<PlayerLocation, PlayerTiles> tiles;
	private final DiscardedTiles discardedTiles;
	private final Wall wall;

	/**
	 * 新建一个实例。
	 * 
	 * @param playerNames
	 *            所有玩家名称
	 * @param winInfo
	 *            和牌信息。null表示流局。
	 * @param dealerLocation
	 *            庄家位置
	 * @param tiles
	 *            牌
	 * @param discardedTiles
	 *            打出的牌
	 * @param wall
	 *            牌墙
	 * @param winStrategy
	 *            和牌策略
	 */
	public GameResult(Map<PlayerLocation, String> playerNames, WinInfo winInfo,
			PlayerLocation dealerLocation,
			Map<PlayerLocation, PlayerTiles> tiles,
			DiscardedTiles discardedTiles, Wall wall, WinStrategy winStrategy) {
		this.playerNames = playerNames;
		this.dealerLocation = dealerLocation;
		this.winInfo = winInfo;
		this.tiles = tiles;
		this.discardedTiles = discardedTiles;
		this.wall = wall;
		points = PointsResult.generate(tiles, dealerLocation, winInfo,
				winStrategy);
	}

	/**
	 * 返回所有玩家名称。
	 * 
	 * @return 所有玩家
	 */
	public Map<PlayerLocation, String> getPlayers() {
		return playerNames;
	}

	/**
	 * 返回庄家位置。
	 * 
	 * @return 庄家位置
	 */
	public PlayerLocation getDealerLocation() {
		return dealerLocation;
	}

	/**
	 * 返回和牌信息。
	 * 
	 * @return 和牌信息。如果流局则返回null。
	 */
	public WinInfo getWinInfo() {
		return winInfo;
	}

	/**
	 * 返回分数。
	 * 
	 * @return 分数
	 */
	public PointsResult getPoints() {
		return points;
	}

	/**
	 * 返回玩家的牌。
	 * 
	 * @return 玩家的牌
	 */
	public Map<PlayerLocation, PlayerTiles> getTiles() {
		return tiles;
	}

	/**
	 * 返回打出的牌。
	 * 
	 * @return 打出的牌
	 */
	public DiscardedTiles getDiscardedTiles() {
		return discardedTiles;
	}

	/**
	 * 返回牌墙。
	 * 
	 * @return 牌墙
	 */
	public Wall getWall() {
		return wall;
	}

	/**
	 * 和牌信息。
	 * 
	 * @author blovemaple
	 */
	public static class WinInfo {
		private final PlayerLocation winnerLocation;
		private final Tile winTile;
		private final PlayerLocation paoerLocation;

		/**
		 * 新建一个实例。
		 * 
		 * @param winnerLocation
		 *            赢家位置
		 * @param winTile
		 *            和牌
		 * @param paoerLocation
		 *            点炮者位置
		 */
		public WinInfo(PlayerLocation winnerLocation, Tile winTile,
				PlayerLocation paoerLocation) {
			this.winnerLocation = winnerLocation;
			this.winTile = winTile;
			this.paoerLocation = paoerLocation;
		}

		/**
		 * 返回赢家位置。
		 * 
		 * @return 赢家位置
		 */
		public PlayerLocation getWinnerLocation() {
			return winnerLocation;
		}

		/**
		 * 返回和牌。
		 * 
		 * @return 和牌
		 */
		public Tile getWinTile() {
			return winTile;
		}

		/**
		 * 返回点炮者位置。
		 * 
		 * @return 点炮者位置
		 */
		public PlayerLocation getPaoerLocation() {
			return paoerLocation;
		}

	}

}
