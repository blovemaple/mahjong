package blove.mj.bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import blove.mj.Cpk;
import blove.mj.Player;
import blove.mj.PlayerView;
import blove.mj.Tile;
import blove.mj.TileType;
import blove.mj.TileType.Suit;
import blove.mj.board.PlayerTiles;

/**
 * 机器人玩家“Foo”。
 * 
 * @author blovemaple
 */
public class FooBot extends Player {
	private static final int MIN_THINKING_TIME = 2, MAX_THINKING_TIME = 5;
	private final Random random = new Random();

	@SuppressWarnings("unused")
	private final String name;

	/**
	 * 新建一个实例。
	 * 
	 * @param name
	 *            名字
	 */
	public FooBot(String name) {
		super(name);
		this.name = name;
	}

	@Override
	public void forReady(PlayerView playerView) {
		playerView.readyForGame();
	}

	@Override
	public void forLeaving(PlayerView playerView) {
	}

	@Override
	public CpkwChoice chooseCpkw(PlayerView playerView,
			Set<CpkwChoice> cpkwChances, Tile newTile, boolean drawed)
			throws InterruptedException {
		delay();

		PlayerTiles myTiles = playerView.getMyTiles();

		// 提取所有吃/碰/杠机会，并制作其到选择的映射以便返回时查询。
		Map<Cpk, CpkwChoice> cpkChancesToChoices = new HashMap<>();
		Set<Cpk> cpkChances = new HashSet<>();

		for (CpkwChoice cpkwChance : cpkwChances) {
			// 如果有和牌机会则和牌
			if (cpkwChance.win)
				return cpkwChance;
			else {
				if (cpkwChance.cpk == null)
					throw new IllegalArgumentException();
				cpkChances.add(cpkwChance.cpk);
				cpkChancesToChoices.put(cpkwChance.cpk, cpkwChance);
			}
		}

		// 所有牌分组
		Set<Set<Tile>> groups = group(myTiles.getAliveTiles());
		// 计算可选将牌
		Set<Set<Tile>> eyeCandidates = countEyeCandidates(groups, myTiles);
		// 吃碰杠牌型化
		Set<Cpk> cpkChancesAsTileType = new TreeSet<>(Cpk.tileTypeComparator);
		cpkChancesAsTileType.addAll(cpkChances);
		cpkChances = cpkChancesAsTileType;

		// 取得牌所在组
		Set<Tile> forGroup = null;
		for (Set<Tile> group : groups) {
			if (isSuitableGroup(group, newTile)) {
				forGroup = group;
				break;
			}
		}
		if (forGroup == null)
			throw new RuntimeException("得牌是独组");// 得牌不可能是独组

		// 算出所有顺子、刻子
		Set<Face> allFaces = countFaces(myTiles.getAliveTiles(), eyeCandidates,
				myTiles);

		// 若杠牌中有上面算出的刻子，则杠牌
		for (Cpk cpkChance : cpkChances) {
			if (cpkChance.getType().isKong()) {
				for (Face face : allFaces) {
					TileType faceTileType = face.tiles.iterator().next()
							.getType();
					TileType kongTileType = cpkChance.getTiles().iterator()
							.next().getType();
					if (faceTileType.equals(kongTileType))
						return cpkChancesToChoices.get(cpkChance);
				}
			}
		}

		// 去掉上面算出的顺子、刻子
		for (Face face : allFaces)
			groups = removeTiles(groups, face.tiles);

		// 去掉剩下牌中已不可能的吃碰机会
		Iterator<Cpk> cpkChanceItr = cpkChances.iterator();
		while (cpkChanceItr.hasNext()) {
			Cpk cpkChance = cpkChanceItr.next();
			for (Tile tileInCpk : cpkChance.getTiles()) {
				if (!tileInCpk.equals(cpkChance.getForTile())
						&& !containsTile(groups, tileInCpk)) {
					cpkChanceItr.remove();
					break;
				}
			}
		}

		if (cpkChances.isEmpty()) {
			// 若没有剩下机会，放弃
			return null;
		} else if (cpkChances.size() == 1) {
			// 若剩下一个机会，选择之
			return cpkChancesToChoices.get(cpkChances.iterator().next());
		} else {
			// 若剩下多个机会，取选择后组中剩余牌等牌数量最多者，选择之
			int mostWaitingTileCount = -1;
			Cpk chanceChoose = null;
			for (Cpk cpkChance : cpkChances) {
				Set<Tile> groupWithoutCpkTiles = new HashSet<>(forGroup);
				groupWithoutCpkTiles.removeAll(cpkChance.getTiles());
				int waitingTileCount = countWaitingTiles(groupWithoutCpkTiles,
						eyeCandidates, myTiles);
				if (waitingTileCount > mostWaitingTileCount) {
					mostWaitingTileCount = waitingTileCount;
					chanceChoose = cpkChance;
				}
			}
			return cpkChancesToChoices.get(chanceChoose);
		}
	}

