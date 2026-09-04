package com.etk2000.checkstyle.inputs.redundantcast;

import java.io.Serializable;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * Positions for {@code RedundantCastLocatorTest}, which addresses them directly rather than through
 * a reported violation. Nothing here is a slice: the file carries no markers and is never fix-run.
 */
public class InputLocatorPositions {
	static String takes(String s) {
		return s;
	}

	String argumentParen(String s) {
		return takes((String) s);
	}

	int arrayDepthTwo(int[][] a) {
		return ((int[][]) a).length;
	}

	String bareWrap(String s) {
		return ((String) s);
	}

	int castAtColumnZero(String s) {
		return
((String) s).length();
	}

	int castInsideACommentAndAString(String s) {
		final var text = "(String) u";
		return ((String) s).length() + text.length(); // (String) t
	}

	String constructorArgumentParen(String s) {
		return new StringBuilder((String) s).toString();
	}

	int doubleWrap(String s) {
		return (((String) s)).length();
	}

	int ifConditionParenIsNotAWrap(boolean b) {
		if ((boolean) b)
			return 0;
		return 1;
	}

	Runnable intersectionCast(Runnable r) {
		return (Runnable & Serializable) r;
	}

	IntSupplier methodReferenceReceiver(String s) {
		return ((String) s)::length;
	}

	int nestedCastKeepsItsWrap(String s) {
		return ((String) (String) s).length();
	}

	Object qualifiedCastType(Object o) {
		return (java.util.List) o;
	}

	int receiverWrap(String s) {
		return ((String) s).length();
	}

	int receiverWrapSpanningLines(String s) {
		return ((String) s)
				.length();
	}

	char switchSelector(char c) {
		return switch ((char) c) {
			case 'a' -> 'b';
			default -> c;
		};
	}

	void synchronizedLock(Object o) {
		synchronized ((Object) o) {
			o.notifyAll();
		}
	}

	int twoCastsOnOneLine(String a, String b) {
		return ((String) a).length() + ((String) b).length();
	}

	List<String> typeArgumentsInTheName(List<String> list) {
		return (List<String>) list;
	}
}