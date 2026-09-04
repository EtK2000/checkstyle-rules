package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbstractResolvingCheckTest {
	private static final class ScopeProbe extends AbstractResolvingCheck {
		private final List<Integer> scopedTokens = new ArrayList<>();

		@Nonnull
		private final int[] tokens;

		private int beginFileCalls;
		private String resolvedInBeginFile, resolvedInFinishFile;

		private ScopeProbe(@Nonnull int... tokens) {
			this.tokens = tokens;
		}

		@Override
		protected void beginFile(@Nullable DetailAST rootAST) {
			++beginFileCalls;
			resolvedInBeginFile = resolve("List");
		}

		@Override
		protected void finishFile(@Nullable DetailAST rootAST) {
			resolvedInFinishFile = resolve("List");
		}

		@Nonnull
		@Override
		public int[] getDefaultTokens() {
			return tokens;
		}

		@Override
		protected void visitScopedToken(@Nonnull DetailAST ast) {
			scopedTokens.add(ast.getType());
		}
	}

	private static final int[] SCOPE_PROBE_TOKENS = {
			TokenTypes.IMPORT, TokenTypes.PACKAGE_DEF, TokenTypes.STATIC_IMPORT
	};

	private static final Pattern CUSTOM_MODULE = Pattern.compile("<module name=\"(com\\.etk2000\\.checkstyle\\.\\w+)\"");

	@CheckReturnValue
	@Nonnull
	private static List<String> descriptors(@Nonnull List<AuditEvent> events) {
		final var out = new ArrayList<String>(events.size());
		for (var event : events)
			out.add(event.getLine() + ":" + event.getColumn() + ":" + event.getSeverityLevel() + ":" + event.getMessage());
		return out;
	}

	/**
	 * The left-leaning {@code DOT} tree checkstyle builds for a qualified name, so
	 * {@code FullIdent} reads it back as {@code fqcn}.
	 */
	@CheckReturnValue
	@Nonnull
	private static DetailAstImpl dotted(@Nonnull String fqcn) {
		final var parts = fqcn.split("\\.");
		var left = node(TokenTypes.IDENT, parts[0]);
		for (var i = 1; i < parts.length; ++i) {
			final var dot = node(TokenTypes.DOT, ".");
			dot.addChild(left);
			dot.addChild(node(TokenTypes.IDENT, parts[i]));
			left = dot;
		}
		return left;
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAstImpl importNode(@Nonnull String fqcn) {
		final var imported = node(TokenTypes.IMPORT, "import");
		imported.addChild(dotted(fqcn));
		return imported;
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAstImpl node(int type, @Nonnull String text) {
		final var built = new DetailAstImpl();
		built.setType(type);
		built.setText(text);
		return built;
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAstImpl packageNode(@Nonnull String name) {
		final var declaration = node(TokenTypes.PACKAGE_DEF, "package");
		declaration.addChild(dotted(name));
		return declaration;
	}

	@CheckReturnValue
	@Nonnull
	static Stream<Class<? extends AbstractCheck>> pureResolvingChecks() {
		return Stream.of(
				PreferBulkOperationCheck.class,
				PreferCollectionInterfaceCheck.class,
				PreferLambdaCheck.class,
				PreferVarCheck.class,
				RedundantArrayCreationCheck.class
		);
	}

	@CheckReturnValue
	@Nonnull
	static Stream<Class<? extends AbstractCheck>> registeredResolvingChecks() throws Exception {
		final String xml;
		try (var in = AbstractResolvingCheck.class.getResourceAsStream("/com/etk2000/checkstyle/checkstyle.xml")) {
			assertNotNull(in, "checkstyle.xml is not on the test classpath");
			xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}

		final var found = new ArrayList<Class<? extends AbstractCheck>>();
		final var matcher = CUSTOM_MODULE.matcher(xml);
		while (matcher.find()) {
			final var type = Class.forName(matcher.group(1));
			if (AbstractResolvingCheck.class.isAssignableFrom(type))
				found.add(type.asSubclass(AbstractCheck.class));
		}

		// pins the extraction itself: a broken regex would otherwise yield an empty
		// roster and pass every parameterized case vacuously
		assertTrue(
				found.containsAll(List.of(
						PreferCollectionInterfaceCheck.class,
						PreferLambdaCheck.class,
						PreferSpecificApiCheck.class,
						PreferStandardCharsetsCheck.class,
						PreferVarCheck.class,
						RedundantArrayCreationCheck.class
				)),
				"module regex did not match the known resolving checks; matched " + found
		);
		return found.stream();
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAstImpl unit(@Nonnull DetailAstImpl... children) {
		final var root = node(TokenTypes.COMPILATION_UNIT, "COMPILATION_UNIT");
		for (var child : children)
			root.addChild(child);
		return root;
	}

	@Test
	public void beginFileRunsForEveryTreeIncludingNullRoot() {
		final var probe = new ScopeProbe(SCOPE_PROBE_TOKENS);
		probe.beginTree(unit());
		probe.beginTree(null);
		assertEquals(2, probe.beginFileCalls);
	}

	@Test
	public void childlessImportDoesNotBreakResolution() {
		final var probe = new ScopeProbe(SCOPE_PROBE_TOKENS);
		probe.beginTree(unit(node(TokenTypes.IMPORT, "import")));
		assertNull(probe.resolve("List"));
	}

	@Test
	public void defaultPackageCompilationUnitResolvesThroughImports() throws Exception {
		final var events = BaseCheckTest.runCheck(PreferVarCheck.class, "prefervar/InputDefaultPackageResolution.java");
		assertEquals(
				List.of("6:15:error:Local variable must use 'var' instead of an explicit type."),
				descriptors(events)
		);
	}

	@MethodSource("registeredResolvingChecks")
	@ParameterizedTest
	public void defaultPackageCompilationUnitYieldsNoViolations(
			@Nonnull Class<? extends AbstractCheck> checkClass
	) throws Exception {
		final var events = BaseCheckTest.runCheck(checkClass, "resolvingscope/InputDefaultPackage.java");
		assertEquals(List.of(), descriptors(events));
	}

	@Test
	public void everyVisitedTokenIsForwarded() {
		final var probe = new ScopeProbe(SCOPE_PROBE_TOKENS);
		probe.beginTree(unit());

		probe.visitToken(importNode("java.util.List"));
		probe.visitToken(packageNode("java.util"));
		probe.visitToken(node(TokenTypes.STATIC_IMPORT, "import"));

		assertEquals(
				List.of(TokenTypes.IMPORT, TokenTypes.PACKAGE_DEF, TokenTypes.STATIC_IMPORT),
				probe.scopedTokens
		);
	}

	@Test
	public void finishFileCanStillResolve() {
		final var probe = new ScopeProbe(SCOPE_PROBE_TOKENS);
		probe.beginTree(unit(importNode("java.util.List")));
		probe.finishTree(null);
		assertEquals("java.util.List", probe.resolvedInFinishFile);
	}

	@Test
	public void importScopeDoesNotLeakBetweenFiles() {
		final var probe = new ScopeProbe(SCOPE_PROBE_TOKENS);
		probe.beginTree(unit(importNode("java.util.List")));
		assertEquals("java.util.List", probe.resolve("List"));

		probe.beginTree(unit());
		assertNull(probe.resolve("List"));
	}

	@Test
	public void importShapesPopulateScope() {
		final var probe = new ScopeProbe(SCOPE_PROBE_TOKENS);
		probe.beginTree(unit(
				importNode("java.util.List"),
				importNode("java.util.concurrent.*")
		));

		assertEquals("java.util.List", probe.resolve("List"));
		assertEquals("java.util.concurrent.Callable", probe.resolve("Callable"));
	}

	@Test
	public void memoizedResolutionMatchesUncached() {
		final var probe = new ScopeProbe(SCOPE_PROBE_TOKENS);
		probe.beginTree(unit(importNode("java.util.List")));

		final var imports = Set.of("java.util.List");
		for (var name : List.of("List", "T")) {
			final var expected = ReflectionUtil.resolveClassName(name, null, imports);
			assertEquals(expected, probe.resolve(name), name);
			assertEquals(expected, probe.resolve(name), name + " on the memoized second call");
		}
	}

	@MethodSource("pureResolvingChecks")
	@ParameterizedTest
	public void minSdkIsInertOnPureResolvingCheck(
			@Nonnull Class<? extends AbstractCheck> checkClass
	) throws Exception {
		final var fixture = BaseCheckTest.deriveTopic(checkClass) + "/cases.in.java";
		final var low = BaseCheckTest.runCheck(checkClass, fixture, "minSdk", "1");
		final var high = BaseCheckTest.runCheck(checkClass, fixture, "minSdk", "99");
		assertFalse(low.isEmpty(), "fixture must report violations for this comparison to mean anything");
		assertEquals(descriptors(low), descriptors(high));
	}

	@Test
	public void nonScopeChildDoesNotStopTheScan() {
		final var probe = new ScopeProbe(SCOPE_PROBE_TOKENS);
		probe.beginTree(unit(node(TokenTypes.CLASS_DEF, "CLASS_DEF"), importNode("java.util.List")));
		assertEquals("java.util.List", probe.resolve("List"));
	}

	@MethodSource("registeredResolvingChecks")
	@ParameterizedTest
	public void packageOnlyCompilationUnitYieldsNoViolations(
			@Nonnull Class<? extends AbstractCheck> checkClass
	) throws Exception {
		final var events = BaseCheckTest.runCheck(checkClass, "resolvingscope/InputPackageOnly.java");
		assertEquals(List.of(), descriptors(events));
	}

	@Test
	public void packageScopeDoesNotLeakBetweenFiles() {
		final var probe = new ScopeProbe(SCOPE_PROBE_TOKENS);
		probe.beginTree(unit(packageNode("com.etk2000.checkstyle")));
		assertEquals("com.etk2000.checkstyle.ReflectionUtil", probe.resolve("ReflectionUtil"));

		probe.beginTree(unit());
		assertNull(probe.resolve("ReflectionUtil"));
	}

	@MethodSource("registeredResolvingChecks")
	@ParameterizedTest
	public void registeredResolvingChecksDoNotRequestScopeTokens(
			@Nonnull Class<? extends AbstractCheck> checkClass
	) throws Exception {
		final var tokens = checkClass.getDeclaredConstructor().newInstance().getDefaultTokens();
		final var requested = IntStream.of(tokens).boxed().toList();
		final var name = checkClass.getSimpleName();

		assertFalse(
				requested.contains(TokenTypes.IMPORT),
				name + " still registers IMPORT, which the base no longer consumes"
		);
		assertFalse(
				requested.contains(TokenTypes.PACKAGE_DEF),
				name + " still registers PACKAGE_DEF, which the base no longer consumes"
		);
	}

	@Test
	public void resolutionMemoDoesNotLeakBetweenFiles() {
		final var probe = new ScopeProbe(SCOPE_PROBE_TOKENS);
		probe.beginTree(unit(importNode("java.util.List")));
		assertEquals("java.util.List", probe.resolve("List"));

		probe.beginTree(unit(importNode("java.awt.List")));
		assertEquals("java.awt.List", probe.resolve("List"));
	}

	@Test
	public void resolutionWorksWithoutRegisteringScopeTokens() {
		final var probe = new ScopeProbe(TokenTypes.METHOD_CALL);
		probe.beginTree(unit(importNode("java.util.List")));
		assertEquals("java.util.List", probe.resolve("List"));
	}

	@Test
	public void resolveWorksFromBeginFile() {
		final var probe = new ScopeProbe(SCOPE_PROBE_TOKENS);
		probe.beginTree(unit(importNode("java.util.List")));
		assertEquals("java.util.List", probe.resolvedInBeginFile);
	}

	@Test
	public void rostersPartitionTheRegisteredResolvingChecks() throws Exception {
		// pureResolvingChecks is hand-written while registeredResolvingChecks is derived, so
		// without this a new resolving check silently escapes the minSdk-inertness assertion
		final var gating = new HashSet<Class<? extends AbstractCheck>>(registeredResolvingChecks().toList());
		gating.removeAll(pureResolvingChecks().toList());

		assertEquals(
				Set.of(PreferSpecificApiCheck.class, PreferStandardCharsetsCheck.class),
				gating,
				"a registered resolving check is in neither roster; classify it as gating or pure"
		);
	}

	@Test
	public void scopeIsReleasedAfterFinishTree() {
		final var probe = new ScopeProbe(SCOPE_PROBE_TOKENS);
		probe.beginTree(unit(importNode("java.util.List")));
		probe.finishTree(unit());
		assertNull(probe.resolve("List"));
	}

	@Test
	public void scopeIsReleasedAfterNullFinishTree() {
		final var probe = new ScopeProbe(SCOPE_PROBE_TOKENS);
		probe.beginTree(unit(importNode("java.util.List")));
		probe.finishTree(null);
		assertNull(probe.resolve("List"));
	}
}