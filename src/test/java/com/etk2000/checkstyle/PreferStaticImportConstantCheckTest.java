package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.Nonnull;

public class PreferStaticImportConstantCheckTest {
	@Nonnull
	private static List<AuditEvent> run(@Nonnull String source) throws Exception {
		return BaseCheckTest.runCheckInline(PreferStaticImportConstantCheck.class, source);
	}

	@Test
	public void testCinitConflictSuppresses() throws Exception {
		final var source = "import foo.Foo;\nimport static other.Bar.X;\nclass T {\n\tprivate static final int X;\n\n\tstatic {\n\t\tX = Foo.X;\n\t}\n}";
		assertEquals(0, run(source).size());
	}

	@Test
	public void testCompletesOnDeeplyNestedDotChain() throws Exception {
		final var chain = new StringBuilder("a0");
		for (var i = 1; i < 2000; ++i)
			chain.append(".a").append(i);
		final var source = "class T {\n\tprivate static final int X = " + chain + ";\n}\n";
		final var violations = BaseCheckTest.runCheckInline(PreferStaticImportConstantCheck.class, source);
		assertEquals(1, violations.size());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals(2, violations.getFirst().getLine());
		assertEquals("Replace 'X' alias of '" + chain + "' with a static import.", violations.getFirst().getMessage());
	}

	@Test
	public void testConflictViaDefaultPackageSiblingSuppresses(@Nonnull @TempDir Path dir) throws Exception {
		Files.writeString(dir.resolve("Foo.java"), "public class Foo {\n\tpublic static final int X = 0;\n}");
		final var input = dir.resolve("Input.java");
		Files.writeString(input, "import static other.Bar.X;\nclass Input {\n\tprivate static final int X = Foo.X;\n}");
		assertEquals(0, BaseCheckTest.runCheckOnDiskFile(PreferStaticImportConstantCheck.class, input.toFile()).size());
	}

	@Test
	public void testConflictViaExplicitImportSuppresses() throws Exception {
		final var source = "import foo.Foo;\nimport static other.Bar.X;\nclass T {\n\tprivate static final int X = Foo.X;\n}";
		assertEquals(0, run(source).size());
	}

	@Test
	public void testConflictViaFqLowercaseSuppresses() throws Exception {
		final var source = "import static other.Bar.X;\nclass T {\n\tprivate static final int X = com.foo.Bar.X;\n}";
		assertEquals(0, run(source).size());
	}

	@Test
	public void testConflictViaJavaLangImplicitSuppresses() throws Exception {
		final var source = "import static other.Sizes.SIZE;\nclass T {\n\tprivate static final int X = Integer.SIZE;\n}";
		assertEquals(0, run(source).size());
	}

	@Test
	public void testConflictViaSamePackageSiblingSuppresses(@Nonnull @TempDir Path dir) throws Exception {
		Files.writeString(dir.resolve("Foo.java"), "package x;\npublic class Foo {\n\tpublic static final int X = 0;\n}");
		final var input = dir.resolve("Input.java");
		Files.writeString(input, "package x;\nimport static other.Bar.X;\nclass Input {\n\tprivate static final int X = Foo.X;\n}");
		assertEquals(0, BaseCheckTest.runCheckOnDiskFile(PreferStaticImportConstantCheck.class, input.toFile()).size());
	}

	@Test
	public void testConflictViaSingleWildcardSuppresses() throws Exception {
		final var source = "import foo.*;\nimport static other.Bar.X;\nclass T {\n\tprivate static final int X = Foo.X;\n}";
		assertEquals(0, run(source).size());
	}

	@Test
	public void testFqLhsCinitFires() throws Exception {
		final var source = """
				package com.etk2000.checkstyle.inputs.preferstaticimportconstant;

				import com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass;

				class InputPreferStaticImportConstantFqLhsCinit {
					private static final int FQ_LHS_CINIT;

					static {
						com.etk2000.checkstyle.inputs.preferstaticimportconstant.InputPreferStaticImportConstantFqLhsCinit.FQ_LHS_CINIT = AnchorClass.X27;
					}
				}
				""";
		final var violations = BaseCheckTest.runCheckInline(PreferStaticImportConstantCheck.class, source);
		assertEquals(1, violations.size());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals(6, violations.getFirst().getLine());
		assertEquals("Replace 'FQ_LHS_CINIT' alias of 'AnchorClass.X27' with a static import.", violations.getFirst().getMessage());
	}

	@Test
	public void testMultiVariableSingleLineFiresPerAliasVariable() throws Exception {
		final var source = """
				package com.etk2000.checkstyle.inputs.preferstaticimportconstant;

				import com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass;

				class InputPreferStaticImportConstantMultiVarSingleLine {
					private static final int SINGLE_A = AnchorClass.X1, SINGLE_B = AnchorClass.X2;
				}
				""";
		final var violations = BaseCheckTest.runCheckInline(PreferStaticImportConstantCheck.class, source);
		assertEquals(2, violations.size());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals(6, violations.get(0).getLine());
		assertEquals("Replace 'SINGLE_A' alias of 'AnchorClass.X1' with a static import.", violations.get(0).getMessage());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals(6, violations.get(1).getLine());
		assertEquals("Replace 'SINGLE_B' alias of 'AnchorClass.X2' with a static import.", violations.get(1).getMessage());
	}

	@Test
	public void testNestedClassAliasConflictSuppresses() throws Exception {
		final var source = "import foo.Outer;\nimport static other.Baz.X;\nclass T {\n\tprivate static final int X = Outer.Inner.X;\n}";
		assertEquals(0, run(source).size());
	}

	@Test
	public void testNonStaticTypeImportSameNameDoesNotConflict() throws Exception {
		final var source = "import foo.Foo;\nimport other.Bar.X;\nclass T {\n\tprivate static final int X = Foo.X;\n}";
		final var violations = run(source);
		assertEquals(1, violations.size());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals(4, violations.getFirst().getLine());
		assertEquals("Replace 'X' alias of 'Foo.X' with a static import.", violations.getFirst().getMessage());
	}

	@Test
	public void testStaticWildcardImportDoesNotConflict() throws Exception {
		final var source = "import foo.Foo;\nimport static other.Bar.*;\nclass T {\n\tprivate static final int X = Foo.X;\n}";
		final var violations = run(source);
		assertEquals(1, violations.size());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals(4, violations.getFirst().getLine());
		assertEquals("Replace 'X' alias of 'Foo.X' with a static import.", violations.getFirst().getMessage());
	}

	@Test
	public void testTwoWildcardsIndeterminateFiresDespiteConflict() throws Exception {
		final var source = "import foo.*;\nimport baz.*;\nimport static other.Bar.X;\nclass T {\n\tprivate static final int X = Foo.X;\n}";
		final var violations = run(source);
		assertEquals(1, violations.size());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals(5, violations.getFirst().getLine());
		assertEquals("Replace 'X' alias of 'Foo.X' with a static import.", violations.getFirst().getMessage());
	}
}