package com.github.blovemaple.mj.rule.fan;

import static com.github.blovemaple.mj.object.TileGroupType.*;
import static com.github.blovemaple.mj.object.TileRank.ZiRank.*;
import static com.github.blovemaple.mj.object.TileSuit.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroup;
import com.github.blovemaple.mj.object.TileGroupType;
import com.github.blovemaple.mj.object.TileRank;
import com.github.blovemaple.mj.object.TileSuit;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.rule.win.WinInfo;

/**
 * 测试算番时构造WinInfo的工具。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class FanTestUtils {
	/**
	 * 解析字符串构造WinInfo。
	 */
	public static WinInfo parseWinInfo(String str) {
		List<Tile> tiles = new ArrayList<>(Tile.all());

		WinInfo winInfo = new WinInfo();
		Set<Tile> aliveTiles = winInfo.getAliveTiles();
		List<TileGroup> groups = winInfo.getTileGroups();

		String[] strs = str.split("\\s");
		try {
			Arrays.stream(strs).map(String::toUpperCase).forEach(part -> {
				if (GROUP_TYPES.containsKey(part.charAt(0)))
					groups.add(parseGroup(part, tiles));
				else
					aliveTiles.addAll(parseTiles(part, tiles));
			});
		} catch (Exception e) {
			throw new RuntimeException("Error parsing win info from: " + str, e);
		}

		return winInfo;
	}

	private static final Map<Character, TileGroupType> GROUP_TYPES = new HashMap<>();
	static {
		GROUP_TYPES.put('C', CHI_GROUP);
		GROUP_TYPES.put('P', PENG_GROUP);
		GROUP_TYPES.put('G', ZHIGANG_GROUP);
		GROUP_TYPES.put('U', BUGANG_GROUP);
		GROUP_TYPES.put('A', ANGANG_GROUP);
	}

	private static TileGroup parseGroup(String str, List<Tile> remainTiles) {
		TileGroupType type = GROUP_TYPES.get(str.charAt(0));
		char[] tilesStr = new char[str.length() - 1];
		str.getChars(1, str.length(), tilesStr, 0);
		Set<Tile> tiles = parseTiles(String.valueOf(tilesStr), remainTiles);
		return new TileGroup(type, tiles);
	}

	private static final Map<Character, TileSuit> SUITS = new HashMap<>();
	private static final Map<Character, TileRank<?>> RANKS = new HashMap<>();
	static {
		SUITS.put('W', WAN);
		SUITS.put('T', TIAO);
		SUITS.put('B', BING);
		SUITS.put('Z', ZI);

		RANKS.put('E', DONG_FENG);
		RANKS.put('S', NAN);
		RANKS.put('W', XI);
		RANKS.put('N', BEI);
		RANKS.put('Z', ZHONG);
		RANKS.put('F', FA);
		RANKS.put('B', BAI);
		for (char c = '1'; c <= '9'; c++)
			RANKS.put(c, TileRank.NumberRank.ofNumber(Integer.parseInt(Character.toString(c))));
	}

	private static Set<Tile> parseTiles(String str, List<Tile> remainTiles) {
		Set<Tile> tiles = new HashSet<>();
		for (int i = 0; i < str.length(); i += 2) {
			TileSuit suit = SUITS.get(str.charAt(i));
			TileRank<?> rank = RANKS.get(str.charAt(i + 1));
			tiles.add(findTile(suit, rank, remainTiles));
		}
		return tiles;
	}

	private static Tile findTile(TileSuit suit, TileRank<?> rank, List<Tile> remainTiles) {
		Iterator<Tile> itr = remainTiles.iterator();
		while (itr.hasNext()) {
			Tile tile = itr.next();
			TileType tileType = tile.type();
			if (tileType.suit() == suit && tileType.rank() == rank) {
				itr.remove();
				return tile;
			}
		}
		throw new RuntimeException("No remain tile for " + suit + " " + rank);
	}
}
