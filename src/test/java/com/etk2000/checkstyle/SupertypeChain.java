package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.stream.IntStream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * A same-file supertype chain built from a template under {@code inputs/}, whose one link,
 * {@code C1 extends C0}, is repeated as {@code C1} to {@code C<depth>}.
 */
final class SupertypeChain {
	static final int DEEP = 8000;
	private static final int LINK_LINES = 3;
	/**
	 * Small enough that a walk recursing once per level of a {@link #DEEP} chain overflows it,
	 * compiled or interpreted, whatever the platform's default stack size.
	 */
	private static final long SMALL_STACK_BYTES = 256 * 1024L;

	/**
	 * {@code fixture} with its link repeated up to {@code C<depth>}, {@code Sub} extending the last,
	 * {@code create()} returning a new {@code created}, and its {@code var chained} declared as
	 * {@code declared}.
	 */
	@CheckReturnValue
	@Nonnull
	static List<String> build(@Nonnull String fixture, int depth, @Nonnull String declared, @Nonnull String created) throws Exception {
		final var url = requireNonNull(
				SupertypeChain.class.getResource("/com/etk2000/checkstyle/inputs/" + fixture),
				"Test input file not found: " + fixture
		);
		final var template = Files.readAllLines(Path.of(url.toURI()));
		final var link = lineContaining(template, " extends C0<");
		final var lines = new ArrayList<>(template.subList(0, link));
		for (var i = 1; i <= depth; ++i) {
			for (var line : template.subList(link, link + LINK_LINES))
				lines.add(line.replace("C1<", "C" + i + "<").replace("C0<", "C" + (i - 1) + "<"));
		}
		for (var line : template.subList(link + LINK_LINES, template.size())) {
			final var rewritten = line
					.replace("extends C1<String>", "extends C" + depth + "<String>")
					.replace("static C1<String> create", "static " + created + " create")
					.replace("new C1<>()", "new " + created + "()")
					.replace("var chained", declared + " chained");
			lines.add(rewritten);
		}
		return lines;
	}

	@CheckReturnValue
	static int lineContaining(@Nonnull List<String> lines, @Nonnull String text) {
		return IntStream.range(0, lines.size())
				.filter(i -> lines.get(i).contains(text))
				.findFirst()
				.orElseThrow();
	}

	/** What {@code task} returns when run on a thread with only {@link #SMALL_STACK_BYTES} of stack. */
	@CheckReturnValue
	static <T> T onSmallStack(@Nonnull Callable<T> task) throws Exception {
		final var future = new FutureTask<>(task);
		new Thread(null, future, "small-stack", SMALL_STACK_BYTES).start();
		return future.get();
	}

	private SupertypeChain() {
	}
}