	@Override
	public DiscardChoice chooseDiscard(PlayerView playerView,
			Set<TileType> readyHandTypes, Tile drawedTile)
			throws InterruptedException {
		delay();

		PlayerTiles myTiles = playerView.getMyTiles();

		// 所有牌分组
		Set<Set<Tile>> groups = group(myTiles.getAliveTiles());
		// 计算可选将牌
		Set<Set<Tile>> eyeCandidates = countEyeCandidates(groups, myTiles);

		// 去掉所有顺子、刻子
		Set<Face> allFaces = countFaces(myTiles.getAliveTiles(), eyeCandidates,
				myTiles);
		Set<Tile> tilesInFaces = new HashSet<>();
		for (Face face : allFaces)
			tilesInFaces.addAll(face.tiles);
		groups = removeTiles(groups, tilesInFaces);

		Tile discardTile;// 存放待打出的牌

		// 取出只有一张牌的组（独牌组）
		List<Set<Tile>> singleTileGroups = new LinkedList<>();
		for (Set<Tile> group : groups)
			if (group.size() == 1)
				singleTileGroups.add(group);

		if (singleTileGroups.size() == 1) {
			// 若只有一个独牌，打出这张牌
			discardTile = singleTileGroups.get(0).iterator().next();
		} else if (singleTileGroups.size() > 1) {
			// 若有多张独牌

			Map<Set<Tile>, Integer> oriWaitingCounts = new HashMap<>();// lazy存放每组的等牌数量，避免重复计算

			// 遍历每个独牌，选出增加任何一张牌带来的等牌数量增加值最小者
			Set<Tile> worstSingleTileGroup = null;
			int worstWaitingIncSum = Integer.MAX_VALUE;
			for (Set<Tile> singleTileGroup : singleTileGroups) {
				Tile singleTile = singleTileGroup.iterator().next();
				TileType singleTileType = singleTile.getType();
				// 取得所有可以取消独牌的增加牌
				Map<TileType, Set<Tile>> avNextTiles = new HashMap<>();
				if (singleTileType.getSuit().isHonor()) {
					avNextTiles.put(singleTileType,
							getTilesRemain(myTiles, singleTileType));
				} else {
					for (int avRank = Math.max(1, singleTileType.getRank() - 2); avRank <= Math
							.min(9, singleTileType.getRank() + 2); avRank++) {
						TileType avType = TileType.get(
								singleTileType.getSuit(), avRank);
						avNextTiles
								.put(avType, getTilesRemain(myTiles, avType));
					}
				}

				// 遍历所有可以取消独牌的增加牌，算出增加他们所带来的等牌数量的增加值之和。以每种类型的增加牌数量为权重。
				int waitingIncSum = 0;
				for (Map.Entry<TileType, Set<Tile>> avNextTilesEntry : avNextTiles
						.entrySet()) {
					Set<Tile> avNextTilesTiles = avNextTilesEntry.getValue();
					if (avNextTilesTiles.isEmpty())
						continue;
					Tile avNextTilesTile = avNextTilesTiles.iterator().next();

					// 取得所在组
					List<Set<Tile>> belongGroups = new LinkedList<>();
					for (Set<Tile> group : groups) {
						if (isSuitableGroup(group, avNextTilesTile))
							belongGroups.add(group);
					}
					// 计算所在组原等牌数量之和
					int oriWaitingSum = 0;
					for (Set<Tile> group : belongGroups) {
						Integer oriWaitingCount = oriWaitingCounts.get(group);
						if (oriWaitingCount == null) {
							oriWaitingCount = countWaitingTiles(group,
									eyeCandidates, myTiles);
							oriWaitingCounts.put(group, oriWaitingCount);
						}
						oriWaitingSum += oriWaitingCount;
					}
					// 计算所在组增加后等牌数量之和
					int tarWaitingSum;
					Set<Tile> newGroup = new HashSet<>();
					for (Set<Tile> group : belongGroups)
						newGroup.addAll(group);
					tarWaitingSum = countWaitingTiles(newGroup, eyeCandidates,
							myTiles);

					// 记入增加数量
					waitingIncSum += (tarWaitingSum - oriWaitingSum)
							* avNextTilesTiles.size();
				}

				// 比较
				if (waitingIncSum < worstWaitingIncSum) {
					worstWaitingIncSum = waitingIncSum;
					worstSingleTileGroup = singleTileGroup;
				}
			}

			// 打出等牌数量增加和最少的独牌
			discardTile = worstSingleTileGroup.iterator().next();
		} else {
			// 若没有独牌
			// 遍历每张剩下的牌，选出打出之后使等牌数量减少最少者
			Tile leastImpTile = null;
			int leastWaitingCountDec = Integer.MAX_VALUE;
			for (Set<Tile> group : groups) {
				int oriWaitingCount = countWaitingTiles(group, eyeCandidates,
						myTiles);
				for (Tile tile : new LinkedList<>(group)) {
					group.remove(tile);

					int newWaitingCount = countWaitingTiles(group,
							eyeCandidates, myTiles);
					int waitingCountDec = oriWaitingCount - newWaitingCount;
					if (waitingCountDec < leastWaitingCountDec) {
						leastWaitingCountDec = waitingCountDec;
						leastImpTile = tile;
					}

					group.add(tile);
				}
			}

			// 打出使等牌数量减少最少者
			discardTile = leastImpTile;
		}

		// 目前已确定打出的牌discardTile
		// 判断是否可以听牌，若可以听牌则听牌
		boolean readyHand = readyHandTypes.contains(discardTile.getType());

		return new DiscardChoice(discardTile, readyHand);
	}

