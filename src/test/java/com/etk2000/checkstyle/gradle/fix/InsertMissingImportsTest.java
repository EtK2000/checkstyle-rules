package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InsertMissingImportsTest {
	@Test
	public void testImportAddedAfterLastImportWhenSortsLast() {
		final var lines = new ArrayList<>(List.of("import java.util.Collections;", "import java.util.List;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.Set"));
		assertEquals(1, count);
		final var expected = List.of("import java.util.Collections;", "import java.util.List;", "import java.util.Set;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testImportAddedAtEndWhenTailFullyMasked() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"import java.util.Map; /*",
				"trailing comment",
				"*/"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.Set"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import java.util.Map; /*",
				"trailing comment",
				"*/",
				"import java.util.Set;"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testImportAddedBeforeFirstImport() {
		final var lines = new ArrayList<>(List.of("import java.util.Map;", "import java.util.Set;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of("import java.util.List;", "import java.util.Map;", "import java.util.Set;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testImportAddedBetweenPackageGroupsNoMatchingGroup() {
		final var lines = new ArrayList<>(List.of("import com.example.Foo;", "", "import javax.annotation.Nonnull;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of("import com.example.Foo;", "", "import java.util.List;", "import javax.annotation.Nonnull;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testImportAddedInSamePackageGroup() {
		final var lines = new ArrayList<>(List.of("import java.util.Collections;", "", "import javax.annotation.Nonnull;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of("import java.util.Collections;", "import java.util.List;", "", "import javax.annotation.Nonnull;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testImportAddedInSamePackageGroupBeforeExisting() {
		final var lines = new ArrayList<>(List.of("import com.example.Foo;", "", "import java.util.Map;", "", "import javax.annotation.Nonnull;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"import com.example.Foo;", "", "import java.util.List;", "import java.util.Map;", "", "import javax.annotation.Nonnull;", "class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testImportAddedInSortedPosition() {
		final var lines = new ArrayList<>(List.of("import java.util.Collections;", "import java.util.Map;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of("import java.util.Collections;", "import java.util.List;", "import java.util.Map;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testImportAlreadyPresent() {
		final var lines = new ArrayList<>(List.of("import java.util.List;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(0, count);
		assertEquals(List.of("import java.util.List;", "class T {}"), lines);
	}

	@Test
	public void testImportNotInsertedInsideBlockComment() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"/*",
				"package old.pkg;",
				"import java.util.Map;",
				"*/",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import java.util.List;",
				"/*",
				"package old.pkg;",
				"import java.util.Map;",
				"*/",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testImportNotInsertedInsideBlockCommentedOutImportGroup() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"/*",
				"import static com.zzz.Old.OLD;",
				"*/",
				"import static com.foo.Bar.AAA;",
				"",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static com.foo.Bar.ZZZ"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"/*",
				"import static com.zzz.Old.OLD;",
				"*/",
				"import static com.foo.Bar.AAA;",
				"import static com.foo.Bar.ZZZ;",
				"",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testImportNotInsertedInsideCommentOpenedByAnchorImport() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"import java.util.Map; /*",
				"trailing comment",
				"*/",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.Set"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import java.util.Map; /*",
				"trailing comment",
				"*/",
				"import java.util.Set;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testImportNotInsertedInsidePackageOpenedComment() {
		final var lines = new ArrayList<>(List.of(
				"package com.example; /*",
				"trailing comment",
				"*/",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example; /*",
				"trailing comment",
				"*/",
				"",
				"import java.util.List;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testImportNotInsertedInsideTextBlock() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"class T {",
				"\tString s = \"\"\"",
				"\t\timport java.util.Map;",
				"\t\t\"\"\";",
				"}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import java.util.List;",
				"class T {",
				"\tString s = \"\"\"",
				"\t\timport java.util.Map;",
				"\t\t\"\"\";",
				"}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testImportNotInsertedInsideTextBlockWhenNoRegularImports() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"import static com.foo.Bar.X;",
				"",
				"class T {",
				"\tString s = \"\"\"",
				"\t\timport static com.evil.Y;",
				"\t\t\"\"\";",
				"}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import static com.foo.Bar.X;",
				"",
				"import java.util.List;",
				"class T {",
				"\tString s = \"\"\"",
				"\t\timport static com.evil.Y;",
				"\t\t\"\"\";",
				"}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testImportsAllAlreadyPresent() {
		final var lines = new ArrayList<>(List.of("import java.util.List;", "import java.util.Map;", "import java.util.Set;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List", "java.util.Map", "java.util.Set"));
		assertEquals(0, count);
		final var expected = List.of("import java.util.List;", "import java.util.Map;", "import java.util.Set;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testMaskedRegularImportInFallbackScanIgnored() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"/*",
				"import zzz.First;",
				"*/",
				"import java.util.Map;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("com.other.Thing"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"/*",
				"import zzz.First;",
				"*/",
				"import com.other.Thing;",
				"import java.util.Map;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testMaskedRegularImportInGroupScanIgnored() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"/*",
				"import java.util.Map;",
				"*/",
				"import com.other.Thing;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"/*",
				"import java.util.Map;",
				"*/",
				"import com.other.Thing;",
				"import java.util.List;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testMaskedRegularImportInsideSamePackageGroupIgnored() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"import java.util.ArrayList;",
				"/*",
				"import java.util.Zzz;",
				"*/",
				"import java.util.TreeMap;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.HashMap"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import java.util.ArrayList;",
				"/*",
				"import java.util.Zzz;",
				"*/",
				"import java.util.HashMap;",
				"import java.util.TreeMap;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testMultipleImportsAdded() {
		final var lines = new ArrayList<>(List.of("import java.util.Collections;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List", "java.util.Set"));
		assertEquals(2, count);
		final var expected = List.of("import java.util.Collections;", "import java.util.List;", "import java.util.Set;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testMultipleImportsAddedInSamePackageGroupWithSeparator() {
		final var lines = new ArrayList<>(List.of("import java.util.Collections;", "", "import javax.annotation.Nonnull;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List", "java.util.Set"));
		assertEquals(2, count);
		final var expected = List.of(
				"import java.util.Collections;", "import java.util.List;", "import java.util.Set;", "", "import javax.annotation.Nonnull;", "class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testMultipleImportsInterleavedWithExisting() {
		final var lines = new ArrayList<>(List.of("import java.util.Collections;", "import java.util.Map;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List", "java.util.Set"));
		assertEquals(2, count);
		final var expected = List.of(
				"import java.util.Collections;", "import java.util.List;", "import java.util.Map;", "import java.util.Set;", "class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testMultipleImportsNotInsertedInsideCommentOpenedByAnchorImport() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"import java.util.Map; /*",
				"trailing comment",
				"*/",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.Set", "java.util.TreeMap"));
		assertEquals(2, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import java.util.Map; /*",
				"trailing comment",
				"*/",
				"import java.util.Set;",
				"import java.util.TreeMap;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testMultipleImportsWithPackageNoExistingImports() {
		final var lines = new ArrayList<>(List.of("package com.example;", "", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List", "java.util.Map"));
		assertEquals(2, count);
		final var expected = List.of("package com.example;", "", "import java.util.List;", "import java.util.Map;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testMultipleStaticImportsAddedToExistingGroup() {
		final var lines = new ArrayList<>(List.of(
				"import static org.junit.Assert.assertEquals;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(
				lines, Set.of("static org.junit.Assert.assertNotNull", "static org.junit.Assert.assertTrue")
		);
		assertEquals(2, count);
		final var expected = List.of(
				"import static org.junit.Assert.assertEquals;",
				"import static org.junit.Assert.assertNotNull;",
				"import static org.junit.Assert.assertTrue;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testMultipleStaticImportsAddedToNewGroup() {
		final var lines = new ArrayList<>(List.of("package com.example;", "", "import java.util.Map;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(
				lines, Set.of("static java.util.Objects.requireNonNull", "static java.util.function.Predicate.not")
		);
		assertEquals(2, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import static java.util.Objects.requireNonNull;",
				"import static java.util.function.Predicate.not;",
				"",
				"import java.util.Map;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testMultipleStaticImportsNotInsertedInsideCommentOpenedByAnchor() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"import static com.foo.Bar.AAA; /*",
				"trailing comment",
				"*/",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(
				lines, Set.of("static com.foo.Bar.MMM", "static com.foo.Bar.ZZZ")
		);
		assertEquals(2, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import static com.foo.Bar.AAA; /*",
				"trailing comment",
				"*/",
				"import static com.foo.Bar.MMM;",
				"import static com.foo.Bar.ZZZ;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testNoImportsWithPackage() {
		final var lines = new ArrayList<>(List.of("package com.example;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		assertEquals(List.of("package com.example;", "", "import java.util.List;", "class T {}"), lines);
	}

	@Test
	public void testNoImportsWithPackageAndExistingBlankLine() {
		final var lines = new ArrayList<>(List.of("package com.example;", "", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		assertEquals(List.of("package com.example;", "", "import java.util.List;", "class T {}"), lines);
	}

	@Test
	public void testNoPackageNoImports() {
		final var lines = new ArrayList<>(List.of("class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		assertEquals(List.of("import java.util.List;", "class T {}"), lines);
	}

	@Test
	public void testOnlyStaticImportsNoPackage() {
		final var lines = new ArrayList<>(List.of("import static org.junit.Assert.assertEquals;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		assertEquals(
				List.of("import static org.junit.Assert.assertEquals;", "", "import java.util.List;", "class T {}"),
				lines
		);
	}

	@Test
	public void testOnlyStaticImportsWithBlankAfterNoPackage() {
		final var lines = new ArrayList<>(List.of("import static org.junit.Assert.assertEquals;", "", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		assertEquals(
				List.of("import static org.junit.Assert.assertEquals;", "", "import java.util.List;", "class T {}"),
				lines
		);
	}

	@Test
	public void testOnlyStaticImportsWithPackage() {
		final var lines = new ArrayList<>(List.of("package com.example;", "import static org.junit.Assert.assertEquals;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;", "import static org.junit.Assert.assertEquals;", "", "import java.util.List;", "class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testPartialDuplicate() {
		final var lines = new ArrayList<>(List.of("import java.util.List;", "import java.util.Map;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List", "java.util.Set"));
		assertEquals(1, count);
		final var expected = List.of("import java.util.List;", "import java.util.Map;", "import java.util.Set;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testRegularImportCoveredByWildcard() {
		final var lines = new ArrayList<>(List.of("import java.util.*;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(0, count);
		assertEquals(List.of("import java.util.*;", "class T {}"), lines);
	}

	@Test
	public void testRegularImportNotInsertedInsideCommentOpenedByStaticAnchor() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"import static com.foo.Bar.X; /*",
				"trailing comment",
				"*/",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import static com.foo.Bar.X; /*",
				"trailing comment",
				"*/",
				"",
				"import java.util.List;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testRegularImportPartialWildcardNoMatch() {
		final var lines = new ArrayList<>(List.of("import java.io.*;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		assertEquals(List.of("import java.io.*;", "import java.util.List;", "class T {}"), lines);
	}

	@Test
	public void testStaticAndRegularAddedTogetherNoExistingImports() {
		final var lines = new ArrayList<>(List.of("package com.example;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(
				lines, Set.of("static java.util.Objects.requireNonNull", "java.util.List")
		);
		assertEquals(2, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import static java.util.Objects.requireNonNull;",
				"",
				"import java.util.List;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticAndRegularAddedTogetherNoExistingStaticGroup() {
		final var lines = new ArrayList<>(List.of("package com.example;", "", "import java.util.Map;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static java.util.Objects.requireNonNull", "java.util.Set"));
		assertEquals(2, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import static java.util.Objects.requireNonNull;",
				"",
				"import java.util.Map;",
				"import java.util.Set;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticAndRegularAddedTogetherNoPackageNoExistingImports() {
		final var lines = new ArrayList<>(List.of("class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(
				lines, Set.of("static java.util.Objects.requireNonNull", "java.util.List")
		);
		assertEquals(2, count);
		final var expected = List.of(
				"import static java.util.Objects.requireNonNull;",
				"",
				"import java.util.List;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedBeforeExistingStatics() {
		final var lines = new ArrayList<>(List.of(
				"import static org.junit.Assert.assertNull;",
				"import static org.junit.Assert.assertTrue;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static org.junit.Assert.assertEquals"));
		assertEquals(1, count);
		final var expected = List.of(
				"import static org.junit.Assert.assertEquals;",
				"import static org.junit.Assert.assertNull;",
				"import static org.junit.Assert.assertTrue;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedBeforeExistingStaticsWithRegulars() {
		final var lines = new ArrayList<>(List.of(
				"import static org.junit.Assert.assertTrue;",
				"",
				"import java.util.List;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(
				lines, Set.of("static org.junit.Assert.assertEquals", "java.util.Map")
		);
		assertEquals(2, count);
		final var expected = List.of(
				"import static org.junit.Assert.assertEquals;",
				"import static org.junit.Assert.assertTrue;",
				"",
				"import java.util.List;",
				"import java.util.Map;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedBetweenExistingStatics() {
		final var lines = new ArrayList<>(List.of(
				"import static org.junit.Assert.assertEquals;",
				"import static org.junit.Assert.assertTrue;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static org.junit.Assert.assertNotNull"));
		assertEquals(1, count);
		final var expected = List.of(
				"import static org.junit.Assert.assertEquals;",
				"import static org.junit.Assert.assertNotNull;",
				"import static org.junit.Assert.assertTrue;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedInExistingGroupWithRegularsBelow() {
		final var lines = new ArrayList<>(List.of(
				"import static org.junit.Assert.assertTrue;",
				"",
				"import java.util.List;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static org.junit.Assert.assertEquals"));
		assertEquals(1, count);
		final var expected = List.of(
				"import static org.junit.Assert.assertEquals;",
				"import static org.junit.Assert.assertTrue;",
				"",
				"import java.util.List;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedInSortedPosition() {
		final var lines = new ArrayList<>(List.of(
				"import static org.junit.Assert.assertEquals;",
				"import static org.junit.Assert.assertNull;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static org.junit.Assert.assertTrue"));
		assertEquals(1, count);
		final var expected = List.of(
				"import static org.junit.Assert.assertEquals;",
				"import static org.junit.Assert.assertNull;",
				"import static org.junit.Assert.assertTrue;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedNoExistingGroupPackageNoBlank() {
		final var lines = new ArrayList<>(List.of("package com.example;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static java.util.Objects.requireNonNull"));
		assertEquals(1, count);
		final var expected = List.of("package com.example;", "", "import static java.util.Objects.requireNonNull;", "", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedNoExistingGroupPackageOnly() {
		final var lines = new ArrayList<>(List.of("package com.example;"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static java.util.Objects.requireNonNull"));
		assertEquals(1, count);
		final var expected = List.of("package com.example;", "", "import static java.util.Objects.requireNonNull;", "");
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedNoExistingGroupPackageWithBlank() {
		final var lines = new ArrayList<>(List.of("package com.example;", "", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static java.util.Objects.requireNonNull"));
		assertEquals(1, count);
		final var expected = List.of("package com.example;", "", "import static java.util.Objects.requireNonNull;", "", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedNoExistingGroupWithRegulars() {
		final var lines = new ArrayList<>(List.of("package com.example;", "", "import java.util.Map;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static java.util.Objects.requireNonNull"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import static java.util.Objects.requireNonNull;",
				"",
				"import java.util.Map;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedNoPackageLeadingBlankNoDuplicateBlank() {
		final var lines = new ArrayList<>(List.of("", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static java.util.Objects.requireNonNull"));
		assertEquals(1, count);
		final var expected = List.of("import static java.util.Objects.requireNonNull;", "", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedNoPackageNoExistingStatic() {
		final var lines = new ArrayList<>(List.of("class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static java.util.Objects.requireNonNull"));
		assertEquals(1, count);
		final var expected = List.of("import static java.util.Objects.requireNonNull;", "", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedNoPackageRegularImportsOnly() {
		final var lines = new ArrayList<>(List.of("import java.util.Map;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static java.util.Objects.requireNonNull"));
		assertEquals(1, count);
		final var expected = List.of("import static java.util.Objects.requireNonNull;", "", "import java.util.Map;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportCoveredByWildcard() {
		final var lines = new ArrayList<>(List.of("import static org.junit.Assert.*;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static org.junit.Assert.assertTrue"));
		assertEquals(0, count);
		assertEquals(List.of("import static org.junit.Assert.*;", "class T {}"), lines);
	}

	@Test
	public void testStaticImportNotInsertedInsideCommentOpenedByAnchorStatic() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"import static com.foo.Bar.AAA; /*",
				"trailing comment",
				"*/",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static com.foo.Bar.ZZZ"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import static com.foo.Bar.AAA; /*",
				"trailing comment",
				"*/",
				"import static com.foo.Bar.ZZZ;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportNotInsertedInsidePackageOpenedComment() {
		final var lines = new ArrayList<>(List.of(
				"package com.example; /*",
				"trailing comment",
				"*/",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static java.util.Objects.requireNonNull"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example; /*",
				"trailing comment",
				"*/",
				"",
				"import static java.util.Objects.requireNonNull;",
				"",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportsNotConfused() {
		final var lines = new ArrayList<>(List.of("import static org.junit.Assert.assertEquals;", "import java.util.Collections;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of("import static org.junit.Assert.assertEquals;", "import java.util.Collections;", "import java.util.List;", "class T {}");
		assertEquals(expected, lines);
	}
}