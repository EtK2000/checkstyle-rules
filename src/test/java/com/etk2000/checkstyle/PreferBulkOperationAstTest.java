package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;

/**
 * Direct AST tests for token types that cannot appear in test resource files
 * because other checks would flag them (e.g. POST_INC blocked by PreferPrefixIncrementCheck).
 */
public class PreferBulkOperationAstTest {
	@Nonnull
	private static List<AuditEvent> runCheckOnSource(@Nonnull String source) throws Exception {
		final var tmp = File.createTempFile("test", ".java");
		try {
			Files.writeString(tmp.toPath(), source);

			final var checkConfig = new DefaultConfiguration(PreferBulkOperationCheck.class.getName());
			final var treeWalkerConfig = new DefaultConfiguration(TreeWalker.class.getName());
			treeWalkerConfig.addChild(checkConfig);
			final var checkerConfig = new DefaultConfiguration("Checker");
			checkerConfig.addChild(treeWalkerConfig);

			final var checker = new Checker();
			checker.setModuleClassLoader(PreferBulkOperationCheck.class.getClassLoader());
			checker.configure(checkerConfig);

			final var violations = new ArrayList<AuditEvent>();
			checker.addListener(new AuditListener() {
				@Override
				public void addError(@Nonnull AuditEvent event) {
					violations.add(event);
				}

				@Override
				public void addException(@Nonnull AuditEvent event, @Nonnull Throwable throwable) {
				}

				@Override
				public void auditFinished(@Nonnull AuditEvent event) {
				}

				@Override
				public void auditStarted(@Nonnull AuditEvent event) {
				}

				@Override
				public void fileFinished(@Nonnull AuditEvent event) {
				}

				@Override
				public void fileStarted(@Nonnull AuditEvent event) {
				}
			});

			checker.process(List.of(tmp));
			checker.destroy();
			return violations;
		}
		finally {
			tmp.delete();
		}
	}

	@Test
	public void testPostIncrementArrayCopy() throws Exception {
		final var violations = runCheckOnSource("class T {\n\tvoid f(int[] dst, int[] src) {\n\t\tfor (var i = 0; i < src.length; i++)\n\t\t\tdst[i] = src[i];\n\t}\n}");
		assertEquals(1, violations.size());
		assertEquals("Use 'System.arraycopy(src, 0, dst, 0, src.length)' instead of a loop that copies elements one at a time.", violations.getFirst().getMessage());
	}

	@Test
	public void testPostIncrementArrayFill() throws Exception {
		final var violations = runCheckOnSource("class T {\n\tvoid f(int[] arr) {\n\t\tfor (var i = 0; i < arr.length; i++)\n\t\t\tarr[i] = 0;\n\t}\n}");
		assertEquals(1, violations.size());
		assertEquals("Use 'Arrays.fill(arr, 0)' instead of a loop that assigns a constant.", violations.getFirst().getMessage());
	}

	@Test
	public void testPostIncrementIndexedAddAll() throws Exception {
		final var violations = runCheckOnSource("import java.util.List;\nclass T {\n\tvoid f(List<String> target, List<String> source) {\n\t\tfor (var i = 0; i < source.size(); i++)\n\t\t\ttarget.add(source.get(i));\n\t}\n}");
		assertEquals(1, violations.size());
		assertEquals("Use 'target.addAll(source)' instead of a loop that adds elements one at a time.", violations.getFirst().getMessage());
	}
}