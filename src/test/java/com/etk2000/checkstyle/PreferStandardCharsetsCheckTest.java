package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.TestResources.CaseSlice;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.tools.ToolProvider;

/**
 * Unit tests for what a {@code cases.*} slice cannot express: the whole slice corpus re-run under
 * a foreign JVM default locale, a comment-only file, and
 * {@link PreferStandardCharsetsCheck#standardCharsetConstant} swept over every charset alias the running JDK reports,
 * several default locales, and names that resolve to nothing.
 *
 * <p>Charset names are obtained from {@link StandardCharsets} rather than written as literals, so
 * that this file does not trip the very check it tests.</p>
 */
@Isolated
public class PreferStandardCharsetsCheckTest {
	private static final Path LINKAGE_PROBE_SOURCES = Path.of("src", "test", "resources", "com", "etk2000", "checkstyle", "inputs", "reflectionutil");
	private static final String MSG_FIXABLE_FOR_NAME = "Use 'StandardCharsets.UTF_8' instead of 'Charset.forName(\"UTF-8\")'.";
	private static final String MSG_FIXABLE_LITERAL = "Use 'StandardCharsets.UTF_8' instead of \"UTF-8\".";
	private static final String MSG_TAKEN_NAME = "Consider 'StandardCharsets.UTF_8' instead of the charset name \"UTF-8\" where a 'Charset' overload accepts it.";
	private static boolean linkageProbeDefined;

	/** The one event {@code source} reports at minSdk 19 must sit on the line holding {@code site}. */
	private static void assertSingleEvent(
			@Nonnull String source,
			@Nonnull String site,
			@Nonnull SeverityLevel severity,
			@Nonnull String message
	) throws Exception {
		assertSingleEventIn(BaseCheckTest.runCheckInline(PreferStandardCharsetsCheck.class, source, "minSdk", "19"), source, site, severity, message);
	}

	/** {@code events}, reported for {@code source}, must be one event on the line holding {@code site}. */
	private static void assertSingleEventIn(
			@Nonnull List<AuditEvent> events,
			@Nonnull String source,
			@Nonnull String site,
			@Nonnull SeverityLevel severity,
			@Nonnull String message
	) {
		assertEquals(1, events.size());
		assertEquals(source.substring(0, source.indexOf(site)).split("\n", -1).length, events.getFirst().getLine());
		assertEquals(severity, events.getFirst().getSeverityLevel());
		assertEquals(message, events.getFirst().getMessage());
	}

	/**
	 * Compiles the probe classes into {@code dir} and defines the one whose field type is then
	 * missing, once per JVM: defining a class a second time fails.
	 */
	private static synchronized void defineLinkageProbe(@Nonnull Path dir) throws Exception {
		if (linkageProbeDefined)
			return;
		final var compiled = ToolProvider.getSystemJavaCompiler().run(
				null,
				null,
				null,
				"-d",
				dir.toString(),
				LINKAGE_PROBE_SOURCES.resolve("InputLinkageProbeMissing.java").toString(),
				LINKAGE_PROBE_SOURCES.resolve("InputLinkageProbeCharsetsScope.java").toString()
		);
		assertEquals(0, compiled);
		MethodHandles.lookup().defineClass(Files.readAllBytes(dir.resolve("com/etk2000/checkstyle/InputLinkageProbeCharsetsScope.class")));
		linkageProbeDefined = true;
	}

	@Nonnull
	private static List<String> everyNameAndAlias() {
		final var names = new ArrayList<String>();
		for (var charset : standardCharsets()) {
			for (var name : namesOf(charset))
				names.add(name + '\t' + fieldNameOf(charset));
		}
		return names;
	}

	@Nonnull
	private static String fieldNameOf(@Nonnull Charset charset) {
		for (var field : StandardCharsets.class.getDeclaredFields()) {
			try {
				if (Modifier.isStatic(field.getModifiers()) && field.get(null) == charset)
					return field.getName();
			}
			catch (IllegalAccessException e) {
				throw new AssertionError(e);
			}
		}
		throw new AssertionError(charset);
	}

