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
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbstractResolvingCheckTest {
	private static final class ScopeProbe extends AbstractResolvingCheck {
		private final List<Integer> scopedTokens = new ArrayList<>();

		private int beginFileCalls;

		@Override
		protected void beginFile(@Nullable DetailAST rootAST) {
			++beginFileCalls;
		}

		@Nonnull
		@Override
		public int[] getDefaultTokens() {
			return new int[]{TokenTypes.IMPORT, TokenTypes.PACKAGE_DEF, TokenTypes.STATIC_IMPORT};
		}

		@Override
		protected void visitScopedToken(@Nonnull DetailAST ast) {
			scopedTokens.add(ast.getType());
		}
	}

	private static final Pattern CUSTOM_MODULE = Pattern.compile("<module name=\"(com\\.etk2000\\.checkstyle\\.\\w+)\"");

	@CheckReturnValue
	@Nonnull
	private static List<String> descriptors(@Nonnull List<AuditEvent> events) {
		final var out = new ArrayList<String>(events.size());
		for (var event : events)
			out.add(event.getLine() + ":" + event.getColumn() + ":" + event.getMessage());
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

	/**
	 * A {@code PACKAGE_DEF} under a compilation-unit root, because
	 * {@code AstUtil.getPackageName} walks to the root and looks for the
	 * declaration among its children rather than reading the node handed to it.
	 */
	@CheckReturnValue
	@Nonnull
	private static DetailAstImpl packageNode(@Nonnull String name) {
		final var root = node(TokenTypes.COMPILATION_UNIT, "COMPILATION_UNIT");
		final var declaration = node(TokenTypes.PACKAGE_DEF, "package");
		declaration.addChild(dotted(name));
		root.addChild(declaration);
		return declaration;
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

	@Test
	public void beginFileRunsForEveryTreeIncludingNullRoot() {
		final var probe = new ScopeProbe();
		probe.beginTree(node(TokenTypes.COMPILATION_UNIT, "COMPILATION_UNIT"));
		probe.beginTree(null);
		assertEquals(2, probe.beginFileCalls);
	}

	@Test
	public void importScopeDoesNotLeakBetweenFiles() {
		final var probe = new ScopeProbe();
		probe.beginTree(null);
		probe.visitToken(importNode("java.util.List"));
		assertEquals("java.util.List", probe.resolve("List"));

		// checkstyle reuses one check instance for every file in a run
		probe.beginTree(null);
		assertNull(probe.resolve("List"));
	}

	@Test
	public void minSdkIsInertOnPureResolvingCheck() throws Exception {
		final var low = BaseCheckTest.runCheck(PreferVarCheck.class, "prefervar/cases.in.java", "minSdk", "1");
		final var high = BaseCheckTest.runCheck(PreferVarCheck.class, "prefervar/cases.in.java", "minSdk", "99");
		assertFalse(low.isEmpty(), "fixture must report violations for this comparison to mean anything");
		assertEquals(descriptors(low), descriptors(high));
	}

	@Test
	public void packageScopeDoesNotLeakBetweenFiles() {
		final var probe = new ScopeProbe();
		probe.beginTree(null);
		probe.visitToken(packageNode("com.etk2000.checkstyle"));
		assertEquals("com.etk2000.checkstyle.AstUtil", probe.resolve("AstUtil"));

		probe.beginTree(null);
		assertNull(probe.resolve("AstUtil"));
	}

	@MethodSource("registeredResolvingChecks")
	@ParameterizedTest
	public void registeredResolvingChecksRequestScopeTokens(
			@Nonnull Class<? extends AbstractCheck> checkClass
	) throws Exception {
		final var tokens = checkClass.getDeclaredConstructor().newInstance().getDefaultTokens();
		final var requested = IntStream.of(tokens).boxed().toList();

		assertTrue(
				requested.contains(TokenTypes.IMPORT),
				checkClass.getSimpleName() + " omits IMPORT, so its import scope stays empty and every simple name fails to resolve"
		);
		assertTrue(
				requested.contains(TokenTypes.PACKAGE_DEF),
				checkClass.getSimpleName() + " omits PACKAGE_DEF, so same-package names fail to resolve"
		);
	}

	@Test
	public void scopeTokensAreConsumedAndOthersForwarded() {
		final var probe = new ScopeProbe();
		probe.beginTree(null);

		probe.visitToken(importNode("java.util.List"));
		probe.visitToken(packageNode("java.util"));
		assertEquals(List.of(), probe.scopedTokens);

		probe.visitToken(node(TokenTypes.STATIC_IMPORT, "import"));
		assertEquals(List.of(TokenTypes.STATIC_IMPORT), probe.scopedTokens);
	}
}