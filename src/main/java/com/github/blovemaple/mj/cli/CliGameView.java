package com.github.blovemaple.mj.cli;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;
import static com.github.blovemaple.mj.utils.LambdaUtils.*;
import static com.github.blovemaple.mj.utils.LanguageManager.*;
import static com.github.blovemaple.mj.utils.LanguageManager.ExtraMessage.*;
import static com.github.blovemaple.mj.utils.MyUtils.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionAndLocation;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.action.standard.StandardActionType;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.game.GameResult;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroup;
import com.github.blovemaple.mj.object.TileGroupType;
import com.github.blovemaple.mj.object.TileRank;
import com.github.blovemaple.mj.object.TileRank.NumberRank;
import com.github.blovemaple.mj.rule.FanType;
import com.github.blovemaple.mj.object.TileSuit;
import com.github.blovemaple.mj.utils.LanguageManager.Message;
import com.github.blovemaple.mj.utils.MyUtils;

/**
 * 提供向CliView提供展示游戏信息的便捷方法。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliGameView {
	private final CliView cliView;

	private GameContext.PlayerView contextView;
	private PlayerInfo playerInfo;
	private Set<Tile> focusedAliveTiles;
	private Set<TileGroup> focusedGroups;
	private Map<Message, Message> options; // 按键->说明
	private Integer timeLimit;

	/**
	 * 牌的比较器，定义牌的显示顺序。
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Comparator<Tile> TILE_COMPARATOR = Comparator
			// 先按花色
			.<Tile, TileSuit> comparing(tile -> tile.type().suit())
			// 再按种类
			.<TileRank> thenComparing(tile -> tile.type().rank())
			// 再按ID
			.<Integer> thenComparing(tile -> tile.id());

	private static final char GROUP_START_STR = '<', GROUP_END_STR = '>';
	private static final char FOCUS_START_STR = '[', FOCUS_END_STR = ']';
	// 用于听牌状态
	private static final char DESC_START_STR = '(', DESC_END_STR = ')';
	private static final char OPTION_START_STR = '<', OPTION_END_STR = '>';
	private static final char OPTION_MIDDLE_STR = ':';
	private static final char TIME_START_STR = '(', TIME_END_STR = ')';
	private static final char TING_START_STR = '(', TING_END_STR = ')';

	private static final int POSITION_WIDTH = 20;

	/**
	 * 新建一个实例。
	 * 
	 * @param cliView
	 *            CliView
	 */
	CliGameView(CliView cliView) {
		this.cliView = cliView;
	}

	public CliView getCliView() {
		return cliView;
	}

	public void setContext(GameContext.PlayerView contextView)
			throws IOException {
		cliView.init();
		focusedAliveTiles = Collections.emptySet();
		focusedGroups = Collections.emptySet();
		options = Collections.emptyMap();
		timeLimit = null;
		this.contextView = contextView;
		this.playerInfo = contextView.getMyInfo();
		updateTileLine();
	}

	public void setFocusedAliveTiles(Set<Tile> focusedAliveTiles) {
		this.focusedAliveTiles = focusedAliveTiles != null ? focusedAliveTiles
				: Collections.emptySet();
		updateTileLine();
	}

	public void setFocusedGroups(Set<TileGroup> focusedGroups) {
		this.focusedGroups = focusedGroups != null ? focusedGroups
				: Collections.emptySet();
		updateTileLine();
	}

	public void setOptions(Map<Message, Message> options) {
		this.options = options != null ? options : Collections.emptyMap();
		updateTileLine();
	}

	public void setTimeLimit(Integer timeLimit) {
		this.timeLimit = timeLimit;
		updateTileLine();
	}

	private void updateTileLine() {
		if (contextView == null) {
			cliView.updateStatus("Waiting...");
			return;
		}

		// groups aliveTiles options timelimit
		StringBuilder str = new StringBuilder();
		appendGroups(str);
		appendAliveTiles(str);
		appendTingStatus(str);
		appendOptions(str);
		appendTimeLimit(str);
		cliView.updateStatus(str.toString());
	}

	private void appendGroups(StringBuilder str) {
		if (playerInfo == null)
			return;
		appendGroups(str, playerInfo.getTileGroups(), focusedGroups);
	}

	private static void appendGroups(StringBuilder str,
			List<TileGroup> tileGroups, Set<TileGroup> focusedGroups) {

		// <MZ> <345W> [789T] <2T4W5W>[空格] (最后一种是容错，实际不应该存在这样的)

		List<Tile> huas = tileGroups.stream()
				.filter(group -> group.getType() == TileGroupType.BUHUA_GROUP)
				.flatMap(group -> group.getTiles().stream())
				.collect(Collectors.toList());
		if (!huas.isEmpty()) {
			str.append(GROUP_START_STR);
			huas.forEach(hua -> str.append(str(hua.type().rank())));
			str.append(GROUP_END_STR);
			str.append(' ');
		}

		tileGroups.stream()
				.filter(group -> group.getType() != TileGroupType.BUHUA_GROUP)
				.forEach(group -> {
					List<Tile> groupTiles = group.getTiles().stream()
							.sorted(TILE_COMPARATOR)
							.collect(Collectors.toList());
					boolean focused = focusedGroups != null
							&& focusedGroups.contains(group);
					str.append(focused ? FOCUS_START_STR : GROUP_START_STR);
					List<TileSuit> suits = groupTiles.stream()
							.map(tile -> tile.type().suit()).distinct()
							.collect(Collectors.toList());
					if (suits.size() == 1) {
						groupTiles.forEach(tile -> str
								.append(str(tile.type().rank())));
						TileSuit suit = suits.get(0);
						if (suit.getRankClass() == NumberRank.class)
							str.append(str(suits.get(0)));
					} else {
						groupTiles.forEach(tile -> {
							str.append(str(tile.type().rank()));
							TileSuit suit = tile.type().suit();
							if (suit.getRankClass() == NumberRank.class)
								str.append(str(suit));
						});
					}
					str.append(focused ? FOCUS_END_STR : GROUP_END_STR);
					str.append(' ');
				});
	}

	private void appendAliveTiles(StringBuilder str) {
		if (playerInfo == null)
			return;
		appendAliveTiles(str, playerInfo.getAliveTiles(),
				contextView.getJustDrawedTile(), focusedAliveTiles);
	}

	// public，当工具用
	public static void appendAliveTiles(StringBuilder str,
			Set<Tile> aliveTiles, Tile justDrawed,
			Set<Tile> focusedAliveTiles) {
		// [空格]1 8 W [4 5]T 3[5]O DO NA
		// [1]8 W [4 5]T 3[5]O DO NA
		List<Tile> aliveTileList = aliveTiles.stream().sorted(TILE_COMPARATOR)
				.collect(Collectors.toList());
		if (justDrawed != null && aliveTileList.remove(justDrawed))
			// 把刚摸的牌放到最后面去
			aliveTileList.add(justDrawed);

		boolean lastFocused = false;
		TileSuit lastSuit = null;
		for (Tile tile : aliveTileList) {
			TileSuit suit = tile.type().suit();
			if ((lastSuit != null && lastSuit != suit)
					|| tile.equals(justDrawed)) {
				str.append(lastFocused ? FOCUS_END_STR : ' ');
				lastFocused = false;
				if (lastSuit.getRankClass() == NumberRank.class)
					str.append(str(lastSuit)).append(' ');
			}
			lastSuit = suit;

			boolean focused = focusedAliveTiles != null
					&& focusedAliveTiles.contains(tile);
			str.append(!lastFocused && focused ? FOCUS_START_STR
					: lastFocused && !focused ? FOCUS_END_STR : ' ');
			str.append(str(tile.type().rank()));
			lastFocused = focused;
		}

		str.append(lastFocused ? FOCUS_END_STR : ' ');
		lastFocused = false;
		if (lastSuit.getRankClass() == NumberRank.class)
			str.append(str(lastSuit));
	}

	private void appendTingStatus(StringBuilder str) {
		if (playerInfo == null)
			return;
		if (playerInfo.isTing())
			// [空格](听)
			str.append(' ').append(DESC_START_STR).append(TING.str()).append(DESC_END_STR);
	}

	private void appendOptions(StringBuilder str) {
		if (options.isEmpty())
			return;

		// [空格]<按键:说明/说明> <按键:说明> <按键>
		options.forEach((key, desc) -> {
			str.append(' ');
			str.append(OPTION_START_STR);
			str.append(key.str());
			if (desc != null)
				str.append(OPTION_MIDDLE_STR).append(desc.str());
			str.append(OPTION_END_STR);
		});
	}

	private void appendTimeLimit(StringBuilder str) {
		if (timeLimit == null)
			return;

		// [空格](time)
		str.append(' ').append(TIME_START_STR).append(timeLimit).append(TIME_END_STR);
	}

	public void showAction(PlayerLocation location, Action action)
			throws IOException {
		if (!(action.getType() instanceof StandardActionType)) {
			showActionDefault(location, action);
			return;
		}

		switch ((StandardActionType) action.getType()) {
		case DEAL:
			PlayerLocation.Relation zhuangRelation = contextView.getMyLocation()
					.getRelationOf(contextView.getZhuangLocation());
			StringBuilder showStr = new StringBuilder();
			showStr.append(DEAL_DONE.str());
			showStr.append(' ').append(ZHUANG.str());
			showStr.append(':').append(str(zhuangRelation)).append(str(contextView.getZhuangLocation()));
			showStr.append(' ').append(str(contextView.getTableView().getPlayerName(contextView.getZhuangLocation())));
			showActionStr(location, showStr);
			break;
		case CHI:
		case PENG:
		case ZHIGANG:
			ActionAndLocation discardAction = contextView.getDoneActions()
					.get(contextView.getDoneActions().size() - 2);
			Set<Tile> groupTiles = mergedSet(action.getTiles(),
					discardAction.getAction().getTiles());
			showActionStr(location,
					getDefaultActionStr(action.getType(), groupTiles));
			break;
		case BUGANG:
			showActionStr(location, getDefaultActionStr(action.getType(),
					Tile.allOfType(action.getTile().type())));
			break;
		case ANGANG:
			showActionStr(location,
					getDefaultActionStr(action.getType(), null));
			break;
		case DISCARD_WITH_TING:
			StringBuilder str = getDefaultActionStr(DISCARD, action.getTiles());
			str.append(" ");
			str.append(TING_START_STR);
			str.append(TING.str());
			str.append(TING_END_STR);
			showActionStr(location, str);
			break;
		case WIN:
			showWinAction(location, action);
			break;
		default:
			showActionDefault(location, action);
			return;
		}

	}

	private void showWinAction(PlayerLocation location, Action action)
			throws IOException {
		GameResult result = contextView.getGameResult();
		if (result == null) {
			// 和牌却没有结果，容错
			showActionDefault(location, action);
			return;
		}

		// 第一行分割线
		// 本家东 X 自摸
		// 本家东 X 和 上家南 Y 点炮
		PlayerLocation.Relation relation = contextView.getMyLocation()
				.getRelationOf(location);
		StringBuilder firstLine = new StringBuilder();
		firstLine.append(str(relation)).append(str(location));
		firstLine.append(' ').append(
				str(contextView.getTableView().getPlayerName(location)));
		if (result.getPaoerLocation() == null) {
			firstLine.append(' ').append(ZIMO.str());
		} else {
			firstLine.append(' ').append(str(WIN));
			PlayerLocation.Relation paoerRelation = contextView.getMyLocation()
					.getRelationOf(result.getPaoerLocation());
			firstLine.append("  ").append(str(paoerRelation))
					.append(str(result.getPaoerLocation()));
			firstLine.append(' ').append(str(contextView.getTableView()
					.getPlayerName(result.getPaoerLocation())));
			firstLine.append(' ').append(DIANPAO.str());
		}
		cliView.printSplitLine(firstLine.toString(), POSITION_WIDTH * 4);

		// 和牌者的牌
		// groups aliveTiles（focus和牌并放到最后）
		StringBuilder tilesLine = new StringBuilder();
		appendGroups(tilesLine,
				result.getPlayerInfos().get(location).getTileGroups(), null);
		appendAliveTiles(tilesLine,
				mergedSet(
						result.getPlayerInfos().get(location).getAliveTiles(),
						result.getWinTile()),
				result.getWinTile(),
				Collections.singleton(result.getWinTile()));
		cliView.printMessage(tilesLine.toString());

		// 番
		cliView.printMessage("");
		int fanNameWidth = result.getFans().keySet().stream().map(FanType::name)
				.map(MyUtils::strWidth).max(Comparator.naturalOrder())
				.map(maxWidth -> Math.max(maxWidth, strWidth(FAN_TOTLE.str()))
						+ 1)
				.orElse(0);
		AtomicInteger totalFanValue = new AtomicInteger(0);
		result.getFans().forEach(rethrowBiConsumer((fanType, fanValue) -> {
			cliView.printMessage(fixedWidth(str(fanType), fanNameWidth)
					+ String.format("%3d", fanValue) + FAN.str());
			totalFanValue.addAndGet(fanValue);
		}));
		cliView.printMessage(fixedWidth(FAN_TOTLE.str(), fanNameWidth)
				+ String.format("%3d", totalFanValue.get()) + FAN.str());

		// 最后一行分割线
		cliView.printSplitLine(null, POSITION_WIDTH * 4);
	}

	private void showActionDefault(PlayerLocation location, Action action)
			throws IOException {
		showActionStr(location,
				getDefaultActionStr(action.getType(), action.getTiles()));
	}

	private StringBuilder getDefaultActionStr(ActionType actionType,
			Set<Tile> actionTiles) {
		StringBuilder actionStr = new StringBuilder();
		actionStr.append(str(actionType));
		if (!(actionTiles == null || actionTiles.isEmpty())) {
			actionStr.append(' ');
			List<TileSuit> suits = actionTiles.stream()
					.map(tile -> tile.type().suit()).distinct()
					.collect(Collectors.toList());
			List<Tile> actionTileList = actionTiles.stream()
					.sorted(TILE_COMPARATOR).collect(Collectors.toList());
			if (suits.size() == 1) {
				actionTileList.forEach(tile -> actionStr
						.append(str(tile.type().rank())));
				TileSuit suit = suits.get(0);
				if (suit.getRankClass() == NumberRank.class)
					actionStr.append(str(suit));
			} else {
				actionTileList.forEach(tile -> {
					actionStr.append(str(tile.type().rank()));
					TileSuit suit = tile.type().suit();
					if (suit.getRankClass() == NumberRank.class)
						actionStr.append(str(suit));
				});
			}
		}
		return actionStr;
	}

	private void showActionStr(PlayerLocation location, CharSequence str)
			throws IOException {
		if (location == null) {
			cliView.printSplitLine(str.toString(), POSITION_WIDTH * 4);
		} else {
			// player 打出 3W
			// player 摸
			PlayerLocation.Relation relation = contextView.getMyLocation()
					.getRelationOf(location);
			int position = 1 + POSITION_WIDTH * relation.ordinal();
			StringBuilder showStr = new StringBuilder();
			IntStream.range(0, position).forEach(i -> showStr.append(' '));
			showStr.append(
					str(contextView.getTableView().getPlayerName(location)));
			showStr.append(' ');
			showStr.append(str);
			cliView.printMessage(showStr.toString());
		}
		updateTileLine();
	}

}