package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AstUtil;
import com.etk2000.checkstyle.ConstructorAssignmentOrderCheck;
import com.etk2000.checkstyle.ConstructorAssignmentOrderCheck.Assignment;
import com.etk2000.checkstyle.ConstructorAssignmentOrderCheck.BodyClassification;
import com.etk2000.checkstyle.ConstructorAssignmentOrderCheck.LocalVar;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixer for {@code ConstructorAssignmentOrderCheck}. Reuses the check's
 * {@link ConstructorAssignmentOrderCheck#classify AST classifier} to read the constructor/
 * initializer body's {@code this.xxx = ...} assignments and local-variable declarations, then
 * reorders them by group (simple, multi-line, variable-dependent), sub-group (variable
 * declaration order), and field name, honoring field-to-field dependencies. Each local variable
 * is emitted just before the first assignment that references it; an unreferenced one is moved to
 * the tail. Returns a {@link SkipResult} when a comment or a non-assignment statement sits within
 * the reordered region (either would be dropped or have its execution order changed).
 */
class ConstructorAssignmentOrderFixer implements CheckstyleFixer {
	private record ClassifiedBody(@Nonnull DetailAST body, @Nonnull BodyClassification classification) {}

	private static void addCoverage(@Nonnull int[] cover, int regionStart, int start, int end) {
		for (var i = start; i <= end; ++i) {
			final var idx = i - regionStart;
			if (idx >= 0 && idx < cover.length)
				++cover[idx];
		}
	}

	/**
	 * Computes each region var's emission slot: the index in {@code ordered} of the earliest
	 * assignment that needs it, either directly or through another var that needs it, or
	 * {@link Integer#MAX_VALUE} when no assignment needs it (emitted at the tail). A var dependency
	 * always points at an earlier declaration, so propagating a dependent's slot down to the var it
	 * reads (processing later declarations first) reaches a fixed point in one backward pass.
	 */
	@CheckReturnValue
	@Nonnull
	private static int[] emissionSlots(@Nonnull List<Assignment> ordered, @Nonnull List<LocalVar> regionVars) {
		final var slot = new int[regionVars.size()];
		for (var i = 0; i < regionVars.size(); ++i) {
			slot[i] = Integer.MAX_VALUE;
			for (var a = 0; a < ordered.size(); ++a) {
				if (ordered.get(a).usedVars().contains(regionVars.get(i).name())) {
					slot[i] = a;
					break;
				}
			}
		}
		for (var i = regionVars.size() - 1; i >= 0; --i) {
			for (var j = i + 1; j < regionVars.size(); ++j) {
				if (regionVars.get(j).usedVars().contains(regionVars.get(i).name()))
					slot[i] = Math.min(slot[i], slot[j]);
			}
		}
		return slot;
	}

	/**
	 * Extends {@code regionStart} upward over the contiguous run of local-variable declarations
	 * immediately above the first assignment (blank lines between them are skipped). A leading var
	 * group must travel with the assignment that uses it, so it belongs to the reordered region.
	 */
	@CheckReturnValue
	private static int extendOverLeadingVars(
			@Nonnull List<String> lines,
			@Nonnull List<LocalVar> localVars,
			int regionStart,
			int bodyStartLine
	) {
		var start = regionStart;
		for (;;) {
			var i = start - 1;
			while (i > bodyStartLine && lines.get(i).isBlank())
				--i;
			if (i <= bodyStartLine)
				break;
			final var lv = varEndingAt(localVars, i);
			if (lv == null || lv.startLine() <= bodyStartLine)
				break;
			start = lv.startLine();
		}
		return start;
	}

	/**
	 * Orders the assignments by group, sub-group, and field name, then refines that order with a
	 * stable topological sort so a field is assigned before any assignment that reads it (Kahn's,
	 * always taking the assignment earliest in the initial order among those with no unplaced
	 * dependency). Returns {@code null} when a field-dependency cycle leaves some assignment
	 * permanently blocked, since no order satisfies every dependency.
	 */
	@CheckReturnValue
	@Nullable
	private static List<Assignment> orderByDependency(@Nonnull List<Assignment> assignments) {
		final var sorted = new ArrayList<>(assignments);
		sorted.sort(Comparator
				.comparingInt(Assignment::group)
				.thenComparingInt(Assignment::subGroup)
				.thenComparing(Assignment::fieldName, String.CASE_INSENSITIVE_ORDER));

		final var n = sorted.size();
		final var indegree = new int[n];
		final var dependents = new ArrayList<List<Integer>>(n);
		for (var i = 0; i < n; ++i)
			dependents.add(new ArrayList<>());
		for (var a = 0; a < n; ++a) {
			for (var b = 0; b < n; ++b) {
				if (a != b && sorted.get(a).fieldRefs().contains(sorted.get(b).fieldName())) {
					++indegree[a];
					dependents.get(b).add(a);
				}
			}
		}
		final var ordered = new ArrayList<Assignment>(n);
		final var emitted = new boolean[n];
		for (var step = 0; step < n; ++step) {
			var pick = -1;
			for (var i = 0; i < n; ++i) {
				if (!emitted[i] && indegree[i] == 0) {
					pick = i;
					break;
				}
			}
			if (pick < 0)
				return null;
			ordered.add(sorted.get(pick));
			emitted[pick] = true;
			for (var d : dependents.get(pick))
				--indegree[d];
		}
		return ordered;
	}

	@Nonnull
	private static List<String> rebuild(
			@Nonnull List<String> lines,
			@Nonnull List<Assignment> ordered,
			@Nonnull List<LocalVar> regionVars
	) {
		// declaration order is a valid topological order (a var reads only earlier-declared vars),
		// so emitting each var at its slot, scanned in declaration order, always places a var after
		// the vars it depends on
		final var slot = emissionSlots(ordered, regionVars);
		final var replacement = new ArrayList<String>();
		final var placed = new boolean[regionVars.size()];
		var prevGroup = -1;
		var prevSubGroup = -1;
		for (var a = 0; a < ordered.size(); ++a) {
			final var entry = ordered.get(a);
			if (!replacement.isEmpty()
					&& (entry.group() != prevGroup
					|| (entry.group() == ConstructorAssignmentOrderCheck.GROUP_VAR && entry.subGroup() != prevSubGroup)))
				replacement.add("");

			for (var i = 0; i < regionVars.size(); ++i) {
				// slot[i] equals exactly one ordered index, so this fires at most once per var
				if (slot[i] == a) {
					placed[i] = true;
					final var lv = regionVars.get(i);
					replacement.addAll(lines.subList(lv.startLine(), lv.endLine() + 1));
				}
			}

			replacement.addAll(lines.subList(entry.startLine(), entry.endLine() + 1));
			prevGroup = entry.group();
			prevSubGroup = entry.subGroup();
		}

		for (var i = 0; i < regionVars.size(); ++i) {
			if (!placed[i]) {
				final var lv = regionVars.get(i);
				replacement.addAll(lines.subList(lv.startLine(), lv.endLine() + 1));
			}
		}
		return replacement;
	}

	@CheckReturnValue
	@Nullable
	private static LocalVar varEndingAt(@Nonnull List<LocalVar> localVars, int line) {
		for (var lv : localVars) {
			if (lv.endLine() == line)
				return lv;
		}
		return null;
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var classified = FixerAst.withAst(
				lines,
				root -> {
				final var found = ConstructorAssignmentOrderCheck.bodyAt(root, lineIndex, column);
				return found == null ? null : new ClassifiedBody(found, ConstructorAssignmentOrderCheck.classify(found));
				}
		);
		if (classified == null)
			return null;

		final var body = classified.body();
		final var classification = classified.classification();
		final var assignments = classification.assignments();
		if (assignments.size() < 2)
			return null;

		// a field assigned more than once cannot be sorted by field name without changing which
		// value a later read of that field sees, so refuse
		final var seenFields = new HashSet<String>();
		for (var a : assignments) {
			if (!seenFields.add(a.fieldName()))
				return new SkipResult(SkipMessages.CONSTRUCTOR_ASSIGN_SKIP_DUPLICATE_FIELD);
		}

		final var bodyStartLine = body.getLineNo() - 1;
		var regionStart = assignments.getFirst().startLine();
		var regionEnd = assignments.getFirst().endLine();
		for (var a : assignments) {
			regionStart = Math.min(regionStart, a.startLine());
			regionEnd = Math.max(regionEnd, a.endLine());
		}
		regionStart = extendOverLeadingVars(lines, classification.localVars(), regionStart, bodyStartLine);
		if (regionStart < 0 || regionEnd >= lines.size())
			return null;

		final var regionVars = new ArrayList<LocalVar>();
		for (var lv : classification.localVars()) {
			if (lv.startLine() >= regionStart && lv.endLine() <= regionEnd)
				regionVars.add(lv);
		}

		// every region line must belong to exactly one assignment or region-local var decl, or be
		// blank. A line shared by two of them (or by one of them and another statement) can't be
		// reordered by whole-line replacement without duplicating or dropping content; a stray
		// comment or statement on its own line can't be reordered without losing it or changing
		// execution order. Refuse in every such case.
		final var cover = new int[regionEnd - regionStart + 1];
		for (var a : assignments)
			addCoverage(cover, regionStart, a.startLine(), a.endLine());
		for (var lv : regionVars)
			addCoverage(cover, regionStart, lv.startLine(), lv.endLine());
		for (var i = regionStart; i <= regionEnd; ++i) {
			final var covered = cover[i - regionStart];
			final var isStatement = classification.statementLines().contains(i);
			if (covered == 0) {
				if (lines.get(i).isBlank())
					continue;
				return new SkipResult(isStatement
						? SkipMessages.CONSTRUCTOR_ASSIGN_SKIP_STATEMENT
						: SkipMessages.CONSTRUCTOR_ASSIGN_SKIP_COMMENT
				);
			}
			if (covered >= 2 || isStatement)
				return new SkipResult(SkipMessages.CONSTRUCTOR_ASSIGN_SKIP_SHARED_LINE);
		}

		final var ordered = orderByDependency(assignments);
		if (ordered == null)
			return new SkipResult(SkipMessages.CONSTRUCTOR_ASSIGN_SKIP_CYCLE);

		// A var no assignment reads is emitted after every assignment, which moves its
		// initializer's side effects (a lock acquisition, a timestamp, a log call) past
		// the field writes. The output still compiles, so only runtime ordering changes.
		final var slots = emissionSlots(ordered, regionVars);
		for (var i = 0; i < regionVars.size(); ++i) {
			if (slots[i] != Integer.MAX_VALUE)
				continue;
			// the declaration itself always holds an ASSIGN, so only the initializer
			// expression under it says whether evaluating the var can be moved
			final var assign = regionVars.get(i).ast().findFirstToken(TokenTypes.ASSIGN);
			final var initializer = assign == null ? null : assign.getFirstChild();
			if (initializer != null && !AstUtil.isSideEffectFree(initializer))
				return new SkipResult(SkipMessages.CONSTRUCTOR_ASSIGN_SKIP_VAR_SIDE_EFFECT);
		}

		final var replacement = rebuild(lines, ordered, regionVars);
		final var original = new ArrayList<>(lines.subList(regionStart, regionEnd + 1));
		if (replacement.equals(original))
			return null;
		return new FixResult(regionStart, regionEnd, replacement);
	}
}