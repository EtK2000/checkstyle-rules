package com.etk2000.checkstyle.gradle.fix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LambdaParameterTypeFixerTest {
	private final CheckstyleFixer fixer = new LambdaParameterTypeFixer();

	@Test
	public void testColumnOutOfBounds() {
		final var lines = new ArrayList<>(List.of("\t\tlist.forEach((String x) -> {});"));
		assertNull(fixer.fix(lines, 0, 100));
	}

	@Test
	public void testLineIndexOutOfBounds() {
		final var lines = new ArrayList<>(List.of("\t\tlist.forEach((String x) -> {});"));
		assertNull(fixer.fix(lines, 5, 0));
	}

	@Test
	public void testNegativeColumn() {
		final var lines = new ArrayList<>(List.of("\t\tlist.forEach((String x) -> {});"));
		assertNull(fixer.fix(lines, 0, -1));
	}

	@Test
	public void testNegativeLineIndex() {
		final var lines = new ArrayList<>(List.of("\t\tlist.forEach((String x) -> {});"));
		assertNull(fixer.fix(lines, -1, 0));
	}

	@Test
	public void testNoArrow() {
		final var lines = new ArrayList<>(List.of("\t\tmethod((String x));"));
		assertNull(fixer.fix(lines, 0, 10));
	}

	@Test
	public void testNoOpenParen() {
		// arrow found but no paren before it (naked param context — fixer can't fix)
		final var lines = new ArrayList<>(List.of("\t\tx -> System.out.println(x);"));
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void testRemoveParensExpressionBody() {
		final var lines = new ArrayList<>(List.of("\t\tlist.forEach((x) -> System.out.println(x));"));
		final var result = fixer.fix(lines, 0, 15);
		assertNotNull(result);
		assertEquals("\t\tlist.forEach(x -> System.out.println(x));", result.replacement().getFirst());
	}

	@Test
	public void testRemoveTypeMultiParam() {
		final var lines = new ArrayList<>(List.of("\t\tlist.sort((String x, String y) -> x.compareTo(y));"));
		final var result = fixer.fix(lines, 0, 12);
		assertNotNull(result);
		assertEquals("\t\tlist.sort((x, y) -> x.compareTo(y));", result.replacement().getFirst());
	}

	@Test
	public void testRemoveTypeSingleParam() {
		final var lines = new ArrayList<>(List.of("\t\tlist.forEach((String x) -> System.out.println(x));"));
		final var result = fixer.fix(lines, 0, 16);
		assertNotNull(result);
		assertEquals("\t\tlist.forEach(x -> System.out.println(x));", result.replacement().getFirst());
	}

	@Test
	public void testRemoveVarSingleParam() {
		final var lines = new ArrayList<>(List.of("\t\tlist.forEach((var x) -> System.out.println(x));"));
		final var result = fixer.fix(lines, 0, 16);
		assertNotNull(result);
		assertEquals("\t\tlist.forEach(x -> System.out.println(x));", result.replacement().getFirst());
	}

	@Test
	public void testReplaceTypeWithVarAnnotated() {
		final var lines = new ArrayList<>(List.of("\t\tlist.forEach((@A String x) -> System.out.println(x));"));
		final var result = fixer.fix(lines, 0, 16);
		assertNotNull(result);
		assertEquals("\t\tlist.forEach((@A var x) -> System.out.println(x));", result.replacement().getFirst());
	}

	@Test
	public void testReplaceTypeWithVarAnnotatedMultiParam() {
		final var lines = new ArrayList<>(List.of("\t\tlist.sort((@A String x, String y) -> x.compareTo(y));"));
		final var result = fixer.fix(lines, 0, 12);
		assertNotNull(result);
		assertEquals("\t\tlist.sort((@A var x, var y) -> x.compareTo(y));", result.replacement().getFirst());
	}

	@Test
	public void testReplaceTypeWithVarBothAnnotated() {
		final var lines = new ArrayList<>(List.of("\t\tlist.sort((@A String x, @B String y) -> x.compareTo(y));"));
		final var result = fixer.fix(lines, 0, 12);
		assertNotNull(result);
		assertEquals("\t\tlist.sort((@A var x, @B var y) -> x.compareTo(y));", result.replacement().getFirst());
	}

	@Test
	public void testReplaceTypeWithVarMultiAnnotation() {
		final var lines = new ArrayList<>(List.of("\t\tlist.forEach((@A @B String x) -> System.out.println(x));"));
		final var result = fixer.fix(lines, 0, 16);
		assertNotNull(result);
		assertEquals("\t\tlist.forEach((@A @B var x) -> System.out.println(x));", result.replacement().getFirst());
	}

	@Test
	public void testReplaceTypeWithVarSecondAnnotated() {
		final var lines = new ArrayList<>(List.of("\t\tlist.sort((String x, @A String y) -> x.compareTo(y));"));
		final var result = fixer.fix(lines, 0, 12);
		assertNotNull(result);
		assertEquals("\t\tlist.sort((var x, @A var y) -> x.compareTo(y));", result.replacement().getFirst());
	}
}