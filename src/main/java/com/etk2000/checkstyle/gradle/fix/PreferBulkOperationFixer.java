package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.PreferBulkOperationCheck;
import com.etk2000.checkstyle.PreferBulkOperationCheck.BulkOp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixer for {@code PreferBulkOperationCheck}. Reuses the check's
 * {@link PreferBulkOperationCheck#classifyAt AST classifier} to read the flagged one-at-a-time loop
 * or {@code forEach} call, then rewrites the reported span into the bulk operation ({@code addAll},
 * {@code Collections.addAll}, {@code putAll}, {@code System.arraycopy}, {@code Arrays.fill}). For a
 * loop the whole statement is replaced; for a {@code forEach} call only the call expression is
 * replaced, so any leading or trailing text on the span's lines (a guarding {@code if}, an enclosing
 * block, a following statement) is preserved. The receiver/operand text is sliced verbatim from the
 * source.
 */
class PreferBulkOperationFixer implements CheckstyleFixer {
	@CheckReturnValue
	@Nonnull
	private static String buildCall(@Nonnull BulkOp op) {
		return switch (op.kind()) {
			case ADD_ALL -> op.first() + ".addAll(" + op.second() + ")";
			case ARRAY_COPY -> "System.arraycopy(" + op.first() + ", 0, " + op.second() + ", 0, " + op.first() + ".length)";
			case COLLECTIONS_ADD_ALL -> "Collections.addAll(" + op.first() + ", " + op.second() + ")";
			case FILL -> "Arrays.fill(" + op.first() + ", " + op.second() + ")";
			case PUT_ALL -> op.first() + ".putAll(" + op.second() + ")";
		};
	}

	@CheckReturnValue
	@Nonnull
	private static Set<String> requiredImports(@Nonnull BulkOp op) {
		return switch (op.kind()) {
			case COLLECTIONS_ADD_ALL -> Set.of("java.util.Collections");
			case FILL -> Set.of("java.util.Arrays");
			default -> Set.of();
		};
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var op = FixerAst.withAst(lines, root -> PreferBulkOperationCheck.classifyAt(root, lines, lineIndex, column));
		if (op == null)
			return null;
		if (op.startLine() < 0 || op.startLine() > op.endLine() || op.endLine() >= lines.size())
			return null;

		final var startText = lines.get(op.startLine());
		final var endText = lines.get(op.endLine());
		// char indices, so these compare like with like. They did not before the span was
		// converted: a code-point column can never exceed its line's char length, so this guard
		// could not fire on the very corruption it looks like it is catching
		if (op.startIndex() < 0 || op.startIndex() > startText.length()
				|| op.endIndex() < 0 || op.endIndex() > endText.length()
				|| (op.startLine() == op.endLine() && op.startIndex() > op.endIndex()))
			return null;

		final var leading = startText.substring(0, op.startIndex());
		final var trailing = endText.substring(op.endIndex());
		final var call = buildCall(op);
		final var replacement = op.statementForm()
				? leading + call + ";" + trailing
				: leading + call + trailing;

		final var original = new ArrayList<>(lines.subList(op.startLine(), op.endLine() + 1));
		if (original.size() == 1 && original.getFirst().equals(replacement))
			return null;

		final var imports = requiredImports(op);
		return imports.isEmpty()
				? new FixResult(op.startLine(), op.endLine(), List.of(replacement))
				: new FixResult(op.startLine(), op.endLine(), List.of(replacement), imports);
	}
}