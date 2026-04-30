package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

class BaseCheckTest {
	@Nonnull
	static List<AuditEvent> runCheck(@Nonnull Class<? extends AbstractCheck> checkClass, @Nonnull String inputPath) throws Exception {
		return runCheck(checkClass, inputPath, new String[0]);
	}

	@Nonnull
	static List<AuditEvent> runCheck(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull String inputPath,
			@Nonnull String... properties
	) throws Exception {
		final var url = BaseCheckTest.class.getResource("/com/etk2000/checkstyle/inputs/" + inputPath);
		requireNonNull(url, "Test input file not found: " + inputPath);
		return runCheckOnFile(checkClass, new File(url.toURI()), properties);
	}

	@Nonnull
	static List<AuditEvent> runCheckInline(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull String content,
			@Nonnull String... properties
	) throws Exception {
		final var tempFile = File.createTempFile("checkstyle-inline-test", ".java");
		try {
			Files.writeString(tempFile.toPath(), content);
			return runCheckOnFile(checkClass, tempFile, properties);
		}
		finally {
			tempFile.delete();
		}
	}

	@Nonnull
	private static List<AuditEvent> runCheckOnFile(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull File file,
			@Nonnull String... properties
	) throws Exception {
		final var checkConfig = new DefaultConfiguration(checkClass.getName());
		for (var i = 0; i < properties.length; i += 2)
			checkConfig.addProperty(properties[i], properties[i + 1]);

		final var treeWalkerConfig = new DefaultConfiguration(TreeWalker.class.getName());
		treeWalkerConfig.addChild(checkConfig);

		final var checkerConfig = new DefaultConfiguration("Checker");
		checkerConfig.addChild(treeWalkerConfig);

		final var checker = new Checker();
		final var violations = new ArrayList<AuditEvent>();
		try {
			checker.setModuleClassLoader(checkClass.getClassLoader());
			checker.configure(checkerConfig);

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

			checker.process(List.of(file));
		}
		finally {
			checker.destroy();
		}
		return violations;
	}

	@Nonnull
	static List<AuditEvent> runRegexCheck(
			@Nonnull String moduleName,
			@Nonnull String format,
			@Nonnull String inputPath
	) throws Exception {
		final var url = BaseCheckTest.class.getResource("/com/etk2000/checkstyle/inputs/" + inputPath);
		requireNonNull(url, "Test input file not found: " + inputPath);
		return runRegexCheckOnFile(moduleName, format, new File(url.toURI()));
	}

	@Nonnull
	static List<AuditEvent> runRegexCheckInline(
			@Nonnull String moduleName,
			@Nonnull String format,
			@Nonnull String content
	) throws Exception {
		final var tempFile = File.createTempFile("checkstyle-regex-test", ".java");
		try {
			Files.writeString(tempFile.toPath(), content);
			return runRegexCheckOnFile(moduleName, format, tempFile);
		}
		finally {
			tempFile.delete();
		}
	}

	@Nonnull
	private static List<AuditEvent> runRegexCheckOnFile(
			@Nonnull String moduleName,
			@Nonnull String format,
			@Nonnull File file
	) throws Exception {
		final var moduleConfig = new DefaultConfiguration(moduleName);
		moduleConfig.addProperty("format", format);
		moduleConfig.addProperty("message", "test violation");

		final var checkerConfig = new DefaultConfiguration("Checker");
		checkerConfig.addChild(moduleConfig);

		final var checker = new Checker();
		final var violations = new ArrayList<AuditEvent>();
		try {
			checker.setModuleClassLoader(Checker.class.getClassLoader());
			checker.configure(checkerConfig);

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

			checker.process(List.of(file));
		}
		finally {
			checker.destroy();
		}
		return violations;
	}
}