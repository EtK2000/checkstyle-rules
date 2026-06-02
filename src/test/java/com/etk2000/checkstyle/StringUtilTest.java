package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class StringUtilTest {
	@Nonnull
	private static Stream<Arguments> replaceSuffixCases() {
		return Stream.of(
				Arguments.of("cases.in.java", ".in.java", ".out.java", "cases.out.java"),
				Arguments.of("cases.out.java", ".in.java", ".out.java", "cases.out.java"),
				Arguments.of("a.in.java.txt", ".in.java", "X", "a.in.java.txt"),
				Arguments.of(".in.java.in.java", ".in.java", "X", ".in.javaX"),
				Arguments.of(".in.java", ".in.java", ".out.java", ".out.java"),
				Arguments.of("cases.in.java", ".in.java", "", "cases"),
				Arguments.of(".java", ".in.java", "X", ".java"),
				Arguments.of("", ".in.java", "X", ""),
				Arguments.of("cases.IN.JAVA", ".in.java", "X", "cases.IN.JAVA"),
				Arguments.of("abc", "", "X", "abcX")
		);
	}

	@MethodSource("replaceSuffixCases")
	@ParameterizedTest
	public void testReplaceSuffix(@Nonnull String text, @Nonnull String suffix, @Nonnull String replacement, @Nonnull String expected) {
		assertEquals(expected, StringUtil.replaceSuffix(text, suffix, replacement));
	}
}