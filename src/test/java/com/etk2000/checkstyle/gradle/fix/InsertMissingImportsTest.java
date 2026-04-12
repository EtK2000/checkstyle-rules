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
	public void testImportsAllAlreadyPresent() {
		final var lines = new ArrayList<>(List.of("import java.util.List;", "import java.util.Map;", "import java.util.Set;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List", "java.util.Map", "java.util.Set"));
		assertEquals(0, count);
		final var expected = List.of("import java.util.List;", "import java.util.Map;", "import java.util.Set;", "class T {}");
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
	public void testMultipleImportsWithPackageNoExistingImports() {
		final var lines = new ArrayList<>(List.of("package com.example;", "", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List", "java.util.Map"));
		assertEquals(2, count);
		final var expected = List.of("package com.example;", "", "import java.util.List;", "import java.util.Map;", "class T {}");
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
		assertEquals(List.of("import java.util.List;", "import static org.junit.Assert.assertEquals;", "class T {}"), lines);
	}

	@Test
	public void testOnlyStaticImportsWithPackage() {
		final var lines = new ArrayList<>(List.of("package com.example;", "import static org.junit.Assert.assertEquals;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of("package com.example;", "", "import java.util.List;", "import static org.junit.Assert.assertEquals;", "class T {}");
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
	public void testStaticImportsNotConfused() {
		final var lines = new ArrayList<>(List.of("import static org.junit.Assert.assertEquals;", "import java.util.Collections;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of("import static org.junit.Assert.assertEquals;", "import java.util.Collections;", "import java.util.List;", "class T {}");
		assertEquals(expected, lines);
	}
}