	@Nonnull
	private static List<String> namesOf(@Nonnull Charset charset) {
		final var names = new ArrayList<String>();
		names.add(charset.name());
		names.addAll(charset.aliases());
		return names;
	}

	/** The events {@code source} reports at minSdk 19 as a file with an empty {@code siblingName} source file next to it. */
	@Nonnull
	private static List<AuditEvent> runNextTo(@Nonnull Path dir, @Nonnull String siblingName, @Nonnull String source) throws Exception {
		Files.writeString(dir.resolve(siblingName), "");
		final var file = dir.resolve("Main.java");
		Files.writeString(file, source);
		return BaseCheckTest.runCheckOnDiskFile(PreferStandardCharsetsCheck.class, file.toFile(), "minSdk", "19");
	}

	/** Each charset-taking arm, with a package and in the default package. */
	@Nonnull
	private static List<String> siblingCases() {
		final var bodies = List.of(
				"""
				class Main {
					byte[] encode(String s) throws Exception {
						return s.getBytes("UTF-8");
					}
				}
				""",
				"""
				class Main {
					String decode(byte[] bytes) throws Exception {
						return new String(bytes, "UTF-8");
					}
				}
				""",
				"""
				import java.nio.charset.Charset;

				class Main {
					Charset lookup() {
						return Charset.forName("UTF-8");
					}
				}
				"""
		);
		final var cases = new ArrayList<String>();
		for (var body : bodies) {
			cases.add(body);
			cases.add("package p;\n\n" + body);
		}
		return cases;
	}

