package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.FieldConsolidationCheck;
import com.etk2000.checkstyle.FieldConsolidationCheck.DeclarationKey;
import com.etk2000.checkstyle.FieldInitializers;
import com.etk2000.checkstyle.FieldInitializers.BodyOrder;
import com.etk2000.checkstyle.SourceComments;
import com.etk2000.checkstyle.ast.AstText;
import com.etk2000.checkstyle.ast.DocComments;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Re-parses a member-reordering fixer's rewrite and accepts it only when it changed how the
 * fields or enum constants are laid out and nothing else.
 *
 * <p>What has to survive, between the buffer before and after:
 * <ul>
 *   <li>every token and comment outside a run of consecutive field declarations or enum
 *       constants, in order;</li>
 *   <li>for each run, the same members: each field declarator with its name, modifier keywords,
 *       annotations, full type and initializer, and each enum constant whole. They may be
 *       regrouped into other declarations and reordered within the run;</li>
 *   <li>for each run, the comments bound to its members. A doc comment must stay on a
 *       declaration of exactly the same names, since javac gives it to every name of its
 *       declaration. Any other comment may move within the declaration its member ends up in,
 *       and two identical ones there may become one. Those the rewrite does not keep may
 *       instead be joined, in source order and a single space apart, into one line comment
 *       there, which takes in no comment from outside the run.</li>
 *   <li>for each doc comment outside every run, whether it documents a declaration or nothing.</li>
 * </ul>
 *
 * <p>A buffer holding a unicode escape that javac would decode into a quote, a backslash, a
 * comment delimiter or a line break cannot be compared at all: javac and the parse disagree on
 * where its literals and comments end.
 */
final class FieldRewriteVerifier {
	/**
	 * A comment bound to the run member {@code member} names; {@code javadoc} when it is the
	 * Javadoc of that member's declaration. {@code position} is where it starts.
	 */
	private record Binding(@Nonnull String text, boolean javadoc, @Nonnull String member, long position) {}

	/**
	 * A doc comment outside every run; {@code attached} when it documents a declaration, rather
	 * than nothing, so a rewrite cannot turn the one into the other.
	 */
	private record LooseDoc(@Nonnull String text, boolean attached) {}

	/**
	 * One field declarator or enum constant, as far as a rewrite may not change it. A field's
	 * {@code key} is null for a type the merge rules cannot normalize, which is then compared as
	 * {@code written}; {@code body} is the {@link #skeleton} of its initializer, or of the whole
	 * constant.
	 */
	private record Member(
			@Nonnull String name,
			@Nullable DeclarationKey key,
			@Nonnull List<String> written,
			@Nonnull List<Object> body
	) {}

	/**
	 * The last analysis of a buffer, so an unchanged one is not parsed again: its skeleton, and
	 * its class bodies' field orders ({@link FieldInitializers#bodyOrders}), each null when it
	 * does not parse.
	 */
	private record Memo(@Nonnull List<String> lines, @Nullable List<Object> skeleton, @Nullable List<BodyOrder> orders) {}

	private record Positioned(int line, int column, @Nonnull Object value) {}

	/**
	 * One run of consecutive field declarations or enum constants: its members by name, the
	 * names of each member's declaration, and the comments bound to the members.
	 */
	private record Run(
			@Nonnull Map<String, Member> members,
			@Nonnull Map<String, Set<String>> declarations,
			@Nonnull List<Binding> bindings
	) {}

	/**
	 * A run as parsed: its declarations, each a list of members, and each `,` or `;` between
	 * them mapped to the index of the declaration it follows.
	 */
	private record RunShape(@Nonnull List<List<DetailAST>> declarations, @Nonnull Map<DetailAST, Integer> separators) {}

	/**
	 * Where the comments of one buffer belong: the member each bound comment binds to, and the
	 * comments outside every run.
	 */
	private record Attachment(@Nonnull Map<DetailAST, List<Binding>> bound, @Nonnull List<Positioned> loose) {}

	private static final char BOM = 0xFEFF;
	/** A rewrite's buffer before it, and after it with and without the runs folded into it. */
	private static final int MEMO_SIZE = 3;
	private static final String LEXICAL_BREAKS = "\"'*/\\\n\r";
	private static final ThreadLocal<List<Memo>> MEMOS = ThreadLocal.withInitial(ArrayList::new);

