package com.github.blovemaple.mj.utils;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class LambdaUtils {

	@FunctionalInterface
	public interface Consumer_WithExceptions<T, E extends Exception> {
		void accept(T t) throws E;
	}

	@FunctionalInterface
	public interface BiConsumer_WithExceptions<T, U, E extends Exception> {
		void accept(T t, U u) throws E;
	}

	@FunctionalInterface
	public interface Function_WithExceptions<T, R, E extends Exception> {
		R apply(T t) throws E;
	}
	
	@FunctionalInterface
	public interface BiFunction_WithExceptions<T, U, R, E extends Exception> {
		R apply(T t, U u) throws E;
	}

	@FunctionalInterface
	public interface Predicate_WithExceptions<T, E extends Exception> {
		boolean apply(T t) throws E;
	}

	@FunctionalInterface
	public interface Supplier_WithExceptions<T, E extends Exception> {
		T get() throws E;
	}

	public static <T, E extends Exception> Consumer<T> rethrowConsumer(
			Consumer_WithExceptions<T, E> consumer) throws E {
		return t -> {
			try {
				consumer.accept(t);
			} catch (Exception exception) {
				throwActualException(exception);
			}
		};
	}

	public static <T, U, E extends Exception> BiConsumer<T, U> rethrowBiConsumer(
			BiConsumer_WithExceptions<T, U, E> biConsumer) throws E {
		return (t, u) -> {
			try {
				biConsumer.accept(t, u);
			} catch (Exception exception) {
				throwActualException(exception);
			}
		};
	}

	public static <T, R, E extends Exception> Function<T, R> rethrowFunction(
			Function_WithExceptions<T, R, E> function) throws E {
		return t -> {
			try {
				return function.apply(t);
			} catch (Exception exception) {
				throwActualException(exception);
				return null;
			}
		};
	}

	public static <T, U, R, E extends Exception> BiFunction<T, U, R> rethrowBiFunction(
			BiFunction_WithExceptions<T, U, R, E> function) throws E {
		return (t, u) -> {
			try {
				return function.apply(t, u);
			} catch (Exception exception) {
				throwActualException(exception);
				return null;
			}
		};
	}

	public static <T, E extends Exception> Predicate<T> rethrowPredicate(
			Predicate_WithExceptions<T, E> predicate) throws E {
		return t -> {
			try {
				return predicate.apply(t);
			} catch (Exception exception) {
				throwActualException(exception);
				return false;
			}
		};
	}

	public static <T, E extends Exception> Supplier<T> rethrowSupplier(
			Supplier_WithExceptions<T, E> supplier) throws E {
		return () -> {
			try {
				return supplier.get();
			} catch (Exception exception) {
				throwActualException(exception);
				return null;
			}
		};
	}

	@SuppressWarnings("unchecked")
	private static <E extends Exception> void throwActualException(
			Exception exception) throws E {
		throw (E) exception;
	}

}