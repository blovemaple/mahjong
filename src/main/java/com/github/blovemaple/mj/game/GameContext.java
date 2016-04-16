package com.github.blovemaple.mj.game;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionAndLocation;
import com.github.blovemaple.mj.game.rule.GameStrategy;
import com.github.blovemaple.mj.object.MahjongTable;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * 一局游戏进行中的上下文信息。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class GameContext {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(GameContext.class.getSimpleName());

	private final MahjongTable table;
	private GameStrategy gameStrategy;
	private PlayerLocation zhuangLocation;
	private List<ActionAndLocation> doneActions = new ArrayList<>();
	private GameResult gameResult;

	public GameContext(MahjongTable table, GameStrategy gameStrategy) {
		this.table = table;
		this.gameStrategy = gameStrategy;
	}

	public MahjongTable getTable() {
		return table;
	}

	public GameStrategy getGameStrategy() {
		return gameStrategy;
	}

	public PlayerInfo getPlayerInfoByLocation(PlayerLocation location) {
		return table.getPlayerInfos().get(location);
	}

	public PlayerLocation getZhuangLocation() {
		return zhuangLocation;
	}

	public void setZhuangLocation(PlayerLocation zhuangLocation) {
		this.zhuangLocation = zhuangLocation;
	}

	public void actionDone(Action action, PlayerLocation location) {
		doneActions.add(new ActionAndLocation(action, location));
	}

	/**
	 * 返回到目前为止做出的最后一个动作和玩家位置。
	 */
	public ActionAndLocation getLastActionAndLocation() {
		return doneActions.isEmpty() ? null
				: doneActions.get(doneActions.size() - 1);
	}

	/**
	 * 返回到目前为止做出的最后一个动作。
	 */
	public Action getLastAction() {
		ActionAndLocation lastAction = getLastActionAndLocation();
		return lastAction == null ? null : lastAction.getAction();
	}

	/**
	 * 返回到目前为止做出的最后一个动作的玩家位置。
	 */
	public PlayerLocation getLastActionLocation() {
		ActionAndLocation lastAction = getLastActionAndLocation();
		return lastAction == null ? null : lastAction.getLocation();
	}

	public List<ActionAndLocation> getDoneActions() {
		return doneActions;
	}

	public GameResult getGameResult() {
		return gameResult;
	}

	public void setGameResult(GameResult gameResult) {
		this.gameResult = gameResult;
	}

	private final Map<PlayerLocation, PlayerView> playerViews = new HashMap<>();

	/**
	 * 获取指定位置的玩家视图。
	 */
	public PlayerView getPlayerView(PlayerLocation location) {
		PlayerView view = playerViews.get(location);
		if (view == null) { // 不需要加锁，因为多创建了也没事
			view = new PlayerView(location);
			playerViews.put(location, view);
		}
		return view;
	}

	/**
	 * 一个位置的玩家的视图。需要限制一些权限。
	 * 
	 * @author blovemaple <blovemaple2010(at)gmail.com>
	 */
	public class PlayerView {

		private final PlayerLocation myLocation;

		private PlayerView(PlayerLocation myLocation) {
			this.myLocation = myLocation;
		}

		public MahjongTable.PlayerView getTableView() {
			return table.getPlayerView(myLocation);
		}

		public GameStrategy getGameStrategy() {
			return gameStrategy;
		}

		public PlayerLocation getMyLocation() {
			return myLocation;
		}

		public PlayerInfo getMyInfo() {
			return getPlayerInfoByLocation(myLocation);
		}

		public PlayerLocation getZhuangLocation() {
			return zhuangLocation;
		}

		/**
		 * 返回到目前为止做出的最后一个动作和玩家位置。
		 */
		public ActionAndLocation getLastActionAndLocation() {
			return doneActions.isEmpty() ? null
					: doneActions.get(doneActions.size() - 1);
		}

		/**
		 * 返回到目前为止做出的最后一个动作。
		 */
		public Action getLastAction() {
			ActionAndLocation lastAction = getLastActionAndLocation();
			return lastAction == null ? null : lastAction.getAction();
		}

		/**
		 * 返回到目前为止做出的最后一个动作的玩家位置。
		 */
		public PlayerLocation getLastActionLocation() {
			ActionAndLocation lastAction = getLastActionAndLocation();
			return lastAction == null ? null : lastAction.getLocation();
		}

		/**
		 * 如果刚刚摸牌，则返回刚摸的牌，否则返回null。
		 */
		public Tile getJustDrawedTile() {
			ActionAndLocation laa = getLastActionAndLocation();
			if (laa.getLocation() != myLocation) {
				return null;
			}
			if (!DRAW.matchBy(laa.getAction().getType())) {
				return null;
			}
			return getMyInfo().getLastDrawedTile();
		}

		public List<ActionAndLocation> getDoneActions() {
			return doneActions;
		}

		public GameResult getGameResult() {
			return gameResult;
		}

	}

}
