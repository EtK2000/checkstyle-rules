package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.AstSplice.afterLparenOf;
import static com.etk2000.checkstyle.gradle.fix.AstSplice.argumentsIn;
import static com.etk2000.checkstyle.gradle.fix.AstSplice.argumentsOf;
import static com.etk2000.checkstyle.gradle.fix.AstSplice.asReceiver;
import static com.etk2000.checkstyle.gradle.fix.AstSplice.discardsAComment;
import static com.etk2000.checkstyle.gradle.fix.AstSplice.firstCommaOf;
import static com.etk2000.checkstyle.gradle.fix.AstSplice.parenthesizeIfNeeded;
import static com.etk2000.checkstyle.gradle.fix.AstSplice.receiverOf;
import static com.etk2000.checkstyle.gradle.fix.AstSplice.reemittableReceiverOf;
import static com.etk2000.checkstyle.gradle.fix.AstSplice.rparenOf;
import static com.etk2000.checkstyle.gradle.fix.AstSplice.spliceNegated;
import static com.etk2000.checkstyle.gradle.fix.AstSplice.spliceNode;
import static com.etk2000.checkstyle.gradle.fix.AstSplice.spliceSpan;
import static com.etk2000.checkstyle.gradle.fix.AstSplice.spliceTail;
import static com.etk2000.checkstyle.gradle.fix.AstSplice.spliceTailToLparen;
import static com.etk2000.checkstyle.gradle.fix.AstSplice.textBetween;