	/**
	 * 用延时模拟思考时间。
	 * 
	 * @throws InterruptedException
	 */
	private void delay() throws InterruptedException {
		TimeUnit.MILLISECONDS
				.sleep(MIN_THINKING_TIME
						* 1000
						+ random.nextInt((MAX_THINKING_TIME - MIN_THINKING_TIME) * 1000));
	}

	/**
	 * 分组。非字牌以相同Suit其中顺序间隔不超过一张牌者为一组，字牌以相同的TileType为一组。
	 * 
	 * @param tiles
	 *            待分组的牌
	 * @return 所有组
	 */
	private Set<Set<Tile>> group(Set<Tile> tiles) {
		if (tiles.isEmpty())
			return Collections.emptySet();

		Set<Set<Tile>> groups = new HashSet<>();

		List<Tile> tileList = new ArrayList<>(tiles);
		Collections.sort(tileList);

		Set<Tile> crtGroup = new HashSet<>();
		TileType lastTileType = null;
		for (Tile tile : tileList) {
			TileType tileType = tile.getType();
			if (lastTileType != null
					&& (!tileType.getSuit().equals(lastTileType.getSuit()) || tileType
							.getRank() > lastTileType.getRank() + 2)) {
				groups.add(crtGroup);
				crtGroup = new HashSet<>();
			}
			crtGroup.add(tile);
			lastTileType = tileType;
		}
		if (crtGroup != null && !crtGroup.isEmpty())
			groups.add(crtGroup);

		return groups;
	}

