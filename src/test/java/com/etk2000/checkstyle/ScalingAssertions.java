package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/** Asserts that the code under measure takes time linear in the size of its input. */
public final class ScalingAssertions {
	/** Builds the input of {@code size}, once per size and outside the measure. */
	@FunctionalInterface
	public interface SizedInput<T> {
		@Nonnull
		T of(int size) throws Exception;
	}

	/** One run of the code under measure on the {@code input} of {@code size}, returning the nanoseconds it took. */
	@FunctionalInterface
	public interface TimedRun<T> {
		long nanos(int size, @Nonnull T input) throws Exception;
	}

	private static final int MAX_REPEATS = 1000, WARMUP_RUNS = 3;
	private static final long LARGEST_LIMIT = TimeUnit.SECONDS.toNanos(5), MEASURABLE = TimeUnit.MILLISECONDS.toNanos(20);

	/**
	 * Times {@code run} at sizes doubling from {@code smallest} up to {@code largest}, best of three
	 * each, for a minute at most: each size may take less than three times as long as the one before
	 * (a quadratic walk takes four), and the last under five seconds. A measure sums as many runs as
	 * the warmed-up smallest size needs to pass twice {@link #MEASURABLE}, the same count at every
	 * size, so a small input still times the code rather than the clock. The last size stops at its
	 * first measure within both bounds, since the best of three can only pass where one of them does.
	 */
	public static <T> void assertScalesLinearly(int smallest, int largest, @Nonnull SizedInput<T> inputs, @Nonnull TimedRun<T> run) throws Exception {
		final var smallestInput = inputs.of(smallest);
		for (var warmup = 0; warmup < WARMUP_RUNS; ++warmup)
			run.nanos(smallest, smallestInput);
		final var repeats = (int) Math.min(MAX_REPEATS, 2 * MEASURABLE / Math.max(1, run.nanos(smallest, smallestInput)) + 1);
		final var deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(60);
		var previous = -1L;
		for (var size = smallest; size <= largest && System.nanoTime() < deadline; size *= 2) {
			final var input = size == smallest ? smallestInput : inputs.of(size);
			final var passing = size > largest / 2 ? Math.min(LARGEST_LIMIT, previous > MEASURABLE ? previous * 3 : Long.MAX_VALUE) : Long.MIN_VALUE;
			var best = Long.MAX_VALUE;
			for (var attempt = 0; attempt < 3 && best >= passing; ++attempt) {
				var measure = 0L;
				for (var repeat = 0; repeat < repeats; ++repeat)
					measure += run.nanos(size, input);
				best = Math.min(best, measure);
			}
			if (previous > MEASURABLE)
				assertEquals(-1, Long.compare(best, previous * 3), "size " + size + " took " + best / 1_000_000 + " ms after " + previous / 1_000_000 + " ms");
			previous = best;
		}
		assertEquals(-1, Long.compare(previous, LARGEST_LIMIT), "the largest size took " + previous / 1_000_000 + " ms");
	}

	private ScalingAssertions() {
	}
}