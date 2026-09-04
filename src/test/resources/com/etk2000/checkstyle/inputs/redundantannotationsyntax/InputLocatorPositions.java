package com.etk2000.checkstyle.inputs.redundantannotationsyntax;

@interface LocatorKey {
	String key();
}
@interface LocatorMarker {}
@interface LocatorMarkers {
	LocatorMarker[] value();
}
@interface LocatorMulti {
	String key();
	String value();
}
@interface LocatorNested {
	LocatorMarker value();
}
@interface LocatorNum {
	int value();
}
@interface LocatorStrings {
	String[] value();
}
@interface LocatorÄ {}

// positions for RedundantAnnotationSyntaxLocatorTest to address directly. No violation markers:
// most of these are positions the check never reports
@SuppressWarnings("unused")
class InputLocatorPositions {
	void annotationInsideArrayInit() {
		@LocatorMarkers({@LocatorMarker()})
		final var x = 1;
	}

	void commentAfterAt() {
		@/* c */LocatorMarker()
		final var x = 1;
	}

	void decoyInsideComment() {
		/* @LocatorMarker() */
		final var x = 1;
	}

	void decoyInsideString() {
		final var decoy = "@LocatorMarker()";
	}

	void decoyInsideTextBlock() {
		final var decoy = """
				@LocatorMarker()
				value = y""";
	}

	void emptyArrayInit() {
		@LocatorStrings({})
		final var x = 1;
	}

	void exactColumnEmptyParens() {
		@LocatorMarker()
		final var x = 1;
	}

	void exactColumnExplicitValue() {
		@LocatorNum(value = 1)
		final var x = 1;
	}

	void markerAnnotation() {
		@LocatorMarker
		final var x = 1;
	}

	void multipleMembers() {
		@LocatorMulti(key = "a", value = "b")
		final var x = 1;
	}

	void nestedAnnotations() {
		@LocatorNested(value = @LocatorMarker())
		final var x = 1;
	}

	void nonAsciiName() {
		@LocatorÄ()
		final var x = 1;
	}

	void nonValueKey() {
		@LocatorKey(key = "x")
		final var x = 1;
	}

	void qualifiedName() {
		@com.example.Qualified()
		final var x = 1;
	}

	void shorthandValue() {
		@LocatorStrings("x")
		final var x = 1;
	}

	void spaceAfterAt() {
		@ LocatorMarker()
		final var x = 1;
	}

	void splitQualifiedName() {
		@com.example
				.Split()
		final var x = 1;
	}

	void supplementaryAhead() {
		/* 𝒜 */ @LocatorMarker() final var x = 1;
	}

	void twoAnnotationsOnOneLine() {
		@LocatorMarker() @LocatorStrings("x") final var x = 1;
	}

	void typeUseAnnotation() {
		final var list = new java.util.ArrayList<@LocatorMarker() String>();
	}

	void unaryMinusValue() {
		@LocatorNum(value = -1)
		final var x = 1;
	}

	void valueStatement() {
		final var value = 1;
	}
}