	/**
	 * 计算候选将牌。
	 * 
	 * @param groups
	 *            所有分组
	 * @param myTiles
	 *            玩家的牌
	 * @return 候选将牌
	 */
	private Set<Set<Tile>> countEyeCandidates(Set<Set<Tile>> groups,
			PlayerTiles myTiles) {
		// 计算所有对牌
		Set<Set<Tile>> allPairs = new HashSet<>();// 所有对牌
		Map<Set<Tile>, Set<Tile>> pairToGroup = new HashMap<>();// 所有对牌到所在分组的映射

		Map<TileType, Set<Tile>> typeToTiles = new HashMap<>();
		Map<Tile, Set<Tile>> tileToGroup = new HashMap<>();
		for (Set<Tile> group : groups) {
			for (Tile tile : group) {
				TileType tileType = tile.getType();
				Set<Tile> typeTiles = typeToTiles.get(tileType);
				if (typeTiles == null) {
					typeTiles = new HashSet<>();
					typeToTiles.put(tileType, typeTiles);
				}
				typeTiles.add(tile);

				tileToGroup.put(tile, group);
			}
		}
		for (Map.Entry<TileType, Set<Tile>> typeToTilesEntry : typeToTiles
				.entrySet()) {
			List<Tile> typeTiles = new ArrayList<>(typeToTilesEntry.getValue());
			if (typeTiles.size() >= 2) {
				for (int i1 = 0; i1 < typeTiles.size() - 1; i1++) {
					for (int i2 = 1; i2 < typeTiles.size(); i2++) {
						Set<Tile> pair = new HashSet<>();
						pair.add(typeTiles.get(i1));
						pair.add(typeTiles.get(i2));
						allPairs.add(pair);
						pairToGroup.put(pair,
								tileToGroup.get(typeTiles.get(i1)));
					}
				}
			}
		}

		// 计算去掉对牌后组内所剩顺子刻子
		Map<Set<Tile>, Set<Face>> pairToRemainFaces = new HashMap<>();// 对牌到去掉对牌后组内所剩顺子刻子的映射
		for (Set<Tile> pair : allPairs) {
			Set<Tile> group = pairToGroup.get(pair);
			group.removeAll(pair);
			pairToRemainFaces.put(
					pair,
					countFaces(group, Collections.<Set<Tile>> emptySet(),
							myTiles));
			group.addAll(pair);
		}

		// 计算去掉对牌、去掉剩余顺子、刻子后所剩的牌
		Map<Set<Tile>, Set<Tile>> pairToRemainTiles = new HashMap<>();
		for (Set<Tile> pair : allPairs) {
			Set<Tile> groupCopy = new HashSet<>(pairToGroup.get(pair));
			groupCopy.removeAll(pair);
			for (Face face : pairToRemainFaces.get(pair))
				groupCopy.removeAll(face.tiles);
			pairToRemainTiles.put(pair, groupCopy);
		}

		// 取出没有剩余牌者，若不为空则返回这些
		Set<Set<Tile>> noRemainPairs = new HashSet<>();
		for (Map.Entry<Set<Tile>, Set<Tile>> pairToRemainTilesEntry : pairToRemainTiles
				.entrySet()) {
			if (pairToRemainTilesEntry.getValue().isEmpty())
				noRemainPairs.add(pairToRemainTilesEntry.getKey());
		}
		if (!noRemainPairs.isEmpty())
			return noRemainPairs;

		// 计算原始每组内的顺子、刻子
		Map<Set<Tile>, Set<Face>> oriGroupFaces = new HashMap<>();
		for (Set<Tile> group : groups) {
			Set<Face> faces = countFaces(group,
					Collections.<Set<Tile>> emptySet(), myTiles);
			oriGroupFaces.put(group, faces);
		}

		// 计算若选择对牌作为将牌本组内顺子刻子减少的量；忽略顺子刻子减少量不为零，且对牌包含于所在组原始刻子中者
		Map<Set<Tile>, Integer> pairToFaceDec = new HashMap<>();
		pairs: for (Set<Tile> pair : allPairs) {
			Set<Face> oriFaces = oriGroupFaces.get(pairToGroup.get(pair));
			int faceDec = oriFaces.size() - pairToRemainFaces.get(pair).size();
			if (faceDec > 0) {
				for (Face face : oriFaces) {
					if (face.type == Face.FaceType.TRIPLET
							&& face.tiles.containsAll(pair)) {
						continue pairs;
					}
				}
			}
			pairToFaceDec.put(pair, faceDec);
		}

		if (!pairToFaceDec.isEmpty()) {
			// 计算顺子刻子减少量最少者
			Set<Set<Tile>> leastImpPairs = new HashSet<>();
			int leastDec = Integer.MAX_VALUE;
			for (Map.Entry<Set<Tile>, Integer> pairToFaceDecEntry : pairToFaceDec
					.entrySet()) {
				Set<Tile> pair = pairToFaceDecEntry.getKey();
				int dec = pairToFaceDecEntry.getValue();
				if (dec == leastDec) {
					leastImpPairs.add(pair);
				} else if (dec < leastDec) {
					leastDec = dec;
					leastImpPairs.clear();
					leastImpPairs.add(pair);
				}
			}

			// 若有多对则选择去掉对牌、顺子刻子后，所在组等牌数量减少最少者
			if (leastImpPairs.size() > 1) {
				Set<Set<Tile>> leastWaitingCountDecPairs = new HashSet<>();
				int leastWaitingCountDec = Integer.MAX_VALUE;

				Map<Set<Tile>, Integer> oriGroupWaitingCount = new HashMap<>();// 存放原始组等牌数量，防止重复计算
				for (Set<Tile> pair : leastImpPairs) {
					Set<Tile> group = pairToGroup.get(pair);
					Integer oriWaitingCount = oriGroupWaitingCount.get(group);
					if (oriWaitingCount == null) {
						oriWaitingCount = countWaitingTiles(group,
								Collections.<Set<Tile>> emptySet(), myTiles);
						oriGroupWaitingCount.put(group, oriWaitingCount);
					}
					int remainWaitingCount = countWaitingTiles(
							pairToRemainTiles.get(pair),
							Collections.<Set<Tile>> emptySet(), myTiles);
					int waitingCountDec = oriWaitingCount - remainWaitingCount;// 等牌数量减少量

					if (waitingCountDec == leastWaitingCountDec) {
						leastWaitingCountDecPairs.add(pair);
					} else if (waitingCountDec < leastWaitingCountDec) {
						leastWaitingCountDec = waitingCountDec;
						leastWaitingCountDecPairs.clear();
						leastWaitingCountDecPairs.add(pair);
					}
				}

				leastImpPairs.retainAll(leastWaitingCountDecPairs);// 下面以leastImpPairs为准
			}

			if (!leastImpPairs.isEmpty())
				return leastImpPairs;
		}

		// 没有合适的对牌。选择所有defg类型的牌组。
		Set<Set<Tile>> defgGroups = new HashSet<>();
		groups: for (Set<Tile> group : groups) {
			if (group.size() != 4)
				continue groups;

			List<Tile> groupTileList = new ArrayList<>(group);
			Collections.sort(groupTileList);

			int lastRank = 0;
			for (Tile tile : groupTileList) {
				int crtRank = tile.getType().getRank();
				if (lastRank > 0 && crtRank != lastRank + 1) {
					continue groups;
				}
				lastRank = crtRank;
			}

			defgGroups.add(group);
		}

		if (!defgGroups.isEmpty())
			return defgGroups;

		return Collections.emptySet();
	}

