package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The positional predicate tests need a buffer that parses, which no fragment in the corpus is, so
 * their refusal and unresolved arms are only reachable from here.
 */
public class PreferVarCheckTest {
	private static final String CLEAN_FIXTURE = "prefervar/cases.clean.java";
	private static final String DOUBLING_CHAIN_FIXTURE = "prefervar/InputSupertypeChainDoubling.java";
	private static final String MSG_ERROR = "Local variable must use 'var' instead of an explicit type.";
	private static final String MSG_FOREACH = "For-each variable must use 'var' instead of an explicit type.";
	private static final String MSG_WARNING = "Local variable should use 'var' instead of an explicit type.";
	private static final String NESTED_INNER_FIXTURE = "prefervar/InputNestedInnerSubclasses.java";
	private static final String SUPERTYPE_CHAIN_FIXTURE = "prefervar/InputSupertypeChain.java";
	private static final String VAR_CHAIN_FIXTURE = "prefervar/InputVarChain.java";

	private static void assertChainReported(@Nonnull List<String> lines, @Nonnull SeverityLevel severity, @Nonnull String message) throws Exception {
		final var events = SupertypeChain.onSmallStack(() -> BaseCheckTest.runCheckInline(PreferVarCheck.class, String.join("\n", lines)));
		assertOnlyChainedReported(events, lines, severity, message);
	}

	private static void assertOnlyChainedReported(
			@Nonnull List<AuditEvent> events,
			@Nonnull List<String> lines,
			@Nonnull SeverityLevel severity,
			@Nonnull String message
	) {
		assertEquals(1, events.size(), "expected only the chain's declaration, got " + events);
		final var event = events.getFirst();
		assertEquals(SupertypeChain.lineContaining(lines, " chained ") + 1, event.getLine());
		assertEquals(severity, event.getSeverityLevel());
		assertEquals(message, event.getMessage());
	}

