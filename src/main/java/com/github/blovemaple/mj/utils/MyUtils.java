package com.github.blovemaple.mj.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 一些通用工具。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class MyUtils {

	/**
	 * 返回指定集合中指定个数的元素组合（集合）组成的流。
	 * 
	 * @param coll
	 *            指定集合
	 * @param size
	 *            组合中的元素个数
	 * @return 组合Set的流。流中的所有Set都是可以做写操作的。
	 */
	public static <E> Stream<Set<E>> combinationSetStream(Collection<E> coll, int size) {
		return combinationStream(coll, size, HashSet<E>::new, null, null);
	}

	/**
	 * 返回指定集合中指定个数的元素组合（列表）组成的流。
	 * 
	 * @param coll
	 *            指定集合
	 * @param size
	 *            组合中的元素个数
	 * @return 组合Set的流。流中的所有Set都是可以做写操作的。
	 */
	public static <E> Stream<List<E>> combinationListStream(Collection<E> coll, int size) {
		return combinationStream(coll, size, ArrayList<E>::new, null, null);
	}

	/**
	 * 返回指定集合中指定个数的元素组合组成的流。
	 * 
	 * @param coll
	 *            指定集合
	 * @param size
	 *            组合中的元素个数
	 * @param combCollFactory
	 *            新建集合对象的函数，用于新建元素组合使用的集合，参数为元素集合
	 * @param elementFilter
	 *            组合中所有元素需要符合的条件，null表示不设此条件
	 * @param elementInCombFilter
	 *            一个组合中的元素需要相互符合的条件，null表示不设此条件
	 * @return 组合Set的流。流中的所有Set都是可以做写操作的。
	 */
	public static <E, C extends Collection<E>> Stream<C> combinationStream(Collection<E> coll, int size,
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
			return combinationStream(others, size - 1, combCollFactory, othersElementFilter, elementInCombFilter)
					.peek(comb -> comb.add(first));
		});
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
	public static <E, C extends Collection<E>> C newRemainColl(Function<Collection<E>, C> newCollConstructor,
			Collection<E> collection, E... removedElement) {
		C newColl = newCollConstructor.apply(collection);
		newColl.removeAll(Arrays.asList(removedElement));
		return newColl;
	}

	/**
	 * 将指定的一个集合中的元素减去指定元素产生一个新的集合并返回。
	 */
	public static <E, C extends Collection<E>> C newRemainColl(Function<Collection<E>, C> newCollConstructor,
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

	public static void testTime(String name, Runnable runnable) {
		long startTime = System.currentTimeMillis();
		runnable.run();
		System.out.println(name + " " + (System.currentTimeMillis() - startTime));
	}

	private MyUtils() {
	}
}