	/**
	 * 计算顺子、刻子。若有冲突，在顺子刻子数量最多的所有组合情况中按照剩下的牌中等牌多少，选择最多者。
	 * 
	 * @param tiles
	 *            待计算的牌
	 * @param eyeCandidates
	 *            将牌候选
	 * @param myTiles
	 *            玩家的牌
	 * @return 所有顺子、刻子
	 */
	private Set<Face> countFaces(Set<Tile> tiles, Set<Set<Tile>> eyeCandidates,
			PlayerTiles myTiles) {
		SortedSet<Tile> sortedTiles = new TreeSet<>(tiles);

		// 计算所有可能的顺子、刻子
		Set<Face> allFaces = new HashSet<>();

		Set<Set<Tile>> allTriplets = new HashSet<>();
		Set<Set<Tile>> allSequences = new HashSet<>();
		for (Tile tile : tiles) {
			allTriplets.addAll(findTripletsFor(tile, sortedTiles));
			allSequences.addAll(findSequencesFor(tile, sortedTiles));
		}
		for (Set<Tile> triplet : allTriplets)
			allFaces.add(new Face(Face.FaceType.TRIPLET, triplet));
		for (Set<Tile> sequence : allSequences)
			allFaces.add(new Face(Face.FaceType.SEQUENCE, sequence));

		// 计算所有可能的顺子刻子搭配组合
		Set<Set<Face>> faceGroups = countFaceGroups(allFaces);

		// 去掉破坏所有将牌候选的组合中与将牌候选冲突的Face
		Set<Tile> allEyeCandidatesTiles = new HashSet<>();
		for (Set<Tile> eyeCandidate : eyeCandidates)
			allEyeCandidatesTiles.addAll(eyeCandidate);
		Set<Tile> faceGroupTiles = new HashSet<>();
		for (Set<Face> faceGroup : new LinkedList<>(faceGroups)) {
			faceGroupTiles.clear();
			for (Face face : faceGroup)
				faceGroupTiles.addAll(face.tiles);

			boolean allConflict = true;
			for (Set<Tile> eyeCandidate : eyeCandidates) {
				if (Collections.disjoint(faceGroupTiles, eyeCandidate)) {
					allConflict = false;
					break;
				}
			}
			if (allConflict) {
				for (Face face : new LinkedList<>(faceGroup))
					if (!Collections
							.disjoint(face.tiles, allEyeCandidatesTiles))
						faceGroup.remove(face);
				if (faceGroup.isEmpty())
					faceGroups.remove(faceGroup);
			}
		}
		faceGroups = new HashSet<>(faceGroups);// 清除因去掉某些group中的元素而导致的相同group

		if (faceGroups.isEmpty())
			return Collections.emptySet();
		if (faceGroups.size() == 1)
			return faceGroups.iterator().next();

		// 算出顺子刻子最多的组合
		Set<Set<Face>> mostFaceGroups = new HashSet<>();
		int mostFaceCount = -1;
		for (Set<Face> faceGroup : faceGroups) {
			int faceCount = faceGroup.size();
			if (faceCount == mostFaceCount) {
				mostFaceGroups.add(faceGroup);
			} else if (faceCount > mostFaceCount) {
				mostFaceCount = faceCount;
				mostFaceGroups.clear();
				mostFaceGroups.add(faceGroup);
			}
		}

		if (mostFaceGroups.isEmpty())
			return Collections.emptySet();
		if (mostFaceGroups.size() == 1)
			return mostFaceGroups.iterator().next();

		// 算出去掉这些顺子刻子后剩下的等牌数量最多者
		Set<Face> mostRemainWaitingFaceGroup = null;
		int mostRemainWaitingCount = -1;
		for (Set<Face> faceGroup : mostFaceGroups) {
			Set<Tile> remainTiles = new HashSet<>(tiles);
			for (Face face : faceGroup)
				remainTiles.removeAll(face.tiles);
			int remainWaitingCount = countWaitingTiles(remainTiles,
					eyeCandidates, myTiles);
			if (remainWaitingCount > mostRemainWaitingCount) {
				mostRemainWaitingCount = remainWaitingCount;
				mostRemainWaitingFaceGroup = faceGroup;
			}
		}
		return mostRemainWaitingFaceGroup;
	}