	/** {@code before} with {@code result} applied, or null for a range outside {@code before}. */
	@CheckReturnValue
	@Nullable
	private static List<String> applied(@Nonnull List<String> before, @Nonnull FixResult result) {
		if (result.startLine() < 0 || result.startLine() > before.size() || result.endLine() >= before.size())
			return null;
		final var after = new ArrayList<>(before);
		if (result.endLine() >= result.startLine())
			after.subList(result.startLine(), result.endLine() + 1).clear();
		after.addAll(result.startLine(), result.replacement());
		return after;
	}

	/**
	 * Binds every comment of {@code lines} against their parse, {@code root}. A doc comment
	 * documenting the first declaration of a run, by {@link DocComments}, binds to it. One whose
	 * next token is outside every run stays loose, marked by whether it documents a declaration
	 * there or nothing at all. Any other comment goes by the code after it on the line it ends,
	 * else the code before it on the line it starts, else the next code, and binds to the member
	 * owning that code. A comment whose code is outside every run stays loose, positioned among
	 * the tokens.
	 */
	@CheckReturnValue
	@Nonnull
	private static Attachment attach(@Nonnull DetailAST root, @Nonnull List<String> lines) {
		final var owners = new TreeMap<Long, Integer>();
		final var comments = SourceComments.scan(lines);
		final var objBlocks = new ArrayList<DetailAST>();
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.OBJBLOCK)
				objBlocks.add(node);
			if (!AstText.isImaginaryToken(node))
				owners.put(position(node), -1);
			for (var child = node.getLastChild(); child != null; child = child.getPreviousSibling())
				stack.push(child);
		}
		final var docStarts = new TreeSet<Long>();
		for (var comment : comments) {
			if (DocComments.isDocComment(comment.text()))
				docStarts.add(comment.start());
		}
		// copied before an enum's terminator leaves the owners: javac reads it as the token after
		// a doc comment ahead of it, which then documents nothing
		final var docs = new DocComments(new TreeSet<>(owners.navigableKeySet()), docStarts);
		final var declarationStarts = DocComments.declarationStarts(root);

		// an OBJBLOCK is listed before any nested in it, so an inner run's claim on a token
		// replaces the outer member's
		final var declarations = new ArrayList<List<DetailAST>>();
		final var runStarts = new HashMap<Long, DetailAST>();
		for (var objBlock : objBlocks) {
			for (var run : partition(objBlock, new ArrayList<>())) {
				final var firstId = declarations.size();
				declarations.addAll(run.declarations());
				for (var id = firstId; id < declarations.size(); ++id) {
					final var first = declarations.get(id).getFirst();
					runStarts.put(DocComments.firstToken(first), first);
					for (var member : declarations.get(id))
						claim(member, id, owners);
				}
				for (var separator : run.separators().entrySet()) {
					// an enum's terminating `;` is syntax a rebuild may move past a comment, not code
					// a comment belongs to
					if (separator.getKey().getType() == TokenTypes.SEMI
							&& declarations.get(firstId).getFirst().getType() == TokenTypes.ENUM_CONSTANT_DEF)
						owners.remove(position(separator.getKey()));
					else
						owners.put(position(separator.getKey()), firstId + separator.getValue());
				}
			}
		}

		final var bound = new HashMap<DetailAST, List<Binding>>();
		final var loose = new ArrayList<Positioned>();
		for (var comment : comments) {
			final var at = comment.start();
			final var before = owners.lowerEntry(at);
			final var after = owners.higherEntry(at);
			final var text = comment.text();
			if (DocComments.isDocComment(text)) {
				final var documented = docs.documentedToken(at);
				final var first = documented == null ? null : runStarts.get(documented);
				if (first != null) {
					bound.computeIfAbsent(first, k -> new ArrayList<>())
							.add(new Binding(text, true, first.findFirstToken(TokenTypes.IDENT).getText(), at));
					continue;
				}
				final var owner = documented == null ? null : owners.get(documented);
				if (owner == null || owner < 0) {
					loose.add(new Positioned(lineOf(at), (int) at, new LooseDoc(text, documented != null && declarationStarts.contains(documented))));
					continue;
				}
			}
			final var codeFollows = after != null && lineOf(after.getKey()) == lineOf(comment.end());
			final var codePrecedes = before != null && lineOf(before.getKey()) == lineOf(at);
			DetailAST member = null;
			if (codeFollows && after.getValue() >= 0 || !codePrecedes && after != null && after.getValue() >= 0)
				member = memberAt(declarations.get(after.getValue()), after.getKey());
			else if (!codeFollows && codePrecedes && before.getValue() >= 0)
				member = memberAt(declarations.get(before.getValue()), before.getKey());
			if (member == null)
				loose.add(new Positioned(lineOf(at), (int) at, text));
			else
				bound.computeIfAbsent(member, k -> new ArrayList<>()).add(new Binding(text, false, member.findFirstToken(TokenTypes.IDENT).getText(), at));
		}
		return new Attachment(bound, loose);
	}

	/** Marks every token of {@code member}'s subtree as owned by declaration {@code id}. */
	private static void claim(@Nonnull DetailAST member, int id, @Nonnull Map<Long, Integer> owners) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(member);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (!AstText.isImaginaryToken(node))
				owners.put(position(node), id);
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
	}

	/** Drops the memoized analyses, which hold whole copies of a file. */
	static void clearCache() {
		MEMOS.remove();
	}

	/**
	 * True when {@code lines} hold a unicode escape javac would decode into a quote, a backslash,
	 * a comment delimiter or a line break. javac decodes escapes before it lexes and the parse
	 * does not, so the two disagree on where a literal or comment ends, and code one of them sees
	 * the other reads as text. An escape counts when an even number of backslashes precede it.
	 */
	@CheckReturnValue
	private static boolean hidesCodeFromParse(@Nonnull List<String> lines) {
		for (var line : lines) {
			var backslashes = 0;
			for (var i = 0; i < line.length(); ++i) {
				if (line.charAt(i) != '\\') {
					backslashes = 0;
					continue;
				}
				var hex = i + 1;
				while (hex < line.length() && line.charAt(hex) == 'u')
					++hex;
				if (backslashes % 2 == 0 && hex > i + 1 && hex + 4 <= line.length()) {
					final var digits = line.substring(hex, hex + 4);
					if (digits.chars().allMatch(c -> Character.digit(c, 16) >= 0)
							&& LEXICAL_BREAKS.indexOf((char) Integer.parseInt(digits, 16)) >= 0)
						return true;
				}
				++backslashes;
			}
		}
		return false;
	}

	/**
	 * The one line comment that {@code comments}, in source order, are joined into when a merge
	 * moves them above the merged declaration: their texts, without delimiters, separated by
	 * single spaces.
	 */
	@CheckReturnValue
	@Nonnull
	static String joinComments(@Nonnull List<String> comments) {
		final var bodies = new ArrayList<String>(comments.size());
		for (var comment : comments) {
			final var text = SourceComments.body(comment);
			if (!text.isEmpty())
				bodies.add(text);
		}
		return bodies.isEmpty() ? "//" : "// " + String.join(" ", bodies);
	}

	/**
	 * True when {@code after}, less one {@code joined} comment when that is not null, keeps the
	 * comments of {@code before} (identical ones may become one, none may multiply) and every
	 * comment of {@code before} it no longer has is in {@code joined}, in source order.
	 */
	@CheckReturnValue
	private static boolean keeps(@Nonnull List<String> before, @Nonnull List<String> after, @Nullable String joined) {
		final var kept = new HashMap<String, Integer>();
		for (var text : after)
			kept.merge(text, 1, Integer::sum);
		if (joined != null)
			kept.merge(joined, -1, Integer::sum);
		final var available = new HashMap<String, Integer>();
		for (var text : before)
			available.merge(text, 1, Integer::sum);
		for (var entry : kept.entrySet()) {
			if (entry.getValue() > available.getOrDefault(entry.getKey(), 0))
				return false;
		}
		final var dropped = new ArrayList<String>();
		for (var text : before) {
			if (kept.getOrDefault(text, 0) == 0 && !dropped.contains(text))
				dropped.add(text);
		}
		return joined == null ? dropped.isEmpty() : !dropped.isEmpty() && joined.equals(joinComments(dropped));
	}

	/**
	 * True when {@code result}, applied to {@code before}, keeps in order every pair of fields of
	 * one class body that may not trade places ({@link FieldInitializers#keepsOrder}), each body
	 * matched to its rewrite by its {@link BodyOrder#path}. False as well when either buffer does
	 * not parse, a range is outside {@code before}, or a body has no counterpart.
	 */
	@CheckReturnValue
	static boolean keepsInitializationOrder(@Nonnull List<String> before, @Nonnull FixResult result) {
		final var after = applied(before, result);
		if (after == null)
			return false;
		final var beforeOrders = memoized(before).orders();
		final var afterOrders = memoized(after).orders();
		if (beforeOrders == null || afterOrders == null || beforeOrders.size() != afterOrders.size())
			return false;
		final var afterByPath = new HashMap<String, BodyOrder>();
		for (var order : afterOrders)
			afterByPath.put(order.path(), order);
		for (var order : beforeOrders) {
			final var counterpart = afterByPath.get(order.path());
			if (counterpart == null || !FieldInitializers.keepsOrder(order, counterpart))
				return false;
		}
		return true;
	}

	@CheckReturnValue
	private static int lineOf(long position) {
		return (int) (position >>> 32);
	}

	/**
	 * Whether {@code before} and {@code after}, two {@link #skeleton}s or parts of them, describe
	 * the same code. Equality, except that runs are compared with {@link #runMatches}.
	 */
	@CheckReturnValue
	private static boolean matches(@Nullable Object before, @Nullable Object after) {
		if (before instanceof List<?> beforeList && after instanceof List<?> afterList) {
			if (beforeList.size() != afterList.size())
				return false;
			for (var i = 0; i < beforeList.size(); ++i) {
				if (!matches(beforeList.get(i), afterList.get(i)))
					return false;
			}
			return true;
		}
		if (before instanceof Run beforeRun && after instanceof Run afterRun)
			return runMatches(beforeRun, afterRun);
		if (before instanceof Member beforeMember && after instanceof Member afterMember) {
			return beforeMember.name().equals(afterMember.name())
					&& Objects.equals(beforeMember.key(), afterMember.key())
					&& beforeMember.written().equals(afterMember.written())
					&& matches(beforeMember.body(), afterMember.body());
		}
		return Objects.equals(before, after);
	}

	@CheckReturnValue
	@Nullable
	private static Member member(@Nonnull DetailAST node, @Nonnull Attachment attachment) {
		final var ident = node.findFirstToken(TokenTypes.IDENT);
		if (node.getType() == TokenTypes.ENUM_CONSTANT_DEF) {
			final var body = skeleton(node, attachment, List.of());
			return body == null ? null : new Member(ident.getText(), null, List.of(), body);
		}
		final var assign = node.findFirstToken(TokenTypes.ASSIGN);
		// an initializer can hold an anonymous class whose own fields a rewrite regroups, so it
		// is compared the way the whole file is
		final var initializer = assign == null ? List.of() : skeleton(assign, attachment, List.of());
		if (initializer == null)
			return null;
		final var key = FieldConsolidationCheck.declarationKey(node);
		if (key != null)
			return new Member(ident.getText(), key, List.of(), initializer);
		final var written = writtenPrefix(node, ident);
		return written == null ? null : new Member(ident.getText(), null, written, initializer);
	}

	/**
	 * The member of {@code declaration} that the code at {@code position} belongs to: the one
	 * named there, else the last named ahead of it, else the first.
	 */
	@CheckReturnValue
	@Nonnull
	private static DetailAST memberAt(@Nonnull List<DetailAST> declaration, long position) {
		var found = declaration.getFirst();
		for (var member : declaration) {
			if (position(member.findFirstToken(TokenTypes.IDENT)) <= position)
				found = member;
		}
		return found;
	}

	/**
	 * The analysis of {@code lines}, from the memo when the buffer was just analyzed. Its skeleton
	 * is null when it does not parse or cannot be told apart reliably. The memo drops the analysis
	 * used longest ago, so the buffer every verification starts from stays in it.
	 */
	@CheckReturnValue
	@Nonnull
	private static Memo memoized(@Nonnull List<String> lines) {
		final var memos = MEMOS.get();
		for (var i = 0; i < memos.size(); ++i) {
			final var memo = memos.get(i);
			if (memo.lines().equals(lines)) {
				memos.addLast(memos.remove(i));
				return memo;
			}
		}
		final var parsed = new ArrayList<>(lines);
		// the parser refuses a byte-order mark, which carries no code
		if (!parsed.isEmpty() && !parsed.getFirst().isEmpty() && parsed.getFirst().charAt(0) == BOM)
			parsed.set(0, parsed.getFirst().substring(1));
		// the shared parse: the buffer after a rewrite is the one the next fixer call parses
		final var root = hidesCodeFromParse(parsed) ? null : FixerAst.parseOrNull(parsed);
		final var attachment = root == null ? null : attach(root, parsed);
		final var skeleton = attachment == null ? null : skeleton(root, attachment, attachment.loose());
		if (memos.size() == MEMO_SIZE)
			memos.removeFirst();
		final var memo = new Memo(List.copyOf(lines), skeleton, root == null ? null : FieldInitializers.bodyOrders(root));
		memos.add(memo);
		return memo;
	}

	/**
	 * Splits {@code objBlock}'s children into runs of field declarations or enum constants,
	 * adding every other child to {@code others}.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<RunShape> partition(@Nonnull DetailAST objBlock, @Nonnull List<DetailAST> others) {
		final var runs = new ArrayList<RunShape>();
		RunShape run = null;
		var enumRun = false;
		var afterComma = false;
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var type = child.getType();
			final var memberType = type == TokenTypes.VARIABLE_DEF || type == TokenTypes.ENUM_CONSTANT_DEF;
			if (run != null && memberType && enumRun != (type == TokenTypes.ENUM_CONSTANT_DEF))
				run = null;
			if (memberType) {
				if (run == null) {
					run = new RunShape(new ArrayList<>(), new HashMap<>());
					runs.add(run);
					enumRun = type == TokenTypes.ENUM_CONSTANT_DEF;
				}
				if (afterComma && !enumRun)
					run.declarations().getLast().add(child);
				else
					run.declarations().add(new ArrayList<>(List.of(child)));
				afterComma = false;
			}
			// within a run, a `,` separates members, and a `;` ends the enum's constants or is a
			// stray one past a field
			else if (run != null && (type == TokenTypes.COMMA || type == TokenTypes.SEMI)) {
				run.separators().put(child, run.declarations().size() - 1);
				afterComma = type == TokenTypes.COMMA;
			}
			else {
				run = null;
				afterComma = false;
				others.add(child);
			}
		}
		return runs;
	}

	@CheckReturnValue
	private static long position(@Nonnull DetailAST node) {
		return DocComments.position(node.getLineNo(), node.getColumnNo());
	}

	/**
	 * True when {@code result}, applied to {@code before}, changes nothing but the layout of its
	 * fields or enum constants. False as well when either buffer does not parse, and for a range
	 * outside {@code before}.
	 */
	@CheckReturnValue
	static boolean preserves(@Nonnull List<String> before, @Nonnull FixResult result) {
		final var after = applied(before, result);
		if (after == null)
			return false;
		final var beforeSkeleton = memoized(before).skeleton();
		return beforeSkeleton != null && matches(beforeSkeleton, memoized(after).skeleton());
	}

	@CheckReturnValue
	@Nullable
	private static Run run(@Nonnull RunShape shape, @Nonnull Attachment attachment) {
		final var members = new HashMap<String, Member>();
		final var declarations = new HashMap<String, Set<String>>();
		final var bindings = new ArrayList<Binding>();
		for (var declaration : shape.declarations()) {
			final var names = new HashSet<String>();
			for (var node : declaration) {
				final var member = member(node, attachment);
				// a name declared twice is not Java, and members are matched by name
				if (member == null || members.put(member.name(), member) != null)
					return null;
				names.add(member.name());
				declarations.put(member.name(), names);
				bindings.addAll(attachment.bound().getOrDefault(node, List.of()));
			}
		}
		return new Run(members, declarations, bindings);
	}

	/**
	 * Whether two runs hold the same members, by name, with the same comments. A Javadoc has to
	 * stay on a declaration of the same names; any other comment only within the declaration its
	 * member ends up in, where identical ones may merge but none may multiply, and those not kept
	 * may be joined into one line comment by {@link #joinComments}.
	 */
	@CheckReturnValue
	private static boolean runMatches(@Nonnull Run before, @Nonnull Run after) {
		if (!before.members().keySet().equals(after.members().keySet()))
			return false;
		for (var name : before.members().keySet()) {
			if (!matches(before.members().get(name), after.members().get(name)))
				return false;
		}
		final var beforeJavadocs = new HashMap<List<Object>, Integer>();
		final var afterJavadocs = new HashMap<List<Object>, Integer>();
		// keyed by the after run's own name sets, each one declaration's, which spares hashing a
		// merged declaration's every name once per comment
		final var beforeComments = new IdentityHashMap<Set<String>, List<Binding>>();
		final var afterComments = new IdentityHashMap<Set<String>, List<String>>();
		for (var binding : before.bindings()) {
			if (binding.javadoc())
				beforeJavadocs.merge(List.of(binding.text(), before.declarations().get(binding.member())), 1, Integer::sum);
			else
				beforeComments.computeIfAbsent(after.declarations().get(binding.member()), k -> new ArrayList<>()).add(binding);
		}
		for (var binding : after.bindings()) {
			if (binding.javadoc())
				afterJavadocs.merge(List.of(binding.text(), after.declarations().get(binding.member())), 1, Integer::sum);
			else
				afterComments.computeIfAbsent(after.declarations().get(binding.member()), k -> new ArrayList<>()).add(binding.text());
		}
		if (!beforeJavadocs.equals(afterJavadocs) || !beforeComments.keySet().equals(afterComments.keySet()))
			return false;
		for (var entry : beforeComments.entrySet()) {
			final var ordered = new ArrayList<>(entry.getValue());
			ordered.sort(Comparator.comparingLong(Binding::position));
			final var texts = new ArrayList<String>(ordered.size());
			for (var binding : ordered)
				texts.add(binding.text());
			if (!survives(texts, afterComments.get(entry.getKey())))
				return false;
		}
		return true;
	}

	/**
	 * Every source token under {@code node} in source order, plus {@code loose}, with each run of
	 * field declarations or enum constants collapsed to one {@link Run}; null when a member cannot
	 * be told apart reliably.
	 */
	@CheckReturnValue
	@Nullable
	private static List<Object> skeleton(@Nonnull DetailAST node, @Nonnull Attachment attachment, @Nonnull List<Positioned> loose) {
		final var entries = new ArrayList<>(loose);
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(node);
		while (!stack.isEmpty()) {
			final var current = stack.pop();
			if (current.getType() == TokenTypes.ANNOTATION) {
				final var identity = AstText.annotationIdentity(current, AstText.MAX_ANNOTATION_DEPTH);
				if (identity == null)
					return null;
				entries.add(new Positioned(current.getLineNo(), current.getColumnNo(), "@" + identity));
				continue;
			}
			if (current.getType() == TokenTypes.OBJBLOCK) {
				final var others = new ArrayList<DetailAST>();
				for (var shape : partition(current, others)) {
					final var run = run(shape, attachment);
					if (run == null)
						return null;
					final var first = shape.declarations().getFirst().getFirst();
					entries.add(new Positioned(first.getLineNo(), first.getColumnNo(), run));
				}
				others.forEach(stack::push);
				continue;
			}
			if (!AstText.isImaginaryToken(current))
				entries.add(new Positioned(current.getLineNo(), current.getColumnNo(), current.getText()));
			for (var child = current.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		entries.sort(Comparator.comparingInt(Positioned::line).thenComparingInt(Positioned::column));
		final var skeleton = new ArrayList<>(entries.size());
		for (var entry : entries)
			skeleton.add(entry.value());
		return skeleton;
	}

	/**
	 * Whether the comments of one merged declaration survive a rewrite: {@code before} in
	 * source order, {@code after} as they end up. Either every comment is kept, or the ones
	 * that are not were joined into exactly one {@code //} line comment by {@link #joinComments}.
	 */
	@CheckReturnValue
	private static boolean survives(@Nonnull List<String> before, @Nonnull List<String> after) {
		if (keeps(before, after, null))
			return true;
		for (var joined : new LinkedHashSet<>(after)) {
			if (joined.startsWith("//") && keeps(before, after, joined))
				return true;
		}
		return false;
	}

	/**
	 * The modifiers and type of a declarator whose type the merge rules cannot normalize, as
	 * written, the type's tokens split at the name so brackets before and after it stay apart.
	 * Null when an annotation's identity is.
	 */
	@CheckReturnValue
	@Nullable
	private static List<String> writtenPrefix(@Nonnull DetailAST varDef, @Nonnull DetailAST ident) {
		final var prefix = new ArrayList<String>();
		final var modifiers = varDef.findFirstToken(TokenTypes.MODIFIERS);
		if (modifiers != null) {
			final var tokens = AstText.identityTokens(modifiers, AstText.MAX_ANNOTATION_DEPTH);
			if (tokens == null)
				return null;
			prefix.addAll(tokens);
		}
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		var pastName = false;
		for (var child = type == null ? null : type.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (!pastName && (child.getLineNo() > ident.getLineNo()
					|| child.getLineNo() == ident.getLineNo() && child.getColumnNo() > ident.getColumnNo())) {
				pastName = true;
				prefix.add(ident.getText());
			}
			final var tokens = AstText.identityTokens(child, AstText.MAX_ANNOTATION_DEPTH);
			if (tokens == null)
				return null;
			prefix.addAll(tokens);
		}
		return prefix;
	}

	private FieldRewriteVerifier() {
	}
}