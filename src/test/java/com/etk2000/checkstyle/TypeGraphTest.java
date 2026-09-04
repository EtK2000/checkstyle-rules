package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Direct tests for the same-file inheritance graph. Driving it through
 * {@code PreferCollectionInterfaceCheck} only ever reports a violation count, which cannot tell a
 * missing edge from a suppressed one, so the graph's own answers are asserted here.
 */
public class TypeGraphTest {
	private static final String FIXTURE = "prefercollectioninterface/cases.clean.java";

	@CheckReturnValue
	@Nullable
	private static DetailAST find(@Nullable DetailAST node, @Nonnull String typeName) {
		for (var current = node; current != null; current = current.getNextSibling()) {
			if (current.getType() == TokenTypes.OBJBLOCK && typeName.equals(AstUtil.getEnclosingTypeName(current)))
				return current;

			final var found = find(current.getFirstChild(), typeName);
			if (found != null)
				return found;
		}
		return null;
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> names(@Nonnull List<DetailAST> bodies) {
		final var result = new ArrayList<String>();
		for (var body : bodies)
			result.add(AstUtil.getEnclosingTypeName(body));
		return result;
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST objBlockOf(@Nonnull DetailAST root, @Nonnull String typeName) {
		return requireNonNull(find(root, typeName), typeName + " not found in " + FIXTURE);
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST parseFixture() throws Exception {
		final var url = TypeGraphTest.class.getResource("/com/etk2000/checkstyle/inputs/" + FIXTURE);
		requireNonNull(url, "Test input file not found: " + FIXTURE);
		return JavaParser.parseFile(new File(url.toURI()), JavaParser.Options.WITHOUT_COMMENTS);
	}

	@Test
	public void testRelatedFindsAForwardDeclaredSupertype() throws Exception {
		final var root = parseFixture();
		final var related = new TypeGraph(root).related(objBlockOf(root, "InputCollectionInterfaceCollapseForwardSub"));
		assertEquals(List.of("InputCollectionInterfaceCollapseForwardSub"), names(related.descendants()));
		assertEquals(List.of("InputCollectionInterfaceCollapseForwardBase"), names(related.ancestors()));
	}

	@Test
	public void testRelatedFindsALocalSubtype() throws Exception {
		final var root = parseFixture();
		final var related = new TypeGraph(root).related(objBlockOf(root, "GraphLocalBase"));
		assertEquals(List.of("GraphLocalBase", "GraphLocalSub"), names(related.descendants()));
		assertEquals(List.of(), names(related.ancestors()));
	}

	@Test
	public void testRelatedFindsAnAnonymousSubtype() throws Exception {
		final var root = parseFixture();
		final var related = new TypeGraph(root).related(objBlockOf(root, "InputCollectionInterfaceGraphAnonymousBase"));
		final var descendants = related.descendants();
		assertEquals(2, descendants.size(), names(descendants).toString());
		// the anonymous body has no type name, so the edge is pinned by its parent token instead
		assertEquals(TokenTypes.LITERAL_NEW, descendants.get(1).getParent().getType());
		assertEquals(List.of(), names(related.ancestors()));
	}

	@Test
	public void testRelatedFindsAnEnumConstantBody() throws Exception {
		final var root = parseFixture();
		final var related = new TypeGraph(root).related(objBlockOf(root, "InputCollectionInterfaceGraphEnum"));
		assertEquals(2, related.descendants().size(), names(related.descendants()).toString());
		assertEquals(List.of(), names(related.ancestors()));
	}

	@Test
	public void testRelatedFindsASubtype() throws Exception {
		final var root = parseFixture();
		final var related = new TypeGraph(root).related(objBlockOf(root, "InputCollectionInterfaceCollapseForwardBase"));
		assertEquals(
				List.of("InputCollectionInterfaceCollapseForwardBase", "InputCollectionInterfaceCollapseForwardSub"),
				names(related.descendants())
		);
		assertEquals(List.of(), names(related.ancestors()));
	}

	@Test
	public void testRelatedIgnoresAnOffFileSupertype() throws Exception {
		final var root = parseFixture();
		final var related = new TypeGraph(root).related(objBlockOf(root, "InputCollectionInterfaceCrossFileCollapse"));
		assertEquals(List.of("InputCollectionInterfaceCrossFileCollapse"), names(related.descendants()));
		assertEquals(List.of(), names(related.ancestors()));
	}

	@Test
	public void testRelatedIsSelfOnlyForAnUnrelatedType() throws Exception {
		final var root = parseFixture();
		final var related = new TypeGraph(root).related(objBlockOf(root, "InputCollectionInterfaceClean"));
		assertEquals(List.of("InputCollectionInterfaceClean"), names(related.descendants()));
		assertEquals(List.of(), names(related.ancestors()));
	}

	@Test
	public void testRelatedReachesASubtypesOtherSupertype() throws Exception {
		final var root = parseFixture();
		final var related = new TypeGraph(root).related(objBlockOf(root, "InputCollectionInterfaceGraphDiamondBase"));
		assertEquals(
				List.of("InputCollectionInterfaceGraphDiamondBase", "InputCollectionInterfaceGraphDiamondSub"),
				names(related.descendants())
		);
		// reached only because the ancestor walk is seeded from every descendant, not just the body asked about
		assertEquals(List.of("InputCollectionInterfaceGraphDiamondIface"), names(related.ancestors()));
	}

	@Test
	public void testRelatedTerminatesOnAnInheritanceCycle() throws Exception {
		final var root = parseFixture();
		final var related = new TypeGraph(root).related(objBlockOf(root, "InputCollectionInterfaceCollapseCycleFirst"));
		assertEquals(
				List.of("InputCollectionInterfaceCollapseCycleFirst", "InputCollectionInterfaceCollapseCycleSecond"),
				names(related.descendants())
		);
		assertEquals(List.of(), names(related.ancestors()));
	}
}