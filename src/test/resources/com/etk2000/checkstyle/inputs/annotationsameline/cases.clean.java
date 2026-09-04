package com.etk2000.checkstyle.inputs.annotationsameline;

import java.util.List;
import java.util.function.Consumer;

@interface a {}
@interface A {}
@interface Av {
	String value();
}
@interface B {}
@interface B𝒜 {}
@interface Ä {}

class InputAnnotationSameLineClean {
	@interface Element {
		@A
		@B
		String value();
	}

	enum Constants {
		@A
		@B
		ALPHA
	}

	record Data(@A String name, @A @B int value) {}

	static class Holder {
		static final String NAME = "n";

		class Inner {}
	}

	static class Res implements AutoCloseable {
		@Override
		public void close() {}
	}

	InputAnnotationSameLineClean(@A String param) {}

	void argumentQualifiedConstant(@Av(Holder.NAME) @B String param) {}

	void arrayLevelAnnotation(String @A [] param) {}

	void arrayParamOrdered(@A @B String[] param) {}

	void catchParam() {
		try {
			Thread.sleep(1);
		}
		catch (@A Exception e) {
			e.printStackTrace();
		}
	}

	void duplicateAnnotation(@A @A String param) {}

	void forEach(List<String> list) {
		for (@A var item : list)
			System.out.println(item);
	}

	void forEachMulti(List<String> list) {
		for (@A @B var item : list)
			System.out.println(item);
	}

	void forInit() {
		for (@A var i = 0; i < 10; ++i)
			System.out.println(i);
	}

	void forInitMulti() {
		for (@A @B var i = 0; i < 10; ++i)
			System.out.println(i);
	}

	void lambda() {
		final Consumer<String> c = (@A var s) -> {};
	}

	void lowercaseNameOrdered(@B @a String param) {}

	void method(@A String param) {}

	void multiCatchOrdered() {
		try {
			Thread.sleep(1);
		}
		catch (@A @B IllegalStateException | IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	void multiParam(@A @B String param1, @A String param2) {}

	void nestedTypeAnnotation(Holder.@A Inner param) {}

	void noAnnotation(String param) {}

	void nonAsciiOrdered(@A @Ä String param) {}

	void qualifiedOrdered(@A @java.lang.Deprecated String param) {}

	void receiverParameterOrdered(@A @B InputAnnotationSameLineClean this) {}

	void resourceAnnotation() throws Exception {
		try (@A @B var r = new Res()) {
			System.out.println(r);
		}
	}

	// boundary: the group shares the declaration's first line. The TYPE node sits on the outer dot,
	// a line below, so an anchor taken from it rather than from its leftmost token reports a
	// violation here that no fix could clear
	void splitQualifiedType(
			@A java.util
			.List<String> param
	) {
		System.out.println(param);
	}

	// boundary: stacked field is NOT checked by SameLineCheck (own-line context)
	@A
	int stackedField;

	// boundary: stacked local is NOT checked (own-line context)
	void stackedLocal() {
		@A
		final var x = "test";
	}

	// boundary: stacked method is NOT checked by SameLineCheck (own-line context)
	@A
	void stackedMethod() {}

	void supplementaryOrdered(@B @B𝒜 String param) {}

	void throwsAnnotation() throws @A IllegalStateException {}

	<@B @A T> void typeParameterAnnotation(T param) {}

	void typeUseOrdered(@B List<@A String> param) {}

	void varargsOrdered(@A @B String... param) {}
}