package com.etk2000.checkstyle.inputs.annotationownline;

import java.util.List;
import java.util.function.Consumer;

@interface LocatorA {}
@interface LocatorB {}
@interface LocatorB𝒜 {}
@interface LocatorC {}
@interface LocatorNest {
	LocatorA value();
}
@interface LocatorNum {
	int value();
}
@interface LocatorVal {
	String value();
}

// positions for AnnotationOwnLineLocatorTest to address directly. Several are positions the check
// never reports
class InputOwnLineLocatorPositions {
	enum Constants {
		@LocatorB @LocatorA ALPHA
	}

	record Component(@LocatorA int value) {}

	// the column the BLANK_AFTER is reported at is past the end of the line it is reported on
				@LocatorNum(
1)

	void blankAfterColumnPastEnd() {}

	@LocatorNum(
			1)

	void blankAfterMultiLine() {}

	// a real annotation sits exactly where the BLANK_AFTER is reported, and is not the holder
	@LocatorNest(
	@LocatorA)

	void blankAfterNestedDecoy() {}

	@LocatorA

	void blankAfterSimple() {}

	@LocatorB @LocatorA
	void bothOnOneLine() {}

	void catchParameter() {
		try {
			Thread.sleep(1);
		}
		catch (@LocatorA InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	void decoys() {
		final var inString = "@LocatorB @LocatorA void f()";
		// @LocatorB @LocatorA void f()
		final var inTextBlock = """
				@LocatorB decoyInTextBlock""";
		assert !inString.isEmpty() && !inTextBlock.isEmpty();
	}

	void forEachAndForInit(List<String> rows) {
		for (@LocatorA var row : rows)
			assert row != null;
		for (@LocatorA var i = 0; i < 1; ++i)
			assert i >= 0;
	}

	@LocatorNum({
// the blank line below is what BLANK_INTERNAL is reported on, at column 0

			1})
	void internalBlank() {}

	@LocatorVal("""
			a

			b""")
	void internalBlankInTextBlock() {}

	void lambdaParameter() {
		final Consumer<String> task = (@LocatorA var value) -> {
			assert !value.isEmpty();
		};
		task.accept("x");
	}

	void methodParameter(@LocatorA @LocatorB String param) {
		assert param != null;
	}

	void nestedAnnotationValue(@LocatorNest(@LocatorA) String param) {
		assert param != null;
	}

	@LocatorB
	@LocatorA

	void orderThenBlankAfter() {}

	void patternVariable(Object obj) {
		if (obj instanceof @LocatorA String text)
			assert !text.isEmpty();
	}

	void receiverParameter(@LocatorA InputOwnLineLocatorPositions this) {}

	@LocatorA java.util
			.List<String> splitQualifiedType() {
		return List.of();
	}

	@LocatorC
	@LocatorA
	@LocatorB
	void stackedOutOfOrder() {}

	// a supplementary character ahead of the group puts its code-point column before its char index
	@LocatorB𝒜 @LocatorA
	void supplementaryNameAhead() {}

	void tryWithResources() throws java.io.IOException {
		try (@LocatorA var reader = new java.io.StringReader("x")) {
			assert reader.read() >= 0;
		}
	}

	<@LocatorA T> void typeUse(List<@LocatorA String> rows, String @LocatorA [] array) {
		assert rows != null && array != null;
	}
}