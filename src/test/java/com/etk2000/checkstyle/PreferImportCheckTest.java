package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.ast.AstQuery;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.tools.ToolProvider;

/**
 * Edge-case coverage for {@link PreferImportCheck} that cannot live in the
 * data-driven {@code StandardCheckTests} slices.
 */
@Isolated
public class PreferImportCheckTest {
	private static final Path LINKAGE_PROBE_SOURCES = Path.of("src", "test", "resources", "com", "etk2000", "checkstyle", "inputs", "reflectionutil");
	private static final String MSG_UNREADABLE_PREFIX = "Consider an import instead of fully qualified name '";
	private static final String MSG_UNREADABLE_SUFFIX = "'; a class this file cannot read may give the short name another meaning.";
	private static boolean linkageProbesDefined;

	private static void assertReportedUnlessObscured(String source, int line, String qualified, boolean obscured) throws Exception {
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		if (obscured) {
			assertEquals(0, violations.size());
			return;
		}
		assertEquals(1, violations.size());
		assertEquals(line, violations.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name '" + qualified + "'.", violations.getFirst().getMessage());
	}

	/** The one event {@code source} reports is the unreadable-class warning for {@code qualified}, on the line holding {@code site}. */
	private static void assertSingleWarning(String source, String site, String qualified) throws Exception {
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(1, violations.size());
		assertEquals(source.substring(0, source.indexOf(site)).split("\n", -1).length, violations.getFirst().getLine());
		assertEquals(SeverityLevel.WARNING, violations.getFirst().getSeverityLevel());
		assertEquals(MSG_UNREADABLE_PREFIX + qualified + MSG_UNREADABLE_SUFFIX, violations.getFirst().getMessage());
	}

	/**
	 * Compiles the probe classes into {@code dir} and defines the two whose field or member types
	 * are then missing, once per JVM: defining a class a second time fails.
	 */
	private static synchronized void defineLinkageProbes(Path dir) throws Exception {
		if (linkageProbesDefined)
			return;
		final var compiled = ToolProvider.getSystemJavaCompiler().run(
				null,
				null,
				null,
				"-d",
				dir.toString(),
				LINKAGE_PROBE_SOURCES.resolve("InputLinkageProbeMissing.java").toString(),
				LINKAGE_PROBE_SOURCES.resolve("InputLinkageProbeImportScope.java").toString(),
				LINKAGE_PROBE_SOURCES.resolve("InputLinkageProbeImportScopeOuter.java").toString()
		);
		assertEquals(0, compiled);
		final var lookup = MethodHandles.lookup();
		lookup.defineClass(Files.readAllBytes(dir.resolve("com/etk2000/checkstyle/InputLinkageProbeImportScope.class")));
		lookup.defineClass(Files.readAllBytes(dir.resolve("com/etk2000/checkstyle/InputLinkageProbeImportScopeOuter.class")));
		linkageProbesDefined = true;
	}

	private static DetailAST parse(String source) throws Exception {
		return JavaParser.parse(new FileContents(new FileText(new File("T.java"), List.of(source.split("\n", -1)))));
	}

	@Test
	public void testCaseGroupLocalDoesNotReachEarlierGroup() throws Exception {
		final var source = """
				class T {
					int m(int k) {
						switch (k) {
							case 1:
								System.out.println(java.lang.Integer.MAX_VALUE);
							case 2:
								final Object Integer = null;
								System.out.println(Integer);
								return 1;
							default:
								return 0;
						}
					}
				}
				""";
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(1, violations.size());
		assertEquals(5, violations.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.lang.Integer'.", violations.getFirst().getMessage());
	}

	/**
	 * A local an unbraced case group declares stays in scope for the later groups, which the
	 * project's own {@code NoCaseBraces} keeps out of every linted fixture.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
			"""
			class T {
				int m(int k) {
					switch (k) {
						case 1:
							final Object Integer = null;
							System.out.println(Integer);
						case 2:
							return java.lang.Integer.MAX_VALUE;
						default:
							return 0;
					}
				}
			}
			""",
			"""
			class T {
				int m(int k, int j) {
					switch (k) {
						case 1:
							final Object Integer = null;
							System.out.println(Integer);
						case 2:
							switch (j) {
								case 3 -> {
									return java.lang.Integer.MAX_VALUE;
								}
								default -> {
									return 1;
								}
							}
						default:
							return 0;
					}
				}
			}
			"""
	})
	public void testCaseGroupLocalObscuresLaterGroup(String source) throws Exception {
		assertEquals(0, BaseCheckTest.runCheckInline(PreferImportCheck.class, source).size());
	}

	@Test
	public void testCaseGroupLocalOfOtherNameDoesNotObscure() throws Exception {
		final var source = """
				class T {
					int m(int k) {
						switch (k) {
							case 1:
								final Object other = null;
								System.out.println(other);
							case 2:
								return java.lang.Integer.MAX_VALUE;
							default:
								return 0;
						}
					}
				}
				""";
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(1, violations.size());
		assertEquals(8, violations.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.lang.Integer'.", violations.getFirst().getMessage());
	}

	@Test
	public void testCompletesOnDeeplyNestedName() throws Exception {
		final var chain = new StringBuilder("a0");
		for (var i = 1; i < 2000; ++i)
			chain.append(".a").append(i);
		final var source = "class T {\n\t" + chain + " field;\n}\n";
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(1, violations.size());
		assertEquals(2, violations.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name '" + chain + "'.", violations.getFirst().getMessage());
	}

	@Test
	public void testDeepSelfQualifiedNameSuppressedWithoutReflection() throws Exception {
		final var name = new StringBuilder("a");
		for (var i = 0; i < 20; ++i)
			name.append(".x").append(i);
		final var source = "class a {\n\t" + name + " f;\n}\n";
		ReflectionUtil.clearCache();
		ReflectionUtil.classForNameCallCount.set(0);
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(0, violations.size());
		assertEquals(0, ReflectionUtil.classForNameCallCount.get());
	}

	/**
	 * A file without a package sees the public and protected fields of a classpath supertype in
	 * another package, never its package-private or private ones.
	 */
	@CsvSource({
			"Boolean, TRUE, true",
			"Byte, MAX_VALUE, false",
			"Character, MAX_VALUE, false",
			"Double, MAX_VALUE, false",
			"Float, MAX_VALUE, true",
			"Integer, MAX_VALUE, true",
			"Long, MAX_VALUE, true",
			"Short, MAX_VALUE, false"
	})
	@ParameterizedTest
	public void testDefaultPackageInheritedFieldVisibility(String field, String member, boolean obscured) throws Exception {
		final var source = """
				import com.etk2000.checkstyle.testhelpers.ClasspathScopeBase;

				class T extends ClasspathScopeBase {
					Object m() {
						return java.lang.%s.%s;
					}
				}
				""".formatted(field, member);
		assertReportedUnlessObscured(source, 5, "java.lang." + field, obscured);
	}

	/** A file without a package sees, through a static on-demand import, only the public static fields. */
	@CsvSource({
			"Boolean, TRUE, true",
			"Byte, MAX_VALUE, false",
			"Character, MAX_VALUE, false",
			"Double, MAX_VALUE, false",
			"Float, MAX_VALUE, false",
			"Integer, MAX_VALUE, false",
			"Long, MAX_VALUE, false",
			"Short, MAX_VALUE, false"
	})
	@ParameterizedTest
	public void testDefaultPackageStaticImportOnDemandVisibility(String field, String member, boolean obscured) throws Exception {
		final var source = """
				import static com.etk2000.checkstyle.testhelpers.ClasspathScopeBase.*;

				class T {
					Object m() {
						return java.lang.%s.%s;
					}
				}
				""".formatted(field, member);
		assertReportedUnlessObscured(source, 5, "java.lang." + field, obscured);
	}

	@Test
	public void testDepthCapWithTypeRootForcesOldMessage() throws Exception {
		final var name = new StringBuilder("Map");
		for (var i = 1; i <= 17; ++i)
			name.append(".a").append(i);
		final var source = "import java.util.Map;\nclass T {\n\t" + name + " f;\n}\n";
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(1, violations.size());
		assertEquals(3, violations.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name '" + name + "'.", violations.getFirst().getMessage());
	}

	@Test
	public void testEnclosingSelfQualAtCeilingScans() throws Exception {
		final var name = new StringBuilder("a");
		for (var i = 1; i <= 16; ++i)
			name.append(".x").append(i);
		final var source = "class a {\n\t" + name + " f;\n}\n";
		ReflectionUtil.clearCache();
		ReflectionUtil.classForNameCallCount.set(0);
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(0, violations.size());
		assertNotEquals(0, ReflectionUtil.classForNameCallCount.get());
	}

	@Test
	public void testEnclosingSelfQualOverCeilingSuppressesWithoutReflection() throws Exception {
		final var name = new StringBuilder("a");
		for (var i = 1; i <= 17; ++i)
			name.append(".x").append(i);
		final var source = "class a {\n\t" + name + " f;\n}\n";
		ReflectionUtil.clearCache();
		ReflectionUtil.classForNameCallCount.set(0);
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(0, violations.size());
		assertEquals(0, ReflectionUtil.classForNameCallCount.get());
	}

	@Test
	public void testGenericOuterNestedTypeDropsTypeArgumentsFromName() throws Exception {
		final var source = "class T {\n\tjava.util.Map<String, Integer>.Entry e;\n}\n";
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(1, violations.size());
		assertEquals(2, violations.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.util.Map.Entry'.", violations.getFirst().getMessage());
	}

	/**
	 * Only the nearest enclosing body that has a member type of the name decides: one that passes
	 * down the qualified type itself strips even inside a body passing down another, an unreadable
	 * one is unknown even when a body further out reads, and a type of this file is left to the
	 * fixer's shadow guard. Two different member types of the name reach the body as a find.
	 */
	@CsvSource(delimiterString = " => ", value = {
			"abstract class T implements java.util.concurrent.Future<String> { static class Worker extends Thread { Object site; } } => ABSENT",
			"abstract class T implements java.util.concurrent.Future<String> { static class Sub extends Missing { Object site; } } => UNKNOWN",
			"class T extends Missing { abstract static class Task implements java.util.concurrent.Future<String> { Object site; } } => FOUND",
			"abstract class T implements java.util.concurrent.Future<String> { static class Base { enum State { A } } static class Sub extends Base { Object site; } } => ABSENT",
			"abstract class T implements java.util.concurrent.Future<String> { static class Nested { Object site; } } => FOUND",
			"abstract class T extends com.etk2000.checkstyle.testhelpers.ClasspathThreadFuture { Object site; } => FOUND",
			"abstract class T extends Thread implements java.util.concurrent.Future<String> { Object site; } => FOUND"
	})
	@ParameterizedTest
	public void testInheritedMemberTypeAtNearestBody(String source, ScopeLookup expected) throws Exception {
		assertEquals(expected, PreferImportCheck.inheritedMemberTypeAt(parse(source), 0, source.indexOf("site"), "java.lang.Thread.State"));
	}

	@Test
	public void testInheritedMemberTypeLocatorOnDeepChainDoesNotOverflow(@TempDir Path dir) throws Exception {
		final var lines = SupertypeChain.build("preferimport/InputSupertypeChainUnknownRoot.java", SupertypeChain.DEEP, "", "");
		final var file = dir.resolve("Chain.java");
		Files.write(file, lines);
		final var root = JavaParser.parse(new FileContents(new FileText(file.toFile(), StandardCharsets.UTF_8.name())));
		final var line = SupertypeChain.lineContaining(lines, "java.lang.Thread.State");
		final var column = lines.get(line).indexOf("java.lang.Thread.State");
		assertEquals(
				ScopeLookup.UNKNOWN,
				SupertypeChain.onSmallStack(() -> PreferImportCheck.inheritedMemberTypeAt(root, line, column, "java.lang.Thread.State"))
		);
	}

	@Test
	public void testInheritedMemberTypeOutranksSingleTypeImport() throws Exception {
		final var source = """
				import com.etk2000.checkstyle.testhelpers.ClasspathGenericOuter.Inner;
				import com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter;

				class T extends ClasspathScopeOuter {
					static class Sub extends Inner {
						Thread current() {
							return java.lang.Thread.currentThread();
						}
					}
				}
				""";
		assertEquals(0, BaseCheckTest.runCheckInline(PreferImportCheck.class, source).size());
	}

	/**
	 * A supertype's simple name reads as a single-type import, then a static member type a single
	 * static import brings in, then the package, on-demand imports and {@code java.lang}.
	 * {@code imports} holds the file's import lines separated by {@code |}.
	 */
	@CsvSource({
			"import static java.awt.geom.Point2D.Double;, Double, java.lang.Float, FOUND",
			"import static java.awt.geom.Point2D.Double;, Double, java.awt.geom.Point2D.Float, ABSENT",
			"import static com.etk2000.checkstyle.testhelpers.ClasspathStaticImportShadows.Exception;, Exception, java.lang.Thread.State, ABSENT",
			"import static com.etk2000.checkstyle.testhelpers.ClasspathStaticImportShadows.Number;, Number, java.lang.Thread.State, ABSENT",
			"import java.lang.Exception;|import static com.etk2000.checkstyle.testhelpers.ClasspathStaticImportShadows.Exception;, Exception, java.lang.Thread.State, ABSENT",
			"import java.lang.Thread;|import static com.unknown.Missing.Thread;, Thread, java.util.concurrent.Future.State, FOUND",
			"import static com.unknown.Missing.Thread;, Thread, java.util.concurrent.Future.State, UNKNOWN",
			"import static com.etk2000.checkstyle.testhelpers.ClasspathThreadFuture.State;, State, java.lang.Thread.State, UNKNOWN",
			"import static java.awt.geom.Point2D.Float;, Thread, java.util.concurrent.Future.State, FOUND",
			"import static com.etk2000.checkstyle.testhelpers.ClasspathStaticImportShadows.Double;|import static java.awt.geom.Point2D.Double;, Double, java.lang.Float, FOUND",
			"import static java.awt.geom.Point2D.Double;|import static com.etk2000.checkstyle.testhelpers.ClasspathStaticImportShadows.Double;, Double, java.lang.Float, FOUND",
			"import java.awt.geom.Point2D.Double;|import static com.unknown.Missing.Double;, Double, java.lang.Float, FOUND",
			"import java.awt.geom.Point2D.*;|import static com.unknown.Missing.Double;, Double, java.lang.Float, UNKNOWN",
			"import static com.etk2000.checkstyle.testhelpers.ClasspathStaticImportShadows.Thread;, Thread, java.util.concurrent.Future.State, ABSENT",
			"import static com.unknown.Missing.Double;|import static java.awt.geom.Point2D.Double;, Double, java.lang.Float, FOUND",
			"import static java.awt.geom.Point2D.Double;|import static com.unknown.Missing.Double;, Double, java.lang.Float, FOUND"
	})
	@ParameterizedTest
	public void testInheritedMemberTypeThroughStaticImportedSupertype(String imports, String supertype, String qualifiedName, ScopeLookup expected) throws Exception {
		final var source = imports.replace('|', '\n') + "\nclass T extends " + supertype + " {\n\tObject site;\n}\n";
		final var lines = List.of(source.split("\n", -1));
		final var line = lines.size() - 3;
		assertEquals(expected, PreferImportCheck.inheritedMemberTypeAt(parse(source), line, lines.get(line).indexOf("site"), qualifiedName));
	}

	@Test
	public void testJustUnderDepthCapWithTypeRootIsNested() throws Exception {
		final var name = new StringBuilder("Map");
		for (var i = 1; i <= 16; ++i)
			name.append(".a").append(i);
		final var full = name.toString();
		final var source = "import java.util.Map;\nclass T {\n\t" + full + " f;\n}\n";
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(1, violations.size());
		assertEquals(3, violations.getFirst().getLine());
		assertEquals(SeverityLevel.WARNING, violations.getFirst().getSeverityLevel());
		final var qualifier = full.substring(0, full.lastIndexOf('.'));
		assertEquals("Consider importing the nested type 'a16' instead of qualifying it through '" + qualifier + "'.", violations.getFirst().getMessage());
	}

	/**
	 * A member type a nearer body inherits, from a classpath supertype, through a same-file one, or
	 * from a same-file one, outranks a top-level type of the file. Every line marked {@code flagged}
	 * reports its {@code java.lang} qualifier as the error, and nothing else reports.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
			"""
			import com.etk2000.checkstyle.testhelpers.ClasspathScopeBase;
			import com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter;
			class T {
				static class Mid extends ClasspathScopeOuter {
					static class Sub extends Inner {
						Thread current() {
							return java.lang.Thread.currentThread();
						}
						long max() {
							return java.lang.Long.MAX_VALUE; // flagged
						}
					}
				}
			}
			class Inner extends ClasspathScopeBase {}
			""",
			"""
			import com.etk2000.checkstyle.testhelpers.ClasspathScopeBase;
			import com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter;
			class T {
				static class Base extends ClasspathScopeOuter {}
				static class Mid extends Base {
					static class Sub extends Inner {
						Thread current() {
							return java.lang.Thread.currentThread();
						}
						long max() {
							return java.lang.Long.MAX_VALUE; // flagged
						}
					}
				}
			}
			class Inner extends ClasspathScopeBase {}
			""",
			"""
			import com.etk2000.checkstyle.testhelpers.ClasspathScopeBase;
			import com.etk2000.checkstyle.testhelpers.ClasspathScopeConstants;
			class T {
				static class Mid extends MidBase {
					static class Sub extends Inner {
						Thread current() {
							return java.lang.Thread.currentThread(); // flagged
						}
						long max() {
							return java.lang.Long.MAX_VALUE; // flagged
						}
						String separator() {
							return java.lang.System.lineSeparator();
						}
					}
				}
				static class MidBase {
					static class Inner implements ClasspathScopeConstants {}
				}
			}
			class Inner extends ClasspathScopeBase {}
			"""
	})
	public void testNearerInheritedMemberTypeOutranksTopLevelType(String source) throws Exception {
		final var lines = source.split("\n", -1);
		final var expected = new ArrayList<String>();
		for (var i = 0; i < lines.length; ++i) {
			if (lines[i].endsWith("// flagged")) {
				final var qualified = lines[i].substring(lines[i].indexOf("java.lang."), lines[i].indexOf('.', lines[i].indexOf("java.lang.") + "java.lang.".length()));
				expected.add((i + 1) + " ERROR Use an import instead of fully qualified name '" + qualified + "'.");
			}
		}
		final var actual = new ArrayList<String>();
		for (var event : BaseCheckTest.runCheckInline(PreferImportCheck.class, source))
			actual.add(event.getLine() + " " + event.getSeverityLevel().name() + " " + event.getMessage());
		assertEquals(expected, actual);
	}

	/**
	 * A static import of a class this file declares brings in the static member types it declares or
	 * inherits ({@link VariableScope#lookupStaticImportedMemberType}); one it declares itself is left
	 * to the fixer's shadow guard, and a non-static one hides the static member type of its supertype.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"class Double {}", "static class Double {}"})
	public void testSameFileStaticImportOwnerOwnMemberType(String member) throws Exception {
		final var root = parse("package p;\nimport static p.T.Holder.*;\nimport java.awt.geom.Point2D;\nclass T {\n\tabstract static class Holder extends Point2D {\n\t\t" + member + "\n\t}\n}\n");
		assertEquals(ScopeLookup.ABSENT, PreferImportCheck.staticImportedMemberType(root, "java.lang.Double", true));
	}

	@Test
	public void testSamePackageClasspathRootIsNested() throws Exception {
		final var source = "package com.etk2000.checkstyle;\nclass T {\n\tLineText.Foo f;\n}\n";
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(1, violations.size());
		assertEquals(3, violations.getFirst().getLine());
		assertEquals(SeverityLevel.WARNING, violations.getFirst().getSeverityLevel());
		assertEquals("Consider importing the nested type 'Foo' instead of qualifying it through 'LineText'.", violations.getFirst().getMessage());
	}

	@Test
	public void testScopeDoesNotLeakBetweenFiles() throws Exception {
		final var violations = BaseCheckTest.runCheckOnFiles(
				PreferImportCheck.class,
				"preferimport/InputPreferImportScopeLeakDonor.java",
				"preferimport/InputPreferImportScopeLeakRecipient.java"
		);
		assertEquals(1, violations.size());
		assertTrue(violations.getFirst().getFileName().endsWith("InputPreferImportScopeLeakRecipient.java"));
		assertEquals(5, violations.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.lang.Boolean'.", violations.getFirst().getMessage());
	}

	@Test
	public void testSiblingSourceFileLowercaseRootSkipsProbe() throws Exception {
		final var dir = Files.createTempDirectory("preferimport-sibling-lower");
		try {
			Files.writeString(dir.resolve("foo.java"), "class foo {\n\tstatic class Nested {}\n}\n");
			final var main = dir.resolve("Main.java");
			Files.writeString(main, "class Main {\n\tfoo.Nested field;\n}\n");
			final var violations = BaseCheckTest.runCheckOnDiskFile(PreferImportCheck.class, main.toFile());
			assertEquals(1, violations.size());
			assertEquals(2, violations.getFirst().getLine());
			assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
			assertEquals("Use an import instead of fully qualified name 'foo.Nested'.", violations.getFirst().getMessage());
		}
		finally {
			Files.deleteIfExists(dir.resolve("foo.java"));
			Files.deleteIfExists(dir.resolve("Main.java"));
			Files.deleteIfExists(dir);
		}
	}

	@Test
	public void testSiblingSourceFileRootIsNested() throws Exception {
		final var dir = Files.createTempDirectory("preferimport-sibling");
		try {
			Files.writeString(dir.resolve("Sibling.java"), "class Sibling {\n\tstatic class Nested {}\n}\n");
			final var main = dir.resolve("Main.java");
			Files.writeString(main, "class Main {\n\tSibling.Nested field;\n}\n");
			final var violations = BaseCheckTest.runCheckOnDiskFile(PreferImportCheck.class, main.toFile());
			assertEquals(1, violations.size());
			assertEquals(2, violations.getFirst().getLine());
			assertEquals(SeverityLevel.WARNING, violations.getFirst().getSeverityLevel());
			assertEquals("Consider importing the nested type 'Nested' instead of qualifying it through 'Sibling'.", violations.getFirst().getMessage());
		}
		finally {
			Files.deleteIfExists(dir.resolve("Sibling.java"));
			Files.deleteIfExists(dir.resolve("Main.java"));
			Files.deleteIfExists(dir);
		}
	}

	/** {@code imports} holds the file's import lines separated by {@code |}. */
	@CsvSource({
			"import static java.awt.geom.Point2D.*;|import static java.lang.Math.*;, java.lang.Double, true, FOUND",
			"import static java.lang.Math.*;|import static java.awt.geom.Point2D.*;, java.lang.Double, true, FOUND",
			"import static com.unknown.Missing.*;|import static java.awt.geom.Point2D.*;, java.lang.Double, true, FOUND",
			"import static java.awt.geom.Point2D.*;|import static com.unknown.Missing.*;, java.lang.Double, true, FOUND",
			"import static java.lang.Math.*;|import static com.unknown.Missing.*;, java.lang.Double, true, UNKNOWN",
			"import static com.unknown.Missing.*;|import static java.lang.Math.*;, java.lang.Double, true, UNKNOWN",
			"import java.util.List;, java.lang.Double, true, ABSENT",
			"import java.util.List;, java.lang.Double, false, ABSENT",
			"import static java.awt.geom.Point2D.Float;, java.lang.Double, false, ABSENT",
			"import static com.etk2000.checkstyle.testhelpers.ClasspathScopeBase.Boolean;, java.lang.Boolean, false, ABSENT",
			"import static java.awt.geom.Point2D.Double;, java.lang.Double, false, FOUND",
			"import static java.awt.geom.Point2D.Double;, java.lang.Double, true, ABSENT",
			"import static java.awt.geom.Point2D.*;, java.lang.Double, false, ABSENT",
			"import static com.unknown.Shapes.Double;, java.lang.Double, false, UNKNOWN",
			"import static java.lang.Thread.State;, java.lang.Thread.State, false, ABSENT",
			"import static java.lang.Thread.*;, java.lang.Thread.State, true, ABSENT",
			"import static java.lang.Thread.*;, java.util.concurrent.Future.State, true, FOUND",
			"import static java.awt.geom.Point2D.Double.*;, java.lang.Float, true, FOUND",
			"import static com.etk2000.checkstyle.testhelpers.ClasspathThreadFuture.*;, java.lang.Thread.State, true, FOUND"
	})
	@ParameterizedTest
	public void testStaticImportedMemberType(String imports, String qualifiedName, boolean onDemand, ScopeLookup expected) throws Exception {
		final var root = parse(imports.replace('|', '\n') + "\nclass T {}\n");
		assertEquals(expected, PreferImportCheck.staticImportedMemberType(root, qualifiedName, onDemand));
	}

	/** A subclass inherits the non-static {@code Double} a static import does not bring in, whichever is asked first. */
	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	public void testStaticOnlyAndInheritedMemberTypeProbesDoNotShareAnswers(boolean staticFirst) throws Exception {
		final var root = parse("""
				import static com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes.*;

				import com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes;

				class T extends ClasspathMemberTypes {
					Object site;
				}
				""");
		final var site = AstQuery.findNodeAt(root, 5, 8, node -> node.getText().equals("site"));
		final var imports = Set.of("com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes");
		final var scope = new VariableScope(root, null, name -> ReflectionUtil.resolveClassName(name, null, imports));
		if (staticFirst)
			assertEquals(ScopeLookup.ABSENT, scope.lookupStaticImportedMemberType("java.lang.Double", true));
		assertEquals(ScopeLookup.FOUND, scope.lookupInheritedMemberType(site, "java.lang.Double"));
		assertEquals(ScopeLookup.ABSENT, scope.lookupStaticImportedMemberType("java.lang.Double", true));
	}

	/** A supertype named through a member type of a class that inherits back from it, which only invalid code has, reads as unknown. */
	@Test
	public void testSupertypeCycleThroughMemberTypeTerminates() throws Exception {
		final var source = """
				class T {
					static class A extends B.Inner {
						int max() {
							return java.lang.Integer.MAX_VALUE;
						}
					}
					static class B extends A {}
				}
				""";
		final var actual = new ArrayList<String>();
		for (var event : BaseCheckTest.runCheckInline(PreferImportCheck.class, source))
			actual.add(event.getLine() + " " + event.getSeverityLevel().name() + " " + event.getMessage());
		assertEquals(
				List.of(
						"2 WARNING Consider importing the nested type 'Inner' instead of qualifying it through 'B'.",
						"4 WARNING " + MSG_UNREADABLE_PREFIX + "java.lang.Integer" + MSG_UNREADABLE_SUFFIX
				),
				actual
		);
	}

	/** Only invalid code names a supertype through an ambiguous inherited member type; the walk then cannot read it. */
	@Test
	public void testSupertypeNamedThroughAnAmbiguousInheritedMemberTypeWarns() throws Exception {
		final var source = """
				import com.etk2000.checkstyle.testhelpers.ClasspathThreadFuture;

				abstract class T extends ClasspathThreadFuture {
					static class Inner extends State {
						int max() {
							return java.lang.Integer.MAX_VALUE;
						}
					}
				}
				""";
		assertSingleWarning(source, "java.lang.Integer", "java.lang.Integer");
	}

	@Test
	public void testSupertypeNamedThroughAnInheritedMemberTypeIsRead() throws Exception {
		final var source = """
				abstract class T extends Thread {
					static class Inner extends State {
						int max() {
							return java.lang.Integer.MAX_VALUE;
						}
					}
				}
				""";
		assertReportedUnlessObscured(source, 4, "java.lang.Integer", false);
	}

	@Test
	public void testSupertypeWalkToAClasspathFieldDoesNotOverflow() throws Exception {
		final var lines = SupertypeChain.build("preferimport/InputSupertypeChainClasspathField.java", SupertypeChain.DEEP, "", "");
		final var violations = SupertypeChain.onSmallStack(() -> BaseCheckTest.runCheckInline(PreferImportCheck.class, String.join("\n", lines)));
		assertEquals(1, violations.size());
		assertEquals(SupertypeChain.lineContaining(lines, "java.lang.Thread") + 1, violations.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.lang.Thread'.", violations.getFirst().getMessage());
	}

	@Test
	public void testSupertypeWalkToAnUnknownRootDoesNotOverflow() throws Exception {
		final var lines = SupertypeChain.build("preferimport/InputSupertypeChainUnknownRoot.java", SupertypeChain.DEEP, "", "");
		final var violations = SupertypeChain.onSmallStack(() -> BaseCheckTest.runCheckInline(PreferImportCheck.class, String.join("\n", lines)));
		assertEquals(2, violations.size());
		assertEquals(SupertypeChain.lineContaining(lines, "java.lang.Thread.State") + 1, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.lang.Thread.State'.", violations.get(0).getMessage());
		assertEquals(SupertypeChain.lineContaining(lines, "java.lang.Integer") + 1, violations.get(1).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(1).getSeverityLevel());
		assertEquals(MSG_UNREADABLE_PREFIX + "java.lang.Integer" + MSG_UNREADABLE_SUFFIX, violations.get(1).getMessage());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"""
			import static com.etk2000.checkstyle.testhelpers.ClasspathScopeBase.*;
			import static com.unknown.Missing.*;
			class T {
				Boolean flag() {
					return java.lang.Boolean.TRUE;
				}
				Thread thread() {
					return java.lang.Thread.currentThread();
				}
			}
			""",
			"""
			import static com.unknown.Missing.*;
			import static com.etk2000.checkstyle.testhelpers.ClasspathScopeBase.*;
			class T {
				Boolean flag() {
					return java.lang.Boolean.TRUE;
				}
				Thread thread() {
					return java.lang.Thread.currentThread();
				}
			}
			"""
	})
	public void testUnknownOnDemandOwnerKeepsAProvenOne(String source) throws Exception {
		assertSingleWarning(source, "currentThread", "java.lang.Thread");
	}

	@Test
	public void testUnloadableDottedSupertypeWarns() throws Exception {
		final var source = """
				class T {
					static class Qualified extends com.unknown.Missing {
						int max() {
							return java.lang.Integer.MAX_VALUE;
						}
					}
				}
				""";
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(2, violations.size());
		assertEquals(2, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'com.unknown.Missing'.", violations.get(0).getMessage());
		assertEquals(4, violations.get(1).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(1).getSeverityLevel());
		assertEquals(MSG_UNREADABLE_PREFIX + "java.lang.Integer" + MSG_UNREADABLE_SUFFIX, violations.get(1).getMessage());
	}

	@Test
	public void testUnloadableNestedSupertypeThroughImportedOuterWarns() throws Exception {
		final var source = """
				import com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter;

				class T {
					static class ThroughOuter extends ClasspathScopeOuter.Missing {
						int max() {
							return java.lang.Integer.MAX_VALUE;
						}
					}
				}
				""";
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(2, violations.size());
		assertEquals(4, violations.get(0).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(0).getSeverityLevel());
		assertEquals("Consider importing the nested type 'Missing' instead of qualifying it through 'ClasspathScopeOuter'.", violations.get(0).getMessage());
		assertEquals(6, violations.get(1).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(1).getSeverityLevel());
		assertEquals(MSG_UNREADABLE_PREFIX + "java.lang.Integer" + MSG_UNREADABLE_SUFFIX, violations.get(1).getMessage());
	}

	/**
	 * A class that loads but whose field types are missing, defined at test time: a field inherited
	 * from it, the same field through a static on-demand import of it, and through a same-file
	 * on-demand owner extending it. Its public field still reads, so {@code Integer} is bound.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
			"""
			import com.etk2000.checkstyle.InputLinkageProbeImportScope;
			class T extends InputLinkageProbeImportScope {
				Object id() {
					return java.util.UUID.randomUUID();
				}
				int max() {
					return java.lang.Integer.MAX_VALUE;
				}
			}
			""",
			"""
			import static com.etk2000.checkstyle.InputLinkageProbeImportScope.*;
			class T {
				Object id() {
					return java.util.UUID.randomUUID();
				}
				int max() {
					return java.lang.Integer.MAX_VALUE;
				}
			}
			""",
			"""
			package com.etk2000.checkstyle;
			import static com.etk2000.checkstyle.T.Holder.*;
			class T {
				static class Holder extends InputLinkageProbeImportScope {}
				Object id() {
					return java.util.UUID.randomUUID();
				}
				int max() {
					return java.lang.Integer.MAX_VALUE;
				}
			}
			"""
	})
	public void testUnreadableFieldTypeWarns(String source, @TempDir Path dir) throws Exception {
		defineLinkageProbes(dir);
		assertSingleWarning(source, "randomUUID", "java.util.UUID");
	}

	@Test
	public void testUnreadableMemberTypeWarns(@TempDir Path dir) throws Exception {
		defineLinkageProbes(dir);
		final var source = """
				import com.etk2000.checkstyle.InputLinkageProbeImportScopeOuter;
				class T extends InputLinkageProbeImportScopeOuter {
					static class Sub extends Inner {
						Thread current() {
							return java.lang.Thread.currentThread();
						}
					}
				}
				""";
		assertSingleWarning(source, "currentThread", "java.lang.Thread");
	}
}