	/**
	 * Used by {@link #countFaces(Set, Set, PlayerTiles)}. 计算所有可能的顺子刻子搭配组合。
	 * 
	 * @param allFaces
	 * @return
	 */
	// XXX - 算法效率有提升空间
	private Set<Set<Face>> countFaceGroups(Set<Face> allFaces) {
		return countFaceGroups(new ArrayList<>(allFaces), 0,
				Collections.<Face> emptySet());
	}

	/**
	 * Used by {@link #countFaceGroups(Set)}.
	 * 
	 * @param allFaces
	 * @param fromIndex
	 * @param prevFaces
	 * @return
	 */
	private Set<Set<Face>> countFaceGroups(List<Face> allFaces, int fromIndex,
			Set<Face> prevFaces) {
		/*
		 * 递归算法。每层获得前面选出的一个Faces组合prevFaces，返回基于prevFaces的所有可能的Face组合。
		 */

		if (fromIndex >= allFaces.size()) {
			// 如果已递归到结尾，则直接返回只包含prevFaces这一个Faces组合的结果。
			// 不用Collections.singleton，因为返回的集合必须可以添加元素。
			Set<Set<Face>> groups = new HashSet<>();
			groups.add(prevFaces);
			return groups;
		}

		Face forFace = allFaces.get(fromIndex);// 当前层Face

		// 检查当前层Face与前面选出的Faces是否有冲突
		boolean conflictWithPrev = isConflict(prevFaces, forFace);
		if (conflictWithPrev) {
			// 如果有冲突，则当前层Face不可选，直接递归到下一层。
			return countFaceGroups(allFaces, fromIndex + 1, prevFaces);
		} else {
			// 没有冲突
			Set<Set<Face>> faceGroups = new HashSet<>();

			// 先获得包含当前层Face时，递归到下一层选出的所有Face组合
			Set<Face> prevFacesWithCrt = new HashSet<>(prevFaces);
			prevFacesWithCrt.add(forFace);
			faceGroups.addAll(countFaceGroups(allFaces, fromIndex + 1,
					prevFacesWithCrt));

			// 再获得不包含当前层Face时，递归到下一层选出的，并与当前层Face相冲突的Face组合
			Set<Set<Face>> groupFacesWithoutCrt = countFaceGroups(allFaces,
					fromIndex + 1, prevFaces);
			Iterator<Set<Face>> groupFacesItr = groupFacesWithoutCrt.iterator();
			while (groupFacesItr.hasNext()) {
				Set<Face> groupFace = groupFacesItr.next();
				if (!isConflict(groupFace, forFace))
					groupFacesItr.remove();
			}
			faceGroups.addAll(groupFacesWithoutCrt);

			return faceGroups;
		}
	}

	/**
	 * Used by {@link #countFaceGroups(List, int, Set)}.
	 * 
	 * @param faces
	 * @param face
	 * @return
	 */
	private boolean isConflict(Set<Face> faces, Face face) {
		for (Face aFace : faces)
			if (aFace.isConflictWith(face))
				return true;
		return false;
	}

	/**
	 * Used by {@link #countFaces(Set, Set, PlayerTiles)}.
	 * 
	 * @param tile
	 * @param tiles
	 * @return
	 */
	private Set<Set<Tile>> findTripletsFor(Tile tile, SortedSet<Tile> tiles) {
		if (tiles.size() < 3)
			return Collections.emptySet();

		Set<Tile> sameTypeTiles = tile.getType().findTiles(tiles);
		if (sameTypeTiles.size() < 3)
			return Collections.emptySet();
		else if (sameTypeTiles.size() == 3)
			return Collections.singleton(sameTypeTiles);
		else {
			sameTypeTiles.remove(tile);
			Set<Set<Tile>> triplets = new HashSet<>();
			List<Tile> sameTypeTilesList = new ArrayList<>(sameTypeTiles);
			for (int i1 = 0; i1 < sameTypeTilesList.size() - 1; i1++) {
				for (int i2 = 1; i2 < sameTypeTilesList.size(); i2++) {
					Set<Tile> triplet = new HashSet<>();
					triplet.add(sameTypeTilesList.get(i1));
					triplet.add(sameTypeTilesList.get(i2));
					triplet.add(tile);
					triplets.add(triplet);
				}
			}
			return triplets;
		}

	}

