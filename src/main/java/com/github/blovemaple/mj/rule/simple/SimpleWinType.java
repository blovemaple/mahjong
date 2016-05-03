package com.github.blovemaple.mj.rule.simple;

import static com.github.blovemaple.mj.object.StandardTileUnitType.*;
import static com.github.blovemaple.mj.utils.MyUtils.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileUnit;
import com.github.blovemaple.mj.rule.WinType;

/**
 * 普通和牌（相对于七对等特殊和牌类型而言）。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class SimpleWinType implements WinType {

	/**
	 * 全部解析成完整的TileUnit集合的流，包括将牌和若干个顺子/刻子，失败返回空集合。
	 * 
	 * @param aliveTiles
	 * @param anyOne
	 *            true表示最多只解析一种可能的集合，用于快速判断是否可解析
	 * @return 完整的TileUnit集合的流
	 */
	public Stream<Set<TileUnit>> parseWinTileUnits(PlayerInfo playerInfo,
			Set<Tile> aliveTiles) {
		Set<TileUnit> groupUnits = genGroupUnits(playerInfo);

		// 所有可能的将牌
		return combinationStream(aliveTiles, JIANG.size())
				.filter(JIANG::isLegalTiles)
				// 针对每一种可能的将牌，寻找剩下的牌全部解析成顺子/刻子的所有可能
				.flatMap(jiang -> {
					TileUnit jiangUnit = new TileUnit(JIANG, jiang);
					List<Tile> otherTiles = new ArrayList<Tile>(aliveTiles);
					otherTiles.removeAll(jiang);
					return parseShunKes(otherTiles)
							.peek(shunKes -> shunKes.add(jiangUnit));
				})
				// 加上牌组生成的单元
				.peek(set -> set.addAll(groupUnits));
	}

	/**
	 * 从玩家的所有牌组生成单元集合。
	 */
	protected Set<TileUnit> genGroupUnits(PlayerInfo playerInfo) {
		return playerInfo.getTileGroups().stream()
				.map(group -> new TileUnit(group.getType().getUnitType(),
						group.getTiles()))
				.collect(Collectors.toSet());
	}

	/**
	 * 全部解析成顺子/刻子集合的可行情况组成的流，失败返回空流。
	 */
	private Stream<Set<TileUnit>> parseShunKes(List<Tile> tiles) {
		if (tiles.isEmpty())
			return Stream.of(new HashSet<>());
		// 取出第一张牌 XXX
		Tile fixedTile = tiles.get(0);
		// （先找一个顺子单元，再递归解析剩下的牌）
		Stream<Set<TileUnit>> shunFirstStream =
				// 从第二张开始，从中取两张牌，与第一张牌组成一个单元
				combinationStream(tiles.subList(1, tiles.size()),
						SHUNZI.size() - 1)
								.peek(halfUnit -> halfUnit.add(fixedTile))
								// 过滤出合法的顺子（下面的是刻子）单元
								.filter(SHUNZI::isLegalTiles)
								// 在剩下的牌中递归解析顺子/刻子单元集合，并添加上面这个单元
								.flatMap(unit -> {
									TileUnit shunUnit = new TileUnit(SHUNZI,
											unit);
									List<Tile> otherTiles = new LinkedList<>(
											tiles);
									otherTiles.removeAll(unit);
									return parseShunKes(otherTiles).peek(
											shunKes -> shunKes.add(shunUnit));
								});
		// （先找一个刻子单元，再递归解析剩下的牌，逻辑与上面相同）
		Stream<Set<TileUnit>> keFirstStream = combinationStream(
				tiles.subList(1, tiles.size()), KEZI.size() - 1)
						.peek(halfUnit -> halfUnit.add(fixedTile))
						.filter(KEZI::isLegalTiles).flatMap(unit -> {
							TileUnit keUnit = new TileUnit(KEZI, unit);
							List<Tile> otherTiles = new LinkedList<>(tiles);
							otherTiles.removeAll(unit);
							return parseShunKes(otherTiles)
									.peek(shunKes -> shunKes.add(keUnit));
						});
		// 把上面两个流合在一起返回
		return Stream.concat(shunFirstStream, keFirstStream);
	}

}
