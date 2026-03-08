package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

class BaseCheckTest {
	@Nonnull
	static List<AuditEvent> runCheck(@Nonnull Class<? extends AbstractCheck> checkClass, @Nonnull String inputPath) throws Exception {
		final var checkConfig = new DefaultConfiguration(checkClass.getName());

		final var treeWalkerConfig = new DefaultConfiguration(TreeWalker.class.getName());
		treeWalkerConfig.addChild(checkConfig);

		final var checkerConfig = new DefaultConfiguration("Checker");
		checkerConfig.addChild(treeWalkerConfig);

		final var checker = new Checker();
		checker.setModuleClassLoader(checkClass.getClassLoader());
		checker.configure(checkerConfig);

		final var violations = new ArrayList<AuditEvent>();
		checker.addListener(new AuditListener() {
			@Override
			public void addError(AuditEvent event) {
				violations.add(event);
			}

			@Override
			public void addException(AuditEvent event, Throwable throwable) {
			}

			@Override
			public void auditFinished(AuditEvent event) {
			}

			@Override
			public void auditStarted(AuditEvent event) {
			}

			@Override
			public void fileFinished(AuditEvent event) {
			}

			@Override
			public void fileStarted(AuditEvent event) {
			}
		});

		final var url = BaseCheckTest.class.getResource("/com/etk2000/checkstyle/inputs/" + inputPath);
		Objects.requireNonNull(url, "Test input file not found: " + inputPath);

		checker.process(List.of(new File(url.toURI())));
		checker.destroy();
		return violations;
	}
}