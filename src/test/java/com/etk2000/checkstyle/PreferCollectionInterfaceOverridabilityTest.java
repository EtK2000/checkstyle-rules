package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Direct tests for the "could another file override this" predicate. Driving it through the check
 * needs a signature naming a collection, which limits a fixture to one public top-level type and
 * makes the flagged half fail the cross-check lint that reads every test resource. Calling the
 * predicate instead lets one collection-free resource carry every modifier and owner shape at once,
 * and distinguishes which arm answered rather than only whether a violation appeared.
 */
public class PreferCollectionInterfaceOverridabilityTest {
	private static final String FIXTURE = "prefercollectioninterface/InputCollectionInterfaceOverridability.java";

	@CheckReturnValue
	@Nullable
	private static DetailAST findConstructor(@Nullable DetailAST node, @Nonnull String ownerName) {
		for (var current = node; current != null; current = current.getNextSibling()) {
			final var ident = current.getType() == TokenTypes.CTOR_DEF
					? current.findFirstToken(TokenTypes.IDENT)
					: null;
			if (ident != null && ownerName.equals(ident.getText()))
				return current;

			final var found = findConstructor(current.getFirstChild(), ownerName);
			if (found != null)
				return found;
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findMethod(@Nullable DetailAST node, @Nonnull String name) {
		for (var current = node; current != null; current = current.getNextSibling()) {
			final var ident = current.getType() == TokenTypes.METHOD_DEF
					? current.findFirstToken(TokenTypes.IDENT)
					: null;
			if (ident != null && name.equals(ident.getText()))
				return current;

			final var found = findMethod(current.getFirstChild(), name);
			if (found != null)
				return found;
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findRecord(@Nullable DetailAST node, @Nonnull String name) {
		for (var current = node; current != null; current = current.getNextSibling()) {
			final var ident = current.getType() == TokenTypes.RECORD_DEF
					? current.findFirstToken(TokenTypes.IDENT)
					: null;
			if (ident != null && name.equals(ident.getText()))
				return current;

			final var found = findRecord(current.getFirstChild(), name);
			if (found != null)
				return found;
		}
		return null;
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST parseFixture() throws Exception {
		final var url = PreferCollectionInterfaceOverridabilityTest.class
				.getResource("/com/etk2000/checkstyle/inputs/" + FIXTURE);
		requireNonNull(url, "Test input file not found: " + FIXTURE);
		return JavaParser.parseFile(new File(url.toURI()), JavaParser.Options.WITHOUT_COMMENTS);
	}

	@CheckReturnValue
	private static boolean sealedAgainstOverride(@Nonnull DetailAST root, @Nonnull String methodName) {
		final var method = requireNonNull(findMethod(root, methodName), methodName + " not found in " + FIXTURE);
		return PreferCollectionInterfaceCheck.sealedAgainstOutsideOverride(method);
	}

	@CheckReturnValue
	private static boolean sealedInsideThisFile(@Nonnull DetailAST root, @Nonnull String methodName) {
		final var method = requireNonNull(findMethod(root, methodName), methodName + " not found in " + FIXTURE);
		return PreferCollectionInterfaceCheck.sealedInsideThisFile(method);
	}

	@Test
	public void testAConstructorIsSealedAgainstOverrideButNotAgainstCalls() throws Exception {
		final var root = parseFixture();
		final var ctor = requireNonNull(
				findConstructor(root, "InputCollectionInterfaceOverridability"),
				"the fixture's own constructor"
		);
		assertTrue(
				PreferCollectionInterfaceCheck.sealedAgainstOutsideOverride(ctor),
				"nothing can override a constructor, so its parameters are always safe to widen"
		);
		assertFalse(
				PreferCollectionInterfaceCheck.sealedInsideThisFile(ctor),
				"a public constructor on a nameable type is still callable from another file"
		);
	}

	@CsvSource({
			// shape, sealed against an outside override, sealed inside this file
			"finalMethod, false, false",
			"nestedInEnum, false, false",
			"nestedInFinal, false, false",
			"onAnonymous, true, true",
			"onEnum, true, false",
			"onEnumConstant, true, true",
			"onFinalOwner, true, false",
			"onInterface, false, false",
			"onLocal, true, true",
			"onPackagePrivateOwner, false, false",
			"onPrivateNestedOwner, false, false",
			"onRecord, true, false",
			"onSealedOwner, false, false",
			"packagePrivateMethod, false, false",
			"privateMethod, true, true",
			"publicMethod, false, false",
			"staticMethod, false, false",
			"withPrivateConstructorsOnly, false, false"
	})
	@ParameterizedTest
	public void testReachabilityOfEveryOwnerShape(
			@Nonnull String methodName,
			boolean sealedAgainstOverride,
			boolean sealedInside
	) throws Exception {
		final var root = parseFixture();
		assertEquals(
				sealedAgainstOverride,
				sealedAgainstOverride(root, methodName),
				methodName + ": is it sealed against an override in another file"
		);
		assertEquals(
				sealedInside,
				sealedInsideThisFile(root, methodName),
				methodName + ": is it sealed against being named from another file"
		);
	}

	@CsvSource({
			// record, unnameable outside this file
			"InAnonymous, true",
			"InConstant, true",
			"InInterface, false",
			"InMethod, true",
			"PrivateRecord, true",
			"PublicRecord, false"
	})
	@ParameterizedTest
	public void testWhetherARecordIsNameableFromAnotherFile(@Nonnull String recordName, boolean unnameable) throws Exception {
		final var root = parseFixture();
		final var recordDef = requireNonNull(findRecord(root, recordName), recordName + " not found in " + FIXTURE);
		assertEquals(
				unnameable,
				PreferCollectionInterfaceCheck.recordIsUnnameableOutsideThisFile(recordDef),
				recordName + ": is the record unnameable outside this file"
		);
	}
}