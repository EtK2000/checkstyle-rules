package com.etk2000.checkstyle.inputs.preferstaticimport;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Positions for {@code PreferStaticImportLocatorTest}, which addresses them directly rather than
 * through a reported violation. Nothing here is a slice: the file carries no markers and is never
 * fix-run.
 */
public class InputLocatorPositions {
	/**
	 * Takes the simple name for the whole file, which is what gives the fully-qualified rows a reason
	 * to be written that way.
	 */
	static class Objects {
	}

	Object text;

	boolean arrayAccessReceiver(Objects[] holders) {
		return holders[0].isNull(text);
	}

	boolean blockCommentAfterDot() {
		return Objects./* c */isNull(text);
	}

	boolean blockCommentBeforeDot() {
		return Objects/* c */.isNull(text);
	}

	boolean callReceiver() {
		return supplier().isNull(text);
	}

	String classLiteralReceiver() {
		return Objects.class.getSimpleName();
	}

	boolean dotOnTheNextLine() {
		return Objects
				.isNull(text);
	}

	boolean fullyQualifiedReceiver() {
		return java.util.Objects.isNull(text);
	}

	boolean methodOnTheNextLine() {
		return Objects.
				isNull(text);
	}

	boolean methodReference(List<String> items) {
		return items.stream().anyMatch(Objects::isNull);
	}

	Object nestedCalls() {
		return Objects.requireNonNull(Objects.requireNonNull(text));
	}

	int nonCandidateMethod() {
		return Objects.hash(text);
	}

	int nonCandidateReceiver() {
		return Integer.parseInt("1");
	}

	Predicate<String> predicateReceiver() {
		return Predicate.not(String::isEmpty);
	}

	boolean shadowedReceiver(Object Objects) {
		return Objects.isNull(text);
	}

	boolean spaceAfterDot() {
		return Objects. isNull(text);
	}

	boolean spaceBeforeDot() {
		return Objects .isNull(text);
	}

	boolean spacedDot() {
		return Objects . isNull(text);
	}

	Object staticallyImportedCall() {
		return requireNonNull(text);
	}

	int stringLiteralSpelling() {
		return "Objects.isNull".length();
	}

	String supplementaryCharAhead() {
		return "𝐀" + Objects.isNull(text);
	}

	Objects supplier() {
		return new Objects();
	}

	String textBlockSpelling() {
		return """
				Objects.isNull
				""";
	}

	boolean tightDot() {
		return Objects.isNull(text);
	}

	boolean twoCallsOnOneLine(Object other) {
		return Objects.isNull(text) || Objects.isNull(other);
	}

	Map<String, Integer> typeWitness(List<String> keys) {
		return keys.stream().collect(Collectors.<String, Integer>toMap(k -> k, k -> 1));
	}
}