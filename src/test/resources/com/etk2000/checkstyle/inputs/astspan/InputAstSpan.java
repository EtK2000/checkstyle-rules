package com.etk2000.checkstyle.inputs.astspan;

import java.util.List;

public class InputAstSpan {
	static final class Names {
		static final String EXPRESSION = "y";
		static final String IDENT = "x";

		static String expression() {
			return EXPRESSION;
		}
	}

	public String identifierSpelledLikeItsOwnTokenName() {
		return Names.IDENT;
	}

	public void multiLineCall(List<String> destinationCollectionWithALongName, List<String> sourceCollectionWithALongName) {
		destinationCollectionWithALongName.addAll(
				sourceCollectionWithALongName
		);
	}

	public void noArgCall(List<String> destination) {
		destination.clear();
	}

	public void singleLineCall(List<String> destination, List<String> source) {
		destination.addAll(source);
	}

	public void supplementaryBeforeOperand(List<String> destination, String marker) {
		destination.add("𝐀" + marker);
	}

	public String textBlock() {
		return """
				content
				""";
	}
}