import com.etk2000.checkstyle.AstSpan;
import com.etk2000.checkstyle.AstSpan.TextPos;
import com.etk2000.checkstyle.PreferSpecificApiCheck;
import com.etk2000.checkstyle.PreferSpecificApiCheck.ApiTarget;
import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.gradle.fix.AstSplice.Rewrite;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Rewrites the one call the check reported, located through
 * {@link PreferSpecificApiCheck#locateAt} rather than by matching the line's text.
 *
 * <p>Every rule used to carry its own spelling ({@code scan.indexOf(".stream().count()")}), which
 * made the fixer disagree with the check about what the line said: a space, a comment or a line
 * break inside the chain left the violation reported and silently unfixed. Worse, ten of the
 * eighteen rules ignored the reported column even in the "anchored" sweep, so a rule could
 * rewrite a <em>different</em> call than the one the check accepted, emitting source that did not
 * compile. The AST answers both questions at once: which call was reported, and exactly which
 * characters it occupies.
 *
 * <p>Every rewrite is a splice of one or two spans on one line, and the spans are anchored so
 * that the parts the rewrite keeps are never re-generated from the tree. A tail rewrite starts
 * at the {@code .} introducing the call's own name, so a receiver spanning several lines is left
 * untouched instead of being flattened. Argument text that survives a rewrite is copied verbatim
 * from the source rather than re-sliced per argument, so a comment between two arguments survives
 * with it.
 */
class PreferSpecificApiFixer implements CheckstyleFixer {
	/**
	 * The assertions this rule rewrites, and so the only method names that identify a file's
	 * assertion class well enough to take the replacement from it.
	 */
	private static final Set<String> REWRITTEN_ASSERTIONS =
			Set.of("assertEquals", "assertNotEquals", "assertNotSame", "assertSame");

	/**
	 * Adds a static import for {@code replacementMethod}, taken from the class of the first static
	 * import of a method this rule rewrites.
	 *
	 * <p>The donor is matched against {@link #REWRITTEN_ASSERTIONS} rather than an
	 * {@code assert}/{@code fail} <em>prefix</em>. A prefix accepts anything assertion-shaped, and
	 * in a mixed-framework test {@code org.assertj.core.api.Assertions.assertThat} sorts above the
	 * JUnit import, so rewriting a JUnit {@code assertEquals} emitted an AssertJ {@code assertNull}
	 * that does not exist. Measured: the file stopped compiling.
	 *
	 * <p>Matching the rewritten call's own name exactly would be more faithful still, but it is
	 * worse in practice: a file importing {@code assertEquals} from JUnit 5 and
	 * {@code assertNotEquals} from JUnit 4 then gets {@code assertTrue} from both classes, and two
	 * single-static-imports of one simple name do not compile either. Each fix sees only its own
	 * import set, so no single rewrite can detect that collision.
	 */
	private static void addAssertImport(
			@Nonnull List<String> lines,
			@Nonnull String replacementMethod,
			@Nonnull Set<String> imports
	) {
		// the scan reads every line, not just the import region, and stripComment carries no
		// cross-line state, so an import-shaped line inside a text block or a multi-line comment
		// would be read as a directive. Measured: a class named only inside a text block became
		// the donor once the wildcard stopped aborting the scan
		final var masks = FqnResolver.computeLineMasks(lines);
		for (var i = 0; i < lines.size(); ++i) {
			if (masks.inBlockComment()[i] || masks.inTextBlock()[i])
				continue;
			// ImportLine.parse is contracted to receive a comment-stripped line, and a donor
			// carrying a trailing `// TODO` would otherwise fail to match and go unseen
			final var parsed = ImportLine.parse(LambdaCallParser.stripComment(lines.get(i)));
			if (parsed == null || !parsed.staticImport())
				continue;
			// a wildcard covering the donor class makes the add a no-op in insertMissingImports,
			// so an unrelated one must not stop the scan the way an aborting return did
			if (parsed.wildcard())
				continue;
			final var fqn = parsed.fqn();
			final var lastDot = fqn.lastIndexOf('.');
			if (lastDot < 0)
				continue;
			if (REWRITTEN_ASSERTIONS.contains(fqn.substring(lastDot + 1))) {
				imports.add("static " + fqn.substring(0, lastDot) + "." + replacementMethod);
				return;
			}
		}
	}

	/** The single argument of {@code call} as a call itself, or null when it is not one. */
	@CheckReturnValue
	@Nullable
	private static DetailAST innerCallOf(@Nonnull DetailAST call) {
		final var arguments = argumentsIn(call);
		if (arguments.size() != 1)
			return null;
		final var argument = arguments.getFirst();
		final var inner = argument.getType() == TokenTypes.EXPR ? argument.getFirstChild() : argument;
		return inner != null && inner.getType() == TokenTypes.METHOD_CALL ? inner : null;
	}

	/**
	 * Drops the literal argument and renames the call. Both edits are spliced into the one line
	 * in right-to-left order so the first does not invalidate the second's offsets, and the text
	 * between the surviving arguments is never regenerated, so a comment among them survives.
	 */
	@CheckReturnValue
	@Nullable
	private static Rewrite rewriteAssertion(
			@Nonnull List<String> lines,
			@Nonnull ApiTarget target,
			@Nonnull Set<String> imports
	) {
		final var call = target.node();
		final var replacement = target.replacement();
		final var literal = target.argument();
		if (replacement == null || literal == null)
			return null;

		final var arguments = argumentsIn(call);
		final var index = arguments.indexOf(literal);
		if (index < 0)
			return null;

		// take the separator on whichever side exists, so exactly one comma goes with the argument
		final var dropFrom = index == 0
				? AstSpan.spanStart(lines, literal)
				: AstSpan.spanEnd(lines, arguments.get(index - 1));
		final var dropTo = index == 0
				? AstSpan.spanStart(lines, arguments.get(index + 1))
				: AstSpan.spanEnd(lines, literal);

		final var head = AstSpan.spanStart(lines, call);
		final var afterLparen = afterLparenOf(lines, call);
		if (dropFrom == null || dropTo == null || head == null || afterLparen == null)
			return null;
		if (dropFrom.line() != head.line() || dropTo.line() != head.line() || afterLparen.line() != head.line())
			return null;

		final String prefix;
		if (receiverOf(call) == null) {
			prefix = "";
			addAssertImport(lines, replacement, imports);
		}
		else {
			final var text = reemittableReceiverOf(lines, call);
			if (text == null)
				return null;
			prefix = text + ".";
		}

		// this arm is the one rewrite that never reaches spliceSpan, so the comment guard has to be
		// applied by hand to both spans it rewrites: the head it regenerates and the argument it
		// drops. Measured: `assertEquals/*c*/(null, x)` and `assertEquals(null, /*c*/x)` each lost
		// the comment. A queued import is discarded with the null, so refusing here is clean
		if (discardsAComment(lines, head, afterLparen, prefix + replacement + "(")
				|| discardsAComment(lines, dropFrom, dropTo, ""))
			return null;

		final var line = lines.get(head.line());
		final var withoutLiteral = line.substring(0, dropFrom.index()) + line.substring(dropTo.index());
		return new Rewrite(
				head.line(),
				withoutLiteral.substring(0, head.index()) + prefix + replacement + "("
						+ withoutLiteral.substring(afterLparen.index())
		);
	}

	@CheckReturnValue
	@Nullable
	private static Rewrite rewriteCollectionsSort(@Nonnull List<String> lines, @Nonnull DetailAST call) {
		final var arguments = argumentsIn(call);
		if (arguments.isEmpty() || arguments.size() > 2)
			return null;

		final var afterLparen = afterLparenOf(lines, call);
		final var rparen = rparenOf(lines, call);
		if (arguments.size() == 1) {
			final var list = textBetween(lines, afterLparen, rparen);
			return list == null ? null : spliceNode(lines, call, asReceiver(arguments.getFirst(), list.strip()) + ".sort(null)");
		}

		final var comma = firstCommaOf(lines, call);
		if (comma == null)
			return null;
		final var list = textBetween(lines, afterLparen, comma);
		final var comparator = textBetween(lines, new TextPos(comma.line(), comma.index() + 1), rparen);
		if (list == null || comparator == null)
			return null;
		return spliceNode(lines, call, asReceiver(arguments.getFirst(), list.strip()) + ".sort(" + comparator.strip() + ")");
	}

	/** A size/length or trim-length comparison against zero, collapsed to the boolean-returning call. */
	@CheckReturnValue
	@Nullable
	private static Rewrite rewriteComparison(
			@Nonnull List<String> lines,
			@Nonnull ApiTarget target,
			@Nonnull String suffix
	) {
		final var subject = target.argument();
		final var replacement = target.replacement();
		if (subject == null || replacement == null)
			return null;
		final var receiverText = reemittableReceiverOf(lines, subject);
		if (receiverText == null)
			return null;
		// both detectors spell the negated form with a leading '!', whatever follows it
		return replacement.startsWith("!")
				? spliceNegated(lines, target.node(), receiverText + suffix)
				: spliceNode(lines, target.node(), receiverText + suffix);
	}

	@CheckReturnValue
	@Nullable
	private static Rewrite rewriteIndexOfChar(@Nonnull List<String> lines, @Nonnull ApiTarget target) {
		final var literal = target.argument();
		if (literal == null || literal.getType() != TokenTypes.STRING_LITERAL)
			return null;
		final var text = literal.getText();
		if (text.length() < 2)
			return null;
		final var content = stringContentToCharLiteralContent(text.substring(1, text.length() - 1));
		return content == null ? null : spliceNode(lines, literal, "'" + content + "'");
	}

	/**
	 * {@code r.get(r.size() - 1)} to {@code r.getLast()}, which evaluates {@code r} once where the
	 * source evaluated it twice. Safe only for a receiver with no side effects, so a call like
	 * {@code getList().remove(getList().size() - 1)} is refused rather than quietly changing how
	 * many times {@code getList()} runs.
	 */
	@CheckReturnValue
	@Nullable
	private static Rewrite rewriteLastAccess(
			@Nonnull List<String> lines,
			@Nonnull DetailAST call,
			@Nonnull String suffix
	) {
		final var receiver = receiverOf(call);
		if (receiver == null || !AstQuery.isSideEffectFree(receiver))
			return null;
		return spliceTail(lines, call, call, suffix);
	}

	@CheckReturnValue
	@Nullable
	private static Rewrite rewriteStreamFindFirst(@Nonnull List<String> lines, @Nonnull DetailAST call) {
		final var findFirst = receiverOf(call);
		final var stream = receiverOf(findFirst);
		if (stream == null)
			return null;
		final var receiverText = reemittableReceiverOf(lines, stream);
		return receiverText == null ? null : spliceNegated(lines, call, receiverText + ".isEmpty()");
	}

	@CheckReturnValue
	@Nullable
	private static Rewrite rewriteStringFormat(@Nonnull List<String> lines, @Nonnull DetailAST call) {
		final var arguments = argumentsIn(call);
		if (arguments.size() < 2)
			return null;
		final var first = arguments.getFirst();
		final var inner = first.getType() == TokenTypes.EXPR ? first.getFirstChild() : first;
		if (inner == null || inner.getType() != TokenTypes.STRING_LITERAL)
			return null;

		final var comma = firstCommaOf(lines, call);
		if (comma == null)
			return null;
		final var format = AstSpan.sliceNode(lines, first);
		final var rest = textBetween(lines, new TextPos(comma.line(), comma.index() + 1), rparenOf(lines, call));
		if (format == null || rest == null)
			return null;
		return spliceNode(lines, call, format + ".formatted(" + rest.strip() + ")");
	}

	@CheckReturnValue
	@Nullable
	private static Rewrite rewriteToArray(@Nonnull List<String> lines, @Nonnull ApiTarget target) {
		final var typeName = target.replacement();
		final var arguments = argumentsIn(target.node());
		if (typeName == null || arguments.size() != 1)
			return null;
		final var argument = arguments.getFirst();
		final var inner = argument.getType() == TokenTypes.EXPR ? argument.getFirstChild() : argument;
		return inner == null ? null : spliceNode(lines, inner, typeName + "[]::new");
	}

	/** The rewritten line, or null when this target cannot be rewritten here. */
	@CheckReturnValue
	@Nullable
	private static Rewrite rewrittenLine(
			@Nonnull List<String> lines,
			@Nonnull ApiTarget target,
			@Nonnull Set<String> imports
	) {
		final var node = target.node();
		return switch (target.rule()) {
			case ARRAYS_AS_LIST -> {
				imports.add("java.util.List");
				yield spliceSpan(lines, AstSpan.spanStart(lines, node), afterLparenOf(lines, node), "List.of(");
			}
			case ASSERT -> rewriteAssertion(lines, target, imports);
			case COLLECT_TO_LIST -> spliceTail(lines, node, node, ".toList()");
			case COLLECTIONS_COPY_OF, COLLECTIONS_FACTORY -> {
				final var prefix = target.replacement();
				if (prefix == null)
					yield null;
				imports.add("java.util." + prefix.substring(0, prefix.indexOf('.')));
				yield spliceSpan(lines, AstSpan.spanStart(lines, node), afterLparenOf(lines, node), prefix + "(");
			}
			case COLLECTIONS_SORT -> rewriteCollectionsSort(lines, node);
			case EQUALS_EMPTY -> spliceTail(lines, node, node, ".isEmpty()");
			case GET_FIRST -> spliceTail(lines, node, node, ".getFirst()");
			case GET_LAST -> rewriteLastAccess(lines, node, ".getLast()");
			case INDEX_OF_CHAR -> rewriteIndexOfChar(lines, target);
			// the check reports this one but no rewrite is defined: contains() takes the needle,
			// not the comparison, so the edit is a restructure rather than a substitution
			case INDEX_OF_CONTAINS -> null;
			case MAP_CHAIN -> {
				final var method = target.replacement();
				yield method == null
						? null
						: spliceTailToLparen(lines, receiverOf(node), node, "." + method + "(");
			}
			case REMOVE_FIRST -> spliceTail(lines, node, node, ".removeFirst()");
			case REMOVE_LAST -> rewriteLastAccess(lines, node, ".removeLast()");
			case REPLACE_ALL -> spliceTailToLparen(lines, node, node, ".replace(");
			case SIZE_IS_EMPTY -> rewriteComparison(lines, target, ".isEmpty()");
			case STREAM_COUNT -> spliceTail(lines, receiverOf(node), node, ".size()");
			case STREAM_FIND_FIRST -> rewriteStreamFindFirst(lines, node);
			case STREAM_FOR_EACH -> spliceTailToLparen(lines, receiverOf(node), node, ".forEach(");
			case STRING_FORMAT_FORMATTED -> rewriteStringFormat(lines, node);
			case STRING_FORMAT_STRIP -> {
				final var only = argumentsOf(lines, node);
				final var arguments = argumentsIn(node);
				yield only == null || arguments.size() != 1
						? null
						: spliceNode(lines, node, parenthesizeIfNeeded(node, arguments.getFirst(), only.strip()));
			}
			case TO_ARRAY_GENERATOR -> rewriteToArray(lines, target);
			case TRIM_IS_BLANK -> spliceTail(lines, target.argument(), node, ".isBlank()");
			case TRIM_LENGTH_IS_BLANK -> rewriteComparison(lines, target, ".isBlank()");
			case UNMODIFIABLE_AS_LIST -> {
				final var inner = innerCallOf(node);
				if (inner == null)
					yield null;
				final var arguments = argumentsOf(lines, inner);
				if (arguments == null)
					yield null;
				imports.add("java.util.List");
				yield spliceNode(lines, node, "List.of(" + arguments + ")");
			}
		};
	}

	@CheckReturnValue
	@Nullable
	private static String stringContentToCharLiteralContent(@Nonnull String content) {
		if (content.isEmpty())
			return null;
		if ("'".equals(content))
			return "\\'";
		if ("\\\"".equals(content))
			return "\"";
		if (content.length() == 1 && content.charAt(0) != '\\' && content.charAt(0) != '\'')
			return content;
		if (content.length() >= 2 && content.charAt(0) == '\\') {
			if (content.length() == 2) {
				final var n = content.charAt(1);
				if (n == '"' || n == '\'' || n == '0' || n == '\\' || n == 'b' || n == 'f'
						|| n == 'n' || n == 'r' || n == 's' || n == 't')
					return content;
			}
			if (content.length() == 6 && content.charAt(1) == 'u') {
				for (var i = 2; i < 6; ++i) {
					final var c = content.charAt(i);
					if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')))
						return null;
				}
				return content;
			}
			if (content.length() <= 4) {
				var allOctal = true;
				for (var i = 1; i < content.length(); ++i) {
					if (content.charAt(i) < '0' || content.charAt(i) > '7') {
						allOctal = false;
						break;
					}
				}
				// per JLS: 3-digit octal escape requires first digit 0..3 (max value \377)
				if (allOctal && content.length() == 4 && content.charAt(1) > '3')
					return null;
				if (allOctal && content.length() >= 2)
					return content;
			}
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var target = FixerAst.withAst(lines, root -> PreferSpecificApiCheck.locateAt(root, lineIndex, column));
		if (target == null)
			return new SkipResult(SkipMessages.PREFER_API_SKIP);

		final var imports = new TreeSet<String>();
		final var rewritten = rewrittenLine(lines, target, imports);
		if (rewritten == null)
			return new SkipResult(SkipMessages.PREFER_API_SKIP);

		if (imports.isEmpty())
			return new FixResult(rewritten.line(), rewritten.line(), List.of(rewritten.text()));
		return new FixResult(rewritten.line(), rewritten.line(), List.of(rewritten.text()), imports);
	}
}