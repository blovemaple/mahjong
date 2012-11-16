package blove.mj.rules;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import blove.mj.Tile;
import blove.mj.TileType.Suit;
import blove.mj.board.PlayerTiles;

/**
 * 判断基本和牌的工具类，使用静态方法可以完成判断功能。
 * 
 * @author blovemaple
 */
public class BasicWin {
	private BasicWin() {
	}

	/**
	 * 判断指定玩家的牌是否符合基本的和牌。
	 * 
	 * @param tiles
	 *            玩家的牌
	 * @param aliveTiles
	 *            替代使用的aliveTiles。如果为null，则使用tiles中的aliveTiles。
	 * @return 如果符合，返回true；否则返回false。
	 * @throws IllegalArgumentException
	 *             牌的数量不合法
	 */
	public static boolean match(PlayerTiles tiles, Set<Tile> aliveTiles) {
		if (aliveTiles == null)
			aliveTiles = tiles.getAliveTiles();
		if (aliveTiles.size() % 3 != 2)
			throw new IllegalArgumentException("牌的数量不合法");

		SortedSet<Tile> sortedTiles = new TreeSet<>();
		sortedTiles.addAll(aliveTiles);
		return match(sortedTiles);
	}

	/**
	 * 判断一组单种花色的牌是否符合和牌规则。
	 * 
	 * @param tiles
	 *            牌
	 * @return 如果符合，返回true；否则返回false。
	 */
	private static boolean match(SortedSet<Tile> tiles) {
		Set<Tile> group;
		boolean remainMatch;

		if (tiles.size() % 3 == 2) {
			// 有将牌
			group = extractEyesForFirst(tiles);
			if (group != null) {
				if (tiles.isEmpty())
					return true;
				remainMatch = match(tiles);
				if (remainMatch)
					return true;
				else
					tiles.addAll(group);
			}
		}

		if (tiles.size() < 3)
			return false;

		group = extractTripletForFirst(tiles);
		if (group != null) {
			if (tiles.isEmpty())
				return true;
			remainMatch = match(tiles);
			if (remainMatch)
				return true;
			else
				tiles.addAll(group);
		}

		group = extractSequenceForFirst(tiles);
		if (group != null) {
			if (tiles.isEmpty())
				return true;
			remainMatch = match(tiles);
			if (remainMatch)
				return true;
		}

		return false;
	}

	private static Set<Tile> extractEyesForFirst(SortedSet<Tile> tiles) {
		Iterator<Tile> itr = tiles.iterator();
		Tile first = itr.next();
		Tile second = itr.next();
		if (first.getType().equals(second.getType())) {
			Set<Tile> eyes = new HashSet<>();
			eyes.add(first);
			eyes.add(second);
			tiles.removeAll(eyes);
			return eyes;
		} else
			return null;
	}

	private static Set<Tile> extractTripletForFirst(SortedSet<Tile> tiles) {
		Iterator<Tile> itr = tiles.iterator();
		Tile first = itr.next();
		Tile second = itr.next();
		Tile third = itr.next();
		if (first.getType().equals(second.getType())
				&& first.getType().equals(third.getType())) {
			Set<Tile> triplet = new HashSet<>();
			triplet.add(first);
			triplet.add(second);
			triplet.add(third);
			tiles.removeAll(triplet);
			return triplet;
		} else
			return null;
	}

	private static Set<Tile> extractSequenceForFirst(SortedSet<Tile> tiles) {
		Iterator<Tile> itr = tiles.iterator();

		Tile first = itr.next();
		Suit suit = first.getType().getSuit();
		if (suit.isHonor())
			return null;
		int rank = first.getType().getRank();
		if (rank > 7)
			return null;

		Tile second = null, third = null, temp;
		while (itr.hasNext()) {
			temp = itr.next();
			if (temp.getType().getSuit().equals(suit)
					&& temp.getType().getRank() == rank + 1) {
				second = temp;
				break;
			}
		}
		if (second == null)
			return null;
		while (itr.hasNext()) {
			temp = itr.next();
			if (temp.getType().getSuit().equals(suit)
					&& temp.getType().getRank() == rank + 2) {
				third = temp;
				break;
			}
		}
		if (third == null)
			return null;

		Set<Tile> sequence = new HashSet<>();
		sequence.add(first);
		sequence.add(second);
		sequence.add(third);
		tiles.removeAll(sequence);
		return sequence;
	}
}
