package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

class AstUtil {
	@CheckReturnValue
	@Nonnull
	static String annotationName(@Nonnull DetailAST annotation) {
		final var ident = annotation.findFirstToken(TokenTypes.IDENT);
		if (ident != null)
			return ident.getText();

		// qualified name like @androidx.annotation.NonNull — use last segment
		final var dot = annotation.findFirstToken(TokenTypes.DOT);
		if (dot != null) {
			var last = dot.getFirstChild();
			while (last.getNextSibling() != null)
				last = last.getNextSibling();
			return last.getText();
		}
		return "";
	}

	@CheckReturnValue
	static int lastLine(@Nonnull DetailAST ast) {
		var last = ast.getLineNo();
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var childLast = lastLine(child);
			if (childLast > last)
				last = childLast;
		}
		return last;
	}
}