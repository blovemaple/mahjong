package com.github.blovemaple.mj.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * 一些通用工具。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class MyUtils {

	/**
	 * 返回指定集合中指定个数的元素组合（HashSet）组成的流。
	 * 
	 * @param coll
	 *            指定集合
	 * @param size
	 *            组合中的元素个数
	 * @return 组合Set的流
	 */
	public static <E> Stream<Set<E>> combSetStream(Collection<E> coll, int size) {
		return combStream(coll, size, HashSet<E>::new, null, null);
	}

	/**
	 * 返回指定集合中指定个数的元素组合（ArrayList）组成的流。
	 * 
	 * @param coll
	 *            指定集合
	 * @param size
	 *            组合中的元素个数
	 * @return 组合List的流
	 */
	public static <E> Stream<List<E>> combListStream(Collection<E> coll, int size) {
		return combStream(coll, size, ArrayList<E>::new, null, null);
	}

	/**
	 * 返回指定集合中指定个数的元素组合组成的流。
	 * 
	 * @param coll
	 *            指定集合
	 * @param size
	 *            组合中的元素个数。仅当greedy为false时有意义。
	 * @param combCollFactory
	 *            新建集合对象的函数，用于新建元素组合使用的集合，参数为元素集合
	 * @param elementFilter
	 *            组合中所有元素需要符合的条件，null表示不设此条件 TODO 删除此参数
	 * @param elementInCombFilter
	 *            一个组合中的元素需要相互符合的条件，null表示不设此条件
	 * @return 组合的流
	 */
	public static <E, C extends Collection<E>> Stream<C> combStream(Collection<E> coll, int size,
			Function<Collection<E>, C> combCollFactory, Predicate<E> elementFilter,
			BiPredicate<E, E> elementInCombFilter) {
		if (size == 0)
			return Stream.of(combCollFactory.apply(Collections.emptyList()));
		if (coll.isEmpty() || size > coll.size() || size < 0)
			return Stream.empty();

		if (size == 1) {
			if (elementFilter == null)
				return coll.stream().map(e -> combCollFactory.apply(Arrays.asList(e)));
			else
				return coll.stream().filter(elementFilter).map(e -> combCollFactory.apply(Arrays.asList(e)));
		}

		List<E> list;
		if (elementFilter == null) {
			if (elementInCombFilter == null && size == coll.size())
				return Stream.of(combCollFactory.apply(coll));
			list = (coll instanceof List) ? (List<E>) coll : new ArrayList<>(coll);
		} else {
			list = coll.stream().filter(elementFilter).collect(Collectors.toList());
			if (list.isEmpty() || size > list.size())
				return Stream.empty();
			if (elementInCombFilter == null && size == list.size())
				return Stream.of(combCollFactory.apply(list));
		}

		return IntStream.rangeClosed(0, list.size() - size).boxed().flatMap(index -> {
			E first = list.get(index);
			List<E> others = list.subList(index + 1, list.size());
			Predicate<E> othersElementFilter = elementFilter;
			if (elementInCombFilter != null) {
				Predicate<E> othersWithFirstFilter = e -> elementInCombFilter.test(first, e);
				othersElementFilter = othersElementFilter == null ? othersWithFirstFilter
						: othersElementFilter.and(othersWithFirstFilter);
			}
			return combStream(others, size - 1, combCollFactory, othersElementFilter, elementInCombFilter)
					.peek(comb -> comb.add(first));
		});
	}

	/**
	 * 返回指定集合中符合条件的尽量多的元素组合组成的流。
	 * 
	 * @param coll
	 *            指定集合
	 * @param combCollFactory
	 *            新建集合对象的函数，用于新建元素组合使用的集合
	 * @param condition
	 *            一个组合中的任意两个元素需要相互符合的条件
	 * @param limit
	 *            组合中的元素个数上限
	 * @return 组合的流
	 */
	public static <E, C extends Collection<E>> Stream<C> combStreamGreedy(Collection<E> coll,
			Function<Collection<E>, C> combCollFactory, BiPredicate<E, E> condition, int limit) {
		if (coll.isEmpty())
			return Stream.of(combCollFactory.apply(Collections.emptyList()));

		List<E> list = (coll instanceof List) ? (List<E>) coll : new ArrayList<>(coll);

		// 按顺序遍历全部元素，生成冲突元素map和检查点map
		Map<E, Set<E>> conflictsMap = new HashMap<>();
		Map<E, List<E>> checkPoints = new HashMap<>();
		IntStream.range(0, list.size()).forEach(index -> {
			E crtElement = list.get(index);
			// 在右边的元素中找出与之冲突的元素
			List<E> rightConflicts = list.subList(index + 1, list.size()).stream()
					.filter(e -> !condition.test(crtElement, e)).collect(Collectors.toList());

			// 在 当前元素的冲突元素 集合中加入找到的元素
			if (!rightConflicts.isEmpty()) {
				Set<E> conflicts = conflictsMap.get(crtElement);
				if (conflicts == null)
					conflictsMap.put(crtElement, conflicts = new HashSet<>());
				conflicts.addAll(rightConflicts);
			}
			// 在 找到的元素的冲突元素 集合中分别加入当前元素
			rightConflicts.forEach(conflict -> {
				Set<E> conflicts1 = conflictsMap.get(conflict);
				if (conflicts1 == null)
					conflictsMap.put(conflict, conflicts1 = new HashSet<>());
				conflicts1.add(crtElement);
			});

			// 如果找到了冲突元素，则最后一个冲突元素是当前元素的检查点
			if (!rightConflicts.isEmpty()) {
				E checkPoint = rightConflicts.get(rightConflicts.size() - 1);
				List<E> checked = checkPoints.get(checkPoint);
				if (checked == null)
					checkPoints.put(checkPoint, checked = new ArrayList<>());
				checked.add(crtElement);
			}
		});

		// XXX - 带limit的逻辑要优化

		// 进入递归方法，得出没有组合内元素个数限制的流
		Stream<C> lessGreedyStream = combStreamGreedy0(list, limit - 1, combCollFactory, new HashSet<>(), conflictsMap,
				checkPoints);

		if (limit >= coll.size())
			return lessGreedyStream;

		Stream<C> limitStream = combStream(coll, limit, combCollFactory, null, condition);
		return Stream.concat(lessGreedyStream, limitStream);
	}

	/**
	 * @param coll
	 * @param limit
	 * @param combCollFactory
	 * @param selected
	 *            当前递归分支的已选元素
	 * @param conflictsMap
	 *            所有元素 - 与之冲突的元素集合
	 * @param checkPoints
	 *            检查点元素 - 被检查元素<br>
	 *            若B元素是A元素在列表中的最后一个冲突元素，则B是A的检查点<br>
	 *            递归到B处时，如果A和其冲突元素都未选，则必须选B
	 * @return
	 */
	private static <E, C extends Collection<E>> Stream<C> combStreamGreedy0(List<E> coll, int limit,
			Function<Collection<E>, C> combCollFactory, Set<E> selected, Map<E, Set<E>> conflictsMap,
			Map<E, List<E>> checkPoints) {
		if (limit <= 0)
			return Stream.empty();
		if (coll.isEmpty())
			return Stream.of(combCollFactory.apply(Collections.emptyList()));

		E crtElement = coll.get(0);
		Set<E> conflicts = conflictsMap.get(crtElement);
		List<E> remains = coll.subList(1, coll.size());

		// 同时满足以下条件时，当前元素要选：
		// 1. 已选元素数不足limit
		// 2. 没有冲突元素，或者已选元素不存在与当前元素冲突的元素
		Stream<C> selectCrtStream = null;
		if (selected.size() < limit && (conflicts == null || Collections.disjoint(selected, conflicts))) {
			Set<E> newSelected = mergedSet(selected, crtElement);
			selectCrtStream = combStreamGreedy0(remains, limit, combCollFactory, newSelected, conflictsMap, checkPoints)
					.peek(comb -> comb.add(crtElement));
		}

		// 同时满足以下条件时，当前元素不选：
		// 1. 有冲突元素
		// 2. 冲突元素有已选的，或者剩余元素中有冲突元素
		// 3. 如果是检查点，要求所有被检查元素或其冲突元素有已选的
		Stream<C> notSelectCrtStream = null;
		boolean shouldSelectNot = true;
		if (conflicts == null)
			shouldSelectNot = false;
		if (shouldSelectNot) {
			if (Collections.disjoint(selected, conflicts) && Collections.disjoint(remains, conflicts))
				shouldSelectNot = false;
		}
		if (shouldSelectNot) {
			List<E> checkeds = checkPoints.get(crtElement);
			if (checkeds != null && checkeds.stream().anyMatch(checked -> {
				if (selected.contains(checked))
					return false;
				Set<E> conflistsOfChecked = conflictsMap.get(checked);
				if (conflistsOfChecked != null && !Collections.disjoint(selected, conflistsOfChecked))
					return false;
				// 自身和冲突元素都没选
				return true;
			}))
				shouldSelectNot = false;
		}
		if (shouldSelectNot)
			notSelectCrtStream = combStreamGreedy0(remains, limit, combCollFactory, selected, conflictsMap,
					checkPoints);

		if (selectCrtStream == null && notSelectCrtStream == null)
			return Stream.empty();
		if (selectCrtStream != null && notSelectCrtStream == null)
			return selectCrtStream;
		if (selectCrtStream == null && notSelectCrtStream != null)
			return notSelectCrtStream;
		return Stream.concat(selectCrtStream, notSelectCrtStream);
	}

	/**
	 * TODO
	 * 
	 * @param colls
	 * @return
	 */
	public static <E> Stream<List<E>> selectStream(List<? extends Collection<E>> colls) {
		if (colls.isEmpty())
			return Stream.empty();
		Collection<E> firstColl = colls.get(0);
		if (colls.size() == 1)
			return firstColl.stream().map(e -> new ArrayList<E>(Arrays.asList(e)));
		return firstColl.stream().flatMap(e -> selectStream(colls.subList(1, colls.size())).peek(list -> list.add(e)));
	}

	/**
	 * 将指定的若干个集合中的元素组成一个新的集合并返回。
	 */
	@SafeVarargs
	public static <E, C extends Collection<E>> C merged(Supplier<C> collFactory, Collection<E>... collections) {
		return Stream.of(collections).flatMap(Collection::stream).collect(Collectors.toCollection(collFactory));
	}

	/**
	 * 将指定的若干个集合中的元素组成一个新的Set并返回。
	 */
	@SafeVarargs
	public static <E> Set<E> mergedSet(Collection<E>... collections) {
		return merged(HashSet::new, collections);
	}

	/**
	 * 将指定的若干个集合中的元素组成一个新的集合并返回。
	 */
	@SafeVarargs
	public static <E, C extends Collection<E>> C merged(Supplier<C> collFactory, Collection<E> collection,
			E... newElements) {
		C coll = collFactory.get();
		coll.addAll(collection);
		coll.addAll(Arrays.asList(newElements));
		return coll;
	}

	/**
	 * 将指定的一个集合中的元素和若干个新元素组成一个新的Set并返回。
	 */
	@SafeVarargs
	public static <E> Set<E> mergedSet(Collection<E> collection, E... newElements) {
		return merged(HashSet::new, collection, newElements);
	}

	/**
	 * 将指定的一个集合中的元素减去指定元素产生一个新的集合并返回。
	 */
	@SafeVarargs
	public static <E, C extends Collection<E>> C remainColl(Function<Collection<E>, C> newCollConstructor,
			Collection<E> collection, E... removedElement) {
		C newColl = newCollConstructor.apply(collection);
		newColl.removeAll(Arrays.asList(removedElement));
		return newColl;
	}

	/**
	 * 将指定的一个集合中的元素减去指定元素产生一个新的集合并返回。
	 */
	public static <E, C extends Collection<E>> C remainColl(Function<Collection<E>, C> newCollConstructor,
			Collection<E> collection, Collection<E> removedElement) {
		C newColl = newCollConstructor.apply(collection);
		newColl.removeAll(removedElement);
		return newColl;
	}

	/**
	 * 将指定的流按照标识符去重。标识符用指定的函数获取。
	 * 
	 * @param stream
	 *            流
	 * @param identifierFunction
	 *            获取标识符的函数
	 * @return 去重后的流
	 */
	public static <E> Stream<E> distinctBy(Stream<E> stream, Function<E, ?> identifierFunction) {
		Set<Integer> existIdHashCodes = Collections.synchronizedSet(new HashSet<>());
		return stream.filter(e -> existIdHashCodes.add(identifierFunction.apply(e).hashCode()));
	}

	/**
	 * 将指定的集合流按照标识符去重。标识符用指定的函数获取。
	 * 
	 * @param stream
	 *            集合流
	 * @param identifierFunction
	 *            获取标识符的函数
	 * @return 去重后的集合流
	 */
	public static <E, C extends Collection<E>> Stream<C> distinctCollBy(Stream<C> stream,
			Function<E, ?> identifierFunction) {
		Set<Integer> existIdHashCodes = Collections.synchronizedSet(new HashSet<>());
		return stream.filter(eSet -> {
			int idHashCode = eSet.stream().map(identifierFunction).mapToInt(Object::hashCode).sum();
			return existIdHashCodes.add(idHashCode);
		});
	}

	/**
	 * 判断指定两个集合中的元素是否没有任何标识符重复。标识符用指定的函数获取。
	 * 
	 * @param c1
	 *            集合1
	 * @param c2
	 *            集合2
	 * @param identifierFunction
	 *            获取标识符的函数
	 * @return 没有重复返回true，否则返回false。
	 */
	public static <E> boolean disjointBy(Collection<E> c1, Collection<E> c2, Function<E, ?> identifierFunction) {
		if (c1.isEmpty() || c2.isEmpty())
			return true;
		Collection<E> contains = c2;
		Collection<E> iterate = c1;
		if (c1.size() > c2.size()) {
			iterate = c2;
			contains = c1;
		}
		Set<?> ids = contains.stream().map(identifierFunction).collect(Collectors.toSet());
		return iterate.stream().map(identifierFunction).noneMatch(ids::contains);
	}

	/**
	 * 返回指定字符串在命令行终端里的宽度（一个汉字算两个英文字符的宽度）。
	 */
	public static int strWidth(String str) {
		return str.replaceAll("[^\\x00-\\xff]", "**").length();
	}

	/**
	 * 如果str不够指定长度，则在后面补空格。
	 */
	public static String fixedWidth(String str, int width) {
		int strWidth = strWidth(str);
		if (strWidth >= width)
			return str;
		StringBuilder newStr = new StringBuilder(str);
		IntStream.range(0, width - strWidth).forEach(i -> newStr.append(' '));
		return newStr.toString();
	}

	public static long combCount(long total, int select) {
		long totalP = LongStream.rangeClosed(total - select + 1, total).reduce(Math::multiplyExact).getAsLong();
		long selectP = 1;// LongStream.rangeClosed(1,
							// select).reduce(Math::multiplyExact).getAsLong();
		return totalP / selectP;
	}

	public static void testTime(String name, Runnable runnable) {
		long startTime = System.currentTimeMillis();
		runnable.run();
		System.out.println(name + " " + (System.currentTimeMillis() - startTime));
	}

	private MyUtils() {
	}
}
