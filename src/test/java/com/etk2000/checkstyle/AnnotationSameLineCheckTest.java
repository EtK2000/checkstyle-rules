package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.AnnotationSameLineCheck.AnnotationPlacementTarget;
import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.charset.StandardCharsets;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The arms of {@link AnnotationSameLineCheck} that no valid Java reaches, driven by taking a parsed
 * declaration apart. Everything a compilable file can express lives in
 * {@code inputs/annotationsameline/cases.*.java} and {@link AnnotationSameLineLocatorTest}; these
 * rows exist because the grammar, not a sibling check, is what keeps them unreachable.
 */
public class AnnotationSameLineCheckTest {
	private static final String FIXTURE = "annotationsameline/InputLocatorPositions.java";

	@CheckReturnValue
	@Nullable
	private static DetailAST firstGroup(@Nonnull DetailAST node, int defType, int holderType) {
		if (node.getType() == defType) {
			final var holder = node.findFirstToken(holderType);
			if (holder != null && holder.findFirstToken(TokenTypes.ANNOTATION) != null)
				return holder;
		}
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var found = firstGroup(child, defType, holderType);
			if (found != null)
				return found;
		}
		return null;
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST parseFixture() throws Exception {
		final var url = requireNonNull(
				AnnotationSameLineCheckTest.class.getResource("/com/etk2000/checkstyle/inputs/" + FIXTURE),
				"Test input file not found: " + FIXTURE
		);
		return JavaParser.parseFileText(
				new FileText(new File(url.toURI()), StandardCharsets.UTF_8.name()),
				JavaParser.Options.WITHOUT_COMMENTS
		);
	}

	@CheckReturnValue
	@Nullable
	private static AnnotationPlacementTarget placementOf(@Nonnull DetailAST root, @Nonnull DetailAST holder) {
		final var annotation = holder.findFirstToken(TokenTypes.ANNOTATION);
		assertNotNull(annotation);
		return AnnotationSameLineCheck.placementAt(root, annotation.getLineNo() - 1, annotation.getColumnNo());
	}

	/**
	 * {@code visitToken} reads the holder's parent to decide whether the context is one it checks, so
	 * a node standing on its own has to answer "not mine" rather than dereference null. The parser
	 * never hands one over; only a caller building a tree by hand can.
	 */
	@Test
	public void detachedModifiersIsNotASameLineContext() {
		final var modifiers = new DetailAstImpl();
		modifiers.setType(TokenTypes.MODIFIERS);
		assertDoesNotThrow(() -> new AnnotationSameLineCheck().visitToken(modifiers));
	}

	/**
	 * Whether a {@code VARIABLE_DEF} is a loop's own is read off its parent, so a declaration with
	 * nothing above it has to answer "not mine" rather than dereference null.
	 */
	@Test
	public void detachedVariableDefIsNotASameLineContext() {
		final var variableDef = new DetailAstImpl();
		variableDef.setType(TokenTypes.VARIABLE_DEF);
		final var modifiers = new DetailAstImpl();
		modifiers.setType(TokenTypes.MODIFIERS);
		variableDef.addChild(modifiers);
		assertDoesNotThrow(() -> new AnnotationSameLineCheck().visitToken(modifiers));
	}

	/**
	 * The two annotation checks partition {@code ANNOTATIONS} and {@code MODIFIERS} by context, so
	 * neither may report on the other's fixtures.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
			"annotationownline/cases.clean.java",
			"annotationownline/cases.in.java",
			"annotationownline/cases.out.java",
			"annotationownline/cases.package.clean.java",
			"annotationownline/InputOwnLineLocatorPositions.java"
	})
	public void isSilentOnOwnLineFixtures(String fixture) throws Exception {
		assertTrue(BaseCheckTest.runCheck(AnnotationSameLineCheck.class, fixture).isEmpty());
	}

	/**
	 * {@code placementAt} reads the group's holder off the annotation it finds, so an annotation
	 * standing on its own has none. The parser never produces one; a caller handing over a node it
	 * built itself can.
	 */
	@Test
	public void looseAnnotationWithNoHolderClassifiesAsNothing() {
		final var annotation = new DetailAstImpl();
		annotation.setType(TokenTypes.ANNOTATION);
		annotation.setLineNo(1);
		annotation.setColumnNo(0);
		assertNull(AnnotationSameLineCheck.placementAt(annotation, 0, 0));
	}

	/**
	 * With the type gone the declaration's name is what the group has to share a line with. A
	 * {@code PARAMETER_DEF} always carries a {@code TYPE}, so only unlinking it reaches this.
	 */
	@Test
	public void parameterWithNoTypeAnchorsOnTheIdent() throws Exception {
		final var root = parseFixture();
		final var holder = firstGroup(root, TokenTypes.PARAMETER_DEF, TokenTypes.MODIFIERS);
		assertNotNull(holder);
		final var ident = holder.getParent().findFirstToken(TokenTypes.IDENT);
		assertNotNull(ident);
		((DetailAstImpl) holder).setNextSibling(ident);

		final var target = placementOf(root, holder);
		assertNotNull(target);
		assertEquals(TokenTypes.IDENT, target.anchor().getType());
		assertEquals(ident.getLineNo(), target.anchor().getLineNo());
	}

	/**
	 * With neither a type nor a name the declaration itself is all that is left, and its own text
	 * starts at the group, so the group shares the declaration's line by construction and the check
	 * goes quiet. That is the direction an unprovable case is meant to fail in.
	 */
	@Test
	public void parameterWithNoTypeOrIdentGoesQuiet() throws Exception {
		final var root = parseFixture();
		final var holder = firstGroup(root, TokenTypes.PARAMETER_DEF, TokenTypes.MODIFIERS);
		assertNotNull(holder);
		((DetailAstImpl) holder).setNextSibling(null);
		assertNull(placementOf(root, holder));
	}

	/**
	 * A record component's {@code ANNOTATIONS} is always followed by its type. Without it the
	 * component itself is the anchor, which starts at the group, so the check goes quiet.
	 */
	@Test
	public void recordComponentWithNoSiblingGoesQuiet() throws Exception {
		final var root = parseFixture();
		final var holder = firstGroup(root, TokenTypes.RECORD_COMPONENT_DEF, TokenTypes.ANNOTATIONS);
		assertNotNull(holder);
		((DetailAstImpl) holder).setNextSibling(null);
		assertNull(placementOf(root, holder));
	}
}