	/**
	 * Asserts the check reports the {@code prefervar} slice {@code caseName} once per entry of
	 * {@code tokens}, in order, each at the column where that token starts on the event's line: the
	 * 1-based column with the leading tabs expanded to checkstyle's width of 4.
	 */
	private static void assertReportedAt(@Nonnull String caseName, @Nonnull String... tokens) throws Exception {
		final var lines = TestResources.loadCaseSlice("prefervar", caseName).inputLines();
		final var events = BaseCheckTest.runCheckInline(PreferVarCheck.class, String.join("\n", lines));
		assertEquals(tokens.length, events.size(), caseName);
		final var expected = new ArrayList<String>();
		final var actual = new ArrayList<String>();
		for (var i = 0; i < tokens.length; ++i) {
			final var line = events.get(i).getLine();
			final var start = lines.get(line - 1).replace("\t", "    ").indexOf(tokens[i]);
			assertNotEquals(-1, start, "guard: '" + tokens[i] + "' is on line " + line + " of " + caseName);
			expected.add(line + ":" + (start + 1));
			actual.add(line + ":" + events.get(i).getColumn());
		}
		assertEquals(expected, actual, caseName);
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST find(@Nonnull DetailAST node, @Nonnull Predicate<DetailAST> predicate) {
		for (var child = node; child != null; child = child.getNextSibling()) {
			if (predicate.test(child))
				return child;
			final var found = find(child.getFirstChild(), predicate);
			if (found != null)
				return found;
		}
		return null;
	}

	@CheckReturnValue
	@Nonnull
	private static int[] leafPosition(@Nonnull DetailAST node) {
		var leaf = node;
		while (leaf.getFirstChild() != null)
			leaf = leaf.getFirstChild();
		return new int[]{leaf.getLineNo() - 1, leaf.getColumnNo()};
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST locate(@Nonnull DetailAST root, @Nonnull Predicate<DetailAST> predicate) {
		return requireNonNull(find(root, predicate), "no matching node in " + CLEAN_FIXTURE);
	}

	/**
	 * {@link #NESTED_INNER_FIXTURE} with its {@code C1 extends Inner} nested {@code depth} deep as
	 * {@code C1} to {@code C<depth>}, the innermost holding the {@code String chained} declaration.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<String> nestedInnerSubclasses(int depth) throws Exception {
		final var template = TestResources.readResourceLines("/com/etk2000/checkstyle/inputs/" + NESTED_INNER_FIXTURE);
		final var link = SupertypeChain.lineContaining(template, " extends Inner {");
		final var close = template.lastIndexOf("\t}");
		final var lines = new ArrayList<>(template.subList(0, link));
		for (var i = 1; i <= depth; ++i)
			lines.add("\t".repeat(i - 1) + template.get(link).replace("C1", "C" + i));
		for (var line : template.subList(link + 1, close))
			lines.add("\t".repeat(depth - 1) + line);
		for (var i = depth; i >= 1; --i)
			lines.add("\t".repeat(i - 1) + template.get(close));
		lines.addAll(template.subList(close + 1, template.size()));
		return lines;
	}

	@CheckReturnValue
	private static boolean parentIs(@Nonnull DetailAST node, int tokenType) {
		return node.getParent() != null && node.getParent().getType() == tokenType;
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST parseFixture() throws Exception {
		final var url = PreferVarCheckTest.class.getResource("/com/etk2000/checkstyle/inputs/" + CLEAN_FIXTURE);
		requireNonNull(url, "Test input file not found: " + CLEAN_FIXTURE);
		return JavaParser.parseFile(new File(url.toURI()), JavaParser.Options.WITHOUT_COMMENTS);
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST parseSlice(@Nonnull String caseName) throws Exception {
		final var slice = TestResources.loadCaseSlice("prefervar", caseName);
		return JavaParser.parse(new FileContents(new FileText(new File(caseName + ".java"), slice.inputLines())));
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> supertypeChain(int depth, @Nonnull String declared, @Nonnull String created) throws Exception {
		return SupertypeChain.build(SUPERTYPE_CHAIN_FIXTURE, depth, declared, created);
	}

	@CheckReturnValue
	@Nonnull
	private static int[] typePosition(@Nonnull DetailAST varDef) {
		return leafPosition(requireNonNull(varDef.findFirstToken(TokenTypes.TYPE), "declaration has no TYPE"));
	}

	/** {@link #VAR_CHAIN_FIXTURE} with its {@code a1 = a0} link repeated up to {@code a<links>}, which initializes a {@code Node chained}. */
	@CheckReturnValue
	@Nonnull
	private static List<String> varChain(int links) throws Exception {
		final var template = TestResources.readResourceLines("/com/etk2000/checkstyle/inputs/" + VAR_CHAIN_FIXTURE);
		final var link = SupertypeChain.lineContaining(template, " a1 = a0;");
		final var lines = new ArrayList<>(template.subList(0, link));
		for (var i = 1; i <= links; ++i)
			lines.add(template.get(link).replace("a1", "a" + i).replace("a0", "a" + (i - 1)));
		for (var line : template.subList(link + 1, template.size()))
			lines.add(line.replace("var chained = a1;", "Node chained = a" + links + ';'));
		return lines;
	}

	@CheckReturnValue
	private static boolean varDefNamed(@Nonnull DetailAST node, @Nonnull String name) {
		if (node.getType() != TokenTypes.VARIABLE_DEF)
			return false;
		final var ident = node.findFirstToken(TokenTypes.IDENT);
		return ident != null && name.equals(ident.getText());
	}

	@Test
	public void testCallThroughALongDoublingChainWarns() throws Exception {
		assertChainReported(SupertypeChain.build(DOUBLING_CHAIN_FIXTURE, 30, "Label", "Label"), SeverityLevel.WARNING, MSG_WARNING);
	}

	@Test
	public void testCallThroughAShortDoublingChainConverts() throws Exception {
		assertChainReported(SupertypeChain.build(DOUBLING_CHAIN_FIXTURE, 3, "Label", "Label"), SeverityLevel.ERROR, MSG_ERROR);
	}

	@Test
	public void testChildlessStaticImportDoesNotThrow() {
		final var node = new DetailAstImpl();
		node.setType(TokenTypes.STATIC_IMPORT);
		node.setText("import");

		final var check = new PreferVarCheck();
		check.beginTree(null);
		assertDoesNotThrow(() -> check.visitToken(node));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtAPositionInsideTheInitializer() throws Exception {
		final var root = parseSlice("diamond_carries_declared_type_arguments");
		final var nested = locate(root, node -> varDefNamed(node, "nested"));
		final var at = typePosition(nested);
		assertTrue(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, at[0], at[1]), "guard: the declaration itself moves its arguments");
		final var inside = leafPosition(locate(nested, node -> node.getType() == TokenTypes.IDENT && parentIs(node, TokenTypes.LITERAL_NEW)));
		assertFalse(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, inside[0], inside[1]));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtBareAssignment() throws Exception {
		final var root = parseFixture();
		final Predicate<DetailAST> isStatementAssign =
				node -> node.getType() == TokenTypes.ASSIGN && parentIs(node, TokenTypes.EXPR);
		final var assign = locate(root, isStatementAssign);
		final var at = leafPosition(requireNonNull(assign.getFirstChild(), "assignment has no left-hand side"));
		assertFalse(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, at[0], at[1]));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtConditionalWithAllNewArms() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "everyArmTernary")));
		assertTrue(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, at[0], at[1]));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtConditionalWithNonNewArm() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "ternary")));
		assertFalse(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, at[0], at[1]));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtDeclarationWithoutInitializer() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "uninitialized")));
		assertFalse(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, at[0], at[1]));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtLocalNewInitializer() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "diamond")));
		assertTrue(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, at[0], at[1]));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtMethodCallInitializer() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "targetTyped")));
		assertFalse(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, at[0], at[1]));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtSwitchWithAllNewArms() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "everyArmSwitch")));
		assertTrue(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, at[0], at[1]));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtSwitchWithNonNewArm() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "switched")));
		assertFalse(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, at[0], at[1]));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtTryResource() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> node.getType() == TokenTypes.RESOURCE));
		assertTrue(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, at[0], at[1]));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtTryResourceInsideALambdaArgument() throws Exception {
		final var root = parseSlice("try_resource_inside_a_wrapped_reference_local_initializer_converts");
		final var at = typePosition(locate(root, node -> node.getType() == TokenTypes.RESOURCE));
		assertTrue(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, at[0], at[1]));
	}

	@Test
	public void testDeclaredArgumentsMoveToDiamondAtUnresolvedPosition() throws Exception {
		final var root = parseFixture();
		assertFalse(PreferVarCheck.declaredArgumentsMoveToDiamondAt(root, 0, 0));
	}

	@Test
	public void testDeeplyNestedSubclassesOfAnInnerClassOfAGenericClassFinish() throws Exception {
		// uncached, the inheritance walk cost 3^k here: about 45 s at this depth, against well under one now
		final var lines = nestedInnerSubclasses(18);
		final var events = assertTimeoutPreemptively(
				Duration.ofSeconds(10),
				() -> BaseCheckTest.runCheckInline(PreferVarCheck.class, String.join("\n", lines))
		);
		assertOnlyChainedReported(events, lines, SeverityLevel.ERROR, MSG_ERROR);
	}

	@Test
	public void testExplicitArrayInitAtAnotherArrayType() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "widenedValues")));
		assertFalse(PreferVarCheck.isExplicitArrayInitAt(root, at[0], at[1]));
	}

	@Test
	public void testExplicitArrayInitAtAPositionInsideTheInitializer() throws Exception {
		final var root = parseSlice("explicit_array_init_typed_matching");
		final var inside = leafPosition(locate(root, node -> node.getType() == TokenTypes.IDENT && parentIs(node, TokenTypes.LITERAL_NEW)));
		assertFalse(PreferVarCheck.isExplicitArrayInitAt(root, inside[0], inside[1]));
	}

	@Test
	public void testExplicitArrayInitAtDeclarationWithoutInitializer() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "uninitialized")));
		assertFalse(PreferVarCheck.isExplicitArrayInitAt(root, at[0], at[1]));
	}

	@Test
	public void testExplicitArrayInitAtImplicitArrayInitializer() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "numbers")));
		assertFalse(PreferVarCheck.isExplicitArrayInitAt(root, at[0], at[1]));
	}

	@Test
	public void testExplicitArrayInitAtMatchingTypedLocal() throws Exception {
		final var root = parseSlice("explicit_array_init_typed_matching");
		final var at = typePosition(locate(root, node -> varDefNamed(node, "a")));
		assertTrue(PreferVarCheck.isExplicitArrayInitAt(root, at[0], at[1]));
	}

	@Test
	public void testExplicitArrayInitAtPlainDeclaration() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "diamond")));
		assertFalse(PreferVarCheck.isExplicitArrayInitAt(root, at[0], at[1]));
	}

	@Test
	public void testExplicitArrayInitAtTryResource() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> node.getType() == TokenTypes.RESOURCE));
		assertFalse(PreferVarCheck.isExplicitArrayInitAt(root, at[0], at[1]));
	}

	@Test
	public void testExplicitArrayInitAtTryResourceInsideAnExplicitArrayInitializer() throws Exception {
		final var root = parseSlice("try_resource_inside_a_wrapped_explicit_array_initializer_converts");
		final var at = typePosition(locate(root, node -> node.getType() == TokenTypes.RESOURCE));
		assertFalse(PreferVarCheck.isExplicitArrayInitAt(root, at[0], at[1]));
	}

	@Test
	public void testExplicitArrayInitAtUnresolvedPosition() throws Exception {
		final var root = parseFixture();
		assertFalse(PreferVarCheck.isExplicitArrayInitAt(root, 0, 0));
	}

	@Test
	public void testExplicitArrayInitAtVarLocal() throws Exception {
		final var root = parseSlice("explicit_array_init_var_to_string");
		final var at = typePosition(locate(root, node -> varDefNamed(node, "a")));
		assertTrue(PreferVarCheck.isExplicitArrayInitAt(root, at[0], at[1]));
	}

	@Test
	public void testIsConvertibleDeclarationAtAPositionInsideAnInitializer() throws Exception {
		final var root = parseSlice("explicit_array_init_typed_matching");
		final var at = typePosition(locate(root, node -> varDefNamed(node, "a")));
		assertEquals(Boolean.TRUE, PreferVarCheck.isConvertibleDeclarationAt(root, at[0], at[1]), "guard: the declaration itself converts");
		final var inside = leafPosition(locate(root, node -> node.getType() == TokenTypes.IDENT && parentIs(node, TokenTypes.LITERAL_NEW)));
		assertEquals(Boolean.FALSE, PreferVarCheck.isConvertibleDeclarationAt(root, inside[0], inside[1]));
	}

	@Test
	public void testIsConvertibleDeclarationAtBareAssignment() throws Exception {
		final var root = parseFixture();
		final Predicate<DetailAST> isStatementAssign =
				node -> node.getType() == TokenTypes.ASSIGN && parentIs(node, TokenTypes.EXPR);
		final var assign = locate(root, isStatementAssign);
		final var at = leafPosition(requireNonNull(assign.getFirstChild(), "assignment has no left-hand side"));
		assertEquals(Boolean.FALSE, PreferVarCheck.isConvertibleDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsConvertibleDeclarationAtField() throws Exception {
		final var root = parseFixture();
		final Predicate<DetailAST> isField =
				node -> varDefNamed(node, "x") && parentIs(node, TokenTypes.OBJBLOCK);
		final var at = typePosition(locate(root, isField));
		assertEquals(Boolean.FALSE, PreferVarCheck.isConvertibleDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsConvertibleDeclarationAtForEachVariable() throws Exception {
		final var root = parseFixture();
		final Predicate<DetailAST> isForEachVar = node -> node.getType() == TokenTypes.VARIABLE_DEF
				&& parentIs(node, TokenTypes.FOR_EACH_CLAUSE);
		final var at = typePosition(locate(root, isForEachVar));
		assertEquals(Boolean.TRUE, PreferVarCheck.isConvertibleDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsConvertibleDeclarationAtForInit() throws Exception {
		final var root = parseFixture();
		final Predicate<DetailAST> isForInitVar = node -> node.getType() == TokenTypes.VARIABLE_DEF
				&& parentIs(node, TokenTypes.FOR_INIT);
		final var at = typePosition(locate(root, isForInitVar));
		assertEquals(Boolean.TRUE, PreferVarCheck.isConvertibleDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsConvertibleDeclarationAtLocal() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "diamond")));
		assertEquals(Boolean.TRUE, PreferVarCheck.isConvertibleDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsConvertibleDeclarationAtTryResource() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> node.getType() == TokenTypes.RESOURCE));
		assertEquals(Boolean.TRUE, PreferVarCheck.isConvertibleDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsConvertibleDeclarationAtTryResourceInsideAFieldInitializer() throws Exception {
		final var root = parseSlice("try_resource_inside_a_field_initializer_lambda_converts");
		final var at = typePosition(locate(root, node -> node.getType() == TokenTypes.RESOURCE));
		assertEquals(Boolean.TRUE, PreferVarCheck.isConvertibleDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsConvertibleDeclarationAtUnresolvedPosition() throws Exception {
		final var root = parseFixture();
		assertNull(PreferVarCheck.isConvertibleDeclarationAt(root, 0, 0));
	}

	@Test
	public void testIsMultiVarDeclarationAtAPositionInsideAnInitializer() throws Exception {
		final var root = parseSlice("multi_var_warns");
		final var at = typePosition(locate(root, node -> varDefNamed(node, "x")));
		assertEquals(Boolean.TRUE, PreferVarCheck.isMultiVarDeclarationAt(root, at[0], at[1]), "guard: the declaration itself is multi-variable");
		final var inside = leafPosition(locate(root, node -> node.getType() == TokenTypes.NUM_INT));
		assertNull(PreferVarCheck.isMultiVarDeclarationAt(root, inside[0], inside[1]));
	}

	@Test
	public void testIsMultiVarDeclarationAtBareAssignment() throws Exception {
		final var root = parseFixture();
		final Predicate<DetailAST> isStatementAssign =
				node -> node.getType() == TokenTypes.ASSIGN && parentIs(node, TokenTypes.EXPR);
		final var assign = locate(root, isStatementAssign);
		final var at = leafPosition(requireNonNull(assign.getFirstChild(), "assignment has no left-hand side"));
		assertNull(PreferVarCheck.isMultiVarDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsMultiVarDeclarationAtMultiVar() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "y")));
		assertEquals(Boolean.TRUE, PreferVarCheck.isMultiVarDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsMultiVarDeclarationAtSingleVar() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "diamond")));
		assertEquals(Boolean.FALSE, PreferVarCheck.isMultiVarDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsMultiVarDeclarationAtTryResource() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> node.getType() == TokenTypes.RESOURCE));
		assertEquals(Boolean.FALSE, PreferVarCheck.isMultiVarDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsMultiVarDeclarationAtTryResourceInsideAMultiVariableInitializer() throws Exception {
		final var root = parseSlice("try_resource_inside_a_multi_variable_local_initializer_converts");
		final var at = typePosition(locate(root, node -> node.getType() == TokenTypes.RESOURCE));
		assertEquals(Boolean.FALSE, PreferVarCheck.isMultiVarDeclarationAt(root, at[0], at[1]));
	}

	@Test
	public void testIsMultiVarDeclarationAtUnresolvedPosition() throws Exception {
		final var root = parseFixture();
		assertNull(PreferVarCheck.isMultiVarDeclarationAt(root, 0, 0));
	}

	@Test
	public void testPathlessStaticImportDoesNotThrow() {
		final var node = new DetailAstImpl();
		node.setType(TokenTypes.STATIC_IMPORT);
		node.setText("import");
		final var literalStatic = new DetailAstImpl();
		literalStatic.setType(TokenTypes.LITERAL_STATIC);
		node.addChild(literalStatic);

		final var check = new PreferVarCheck();
		check.beginTree(null);
		assertDoesNotThrow(() -> check.visitToken(node));
	}

	@Test
	public void testPrimitiveTypeUnpinnedAtAPositionInsideTheInitializer() throws Exception {
		final var root = parseSlice("parse_byte");
		final var declaration = locate(root, node -> varDefNamed(node, "bd"));
		final var at = typePosition(declaration);
		assertTrue(PreferVarCheck.primitiveTypeUnpinnedAt(root, at[0], at[1]), "guard: the declaration itself is unpinned");
		final var inside = leafPosition(locate(declaration, node -> node.getType() == TokenTypes.STRING_LITERAL));
		assertFalse(PreferVarCheck.primitiveTypeUnpinnedAt(root, inside[0], inside[1]));
	}

	@Test
	public void testPrimitiveTypeUnpinnedAtDeclarationWithoutInitializer() throws Exception {
		final var root = parseFixture();
		final var at = typePosition(locate(root, node -> varDefNamed(node, "uninitialized")));
		assertFalse(PreferVarCheck.primitiveTypeUnpinnedAt(root, at[0], at[1]));
	}

	@Test
	public void testPrimitiveTypeUnpinnedAtTryResourceInsideAPrimitiveLocalInitializer() throws Exception {
		final var root = parseSlice("try_resource_inside_a_primitive_local_initializer_converts");
		final var at = typePosition(locate(root, node -> node.getType() == TokenTypes.RESOURCE));
		assertFalse(PreferVarCheck.primitiveTypeUnpinnedAt(root, at[0], at[1]));
	}

	@Test
	public void testPrimitiveTypeUnpinnedAtUnresolvedPosition() throws Exception {
		final var root = parseFixture();
		assertFalse(PreferVarCheck.primitiveTypeUnpinnedAt(root, 0, 0));
	}

	@Test
	public void testReferenceTypeUnprovenAtAPositionInsideTheInitializer() throws Exception {
		final var root = parseSlice("diamond_carries_declared_type_arguments");
		final var iface = locate(root, node -> varDefNamed(node, "iface"));
		final var at = typePosition(iface);
		assertTrue(PreferVarCheck.referenceTypeUnprovenAt(root, at[0], at[1]), "guard: the declaration itself narrows");
		final var inside = leafPosition(locate(iface, node -> node.getType() == TokenTypes.IDENT && parentIs(node, TokenTypes.LITERAL_NEW)));
		assertFalse(PreferVarCheck.referenceTypeUnprovenAt(root, inside[0], inside[1]));
	}

	@Test
	public void testReferenceTypeUnprovenAtTryResourceInsideALambdaArgument() throws Exception {
		final var root = parseSlice("try_resource_inside_a_wrapped_reference_local_initializer_converts");
		final var at = typePosition(locate(root, node -> node.getType() == TokenTypes.RESOURCE));
		assertFalse(PreferVarCheck.referenceTypeUnprovenAt(root, at[0], at[1]));
	}

	@Test
	public void testReferenceTypeUnprovenAtUnprovenTryResource() throws Exception {
		final var root = parseSlice("try_with_resources_declared_supertype_warns");
		final var at = typePosition(locate(root, node -> node.getType() == TokenTypes.RESOURCE));
		assertTrue(PreferVarCheck.referenceTypeUnprovenAt(root, at[0], at[1]));
	}

	@Test
	public void testReportedColumnIsTheFirstTokenOfTheType() throws Exception {
		assertReportedAt("explicit_array_init_annotated_declared_dimension", "String @Ann");
		assertReportedAt("explicit_array_init_c_style_typed_matching", "String values", "int[] grid");
		assertReportedAt(
				"explicit_array_init_qualified_declared_and_new_type",
				"java.lang.String[] both",
				"java.lang.String cStyle",
				"java.util.Date[] dates"
		);
		assertReportedAt("for_each_final_qualified_generic_element_type_converts", "java.util.Map.Entry<String, Integer> entry");
		assertReportedAt(
				"qualified_types_are_reported_at_their_first_token_warns",
				"java.lang.AutoCloseable wide",
				"java.lang.AutoCloseable typed",
				"java.lang.String item",
				"java.util.List<String> first",
				"java.util.List<String> empty"
		);
		assertReportedAt("try_resource_annotated_qualified_generic_type_converts", "Outer.Res<String> res");
	}

	@Test
	public void testStaticImportOwnersDoNotLeakBetweenFiles() throws Exception {
		final var events = BaseCheckTest.runCheckOnFiles(
				PreferVarCheck.class,
				"prefervar/InputStaticImportOwnersDonor.java",
				"prefervar/InputStaticImportOwnersRecipient.java"
		);

		assertEquals(1, events.size(), "expected only the donor's violation, got " + events);
		final var event = events.getFirst();
		assertTrue(event.getFileName().endsWith("InputStaticImportOwnersDonor.java"), event.getFileName());
		assertEquals(7, event.getLine());
		assertEquals(SeverityLevel.ERROR, event.getSeverityLevel());
		assertEquals(MSG_ERROR, event.getMessage());
	}

	@Test
	public void testSupertypeWalkFarPastTheCapDoesNotOverflow() throws Exception {
		assertChainReported(supertypeChain(SupertypeChain.DEEP, "C0<String>", "C" + SupertypeChain.DEEP + "<String>"), SeverityLevel.WARNING, MSG_WARNING);
	}

	@Test
	public void testSupertypeWalkForAFieldDoesNotOverflow() throws Exception {
		final var lines = SupertypeChain.build("prefervar/InputSupertypeChainField.java", SupertypeChain.DEEP, "C0<String>", "C" + SupertypeChain.DEEP + "<String>");
		assertChainReported(lines, SeverityLevel.WARNING, MSG_WARNING);
	}

	@Test
	public void testSupertypeWalkForAnArgumentDoesNotOverflow() throws Exception {
		final var lines = SupertypeChain.build("prefervar/InputSupertypeChainOverload.java", SupertypeChain.DEEP, "C0<String>", "C" + SupertypeChain.DEEP + "<String>");
		assertChainReported(lines, SeverityLevel.WARNING, MSG_WARNING);
	}

	@Test
	public void testSupertypeWalkForAnInheritedCallDoesNotOverflow() throws Exception {
		final var lines = SupertypeChain.build("prefervar/InputSupertypeChainInheritedCall.java", SupertypeChain.DEEP, "String", "String");
		assertChainReported(lines, SeverityLevel.ERROR, MSG_ERROR);
	}

	@Test
	public void testSupertypeWalkForAnInheritedFieldDoesNotOverflow() throws Exception {
		final var lines = SupertypeChain.build("prefervar/InputSupertypeChainInheritedField.java", SupertypeChain.DEEP, "Integer", "Integer");
		assertChainReported(lines, SeverityLevel.ERROR, MSG_FOREACH);
	}

	@Test
	public void testSupertypeWalkForAnInheritedOverloadDoesNotOverflow() throws Exception {
		final var lines = SupertypeChain.build("prefervar/InputSupertypeChainInheritedOverload.java", SupertypeChain.DEEP, "C0<String>", "C" + SupertypeChain.DEEP + "<String>");
		assertChainReported(lines, SeverityLevel.WARNING, MSG_WARNING);
	}

	@Test
	public void testVarChainFarPastTheDepthCapDoesNotOverflow() throws Exception {
		final var lines = varChain(SupertypeChain.DEEP);
		assertOnlyChainedReported(BaseCheckTest.runCheckInline(PreferVarCheck.class, String.join("\n", lines)), lines, SeverityLevel.WARNING, MSG_WARNING);
	}

	@Test
	public void testVarChainJustInsideTheDepthCapConverts() throws Exception {
		final var lines = varChain(ProvenTypes.MAX_DEPTH - 2);
		assertOnlyChainedReported(BaseCheckTest.runCheckInline(PreferVarCheck.class, String.join("\n", lines)), lines, SeverityLevel.ERROR, MSG_ERROR);
	}

	@Test
	public void testVarChainJustPastTheDepthCapWarns() throws Exception {
		final var lines = varChain(ProvenTypes.MAX_DEPTH - 1);
		assertOnlyChainedReported(BaseCheckTest.runCheckInline(PreferVarCheck.class, String.join("\n", lines)), lines, SeverityLevel.WARNING, MSG_WARNING);
	}
}