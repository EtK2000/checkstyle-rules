package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public class PreferLambdaCheckTest {
	private static final int NESTING_DEPTH = 100_000;

	@CheckReturnValue
	@Nonnull
	private static DetailAstImpl node(int type, @Nonnull String text) {
		final var built = new DetailAstImpl();
		built.setType(type);
		built.setText(text);
		return built;
	}

	@Test
	public void deeplyNestedAnonymousBodyDoesNotOverflow() {
		var deepest = node(TokenTypes.IDENT, "x");
		for (var i = 0; i < NESTING_DEPTH; ++i) {
			final var wrapper = node(TokenTypes.LNOT, "!");
			wrapper.addChild(deepest);
			deepest = wrapper;
		}

		final var method = node(TokenTypes.METHOD_DEF, "METHOD_DEF");
		method.addChild(deepest);

		final var objBlock = node(TokenTypes.OBJBLOCK, "OBJBLOCK");
		objBlock.addChild(node(TokenTypes.LCURLY, "{"));
		objBlock.addChild(method);
		objBlock.addChild(node(TokenTypes.RCURLY, "}"));

		final var literalNew = node(TokenTypes.LITERAL_NEW, "new");
		literalNew.addChild(node(TokenTypes.IDENT, "NoSuchFunctionalInterfaceXyz"));
		literalNew.addChild(objBlock);

		final var check = new PreferLambdaCheck();
		check.beginTree(null);
		assertDoesNotThrow(() -> check.visitToken(literalNew));
	}
}