	/**
	 * Used by {@link #countFaces(Set, Set, PlayerTiles)}.
	 * 
	 * @param tile
	 * @param tiles
	 * @return
	 */
	private Set<Set<Tile>> findSequencesFor(Tile tile, SortedSet<Tile> tiles) {
		Suit suit = tile.getType().getSuit();
		if (tile.getType().getSuit().isHonor())
			return Collections.emptySet();

		int rank = tile.getType().getRank();
		if (rank >= 8)
			return Collections.emptySet();

		SortedSet<Tile> tilesFromTile = tiles.tailSet(tile);
		if (tilesFromTile.size() < 3)
			return Collections.emptySet();

		Set<Tile> typeInc1Tiles = TileType.get(suit, ++rank).findTiles(
				tilesFromTile);
		if (typeInc1Tiles.isEmpty())
			return Collections.emptySet();

		Set<Tile> typeInc2Tiles = TileType.get(suit, ++rank).findTiles(
				tilesFromTile);
		if (typeInc2Tiles.isEmpty())
			return Collections.emptySet();

		Set<Set<Tile>> sequences = new HashSet<>();
		for (Tile typeInc1Tile : typeInc1Tiles) {
			for (Tile typeInc2Tile : typeInc2Tiles) {
				Set<Tile> sequence = new HashSet<>();
				sequence.add(typeInc1Tile);
				sequence.add(typeInc2Tile);
				sequence.add(tile);
				sequences.add(sequence);
			}
		}
		return sequences;
	}

	/**
	 * 计算等牌数量。等牌多少精确到Tile数（而非TileType数），去掉本玩家已有的Tile。考虑三家碰一家吃。
	 * 
	 * @param tiles
	 *            待计算的牌
	 * @param eyeCandidates
	 *            候选将牌
	 * @param myTiles
	 *            玩家所有的牌
	 * @return 等牌数量
	 */
	private int countWaitingTiles(Set<Tile> tiles,
			Set<Set<Tile>> eyeCandidates, PlayerTiles myTiles) {
		if (!eyeCandidates.isEmpty()) {
			// 如果包含所有将牌候选牌，则选择去掉任意一个将牌候选之后等牌数量最大者
			Set<Tile> allEyeCandidatesTiles = new HashSet<>();
			for (Set<Tile> eyeCandidate : eyeCandidates)
				allEyeCandidatesTiles.addAll(eyeCandidate);
			if (tiles.containsAll(allEyeCandidatesTiles)) {
				int mostCount = -1;
				Set<Tile> tilesCopy = new HashSet<>(tiles);
				for (Set<Tile> eyeCandidate : eyeCandidates) {
					tilesCopy.removeAll(eyeCandidate);
					int count = countWaitingTiles(tilesCopy,
							Collections.<Set<Tile>> emptySet(), myTiles);
					mostCount = Math.max(mostCount, count);
					tilesCopy.addAll(eyeCandidate);
				}
				return mostCount;
			}
		}

		// 计算每种类型的牌有多少张
		Map<TileType, Integer> typeTileCount = new HashMap<>();
		for (Tile tile : tiles) {
			TileType type = tile.getType();
			Integer count = typeTileCount.get(type);
			if (count == null)
				count = 1;
			else
				count++;
			typeTileCount.put(type, count);
		}

		// 计算可碰、可吃的牌
		Set<TileType> pongableTiles = new HashSet<>();
		Set<TileType> chowableTiles = new HashSet<>();
		for (Map.Entry<TileType, Integer> typeTileCountEntry : typeTileCount
				.entrySet()) {
			TileType type = typeTileCountEntry.getKey();
			int count = typeTileCountEntry.getValue();
			if (count >= 2 && count < 4)
				pongableTiles.add(type);
			if (!type.getSuit().isHonor()) {
				if (type.getRank() <= 8
						&& typeTileCount.containsKey(TileType.get(
								type.getSuit(), type.getRank() + 1))) {
					if (type.getRank() >= 2)
						chowableTiles.add(TileType.get(type.getSuit(),
								type.getRank() - 1));
					if (type.getRank() <= 7)
						chowableTiles.add(TileType.get(type.getSuit(),
								type.getRank() + 2));
				}
				if (type.getRank() <= 7
						&& typeTileCount.containsKey(TileType.get(
								type.getSuit(), type.getRank() + 2))) {
					chowableTiles.add(TileType.get(type.getSuit(),
							type.getRank() + 1));
				}
			}
		}

		// 可碰者优先：去掉可吃的牌中与可碰牌冲突者。（因为可碰牌按三倍算）
		chowableTiles.removeAll(pongableTiles);

		// 计算可碰、可吃的牌所剩数量（仅除去本玩家所持牌）
		Map<TileType, Integer> pongOrChowableTilesCount = new HashMap<>();
		for (TileType tileType : pongableTiles)
			pongOrChowableTilesCount.put(tileType,
					getTilesRemain(myTiles, tileType).size());
		for (TileType tileType : chowableTiles)
			pongOrChowableTilesCount.put(tileType,
					getTilesRemain(myTiles, tileType).size());

		// 所有数量相加。可吃牌按一倍算，可碰牌按三倍算。
		int totalCount = 0;
		for (Map.Entry<TileType, Integer> pongOrChowableTileCount : pongOrChowableTilesCount
				.entrySet()) {
			TileType tileType = pongOrChowableTileCount.getKey();
			int count = pongOrChowableTileCount.getValue();

			if (pongableTiles.contains(tileType))
				totalCount += count * 3;
			else if (chowableTiles.contains(tileType))
				totalCount += count;
		}

		return totalCount;
	}

