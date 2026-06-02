package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Flags qualified static method calls that read better as a static import.
 *
 * <p>Targets a small, hard-coded set of well-known utility methods:</p>
 * <ul>
 *   <li>{@code Predicate.not(...)} (Java 11+, API 33+)</li>
 *   <li>{@code Objects.requireNonNull/isNull/nonNull/...} (API 19+ or 30+)</li>
 *   <li>All static {@code Collectors} methods except {@code toList} and
 *       {@code toUnmodifiableList} (those are handled by
 *       {@link PreferSpecificApiCheck} which rewrites them to
 *       {@code stream.toList()}). API 24+.</li>
 * </ul>
 *
 * <p>The check is conservative:</p>
 * <ul>
 *   <li>Only flags calls written as {@code SimpleClass.method(...)}; not
 *       fully-qualified calls (covered by {@link PreferImportCheck}).</li>
 *   <li>Flags when the candidate class is reachable via an explicit
 *       {@code import} or a wildcard import of the candidate's package.
 *       For the wildcard case, skips when a shadowing class with the same
 *       simple name is detected in the same file (nested/sibling type),
 *       in the same directory (sibling {@code .java} file), or via another
 *       explicit {@code import}.</li>
 *   <li>Skips the call when a local method or a conflicting static import
 *       would shadow the bare name.</li>
 *   <li>Only flags when the same {@code <Class>.<method>} appears at least
 *       {@link #setMinOccurrences minOccurrences} times in the file
 *       (default {@code 2}).</li>
 *   <li>Per-method {@code minSdk} gating skips methods unavailable on the
 *       target Android API level.</li>
 * </ul>
 *
 * <p>Known limitations (fall through to the loud compile-error fallback on
 * the rewritten call):</p>
 * <ul>
 *   <li>Cross source-root same-package shadows (e.g. a class in
 *       {@code src/main/java/com/myapp/Predicate.java} visible from
 *       {@code src/test/java/com/myapp/Foo.java}) are not detected.</li>
 *   <li>Same-package classes contributed by a dependency JAR (split
 *       packages) are not detected.</li>
 * </ul>
 */
public class PreferStaticImportCheck extends AbstractAstCheck {
	private static final int DEFAULT_MIN_OCCURRENCES = 2;
	private static final int MIN_SDK_COLLECTORS = 24;
	private static final int MIN_SDK_OBJECTS = 19;
	private static final int MIN_SDK_OBJECTS_ELSE = 30;
	private static final int MIN_SDK_PREDICATE_NOT = 33;
	private static final Map<String, Map<String, Integer>> CANDIDATES_BY_FQCN = buildCandidates();
	private static final Map<String, Set<String>> CANDIDATE_OWNERS_BY_METHOD = buildOwnersByMethod(CANDIDATES_BY_FQCN);
	private static final Map<String, String> SIMPLE_TO_FQCN = buildSimpleToFqcn(CANDIDATES_BY_FQCN);
	private static final String MSG_KEY = "prefer.static.import";

	@CheckReturnValue
	@Nonnull
	private static Map<String, Map<String, Integer>> buildCandidates() {
		final var map = new HashMap<String, Map<String, Integer>>();

		map.put("java.util.function.Predicate", Map.of("not", MIN_SDK_PREDICATE_NOT));

		final var objects = new HashMap<String, Integer>();
		objects.put("isNull", MIN_SDK_OBJECTS);
		objects.put("nonNull", MIN_SDK_OBJECTS);
		objects.put("requireNonNull", MIN_SDK_OBJECTS);
		objects.put("requireNonNullElse", MIN_SDK_OBJECTS_ELSE);
		objects.put("requireNonNullElseGet", MIN_SDK_OBJECTS_ELSE);
		map.put("java.util.Objects", Map.copyOf(objects));

		final var collectors = new HashMap<String, Integer>();
		for (var method : Collectors.class.getDeclaredMethods()) {
			if (!Modifier.isStatic(method.getModifiers()))
				continue;
			if (!Modifier.isPublic(method.getModifiers()))
				continue;
			final var name = method.getName();
			if ("toList".equals(name) || "toUnmodifiableList".equals(name))
				continue;
			collectors.putIfAbsent(name, MIN_SDK_COLLECTORS);
		}
		map.put("java.util.stream.Collectors", Map.copyOf(collectors));

		return Map.copyOf(map);
	}

	@CheckReturnValue
	@Nonnull
	private static Map<String, Set<String>> buildOwnersByMethod(@Nonnull Map<String, Map<String, Integer>> candidates) {
		final var map = new HashMap<String, Set<String>>();
		for (var entry : candidates.entrySet()) {
			for (var method : entry.getValue().keySet())
				map.computeIfAbsent(method, k -> new HashSet<>()).add(entry.getKey());
		}
		final var copy = new HashMap<String, Set<String>>();
		for (var entry : map.entrySet())
			copy.put(entry.getKey(), Set.copyOf(entry.getValue()));
		return Map.copyOf(copy);
	}

	@CheckReturnValue
	@Nonnull
	private static Map<String, String> buildSimpleToFqcn(@Nonnull Map<String, Map<String, Integer>> candidates) {
		final var map = new HashMap<String, String>();
		for (var fqcn : candidates.keySet())
			map.put(AstUtil.simpleName(fqcn), fqcn);
		return Map.copyOf(map);
	}

	private final Map<String, List<DetailAST>> occurrences = new HashMap<>();
	private final Set<String> conflictedMethods = new HashSet<>();
	private final Set<String> imports = new HashSet<>();
	private final Set<String> shadowedClasses = new HashSet<>();

	private int minOccurrences = DEFAULT_MIN_OCCURRENCES;
	private int minSdk = Integer.MAX_VALUE;

	@Override
	public void beginTree(@Nullable DetailAST rootAST) {
		conflictedMethods.clear();
		imports.clear();
		occurrences.clear();
		shadowedClasses.clear();

		// a comments-only or empty file has no compilation unit, so checkstyle passes a
		// null root; the guard sits after the clears so finishTree sees the cleared state
		if (rootAST == null)
			return;

		// pre-scan the AST: collect imports first (needed for conflict and shadow detection),
		// then walk for local shadows (methods and nested types), then probe the filesystem
		// for same-directory sibling types.
		collectImports(rootAST);
		collectConflictsFromImports();
		collectExplicitImportShadows();
		walkForLocalShadows(rootAST);
		probeFilesystemForShadows();
	}

	private void collectConflictsFromImports() {
		for (var imp : imports) {
			final var staticMarker = "static ";
			if (imp.startsWith(staticMarker)) {
				final var fqn = imp.substring(staticMarker.length());

				// static wildcard import from a non-candidate class blocks all our methods
				if (fqn.endsWith(".*")) {
					final var owner = fqn.substring(0, fqn.length() - 2);
					if (!CANDIDATES_BY_FQCN.containsKey(owner))
						conflictedMethods.addAll(CANDIDATE_OWNERS_BY_METHOD.keySet());
					continue;
				}

				final var lastDot = fqn.lastIndexOf('.');
				if (lastDot < 0)
					continue;
				final var owner = fqn.substring(0, lastDot);
				final var simpleMethod = fqn.substring(lastDot + 1);
				final var validOwners = CANDIDATE_OWNERS_BY_METHOD.get(simpleMethod);
				if (validOwners != null && !validOwners.contains(owner))
					conflictedMethods.add(simpleMethod);
			}
			else {
				// non-static import whose simple name matches a candidate method:
				// e.g. `import com.foo.not;` shadows `not(...)`.
				final var simple = AstUtil.simpleName(imp);
				if (CANDIDATE_OWNERS_BY_METHOD.containsKey(simple))
					conflictedMethods.add(simple);
			}
		}
	}

	private void collectExplicitImportShadows() {
		// a non-static, non-wildcard import whose simple name matches a candidate class
		// but whose FQCN differs means the file's `Predicate`/`Objects`/`Collectors` refers
		// to some other class, not ours.
		for (var imp : imports) {
			if (imp.startsWith("static ") || imp.endsWith(".*"))
				continue;
			final var simple = AstUtil.simpleName(imp);
			final var candidateFqcn = SIMPLE_TO_FQCN.get(simple);
			if (candidateFqcn != null && !imp.equals(candidateFqcn))
				shadowedClasses.add(simple);
		}
	}

	void collectImports(@Nonnull DetailAST root) {
		for (var child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.IMPORT -> imports.add(FullIdent.createFullIdentBelow(child).getText());

				case TokenTypes.STATIC_IMPORT -> {
					// STATIC_IMPORT children: LITERAL_STATIC, then the DOT/IDENT path, then SEMI.
					final var first = child.getFirstChild();
					if (first == null)
						continue;
					final var pathNode = first.getNextSibling();
					if (pathNode == null)
						continue;
					imports.add("static " + FullIdent.createFullIdent(pathNode).getText());
				}
			}
		}
	}

	@Override
	public void finishTree(@Nonnull DetailAST rootAST) {
		for (var entry : occurrences.entrySet()) {
			final var asts = entry.getValue();
			if (asts.size() < minOccurrences)
				continue;
			final var key = entry.getKey();
			final var dot = key.indexOf('.');
			final var simpleClass = key.substring(0, dot);
			final var simpleMethod = key.substring(dot + 1);
			for (var ast : asts)
				log(ast, MSG_KEY, simpleClass, simpleMethod);
		}
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.METHOD_CALL};
	}

	private void probeFilesystemForShadows() {
		final var filePath = getFilePath();
		if (filePath == null)
			return;
		final var parent = Path.of(filePath).getParent();
		if (parent == null)
			return;
		for (var simple : SIMPLE_TO_FQCN.keySet()) {
			if (!shadowedClasses.contains(simple)
					&& Files.exists(parent.resolve(simple + ".java")))
				shadowedClasses.add(simple);
		}
	}

	/**
	 * Sets the minimum number of times a {@code Class.method} must appear in
	 * the file before it is flagged. Default is {@value #DEFAULT_MIN_OCCURRENCES}.
	 * Set to {@code 1} to flag every qualified call.
	 * <p>Called by Checkstyle via reflection when {@code minOccurrences} is
	 * set in the config.</p>
	 */
	@SuppressWarnings("unused")
	public void setMinOccurrences(int minOccurrences) {
		this.minOccurrences = minOccurrences;
	}

	/**
	 * Sets the minimum SDK version for the target platform. Methods unavailable
	 * on older platforms are not flagged.
	 * <p>Called by Checkstyle via reflection when {@code minSdk} is set in the config.</p>
	 */
	@SuppressWarnings("unused")
	public void setMinSdk(int minSdk) {
		this.minSdk = minSdk;
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var dot = ast.getFirstChild();
		if (dot == null || dot.getType() != TokenTypes.DOT)
			return;

		final var receiver = dot.getFirstChild();
		if (receiver == null || receiver.getType() != TokenTypes.IDENT)
			return;

		final var methodIdent = receiver.getNextSibling();
		if (methodIdent == null || methodIdent.getType() != TokenTypes.IDENT)
			return;

		final var simpleClass = receiver.getText();
		final var fqcn = SIMPLE_TO_FQCN.get(simpleClass);
		if (fqcn == null)
			return;

		final var simpleMethod = methodIdent.getText();
		final var minSdkForMethod = CANDIDATES_BY_FQCN.get(fqcn).get(simpleMethod);
		if (minSdkForMethod == null)
			return;
		if (minSdk < minSdkForMethod)
			return;

		// receiver class must be reachable. Either via an explicit import, or via a wildcard
		// import of the candidate's package — in the latter case we also need to confirm no
		// class with the same simple name is shadowing it from a closer scope.
		if (!imports.contains(fqcn)) {
			final var pkgWildcard = fqcn.substring(0, fqcn.lastIndexOf('.')) + ".*";
			if (!imports.contains(pkgWildcard))
				return;
			if (shadowedClasses.contains(simpleClass))
				return;
		}

		// receiver must not be a local variable/field/parameter shadowing the class name
		if (AstUtil.resolveVariableType(ast, simpleClass) != null)
			return;

		// adding a static import for this method would conflict
		if (conflictedMethods.contains(simpleMethod))
			return;

		occurrences.computeIfAbsent(simpleClass + "." + simpleMethod, k -> new ArrayList<>()).add(receiver);
	}

	private void walkForLocalShadows(@Nonnull DetailAST node) {
		switch (node.getType()) {
			case TokenTypes.ANNOTATION_DEF, TokenTypes.CLASS_DEF,
			     TokenTypes.ENUM_DEF, TokenTypes.INTERFACE_DEF, TokenTypes.RECORD_DEF -> {
				final var ident = node.findFirstToken(TokenTypes.IDENT);
				if (ident != null && SIMPLE_TO_FQCN.containsKey(ident.getText()))
					shadowedClasses.add(ident.getText());
			}

			case TokenTypes.METHOD_DEF -> {
				final var ident = node.findFirstToken(TokenTypes.IDENT);
				if (ident != null && CANDIDATE_OWNERS_BY_METHOD.containsKey(ident.getText()))
					conflictedMethods.add(ident.getText());
			}
		}

		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
			walkForLocalShadows(child);
	}
}