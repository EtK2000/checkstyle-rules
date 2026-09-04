package com.etk2000.checkstyle;

import com.etk2000.checkstyle.AstSpan.TextPos;
import com.etk2000.checkstyle.FieldInitializers.Kind;
import com.etk2000.checkstyle.SourceComments.Comment;
import com.etk2000.checkstyle.ast.AstText;
import com.etk2000.checkstyle.ast.DocComments;
import com.etk2000.checkstyle.format.SpanReformat;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.IntPredicate;
import java.util.function.Supplier;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags field declarations of the same modifiers, annotations and type
 * in one stretch of a class's fields, which belong in a single declaration
 * ({@code int height = 1;} over {@code int width;} becomes {@code int height = 1, width;}). See
 * {@link #runs} for what keeps two such declarations apart, a declarator with a long initializer
 * or one that cannot be written on one line among it.
 */
public class FieldConsolidationCheck extends AbstractAstCheck {
	/**
	 * Everything a merge rewrites on one declarator besides its name: the merged declaration
	 * keeps one declaration's modifiers, annotations and type for every name, so two declarators
	 * can share it only when these are equal. {@code annotations} are
	 * {@link AstText#annotationIdentities}, {@code type} is the full type with C-style brackets
	 * folded in.
	 */
	public record DeclarationKey(@Nonnull Set<Integer> modifiers, @Nonnull List<String> annotations, @Nonnull String type) {}

	/**
	 * What {@link #crossesPin} reads off one stretch of fields: per position, whether its
	 * declaration is static, holds a declarator that is not inert ({@code live}) and one running
	 * code ({@code runners}); and per static or instance group (index 1 or 0), how many
	 * declarations ahead of each position hold such a declarator.
	 */
	private record PinScope(
			@Nonnull boolean[] statics,
			@Nonnull boolean[] live,
			@Nonnull boolean[] runners,
			@Nonnull int[][] liveBefore,
			@Nonnull int[][] runnersBefore
	) {}

	/** The most code points a declarator's name and initializer may take together to share a declaration. */
	private static final int MAX_SHORT_DECLARATOR_LENGTH = 30;
	// a declaration whose type nests deeper than this (a machine-generated
	// `List<List<...<String>...>>`) joins no run, so it is neither reported nor merged
	private static final int MAX_TYPE_DEPTH = 64;
	private static final String MSG_KEY = "field.consolidate.same.type";

	/**
	 * True when a name of {@code group} reads, or is read by ({@code linked}), a name whose
	 * declaration sits at a {@code position} that {@code crossed} accepts: one the move up into
	 * its run would carry {@code group} past.
	 */
	@CheckReturnValue
	private static boolean crossesDependency(
			@Nonnull List<DetailAST> group,
			@Nonnull Map<String, Set<String>> linked,
			@Nonnull Map<String, Integer> position,
			@Nonnull IntPredicate crossed
	) {
		for (var declarator : group) {
			for (var other : linked.getOrDefault(fieldName(declarator), Set.of())) {
				final var at = position.get(other);
				if (at != null && crossed.test(at))
					return true;
			}
		}
		return false;
	}

	/**
	 * True when a name of the declaration at {@code end} of the stretch {@code scope} reads off is
	 * {@linkplain FieldInitializers#pinned pinned} to a field of its own static or instance group
	 * declared from {@code from} up to it, other than the {@code ownLive} declarations of its run
	 * there that are not inert, {@code ownRunners} of them running code: one the move up into its
	 * run would carry it past. A declaration running code is only pinned to one that is not inert,
	 * any other only to one running code, so counting those is enough.
	 */
	@CheckReturnValue
	private static boolean crossesPin(@Nonnull PinScope scope, int from, int end, @Nonnull int[] ownLive, @Nonnull int[] ownRunners) {
		if (!scope.live()[end])
			return false;
		final var group = scope.statics()[end] ? 1 : 0;
		if (scope.runners()[end])
			return scope.liveBefore()[group][end] - scope.liveBefore()[group][from] > ownLive[group];
		return scope.runnersBefore()[group][end] - scope.runnersBefore()[group][from] > ownRunners[group];
	}

	/**
	 * The declarators of the declaration {@code first} opens: itself plus every
	 * {@code VARIABLE_DEF} reachable through a chain of {@code COMMA} siblings,
	 * since {@code int a, b;} is two sibling {@code VARIABLE_DEF}s under the
	 * {@code OBJBLOCK} rather than one node with two names.
	 */
	@CheckReturnValue
	@Nonnull
	public static List<DetailAST> declarationGroup(@Nonnull DetailAST first) {
		final var group = new ArrayList<DetailAST>();
		group.add(first);
		for (var sibling = first.getNextSibling(); sibling != null && sibling.getType() == TokenTypes.COMMA; ) {
			final var declarator = sibling.getNextSibling();
			// defensive: on parseable input a COMMA in an OBJBLOCK is always followed by
			// the next declarator
			if (declarator == null || declarator.getType() != TokenTypes.VARIABLE_DEF)
				break;
			group.add(declarator);
			sibling = declarator.getNextSibling();
		}
		return group;
	}

	/**
	 * The {@link DeclarationKey} of {@code varDef}, or null when its annotations or its type
	 * cannot be told apart reliably.
	 */
	@CheckReturnValue
	@Nullable
	public static DeclarationKey declarationKey(@Nonnull DetailAST varDef) {
		final var modifiers = varDef.findFirstToken(TokenTypes.MODIFIERS);
		final var annotations = modifiers == null ? List.<String>of() : AstText.annotationIdentities(modifiers, AstText.MAX_ANNOTATION_DEPTH);
		final var type = typeName(varDef);
		return annotations == null || type == null ? null : new DeclarationKey(modifierKeywords(varDef), annotations, type);
	}

	/** The {@code ,} or {@code ;} ending {@code declarator}, or null when it has none. */
	@CheckReturnValue
	@Nullable
	public static DetailAST declaratorBound(@Nonnull DetailAST declarator) {
		final var next = declarator.getNextSibling();
		if (next != null && next.getType() == TokenTypes.COMMA)
			return next;
		var first = declarator;
		while (first.getPreviousSibling() != null && first.getPreviousSibling().getType() == TokenTypes.COMMA)
			first = first.getPreviousSibling().getPreviousSibling();
		return semi(declarationGroup(first));
	}

	@CheckReturnValue
	private static boolean exceedsTypeDepth(@Nonnull DetailAST varDef) {
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return false;
		final var nodes = new ArrayDeque<DetailAST>();
		final var depths = new ArrayDeque<Integer>();
		nodes.push(type);
		depths.push(0);
		while (!nodes.isEmpty()) {
			final var node = nodes.pop();
			final var depth = depths.pop();
			if (depth > MAX_TYPE_DEPTH)
				return true;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
				nodes.push(child);
				depths.push(depth + 1);
			}
		}
		return false;
	}

	@CheckReturnValue
	@Nonnull
	private static String fieldName(@Nonnull DetailAST varDef) {
		final var ident = varDef.findFirstToken(TokenTypes.IDENT);
		return ident != null ? ident.getText() : "";
	}

	/**
	 * True when {@code declarator} reads well beside other names in one declaration: it has no
	 * initializer, or its name and its {@linkplain #oneLineInitializer one-line initializer} come
	 * to at most {@link #MAX_SHORT_DECLARATOR_LENGTH} code points.
	 */
	@CheckReturnValue
	private static boolean isShort(@Nonnull LineColumns lines, @Nonnull DetailAST declarator) {
		if (declarator.findFirstToken(TokenTypes.ASSIGN) == null)
			return true;
		final var value = oneLineInitializer(lines, declarator);
		final var name = fieldName(declarator);
		return value != null && name.codePointCount(0, name.length()) + value.codePointCount(0, value.length()) <= MAX_SHORT_DECLARATOR_LENGTH;
	}

	/**
	 * How the lines of a split initializer join: tight where {@link SpanReformat#joinsTight} is,
	 * and inside braces too, so {@code {} / {@code 1, 2} / {@code }} reads {@code {1, 2}}.
	 */
	@CheckReturnValue
	private static boolean joinsTight(@Nonnull StringBuilder joined, @Nonnull String continuation) {
		return SpanReformat.joinsTight(joined, continuation) || joined.charAt(joined.length() - 1) == '{' || continuation.charAt(0) == '}';
	}

	@CheckReturnValue
	@Nonnull
	private static Set<Integer> modifierKeywords(@Nonnull DetailAST varDef) {
		final var keywords = new TreeSet<Integer>();
		final var modifiers = varDef.findFirstToken(TokenTypes.MODIFIERS);
		if (modifiers != null) {
			for (var child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() != TokenTypes.ANNOTATION && !AstText.isCommentToken(child.getType()))
					keywords.add(child.getType());
			}
		}
		return keywords;
	}

	/**
	 * Whether a space separates {@code next} from the type text ending in {@code last}. It
	 * keeps the message readable ({@code ? extends Number}, {@code String @A []}) and keeps two
	 * words from fusing; everywhere else the tokens are joined tight.
	 */
	@CheckReturnValue
	private static boolean needsSpace(char last, char next) {
		if (next == '@')
			return Character.isJavaIdentifierPart(last) || last == ']' || last == '>';
		return Character.isJavaIdentifierPart(next) && (Character.isJavaIdentifierPart(last) || last == '?');
	}

	/**
	 * {@code declarator}'s initializer on one line, as a merge writes it: from just past its
	 * {@code =} to the {@code ,} or {@code ;} after it, trimmed, comments included. A declarator
	 * spanning lines has them joined ({@code beta =} / {@code 12} reads {@code 12}). Null when it
	 * has no initializer, or when it spans lines and cannot be written on one: a comment or a text
	 * block from its name on, or a {@linkplain #splitsBlock split block}.
	 */
	@CheckReturnValue
	@Nullable
	public static String oneLineInitializer(@Nonnull LineColumns lines, @Nonnull DetailAST declarator) {
		final var assign = declarator.findFirstToken(TokenTypes.ASSIGN);
		final var bound = declaratorBound(declarator);
		if (assign == null || bound == null)
			return null;
		final var assignIndex = lines.charIndex(assign);
		final var ident = declarator.findFirstToken(TokenTypes.IDENT);
		final var identIndex = lines.charIndex(ident);
		if (assignIndex < 0 || identIndex < 0)
			return null;
		final var from = new TextPos(assign.getLineNo() - 1, assignIndex + 1);
		final var to = new TextPos(bound.getLineNo() - 1, lines.charIndex(bound));
		if (ident.getLineNo() == bound.getLineNo()) {
			final var value = AstSpan.sliceSpan(lines.lines(), from, to);
			return value == null ? null : value.strip();
		}
		final var nameEnd = new TextPos(ident.getLineNo() - 1, identIndex + ident.getText().length());
		if (AstSpan.sliceSpan(lines.lines(), nameEnd, to) == null || splitsBlock(assign))
			return null;
		final var value = AstSpan.sliceSpan(lines.lines(), from, to, FieldConsolidationCheck::joinsTight);
		return value == null ? null : value.strip();
	}

	/**
	 * What a declaration has to share with another to fold into one with it: its
	 * {@link DeclarationKey}, and for a {@code final} one whether its declarators carry a value,
	 * since FieldSorting keeps finals with an inline value apart from finals without one, and
	 * whether they are {@code anonymousClassBound}, since FieldSorting places those by the
	 * anonymous-class fields they hold or read. Null when the declaration can join no run: it is
	 * a constant (one with a value written {@code static final}, or any field of an interface or
	 * annotation type), which may stand on its own line or share one alike, its declarators
	 * disagree on any of these, one of them is not {@linkplain #isShort short}, or its type cannot
	 * be told apart reliably.
	 */
	@CheckReturnValue
	@Nullable
	private static List<Object> runIdentity(
			@Nonnull List<DetailAST> group,
			@Nonnull LineColumns lines,
			@Nonnull Set<String> anonymousClassBound
	) {
		final var key = declarationKey(group.getFirst());
		final var initialized = group.getFirst().findFirstToken(TokenTypes.ASSIGN) != null;
		if (key == null || initialized && (FieldSortingCheck.isInterfaceMember(group.getFirst())
				|| key.modifiers().contains(TokenTypes.LITERAL_STATIC) && key.modifiers().contains(TokenTypes.FINAL)))
			return null;
		final var bound = anonymousClassBound.contains(fieldName(group.getFirst()));
		for (var declarator : group) {
			if (exceedsTypeDepth(declarator) || !key.equals(declarationKey(declarator))
					|| anonymousClassBound.contains(fieldName(declarator)) != bound
					|| !isShort(lines, declarator))
				return null;
			if (key.modifiers().contains(TokenTypes.FINAL) && declarator.findFirstToken(TokenTypes.ASSIGN) != null != initialized)
				return null;
		}
		return List.of(key, key.modifiers().contains(TokenTypes.FINAL) && initialized, bound);
	}

	/**
	 * The runs of field declarations in {@code objBlock} that belong in one declaration, each a
	 * list of declarations (each the list of its declarators) in source order, the first being
	 * the one the others fold into. Two declarations of one run share a {@link #runIdentity}
	 * and sit in one stretch of fields: any other member, or a line holding only a comment,
	 * ends the stretch, while blank lines, fields of other types, constants and declarations
	 * holding a declarator that is not {@linkplain #isShort short} do not. A declaration
	 * documented by {@code docs} (only built when needed), or sharing a line with other code,
	 * joins no run, since the merge would give its doc comment to every name or carry off the
	 * code, and neither does one a tool directive among its {@code comments} leads
	 * ({@link SourceComments#leadingDirectives}), which would then cover the merged names too. A
	 * declaration that would be moved past a field it reads or that reads it starts a run of its own instead, and so does one that would be moved to or above an earlier field
	 * whose initializer reads it while it runs ({@link FieldSortingCheck#initTimeForwardReads}),
	 * which would then see its value rather than the default, or past a field it is
	 * {@linkplain FieldInitializers#pinned pinned} to; a declaration that reads a later one of its own
	 * names that way joins no run. In a type that suppresses FieldSorting, whose fields keep the order
	 * written, a declaration only joins the run of the declaration right before it, so no field moves
	 * past another.
	 */
	@CheckReturnValue
	@Nonnull
	public static List<List<List<DetailAST>>> runs(
			@Nonnull DetailAST objBlock,
			@Nonnull LineColumns lines,
			@Nonnull Supplier<DocComments> docs,
			@Nonnull Supplier<List<Comment>> comments
	) {
		return runs(objBlock, lines, docs, comments, FieldSortingCheck.isSuppressed(objBlock));
	}

	/**
	 * The {@link #runs(DetailAST, LineColumns, Supplier, Supplier) runs} of {@code objBlock} with
	 * {@code inOrder} saying whether FieldSorting is suppressed there.
	 */
	@CheckReturnValue
	@Nonnull
	public static List<List<List<DetailAST>>> runs(
			@Nonnull DetailAST objBlock,
			@Nonnull LineColumns lines,
			@Nonnull Supplier<DocComments> docs,
			@Nonnull Supplier<List<Comment>> comments,
			boolean inOrder
	) {
		final var instanceFields = new ArrayList<DetailAST>();
		final var staticFields = new ArrayList<DetailAST>();
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.VARIABLE_DEF)
				(FieldSortingCheck.classifyField(child).isStatic() ? staticFields : instanceFields).add(child);
		}
		final var anonymousClassBound = FieldSortingCheck.anonymousClassBound(instanceFields);
		anonymousClassBound.addAll(FieldSortingCheck.anonymousClassBound(staticFields));

		final var runs = new ArrayList<List<List<DetailAST>>>();
		var segment = new ArrayList<List<DetailAST>>();
		List<DetailAST> prev = null;
		var afterComma = false;
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.COMMA) {
				afterComma = true;
				continue;
			}
			if (child.getType() != TokenTypes.VARIABLE_DEF) {
				runs.addAll(runsOf(segment, lines, anonymousClassBound, docs, comments, inOrder));
				segment = new ArrayList<>();
				prev = null;
			}
			else if (!afterComma) {
				final var group = declarationGroup(child);
				if (prev != null && separatedByComment(prev, group, lines.lines())) {
					runs.addAll(runsOf(segment, lines, anonymousClassBound, docs, comments, inOrder));
					segment = new ArrayList<>();
				}
				segment.add(group);
				prev = group;
			}
			afterComma = false;
		}
		runs.addAll(runsOf(segment, lines, anonymousClassBound, docs, comments, inOrder));
		return runs;
	}

	/** The runs of {@link #runs} within one stretch of fields, {@code segment}. */
	@CheckReturnValue
	@Nonnull
	private static List<List<List<DetailAST>>> runsOf(
			@Nonnull List<List<DetailAST>> segment,
			@Nonnull LineColumns lines,
			@Nonnull Set<String> anonymousClassBound,
			@Nonnull Supplier<DocComments> docs,
			@Nonnull Supplier<List<Comment>> comments,
			boolean inOrder
	) {
		if (segment.size() < 2)
			return List.of();
		final var declarators = new ArrayList<DetailAST>();
		for (var group : segment)
			declarators.addAll(group);
		final var linked = new HashMap<String, Set<String>>();
		for (var entry : FieldSortingCheck.fieldDependencies(declarators).entrySet()) {
			for (var read : entry.getValue()) {
				linked.computeIfAbsent(entry.getKey(), name -> new HashSet<>()).add(read);
				linked.computeIfAbsent(read, name -> new HashSet<>()).add(entry.getKey());
			}
		}
		final var readAtInitBy = new HashMap<String, Set<String>>();
		for (var entry : FieldSortingCheck.initTimeForwardReads(declarators).entrySet()) {
			for (var read : entry.getValue())
				readAtInitBy.computeIfAbsent(read, name -> new HashSet<>()).add(entry.getKey());
		}
		final var position = new HashMap<String, Integer>();
		final var live = new boolean[segment.size()];
		final var runners = new boolean[segment.size()];
		final var statics = new boolean[segment.size()];
		final var liveBefore = new int[2][segment.size() + 1];
		final var runnersBefore = new int[2][segment.size() + 1];
		for (var i = 0; i < segment.size(); ++i) {
			statics[i] = FieldSortingCheck.classifyField(segment.get(i).getFirst()).isStatic();
			for (var declarator : segment.get(i)) {
				position.putIfAbsent(fieldName(declarator), i);
				final var kind = FieldInitializers.kindOf(declarator);
				live[i] |= kind != Kind.INERT;
				runners[i] |= kind == Kind.RUNS_CODE;
			}
			for (var group = 0; group < 2; ++group) {
				final var own = statics[i] == (group == 1);
				liveBefore[group][i + 1] = liveBefore[group][i] + (own && live[i] ? 1 : 0);
				runnersBefore[group][i + 1] = runnersBefore[group][i] + (own && runners[i] ? 1 : 0);
			}
		}
		final var pinScope = new PinScope(statics, live, runners, liveBefore, runnersBefore);
		final var runOf = new int[segment.size()];
		Arrays.fill(runOf, -1);
		// per run, its declarations past the first that are not inert and that run code, per group
		final var ownLive = new ArrayList<int[]>();
		final var ownRunners = new ArrayList<int[]>();
		final var starts = new ArrayList<Integer>();
		final var open = new HashMap<List<Object>, Integer>();
		final var runs = new ArrayList<List<List<DetailAST>>>();
		for (var i = 0; i < segment.size(); ++i) {
			final var group = segment.get(i);
			final var identity = runIdentity(group, lines, anonymousClassBound);
			final var end = i;
			// a declaration one of whose names reads a later one of its own would have them sorted
			if (identity == null
					|| docs.get().isDocumented(group.getFirst())
					|| sharesLine(group, docs.get())
					|| crossesDependency(group, readAtInitBy, position, at -> at == end)
					|| !SourceComments.leadingDirectives(lines, comments.get(), group.getFirst()).isEmpty())
				continue;
			final var run = open.get(identity);
			if (run != null
					&& (!inOrder || runOf[i - 1] == run)
					&& !crossesDependency(group, linked, position, at -> at > starts.get(run) && at < end && runOf[at] != run)
					&& !crossesPin(pinScope, starts.get(run) + 1, end, ownLive.get(run), ownRunners.get(run))
					&& !crossesDependency(group, readAtInitBy, position, at -> at >= starts.get(run))) {
				runs.get(run).add(group);
				runOf[i] = run;
				final var staticGroup = statics[i] ? 1 : 0;
				ownLive.get(run)[staticGroup] += live[i] ? 1 : 0;
				ownRunners.get(run)[staticGroup] += runners[i] ? 1 : 0;
			}
			else {
				runOf[i] = runs.size();
				open.put(identity, runs.size());
				starts.add(i);
				ownLive.add(new int[2]);
				ownRunners.add(new int[2]);
				runs.add(new ArrayList<>(List.of(group)));
			}
		}
		runs.removeIf(run -> run.size() < 2);
		return runs;
	}

	/**
	 * The {@code ;} ending the declaration {@code group}, or null when it has none. The parse
	 * hangs it under the first declarator, whatever the number of names.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST semi(@Nonnull List<DetailAST> group) {
		for (var declarator : group) {
			final var semi = declarator.findFirstToken(TokenTypes.SEMI);
			if (semi != null)
				return semi;
		}
		return null;
	}

	/** The line of the {@code ;} ending the declaration {@code group}, or of its last name when it has none. */
	@CheckReturnValue
	private static int semiLine(@Nonnull List<DetailAST> group) {
		final var semi = semi(group);
		return (semi == null ? group.getLast().findFirstToken(TokenTypes.IDENT) : semi).getLineNo();
	}

	/** True when a line holding only a comment sits between the declarations {@code prev} and {@code curr}. */
	@CheckReturnValue
	private static boolean separatedByComment(@Nonnull List<DetailAST> prev, @Nonnull List<DetailAST> curr, @Nonnull List<String> lines) {
		final var currLine = DocComments.line(DocComments.firstToken(curr.getFirst()));
		for (var line = semiLine(prev) + 1; line < currLine; ++line) {
			if (!lines.get(line - 1).isBlank())
				return true;
		}
		return false;
	}

	/** True when other code shares the first line of {@code group} ahead of it, or its last line after it. */
	@CheckReturnValue
	private static boolean sharesLine(@Nonnull List<DetailAST> group, @Nonnull DocComments docs) {
		final var start = DocComments.firstToken(group.getFirst());
		final var before = docs.tokens().lower(start);
		if (before != null && DocComments.line(before) == DocComments.line(start))
			return true;
		final var semi = semi(group);
		if (semi == null)
			return true;
		final var after = docs.tokens().higher(DocComments.position(semi.getLineNo(), semi.getColumnNo()));
		return after != null && DocComments.line(after) == semi.getLineNo();
	}

	/**
	 * True when a class body, block or switch within {@code node} holds something and opens and
	 * closes on different lines, a layout that cannot be folded onto one line.
	 */
	@CheckReturnValue
	private static boolean splitsBlock(@Nonnull DetailAST node) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(node);
		while (!stack.isEmpty()) {
			final var current = stack.pop();
			final var open = switch (current.getType()) {
				case TokenTypes.LITERAL_SWITCH -> current.findFirstToken(TokenTypes.LCURLY);
				case TokenTypes.OBJBLOCK -> current.getFirstChild();
				case TokenTypes.SLIST -> current;
				default -> null;
			};
			if (open != null) {
				final var content = open == current ? current.getFirstChild() : open.getNextSibling();
				if (content != null && content.getType() != TokenTypes.RCURLY && current.getLastChild().getLineNo() != open.getLineNo())
					return true;
			}
			for (var child = current.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return false;
	}

	/**
	 * The declarator's full type as source-order text, its C-style brackets written as if
	 * they were on the type ({@code int alpha[]} reads {@code int[]}) and its type-use
	 * annotations as their {@link AstText#annotationIdentity}, or null when it cannot be told
	 * apart reliably.
	 *
	 * <p>C-style brackets are TYPE children past the declarator's name, appended after the
	 * type's own dimensions. They are the outer dimensions, though (JLS 10.2:
	 * {@code int @A [] alpha[]} is {@code int [] @A []}), so once a dimension carries an
	 * annotation, the order in the tree is no longer the order of the levels.
	 */
	@CheckReturnValue
	@Nullable
	private static String typeName(@Nonnull DetailAST varDef) {
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return "";
		final var name = varDef.findFirstToken(TokenTypes.IDENT);
		final var tokens = new ArrayList<String>();
		var declaratorDims = 0;
		var annotatedDims = false;
		for (var child = type.getFirstChild(); child != null; child = child.getNextSibling()) {
			// a TYPE's direct ANNOTATIONS child is always a dimension's; annotations on the
			// base type sit in MODIFIERS or inside a qualified name's DOT
			if (child.getType() == TokenTypes.ANNOTATIONS)
				annotatedDims = true;
			if (child.getLineNo() > name.getLineNo()
					|| child.getLineNo() == name.getLineNo() && child.getColumnNo() > name.getColumnNo()) {
				if (child.getType() == TokenTypes.ARRAY_DECLARATOR)
					++declaratorDims;
			}
			else {
				final var childTokens = AstText.identityTokens(child, AstText.MAX_ANNOTATION_DEPTH);
				if (childTokens == null)
					return null;
				tokens.addAll(childTokens);
			}
		}
		if (declaratorDims > 0 && annotatedDims)
			return null;

		final var sb = new StringBuilder();
		var afterAnnotation = false;
		for (var token : tokens) {
			if (!sb.isEmpty() && (afterAnnotation || needsSpace(sb.charAt(sb.length() - 1), token.charAt(0))))
				sb.append(' ');
			sb.append(token);
			afterAnnotation = token.charAt(0) == '@';
		}
		sb.append("[]".repeat(declaratorDims));
		return sb.toString();
	}

	@Nullable
	private DetailAST root;
	@Nullable
	private DocComments docComments;
	@Nullable
	private LineColumns lines;
	@Nullable
	private List<Comment> comments;

	@Override
	public void beginTree(@Nonnull DetailAST rootAst) {
		comments = null;
		docComments = null;
		lines = null;
		root = rootAst;
	}

	/** The file's comments, lexed from its lines once for every class body in it. */
	@CheckReturnValue
	@Nonnull
	private List<Comment> comments() {
		if (comments == null)
			comments = SourceComments.scan(lines().lines());
		return comments;
	}

	/** The file's {@link DocComments}, from the comments checkstyle collected while parsing. */
	@CheckReturnValue
	@Nonnull
	private DocComments docComments() {
		if (docComments == null) {
			final var starts = new TreeSet<Long>();
			final var contents = getFileContents();
			for (var comments : contents.getBlockComments().values()) {
				for (var comment : comments) {
					if (DocComments.isDocComment(comment.getText()[0]))
						starts.add(DocComments.position(comment.getStartLineNo(), comment.getStartColNo()));
				}
			}
			for (var comment : contents.getSingleLineComments().values()) {
				if (DocComments.isDocComment(comment.getText()[0]))
					starts.add(DocComments.position(comment.getStartLineNo(), comment.getStartColNo()));
			}
			docComments = new DocComments(DocComments.tokenPositions(root), starts);
		}
		return docComments;
	}

	@Override
	public void finishTree(@Nonnull DetailAST rootAst) {
		comments = null;
		docComments = null;
		lines = null;
		root = null;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.OBJBLOCK};
	}

	/** The file's lines, read once for every class body in it. */
	@CheckReturnValue
	@Nonnull
	private LineColumns lines() {
		if (lines == null)
			lines = new LineColumns(List.of(getLines()));
		return lines;
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		for (var run : runs(ast, lines(), this::docComments, this::comments)) {
			for (var i = 1; i < run.size(); ++i) {
				final var curr = run.get(i).getFirst();
				log(curr.findFirstToken(TokenTypes.IDENT), MSG_KEY, fieldName(curr), fieldName(run.get(i - 1).getLast()), typeName(curr));
			}
		}
	}
}