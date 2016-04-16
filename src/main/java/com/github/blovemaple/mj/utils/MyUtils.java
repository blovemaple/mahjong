package com.github.blovemaple.mj.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
	 * 返回指定集合中指定个数的所有元素组合组成的流。
	 * 
	 * @param coll
	 *            指定集合
	 * @param size
	 *            组合中的元素个数
	 * @return 组合Set的流。流中的所有Set都是可以做写操作的。
	 */
	public static <E> Stream<Set<E>> combinationStream(Collection<E> coll,
			int size) {
		if (size == 0)
			return Stream.of(new HashSet<>());
		if (coll == null || coll.isEmpty() || size > coll.size() || size < 0)
			return Stream.empty();

		if (size == 1)
			return coll.stream().map(e -> new HashSet<>(Arrays.asList(e)));
		if (size == coll.size())
			return Stream.of(new HashSet<>(coll));

		List<E> list = (coll instanceof List) ? (List<E>) coll
				: new ArrayList<>(coll);

		return IntStream.rangeClosed(0, coll.size() - size).boxed()
				.flatMap(index -> {
					E first = list.get(index);
					List<E> others = list.subList(index + 1, coll.size());
					return combinationStream(others, size - 1)
							.peek(comb -> comb.add(first));
				});
	}

	/**
	 * 将指定的若干个集合中的元素组成一个新的Set并返回。
	 */
	@SafeVarargs
	public static <E> Set<E> newMergedSet(Collection<E>... collections) {
		return Stream.of(collections).flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}

	/**
	 * 将指定的一个集合中的元素和若干个新元素组成一个新的Set并返回。
	 */
	@SafeVarargs
	public static <E> Set<E> newMergedSet(Collection<E> collection,
			E... newElement) {
		Set<E> set = new HashSet<>(collection);
		set.addAll(Arrays.asList(newElement));
		return set;
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

	private MyUtils() {
	}
}
