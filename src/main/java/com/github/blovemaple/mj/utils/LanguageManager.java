package com.github.blovemaple.mj.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.game.rule.FanType;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.TileRank;
import com.github.blovemaple.mj.object.TileSuit;

/**
 * 语言管理器，屏蔽对语言的设置。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class LanguageManager {

	private LanguageManager() {
	}

	private static final ResourceBundle resource = ResourceBundle
			.getBundle("message");

	@FunctionalInterface
	public static interface Message {
		String str();
	}

	private static final String TILE_SUIT_PREFIX = "TILE_SUIT_";
	private static final String TILE_RANK_PREFIX = "TILE_RANK_";
	private static final String ACTION_TYPE_PREFIX = "ACTION_TYPE_";
	private static final String PLAYER_LOCATION_PREFIX = "PLAYER_LOCATION_";
	private static final String PLAYER_RELATION_PREFIX = "PLAYER_RELATION_";
	private static final String FAN_TYPE_PREFIX = "FAN_TYPE_";
	private static final Map<Object, Message> messageCache = Collections
			.synchronizedMap(new HashMap<>());

	public static String str(TileSuit suit) {
		return message(suit).str();
	}

	public static String str(TileRank<?> rank) {
		return message(rank).str();
	}

	public static String str(ActionType actionType) {
		return message(actionType).str();
	}

	public static String str(PlayerLocation location) {
		return message(location).str();
	}

	public static String str(PlayerLocation.Relation relation) {
		return message(relation).str();
	}

	public static String str(FanType fanType) {
		return message(fanType).str();
	}

	public static String str(String str) {
		return str;
	}

	public static Message message(TileSuit suit) {
		return message(TILE_SUIT_PREFIX + suit.name());
	}

	public static Message message(TileRank<?> rank) {
		return message(TILE_RANK_PREFIX + rank.name());
	}

	public static Message message(ActionType actionType) {
		return message(ACTION_TYPE_PREFIX + actionType.name());
	}

	public static Message message(PlayerLocation location) {
		return message(PLAYER_LOCATION_PREFIX + location.name());
	}

	public static Message message(PlayerLocation.Relation relation) {
		return message(PLAYER_RELATION_PREFIX + relation.name());
	}

	public static Message message(FanType fanType) {
		return message(FAN_TYPE_PREFIX + fanType.name());
	}

	private static Message message(String name) {
		Message message = messageCache.get(name);
		if (message == null)
			messageCache.put(name, message = () -> ofName(name));
		return message;
	}

	private static String ofName(String name) {
		return resource.getString(name);
	}

	public static enum ExtraMessage implements Message {
		/**
		 * 发牌结束的提示
		 */
		DEAL_DONE,
		/**
		 * 听牌
		 */
		TING,
		/**
		 * 空格键
		 */
		SPACE_KEY,
		/**
		 * M键
		 */
		M_KEY,
		/**
		 * H键
		 */
		H_KEY,
		/**
		 * 斜杠（/）键
		 */
		SLASH_KEY,
		/**
		 * 逗号和句号键
		 */
		COMMA_AND_PERIOD_KEY,
		/**
		 * 移动到下一个或上一个选项
		 */
		MOVE_CHOICE,
		/**
		 * 放弃选择
		 */
		PASS,
		/**
		 * 自摸
		 */
		ZIMO,
		/**
		 * 点炮
		 */
		DIANPAO,
		/**
		 * 番的总计
		 */
		FAN_TOTLE,
		/**
		 * 番的单位
		 */
		FAN,
		/**
		 * 窗口太窄的提示
		 */
		WINDOW_TOO_NARROW,
		/**
		 * 询问是否开始新游戏，Y/N
		 */
		NEW_GAME_QUESTION;
		@Override
		public String str() {
			return LanguageManager.ofName(name());
		}

	}

}