	/**
	 * 判断一张牌是否适合在一个分组内。
	 * 
	 * @param group
	 *            分组
	 * @param tile
	 *            牌
	 * @return 如果适合，返回true；否则返回false。
	 */
	private boolean isSuitableGroup(Set<Tile> group, Tile tile) {
		Tile aTileInGroup = group.iterator().next();
		if (!tile.getType().getSuit().equals(aTileInGroup.getType().getSuit()))
			return false;

		if (tile.getType().getSuit().isHonor())
			return true;

		List<Tile> groupTileList = new ArrayList<>(group);
		Collections.sort(groupTileList);
		int tileRank = tile.getType().getRank();
		if (tileRank < groupTileList.get(0).getType().getRank() - 2
				|| tileRank > groupTileList.get(groupTileList.size() - 1)
						.getType().getRank() + 2)
			return false;

		return true;
	}

	/**
	 * 从指定分组集合中移除指定牌集合，并移除没有牌的组。
	 * 
	 * @param groups
	 *            分组集合
	 * @param tiles
	 *            牌集合
	 * @return 结果分组集合
	 */
	// XXX - 不原地删除空组，因为用迭代器的remove方法删除失败，不知原因
	private Set<Set<Tile>> removeTiles(Set<Set<Tile>> groups, Set<Tile> tiles) {
		Set<Set<Tile>> newGroups = new HashSet<>();
		for (Set<Tile> group : groups) {
			group.removeAll(tiles);
			if (!group.isEmpty()) {
				newGroups.add(group);
			}
		}
		return newGroups;
	}

	/**
	 * 判断指定分组集合中是否包含指定牌。
	 * 
	 * @param groups
	 *            分组集合
	 * @param tile
	 *            牌
	 * @return 如果包含，返回true；否则返回false。
	 */
	private boolean containsTile(Set<Set<Tile>> groups, Tile tile) {
		for (Set<Tile> group : groups)
			if (group.contains(tile))
				return true;
		return false;
	}

	/**
	 * 返回指定类型的牌中，除了指定玩家的牌外剩下的牌。
	 * 
	 * @param playerTiles
	 *            玩家的牌
	 * @param tileType
	 *            牌的类型
	 * @return 牌集合
	 */
	private Set<Tile> getTilesRemain(PlayerTiles playerTiles, TileType tileType) {
		Set<Tile> typeTiles = new HashSet<>(Tile.getTilesForType(tileType));
		for (Cpk cpk : playerTiles.getCpks())
			typeTiles.removeAll(cpk.getTiles());
		typeTiles.removeAll(playerTiles.getAliveTiles());
		return typeTiles;
	}

	/**
	 * 顺子或刻子。按照牌型排序。
	 * 
	 * @author blovemaple
	 */
	private static class Face {
		enum FaceType {
			TRIPLET, SEQUENCE
		}

		FaceType type;
		Set<Tile> tiles;

		Face(FaceType type, Set<Tile> tiles) {
			this.type = type;
			this.tiles = tiles;
		}

		/**
		 * 返回是否与另一个Face冲突（有重复的牌）。
		 * 
		 * @param o
		 *            另一个Face
		 * @return 如果冲突，返回true；否则返回false。
		 */
		boolean isConflictWith(Face o) {
			return !Collections.disjoint(tiles, o.tiles);
		}

		/**
		 * Just for debug.
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[" + type + ":" + tiles + "]";
		}

	}

}