	@Nonnull
	private static List<Charset> standardCharsets() {
		final var charsets = new ArrayList<Charset>();
		for (var field : StandardCharsets.class.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers()) && field.getType() == Charset.class) {
				try {
					charsets.add((Charset) field.get(null));
				}
				catch (IllegalAccessException e) {
					throw new AssertionError(e);
				}
			}
		}
		return charsets;
	}

	private final Locale originalLocale = Locale.getDefault();

	@Test
	void caseGroupLocalDoesNotReachEarlierGroup() throws Exception {
		final var source = """
				class T {
					byte[] m(int k, String s) throws Exception {
						switch (k) {
							case 1:
								System.out.println(s.getBytes("UTF-8").length);
							case 2:
								final Object StandardCharsets = null;
								System.out.println(StandardCharsets);
								return null;
							default:
								return null;
						}
					}
				}
				""";
		assertSingleEvent(source, "getBytes", SeverityLevel.ERROR, MSG_FIXABLE_LITERAL);
	}

	@Test
	void caseGroupLocalOfOtherNameLeavesTheNameFree() throws Exception {
		final var source = """
				class T {
					byte[] m(int k, String s) throws Exception {
						switch (k) {
							case 1:
								final Object other = null;
								System.out.println(other);
							case 2:
								return s.getBytes("UTF-8");
							default:
								return null;
						}
					}
				}
				""";
		assertSingleEvent(source, "getBytes", SeverityLevel.ERROR, MSG_FIXABLE_LITERAL);
	}

	/**
	 * A local an unbraced case group declares stays in scope for the later groups, which the
	 * project's own {@code NoCaseBraces} keeps out of every linted fixture.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
			"""
			class T {
				byte[] m(int k, String s) throws Exception {
					switch (k) {
						case 1:
							final Object StandardCharsets = null;
							System.out.println(StandardCharsets);
						case 2:
							return s.getBytes("UTF-8");
						default:
							return null;
					}
				}
			}
			""",
			"""
			class T {
				byte[] m(int k, int j, String s) throws Exception {
					switch (k) {
						case 1:
							final Object StandardCharsets = null;
							System.out.println(StandardCharsets);
						case 2:
							switch (j) {
								case 3 -> {
									return s.getBytes("UTF-8");
								}
								default -> {
									return null;
								}
							}
						default:
							return null;
					}
				}
			}
			"""
	})
	void caseGroupLocalTakesStandardCharsetsInLaterGroups(@Nonnull String source) throws Exception {
		assertSingleEvent(source, "getBytes", SeverityLevel.WARNING, MSG_TAKEN_NAME);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"""
			import com.etk2000.checkstyle.testhelpers.ClasspathScopeBase;
			import java.nio.charset.Charset;
			class T extends ClasspathScopeBase {
				Charset lookup() {
					return Charset.forName("UTF-8");
				}
			}
			""",
			"""
			import java.nio.charset.Charset;
			class T {
				enum Lookup {
					Charset
				}
				Charset lookup() {
					return Charset.forName("UTF-8");
				}
			}
			""",
			"""
			import com.etk2000.checkstyle.testhelpers.ClasspathCharsetHolder;
			import java.nio.charset.Charset;
			class T {
				static class Sub extends ClasspathCharsetHolder {}
				Charset lookup() {
					return Charset.forName("UTF-8");
				}
			}
			"""
	})
	void charsetReceiverOutOfReachStaysProven(@Nonnull String source) throws Exception {
		assertSingleEvent(source, "Charset.forName", SeverityLevel.ERROR, MSG_FIXABLE_FOR_NAME);
	}

	@ParameterizedTest
	@ValueSource(strings = {"tr", "az"})
	void checkMatchesEverySliceMarkerUnderForeignDefaultLocale(@Nonnull String languageTag) throws Exception {
		final var topic = BaseCheckTest.deriveTopic(PreferStandardCharsetsCheck.class);
		// caseNames() lists the fragments file, which carries synthetic-target cases the slice
		// loader cannot resolve; the slice names come from cases.in.java itself
		final var caseNames = TestResources.caseNamesIn(
				TestResources.readResourceLines("/com/etk2000/checkstyle/inputs/" + topic + "/cases.in.java")
		);
		// guard: without a locale-sensitive name in the corpus this test would pass on UTF-8 slices
		// alone, which no locale can break
		final var localeSensitiveSlice = StandardCharsets.ISO_8859_1.name().toLowerCase(Locale.ROOT).replace('-', '_');
		assertTrue(caseNames.contains(localeSensitiveSlice), "corpus lost its locale-sensitive slice");
		final var slices = new ArrayList<CaseSlice>();
		for (var caseName : caseNames)
			slices.add(TestResources.loadCaseSlice(topic, caseName));

		Locale.setDefault(Locale.forLanguageTag(languageTag));
		for (var slice : slices) {
			BaseCheckTest.assertCheckMatchesMarkers(
					PreferStandardCharsetsCheck.class,
					slice.inputLines(),
					slice.caseName(),
					"minSdk",
					"19"
			);
		}
	}

	@Test
	void commentOnlyFileReportsNothing() throws Exception {
		assertEquals(List.of(), BaseCheckTest.runCheckInline(PreferStandardCharsetsCheck.class, "// nothing but a comment\n", "minSdk", "19"));
	}

	/**
	 * A pattern in another constant's arguments, or one of another name, leaves the constant's own
	 * {@code Charset.forName} the fixable error.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
			"""
			class T {
				enum E {
					A(new Object() instanceof String Other ? java.nio.charset.Charset.forName("UTF-8") : null);
					E(Object charset) {
						System.out.println(charset);
					}
				}
			}
			""",
			"""
			class T {
				enum E {
					A(new Object() instanceof String StandardCharsets ? 1 : 0),
					B(java.nio.charset.Charset.forName("UTF-8"));
					E(Object value) {
						System.out.println(value);
					}
				}
			}
			"""
	})
	void enumConstantArgumentPatternOutOfReachKeepsForNameFixable(@Nonnull String source) throws Exception {
		assertSingleEvent(source, "java.nio.charset.Charset.forName", SeverityLevel.ERROR, MSG_FIXABLE_FOR_NAME);
	}

	@Test
	void enumConstantArgumentPatternTakesStandardCharsets() throws Exception {
		final var source = """
				class T {
					enum E {
						A(new Object() instanceof String StandardCharsets ? java.nio.charset.Charset.forName("UTF-8") : null);
						E(Object charset) {
							System.out.println(charset);
						}
					}
				}
				""";
		assertSingleEvent(source, "java.nio.charset.Charset.forName", SeverityLevel.WARNING, MSG_TAKEN_NAME);
	}

	@MethodSource("everyNameAndAlias")
	@ParameterizedTest
	void everyNameAndAliasResolvesInEveryCaseForm(@Nonnull String nameAndConstant) {
		final var parts = nameAndConstant.split("\t");
		final var name = parts[0];
		final var constant = parts[1];
		assertEquals(constant, PreferStandardCharsetsCheck.standardCharsetConstant(name));
		assertEquals(
				constant,
				PreferStandardCharsetsCheck.standardCharsetConstant(name.toUpperCase(Locale.ROOT))
		);
		assertEquals(
				constant,
				PreferStandardCharsetsCheck.standardCharsetConstant(name.toLowerCase(Locale.ROOT))
		);
	}

	/**
	 * Binders a static import or the file's package brings in act on every line of the file, so a
	 * multi-slice fixture would leak them into its other slices: a single static import of a field
	 * named {@code StandardCharsets}, a static on-demand import of one (public, and package-private
	 * from the same package), and a same-package supertype's package-private field.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
			"""
			import static com.etk2000.checkstyle.testhelpers.ClasspathStandardCharsetsHolder.StandardCharsets;
			class T {
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
				void use() {
					System.out.println(StandardCharsets);
				}
			}
			""",
			"""
			import static com.etk2000.checkstyle.testhelpers.ClasspathStandardCharsetsHolder.*;
			class T {
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			""",
			"""
			package com.etk2000.checkstyle.testhelpers;
			import static com.etk2000.checkstyle.testhelpers.ClasspathStandardCharsetsPackagePrivate.*;
			class T {
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			""",
			"""
			package com.etk2000.checkstyle.testhelpers;
			class T extends ClasspathStandardCharsetsPackagePrivate {
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			""",
			"""
			import static com.etk2000.checkstyle.testhelpers.ClasspathStaticImportShadows.Thread;
			class T extends Thread {
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			""",
			"""
			import com.etk2000.checkstyle.testhelpers.ClasspathStaticImportShadows;
			class T {
				static class Mid extends ClasspathStaticImportShadows {
					static class Sub extends Thread {
						byte[] encode(String s) throws Exception {
							return s.getBytes("UTF-8");
						}
					}
				}
				static class Thread {}
			}
			""",
			"""
			class T {
				static class Base {
					static class Thread {
						static final Object StandardCharsets = null;
					}
				}
				static class Mid extends Base {
					static class Sub extends Thread {
						byte[] encode(String s) throws Exception {
							return s.getBytes("UTF-8");
						}
					}
				}
				static class Thread {}
			}
			"""
	})
	void fileWideBinderTakesStandardCharsets(@Nonnull String source) throws Exception {
		assertSingleEvent(source, "getBytes", SeverityLevel.WARNING, MSG_TAKEN_NAME);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"""
			import static com.etk2000.checkstyle.testhelpers.ClasspathCharsetHolder.Charset;
			import java.nio.charset.Charset;
			class T {
				Object lookup() {
					return Charset.forName("UTF-8");
				}
			}
			""",
			"""
			import static com.etk2000.checkstyle.testhelpers.ClasspathCharsetHolder.*;
			import java.nio.charset.Charset;
			class T {
				Object lookup() {
					return Charset.forName("UTF-8");
				}
			}
			"""
	})
	void fileWideBinderTakesTheCharsetReceiver(@Nonnull String source) throws Exception {
		assertSingleEvent(source, "Charset.forName", SeverityLevel.WARNING, MSG_TAKEN_NAME);
	}

	/** A single-type import of the JDK class outranks a same-package {@code StandardCharsets}, with a package and without. */
	@ParameterizedTest
	@ValueSource(strings = {"", "package p;\n\n"})
	void jdkStandardCharsetsImportOutranksTheSiblingSource(@Nonnull String packageLine, @TempDir Path dir) throws Exception {
		final var source = packageLine + """
				import java.nio.charset.StandardCharsets;

				class Main {
					byte[] encode(String s) throws Exception {
						return s.getBytes("UTF-8");
					}

					Object constant() {
						return StandardCharsets.UTF_16;
					}
				}
				""";
		assertSingleEventIn(runNextTo(dir, "StandardCharsets.java", source), source, "getBytes", SeverityLevel.ERROR, MSG_FIXABLE_LITERAL);
	}

	@ParameterizedTest
	@ValueSource(strings = {"tr", "az", "lt", "en", "und"})
	void localeSensitiveNamesResolveUnderEveryLocale(@Nonnull String languageTag) {
		Locale.setDefault(Locale.forLanguageTag(languageTag));
		for (var charset : standardCharsets()) {
			final var expected = fieldNameOf(charset);
			for (var name : namesOf(charset))
				assertEquals(expected, PreferStandardCharsetsCheck.standardCharsetConstant(name));
		}
		// a Turkish lowercase of ISO-8859-1 yields this dotless form; it names no charset
		assertNull(PreferStandardCharsetsCheck.standardCharsetConstant("ıso-8859-1"));
	}

	/**
	 * Binders that do not reach the literal leave {@code StandardCharsets} free, so it stays the
	 * fixable error: a supertype that loads without the field, one whose field is package-private
	 * in another package, the same two shapes as static on-demand import owners, an enum constant of
	 * another type, and a pattern of another member.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
			"""
			import com.etk2000.checkstyle.testhelpers.ClasspathScopeBase;
			class T extends ClasspathScopeBase {
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			""",
			"""
			import com.etk2000.checkstyle.testhelpers.ClasspathStandardCharsetsPackagePrivate;
			class T extends ClasspathStandardCharsetsPackagePrivate {
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			""",
			"""
			import static com.etk2000.checkstyle.testhelpers.ClasspathScopeBase.*;
			class T {
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			""",
			"""
			import static com.etk2000.checkstyle.testhelpers.ClasspathStandardCharsetsPackagePrivate.*;
			class T {
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			""",
			"""
			class T {
				enum Names {
					StandardCharsets
				}
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			""",
			"""
			class T {
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
				boolean matches(Object o) {
					return o instanceof String StandardCharsets && StandardCharsets.isEmpty();
				}
			}
			""",
			"""
			import static java.awt.geom.Point2D.Float;
			class T extends Thread {
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			""",
			"""
			import com.etk2000.checkstyle.testhelpers.ClasspathStandardCharsetsHolder;
			class T {
				static class Base {
					static class Thread {}
				}
				static class Mid extends Base {
					static class Sub extends Thread {
						byte[] encode(String s) throws Exception {
							return s.getBytes("UTF-8");
						}
					}
				}
				static class Thread extends ClasspathStandardCharsetsHolder {}
			}
			"""
	})
	void nonBindingScopeKeepsTheLiteralFixable(@Nonnull String source) throws Exception {
		assertSingleEvent(source, "getBytes", SeverityLevel.ERROR, MSG_FIXABLE_LITERAL);
	}

	@MethodSource("siblingCases")
	@ParameterizedTest
	void noSiblingSourceKeepsTheCharsetReportFixable(@Nonnull String source, @TempDir Path dir) throws Exception {
		final var message = source.contains("forName") ? MSG_FIXABLE_FOR_NAME : MSG_FIXABLE_LITERAL;
		assertSingleEventIn(runNextTo(dir, "Other.java", source), source, "\"UTF-8\"", SeverityLevel.ERROR, message);
	}

	@AfterEach
	void restoreLocale() {
		Locale.setDefault(originalLocale);
	}

	/** A field or enum constant a same-file static on-demand import owner passes in takes {@code StandardCharsets}. */
	@ParameterizedTest
	@ValueSource(strings = {
			"""
			package com.etk2000.checkstyle.testhelpers;
			import static com.etk2000.checkstyle.testhelpers.T.Holder.*;
			class T {
				static class Holder extends ClasspathStandardCharsetsHolder {}
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			""",
			"""
			package com.etk2000.checkstyle.testhelpers;
			import static com.etk2000.checkstyle.testhelpers.T.Kind.*;
			class T {
				enum Kind {
					StandardCharsets
				}
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			"""
	})
	void sameFileStaticImportOwnerTakesStandardCharsets(@Nonnull String source) throws Exception {
		assertSingleEvent(source, "getBytes", SeverityLevel.WARNING, MSG_TAKEN_NAME);
	}

	/** A {@code Charset.java} next to the file does not affect a single-type import of the JDK class, or a literal argument. */
	@MethodSource("siblingCases")
	@ParameterizedTest
	void siblingCharsetSourceKeepsTheCharsetReportFixable(@Nonnull String source, @TempDir Path dir) throws Exception {
		final var message = source.contains("forName") ? MSG_FIXABLE_FOR_NAME : MSG_FIXABLE_LITERAL;
		assertSingleEventIn(runNextTo(dir, "Charset.java", source), source, "\"UTF-8\"", SeverityLevel.ERROR, message);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"""
			class Main {
				Object lookup() {
					return java.nio.charset.Charset.forName("UTF-8");
				}
			}
			""",
			"""
			import static java.nio.charset.Charset.forName;
			class Main {
				Object lookup() {
					return forName("UTF-8");
				}
			}
			"""
	})
	void siblingCharsetSourceLeavesQualifiedAndStaticallyImportedForNameFixable(@Nonnull String source, @TempDir Path dir) throws Exception {
		assertSingleEventIn(runNextTo(dir, "Charset.java", source), source, "\"UTF-8\"", SeverityLevel.ERROR, MSG_FIXABLE_FOR_NAME);
	}

	@MethodSource("siblingCases")
	@ParameterizedTest
	void siblingSourceOfAnotherNameKeepsTheLiteralFixable(@Nonnull String source, @TempDir Path dir) throws Exception {
		final var message = source.contains("forName") ? MSG_FIXABLE_FOR_NAME : MSG_FIXABLE_LITERAL;
		assertSingleEventIn(runNextTo(dir, "StandardCharsetsUtil.java", source), source, "\"UTF-8\"", SeverityLevel.ERROR, message);
	}

	/**
	 * A {@code StandardCharsets.java} next to the file is a same-package class the checkstyle
	 * classpath cannot load, which the inserted import would shadow file-wide.
	 */
	@MethodSource("siblingCases")
	@ParameterizedTest
	void siblingStandardCharsetsSourceTakesTheName(@Nonnull String source, @TempDir Path dir) throws Exception {
		assertSingleEventIn(runNextTo(dir, "StandardCharsets.java", source), source, "\"UTF-8\"", SeverityLevel.WARNING, MSG_TAKEN_NAME);
	}

	@ParameterizedTest
	@ValueSource(strings = {"Cp1252", "windows-1252", "Shift_JIS", "UTF-9", "", "x", "\"UTF-8\""})
	void unknownNameResolvesToNull(@Nonnull String name) {
		assertNull(PreferStandardCharsetsCheck.standardCharsetConstant(name));
	}

	/**
	 * A class that loads but whose field types are missing, defined at test time, may declare a
	 * {@code StandardCharsets} field: as a supertype and as a static on-demand import owner.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
			"""
			import com.etk2000.checkstyle.InputLinkageProbeCharsetsScope;
			class T extends InputLinkageProbeCharsetsScope {
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			""",
			"""
			import static com.etk2000.checkstyle.InputLinkageProbeCharsetsScope.*;
			class T {
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			"""
	})
	void unreadableFieldTypeTakesStandardCharsets(@Nonnull String source, @TempDir Path dir) throws Exception {
		defineLinkageProbe(dir);
		assertSingleEvent(source, "getBytes", SeverityLevel.WARNING, MSG_TAKEN_NAME);
	}

	/**
	 * A class the file cannot read may declare a field named {@code StandardCharsets}, so the name
	 * counts as taken: a supertype that does not resolve, simple and qualified, and one imported but
	 * not on the classpath, reached directly, from an anonymous class, a local class, a member of a
	 * subclass and down a same-file chain, and a static on-demand import owner that does not load,
	 * on the classpath or declared in the file with such a supertype.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
			"""
			class T extends MissingSupertype {
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			""",
			"""
			class T extends com.unknown.Missing {
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			""",
			"""
			import com.unknown.Missing;
			class T extends Missing {
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			""",
			"""
			class T {
				Object anonymous() {
					return new MissingSupertype() {
						byte[] encode(String s) throws Exception {
							return s.getBytes("UTF-8");
						}
					};
				}
			}
			""",
			"""
			class T {
				Object local() {
					class Local extends MissingSupertype {
						byte[] encode(String s) throws Exception {
							return s.getBytes("UTF-8");
						}
					}
					return new Local();
				}
			}
			""",
			"""
			class T extends MissingSupertype {
				static class Member {
					byte[] encode(String s) throws Exception {
						return s.getBytes("UTF-8");
					}
				}
			}
			""",
			"""
			class T extends Middle {
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			class Middle extends MissingSupertype {}
			""",
			"""
			import static com.unknown.Missing.*;
			class T {
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			""",
			"""
			package com.etk2000.checkstyle.testhelpers;
			import static com.etk2000.checkstyle.testhelpers.T.Holder.*;
			class T {
				static class Holder extends MissingSupertype {}
				byte[] encode(String s) throws Exception {
					return s.getBytes("UTF-8");
				}
			}
			"""
	})
	void unreadableScopeTakesStandardCharsets(@Nonnull String source) throws Exception {
		assertSingleEvent(source, "getBytes", SeverityLevel.WARNING, MSG_TAKEN_NAME);
	}

	@Test
	void unreadableScopeTakesTheCharsetReceiver() throws Exception {
		final var source = """
				import java.nio.charset.Charset;
				class T extends MissingSupertype {
					Object lookup() {
						return Charset.forName("UTF-8");
					}
				}
				""";
		assertSingleEvent(source, "Charset.forName", SeverityLevel.WARNING, MSG_TAKEN_NAME);
	}

	@ParameterizedTest
	@ValueSource(strings = {"Other.java", "CharsetUtil.java"})
	void wildcardImportedCharsetNextToAnotherSourceIsFixable(@Nonnull String siblingName, @TempDir Path dir) throws Exception {
		final var source = """
				import java.nio.charset.*;

				class Main {
					Charset lookup() {
						return Charset.forName("UTF-8");
					}
				}
				""";
		assertSingleEventIn(runNextTo(dir, siblingName, source), source, "Charset.forName", SeverityLevel.ERROR, MSG_FIXABLE_FOR_NAME);
	}

	/** A same-package {@code Charset}, which the check cannot load, outranks an import-on-demand of the JDK one, with a package and without. */
	@ParameterizedTest
	@ValueSource(strings = {"", "package p;\n\n"})
	void wildcardImportedCharsetNextToCharsetSourceWarns(@Nonnull String packageLine, @TempDir Path dir) throws Exception {
		final var source = packageLine + """
				import java.nio.charset.*;

				class Main {
					Object lookup() {
						return Charset.forName("UTF-8");
					}
				}
				""";
		assertSingleEventIn(runNextTo(dir, "Charset.java", source), source, "Charset.forName", SeverityLevel.WARNING, MSG_TAKEN_NAME);
	}

	/** A same-package {@code StandardCharsets} outranks an import-on-demand of the JDK one. */
	@Test
	void wildcardImportWithSiblingSourceWarns(@TempDir Path dir) throws Exception {
		final var source = """
				package p;

				import java.nio.charset.*;

				class Main {
					byte[] encode(String s) throws Exception {
						return s.getBytes("UTF-8");
					}

					CharsetDecoder decoder() {
						return null;
					}
				}
				""";
		assertSingleEventIn(runNextTo(dir, "StandardCharsets.java", source), source, "getBytes", SeverityLevel.WARNING, MSG_TAKEN_NAME);
	}
}