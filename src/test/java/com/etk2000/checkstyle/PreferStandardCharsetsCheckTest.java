package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.TestResources.CaseSlice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

/**
 * Direct unit tests for {@link PreferStandardCharsetsCheck#standardCharsetConstant}, the lookup
 * shared with {@code PreferStandardCharsetsFixer}. These cover the two things a {@code cases.*}
 * slice cannot express: the JVM default locale, and an exhaustive sweep of every charset alias the
 * running JDK reports.
 *
 * <p>Charset names are obtained from {@link StandardCharsets} rather than written as literals, so
 * that this file does not trip the very check it tests. The locale test builds its map at class
 * load under the JVM's launch locale and looks up under a different one, which is the exact shape
 * of the defect it guards: a locale-sensitive lowercase would leave the two halves disagreeing.</p>
 */
public class PreferStandardCharsetsCheckTest {
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

	/**
	 * The check-level counterpart of {@link #localeSensitiveNamesResolveUnderEveryLocale}: runs every
	 * slice of the topic's corpus under a foreign default locale and asserts its {@code // violation:}
	 * markers still match. Four of the slices carry the only locale-sensitive charset names in the
	 * project ({@code ISO-8859-1} and the {@code US-ASCII} family, whose {@code I} lowercases to a
	 * dotless form in Turkish), so a locale-sensitive lookup makes those violations stop firing.
	 *
	 * <p>Slices are loaded before the locale is changed, and each is asserted against its own lines,
	 * so no expectation here depends on a position in the whole cases file.</p>
	 */
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

	@AfterEach
	void restoreLocale() {
		Locale.setDefault(originalLocale);
	}

	@ParameterizedTest
	@ValueSource(strings = {"Cp1252", "windows-1252", "Shift_JIS", "UTF-9", "", "x", "\"UTF-8\""})
	void unknownNameResolvesToNull(@Nonnull String name) {
		assertNull(PreferStandardCharsetsCheck.standardCharsetConstant(name));
	}
}