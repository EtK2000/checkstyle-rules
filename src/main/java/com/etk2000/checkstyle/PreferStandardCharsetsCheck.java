package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.ast.AstResolve;
import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags charset string usage and suggests
 * {@code StandardCharsets} constants instead. Detects:
 * <ul>
 *   <li>Known charset-accepting methods/constructors (discovered via
 *       reflection) called with any non-{@code StandardCharsets} argument
 *       (string literals, String-typed variables, fields, parameters), when
 *       the {@code Charset} overload exists at {@code minSdk}</li>
 *   <li>{@code Charset.forName} called with one charset name literal, through
 *       a receiver resolving to {@code java.nio.charset.Charset} or a single
 *       static import of {@code forName}</li>
 *   <li>String literals matching charset names passed as arguments to
 *       any other method/constructor, as a warning: nothing proves a
 *       {@code Charset} overload accepts the constant there</li>
 * </ul>
 *
 * <p>Respects {@code minSdk}: {@code StandardCharsets} requires Android
 * API 19+, so the check is suppressed when {@code minSdk < 19}, and an
 * overload added later on Android is only suggested from its own level.</p>
 */
public class PreferStandardCharsetsCheck extends AbstractResolvingCheck {
	private static final int MIN_SDK_STANDARD_CHARSETS = 19;
	private static final Map<String, String> CHARSET_MAP = buildCharsetMap();
	/**
	 * The Android API level of every JDK {@code Charset} overload newer than
	 * {@link #MIN_SDK_STANDARD_CHARSETS} whose {@code String} sibling takes a
	 * charset name, keyed by the declaring class and the method name
	 * ({@code new} for a constructor). From the SDK's {@code api-versions.xml}.
	 */
	private static final Map<String, Integer> CHARSET_OVERLOAD_MIN_SDK = Map.of(
			"java.io.ByteArrayOutputStream#toString", 33,
			"java.io.PrintStream#new", 33,
			"java.io.PrintWriter#new", 33,
			"java.net.URLDecoder#decode", 33,
			"java.net.URLEncoder#encode", 33,
			"java.nio.channels.Channels#newReader", 33,
			"java.nio.channels.Channels#newWriter", 33,
			"java.util.Formatter#new", 34,
			"java.util.Properties#storeToXML", 35,
			"java.util.Scanner#new", 34
	);
	/**
	 * Checked exceptions a charset-name overload throws and its {@code Charset} overload does not: a
	 * catch of one stops compiling when nothing else left in the {@code try} throws it.
	 */
	private static final Set<String> ENCODING_FAILURES = Set.of("IOException", "UnsupportedEncodingException");
	private static final Set<Integer> METHOD_DEFS = Set.of(TokenTypes.METHOD_DEF);
	/** Where a call stands alone as a statement expression, so a constant cannot replace it. */
	private static final Set<Integer> STATEMENT_HOLDERS = Set.of(
			TokenTypes.LABELED_STAT,
			TokenTypes.LAMBDA,
			TokenTypes.LITERAL_DO,
			TokenTypes.LITERAL_ELSE,
			TokenTypes.LITERAL_FOR,
			TokenTypes.LITERAL_IF,
			TokenTypes.LITERAL_WHILE,
			TokenTypes.SLIST,
			TokenTypes.SWITCH_RULE
	);
	private static final Set<Integer> TYPE_DEFS = Set.of(
			TokenTypes.ANNOTATION_DEF,
			TokenTypes.CLASS_DEF,
			TokenTypes.ENUM_DEF,
			TokenTypes.INTERFACE_DEF,
			TokenTypes.RECORD_DEF
	);
	private static final String CHARSET_FQCN = "java.nio.charset.Charset";
	private static final String CHARSET_SIMPLE_NAME = "Charset";
	private static final String FOR_NAME = "forName";
	public static final String MSG_FOR_NAME = "prefer.standard.charsets.for.name";
	public static final String MSG_GENERIC = "prefer.standard.charsets.string";
	public static final String MSG_LITERAL = "prefer.standard.charsets.literal";
	public static final String MSG_SPECIFIC = "prefer.standard.charsets";
	private static final String STANDARD_CHARSETS_FQCN = "java.nio.charset.StandardCharsets";
	private static final String STANDARD_CHARSETS_SIMPLE_NAME = "StandardCharsets";

	@Nonnull
	private static Map<String, String> buildCharsetMap() {
		final var map = new HashMap<String, String>();
		for (var field : StandardCharsets.class.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers()) && field.getType() == Charset.class) {
				try {
					final var charset = (Charset) field.get(null);
					final var fieldName = field.getName();
					map.put(charset.name().toLowerCase(Locale.ROOT), fieldName);
					for (var alias : charset.aliases())
						map.put(alias.toLowerCase(Locale.ROOT), fieldName);
				}
				catch (IllegalAccessException ignored) {
				}
			}
		}
		return Map.copyOf(map);
	}

	@CheckReturnValue
	@Nullable
	private static String callName(@Nonnull DetailAST call) {
		return call.getType() == TokenTypes.LITERAL_NEW ? "new" : AstQuery.getMethodName(call);
	}

	/** Whether a {@code catch} of {@code tryAst} names an {@link #ENCODING_FAILURES} type, as any alternative of a multi-catch. */
	@CheckReturnValue
	private static boolean catchesEncodingFailure(@Nonnull DetailAST tryAst) {
		for (var clause = tryAst.getFirstChild(); clause != null; clause = clause.getNextSibling()) {
			if (clause.getType() == TokenTypes.LITERAL_CATCH
					&& namesEncodingFailure(clause.findFirstToken(TokenTypes.PARAMETER_DEF).findFirstToken(TokenTypes.TYPE)))
				return true;
		}
		return false;
	}

	/**
	 * Whether a direct child of {@code body} written before {@code stop} (every child, when it is
	 * null) is one of {@code declarationTypes} named {@code name}.
	 */
	@CheckReturnValue
	private static boolean declaresBefore(
			@Nonnull DetailAST body,
			@Nullable DetailAST stop,
			@Nonnull Set<Integer> declarationTypes,
			@Nonnull String name
	) {
		for (var member = body.getFirstChild(); member != null && member != stop; member = member.getNextSibling()) {
			if (declarationTypes.contains(member.getType()) && name.equals(member.findFirstToken(TokenTypes.IDENT).getText()))
				return true;
		}
		return false;
	}

	/** Whether {@code scope}, a type or method declaration, declares a type parameter named {@code name}. */
	@CheckReturnValue
	private static boolean declaresTypeParameter(@Nonnull DetailAST scope, @Nonnull String name) {
		if (!TYPE_DEFS.contains(scope.getType()) && scope.getType() != TokenTypes.METHOD_DEF && scope.getType() != TokenTypes.CTOR_DEF)
			return false;
		final var parameters = scope.findFirstToken(TokenTypes.TYPE_PARAMETERS);
		if (parameters == null)
			return false;
		for (var parameter = parameters.getFirstChild(); parameter != null; parameter = parameter.getNextSibling()) {
			if (parameter.getType() == TokenTypes.TYPE_PARAMETER && name.equals(parameter.findFirstToken(TokenTypes.IDENT).getText()))
				return true;
		}
		return false;
	}

	/**
	 * The one-argument {@code forName} call whose argument is the string literal at the 0-based
	 * {@code (line, column)}, written with a receiver ({@code Charset.forName(...)}) or bare (a
	 * static import), or null. Only the shape is matched: whether the name resolves to
	 * {@code java.nio.charset.Charset} is the check's to decide.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST forNameCallAt(@Nonnull DetailAST root, int line, int column) {
		final var literal = AstQuery.findNodeAt(root, line, column, node -> node.getType() == TokenTypes.STRING_LITERAL);
		return literal == null ? null : forNameCallOf(literal);
	}

	/**
	 * The call when {@code literal} is the only argument of a {@code forName(...)} or
	 * {@code receiver.forName(...)} call without type arguments, else null.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST forNameCallOf(@Nonnull DetailAST literal) {
		final var expr = literal.getParent();
		if (expr == null || expr.getType() != TokenTypes.EXPR)
			return null;
		final var elist = expr.getParent();
		if (elist == null || elist.getType() != TokenTypes.ELIST || AstQuery.countArguments(elist) != 1)
			return null;
		final var call = elist.getParent();
		if (call == null || call.getType() != TokenTypes.METHOD_CALL)
			return null;
		final var callee = call.getFirstChild();
		if (callee.getType() == TokenTypes.IDENT)
			return FOR_NAME.equals(callee.getText()) ? call : null;
		if (callee.getType() != TokenTypes.DOT || callee.findFirstToken(TokenTypes.TYPE_ARGUMENTS) != null)
			return null;
		final var name = callee.getLastChild();
		return name.getType() == TokenTypes.IDENT && FOR_NAME.equals(name.getText()) ? call : null;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST getArgExpr(@Nonnull DetailAST elist, int index) {
		var i = 0;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.EXPR) {
				if (i == index)
					return child.getFirstChild();
				++i;
			}
		}
		return null;
	}

	/**
	 * Whether a same-file supertype of the class whose body is {@code body}, or one of its own
	 * same-file supertypes, declares one of {@code declarationTypes} named {@code name}, which the
	 * class inherits. {@code seen} stops a cyclic hierarchy, which does not compile but still parses.
	 */
	@CheckReturnValue
	private static boolean inheritsMember(
			@Nonnull DetailAST body,
			@Nonnull Set<Integer> declarationTypes,
			@Nonnull String name,
			@Nonnull Set<DetailAST> seen
	) {
		for (var superBody : AstResolve.supertypeBodies(body)) {
			if (seen.add(superBody)
					&& (declaresBefore(superBody, null, declarationTypes, name) || inheritsMember(superBody, declarationTypes, name, seen)))
				return true;
		}
		return false;
	}

	/**
	 * Whether {@code node} sits in the body or resources of a {@code try} whose catch would stop
	 * compiling once a charset-name overload there no longer throws, at any depth of nested tries.
	 * A catch or finally body is outside its own try's reach, so only an enclosing try counts there.
	 */
	@CheckReturnValue
	public static boolean isCaughtAsEncodingFailure(@Nonnull DetailAST node) {
		var child = node;
		for (var parent = node.getParent(); parent != null; child = parent, parent = parent.getParent()) {
			if (parent.getType() == TokenTypes.LITERAL_TRY
					&& (child.getType() == TokenTypes.SLIST || child.getType() == TokenTypes.RESOURCE_SPECIFICATION)
					&& catchesEncodingFailure(parent))
				return true;
		}
		return false;
	}

	/** Whether {@code literal} is the only argument of a one-argument {@code forName(...)} call. */
	@CheckReturnValue
	public static boolean isForNameArgument(@Nonnull DetailAST literal) {
		return forNameCallOf(literal) != null;
	}

	@CheckReturnValue
	private static boolean isStandardCharsetsRef(@Nonnull DetailAST expr) {
		if (expr.getType() == TokenTypes.DOT) {
			final var left = expr.getFirstChild();
			return left != null
					&& left.getType() == TokenTypes.IDENT
					&& "StandardCharsets".equals(left.getText());
		}
		return false;
	}

	/**
	 * Whether {@code call} stands alone as a statement expression, or as an expression lambda's
	 * whole body, where a constant cannot take its place: a bare name is not a statement, and the
	 * syntax cannot tell a value-returning lambda target from a {@code void} one. A {@code return},
	 * an initializer or an argument is a value position and does not count.
	 */
	@CheckReturnValue
	public static boolean isStatementExpression(@Nonnull DetailAST call) {
		final var parent = call.getParent();
		if (parent.getType() == TokenTypes.LAMBDA)
			return true;
		if (parent.getType() != TokenTypes.EXPR)
			return false;
		final var holder = parent.getParent();
		if (holder.getType() == TokenTypes.ELIST) {
			final var clause = holder.getParent().getType();
			return clause == TokenTypes.FOR_INIT || clause == TokenTypes.FOR_ITERATOR;
		}
		return STATEMENT_HOLDERS.contains(holder.getType());
	}

	/**
	 * Returns whether the IDENT expression resolves to a {@code String}-typed
	 * variable, field, or parameter, a {@code var} local included when its
	 * initializer's type resolves. Returns {@code false} when the type cannot be
	 * determined.
	 */
	@CheckReturnValue
	private static boolean isStringIdent(@Nonnull DetailAST ident) {
		if (ident.getType() != TokenTypes.IDENT)
			return false;
		final var typeName = AstResolve.resolveVariableType(ident, ident.getText());
		return "String".equals(typeName) || "java.lang.String".equals(typeName);
	}

	/**
	 * If the expression is a string literal matching a known charset
	 * name, returns the {@code StandardCharsets} field name; otherwise null.
	 */
	@CheckReturnValue
	@Nullable
	private static String matchCharsetLiteral(@Nonnull DetailAST expr) {
		if (expr.getType() != TokenTypes.STRING_LITERAL)
			return null;
		final var text = expr.getText();
		return standardCharsetConstant(text.substring(1, text.length() - 1));
	}

	/**
	 * Whether a type among the children of {@code catchType} (a catch parameter's {@code TYPE}, or a
	 * {@code BOR} of a multi-catch union nested in it) names an {@link #ENCODING_FAILURES} type,
	 * simple or qualified.
	 */
	@CheckReturnValue
	private static boolean namesEncodingFailure(@Nonnull DetailAST catchType) {
		for (var alternative = catchType.getFirstChild(); alternative != null; alternative = alternative.getNextSibling()) {
			if (alternative.getType() == TokenTypes.BOR) {
				if (namesEncodingFailure(alternative))
					return true;
			}
			else if (alternative.getType() == TokenTypes.IDENT || alternative.getType() == TokenTypes.DOT) {
				final var name = FullIdent.createFullIdent(alternative).getText();
				if (ENCODING_FAILURES.contains(name.substring(name.lastIndexOf('.') + 1)))
					return true;
			}
		}
		return false;
	}

	/**
	 * Maps a charset name or alias to the matching {@code StandardCharsets} field name, or null
	 * when no constant covers it. Matching is case-insensitive under {@link Locale#ROOT}, since
	 * charset names are ASCII protocol identifiers: a locale-sensitive lowercase would map the
	 * {@code I} in {@code ISO-8859-1} and {@code US-ASCII} to a dotless {@code \u0131} under a
	 * Turkish default locale and stop matching them.
	 *
	 * @param charsetName the unquoted charset name
	 */
	@CheckReturnValue
	@Nullable
	public static String standardCharsetConstant(@Nonnull String charsetName) {
		return CHARSET_MAP.get(charsetName.toLowerCase(Locale.ROOT));
	}

	private boolean charsetSingleTypeImported, forNameStaticallyImported, hasPackage, standardCharsetsDeclaredTopLevel, standardCharsetsSingleTypeImported;
	private VariableScope variableScope;

	/**
	 * The type of the argument {@code expr} as {@link ReflectionUtil#charsetOverloadWidensCheckedExceptions}
	 * reads it: a string, boolean or null literal, a constructor call, or a variable whose declared
	 * type resolves without type arguments or array brackets; null for anything else.
	 */
	@CheckReturnValue
	@Nullable
	private String argumentType(@Nonnull DetailAST expr) {
		return switch (expr.getType()) {
			case TokenTypes.IDENT -> {
				final var declared = AstResolve.resolveVariableType(expr, expr.getText());
				yield declared == null || declared.contains("<") || declared.contains("[") ? null : resolve(declared);
			}
			case TokenTypes.LITERAL_FALSE, TokenTypes.LITERAL_TRUE -> "boolean";
			case TokenTypes.LITERAL_NEW -> {
				final var className = expr.findFirstToken(TokenTypes.ARRAY_DECLARATOR) == null ? AstText.findNewClassName(expr) : null;
				yield className == null ? null : resolve(className);
			}
			case TokenTypes.LITERAL_NULL -> "null";
			case TokenTypes.STRING_LITERAL -> String.class.getName();
			default -> null;
		};
	}

	/** The type of each argument of {@code elist}, in order, as {@link #argumentType} reads it. */
	@CheckReturnValue
	@Nonnull
	private List<String> argumentTypes(@Nonnull DetailAST elist) {
		final var types = new ArrayList<String>();
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.EXPR)
				types.add(argumentType(child.getFirstChild()));
		}
		return types;
	}

	/**
	 * Records the file-level facts the per-call decisions read: a single-type import of
	 * {@code Charset} (without one, in a named package, a same-package {@code Charset} would win
	 * over an import-on-demand), a single static import of {@code Charset.forName}, a single-type
	 * import of {@code StandardCharsets} (which outranks a same-package one), and a top-level type
	 * named {@code StandardCharsets}, which the inserted constant would bind to, along with the
	 * {@link VariableScope} {@link #isNameTakenAt} asks about variables.
	 */
	@Override
	protected void beginFile(@Nullable DetailAST rootAST) {
		charsetSingleTypeImported = false;
		forNameStaticallyImported = false;
		hasPackage = false;
		standardCharsetsDeclaredTopLevel = false;
		standardCharsetsSingleTypeImported = false;
		variableScope = new VariableScope(rootAST, packageName(), this::resolve);
		if (rootAST == null)
			return;
		for (var child = rootAST.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.IMPORT -> {
					final var imported = FullIdent.createFullIdentBelow(child).getText();
					charsetSingleTypeImported |= CHARSET_FQCN.equals(imported);
					standardCharsetsSingleTypeImported |= STANDARD_CHARSETS_FQCN.equals(imported);
				}
				case TokenTypes.PACKAGE_DEF -> hasPackage = true;
				case TokenTypes.STATIC_IMPORT -> {
					if ((CHARSET_FQCN + '.' + FOR_NAME).equals(FullIdent.createFullIdent(child.getFirstChild().getNextSibling()).getText()))
						forNameStaticallyImported = true;
				}
				default -> {
					if (TYPE_DEFS.contains(child.getType())
							&& STANDARD_CHARSETS_SIMPLE_NAME.equals(child.findFirstToken(TokenTypes.IDENT).getText()))
						standardCharsetsDeclaredTopLevel = true;
				}
			}
		}
	}

	/**
	 * The fully qualified class whose constructors or methods {@code call} selects among: the
	 * constructed class, or the receiver's type for a method call. Null when it does not resolve,
	 * and for an unqualified method call.
	 */
	@CheckReturnValue
	@Nullable
	private String callTargetFqcn(@Nonnull DetailAST call) {
		if (call.getType() == TokenTypes.LITERAL_NEW) {
			final var className = AstText.findNewClassName(call);
			return className == null ? null : resolve(className);
		}
		final var callee = call.getFirstChild();
		if (callee == null || callee.getType() != TokenTypes.DOT)
			return null;

		final var receiver = callee.getFirstChild();
		final String receiverTypeName;
		if (receiver != null && receiver.getType() == TokenTypes.STRING_LITERAL)
			receiverTypeName = "String";
		else if (receiver != null && receiver.getType() == TokenTypes.LITERAL_NEW)
			receiverTypeName = AstText.findNewClassName(receiver);
		else
			receiverTypeName = receiverTypeName(call);
		return receiverTypeName == null ? null : resolve(receiverTypeName);
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_NEW, TokenTypes.METHOD_CALL};
	}

	/**
	 * Whether the simple {@code name} means something declared in this file at {@code node}, which
	 * takes precedence over an import of that name. As a type name ({@code typeName}): a type of that
	 * name enclosing the node, a type parameter of an enclosing type or method, a member type of any
	 * enclosing class body (a type's, an anonymous class's or an enum constant's) or of a same-file
	 * supertype of one (the extends and implements chains walked within the file), a local type
	 * declared earlier in an enclosing block, a top-level type of the file, or a variable that may be
	 * in scope ({@link VariableScope#lookupVariable}), which obscures the type; a class that answer
	 * depends on but cannot be read counts as taking the name. As a method name: a method of that name
	 * in any enclosing class body or same-file supertype body. Member types and methods of supertypes
	 * declared in other files are not seen.
	 */
	@CheckReturnValue
	private boolean isNameTakenAt(@Nonnull DetailAST node, @Nonnull String name, boolean typeName) {
		if (typeName && variableScope.lookupVariable(node, name) != ScopeLookup.ABSENT)
			return true;
		final var declarationTypes = typeName ? TYPE_DEFS : METHOD_DEFS;
		var child = node;
		for (var scope = node.getParent(); scope != null; child = scope, scope = scope.getParent()) {
			if (typeName && TYPE_DEFS.contains(scope.getType()) && name.equals(scope.findFirstToken(TokenTypes.IDENT).getText()))
				return true;
			if (typeName && declaresTypeParameter(scope, name))
				return true;
			final var taken = switch (scope.getType()) {
				case TokenTypes.COMPILATION_UNIT -> typeName && declaresBefore(scope, null, TYPE_DEFS, name);
				case TokenTypes.OBJBLOCK -> declaresBefore(scope, null, declarationTypes, name)
						|| inheritsMember(scope, declarationTypes, name, new HashSet<>());
				case TokenTypes.SLIST -> declaresBefore(scope, child, declarationTypes, name);
				default -> false;
			};
			if (taken)
				return true;
		}
		return false;
	}

	/** Whether the {@code Charset} overload behind {@code call}'s {@code parameter} exists at {@code minSdk}. */
	@CheckReturnValue
	private boolean isOverloadAvailable(@Nonnull CharsetParameter parameter, @Nonnull DetailAST call) {
		final var key = parameter.overloadOwner() + '#' + callName(call);
		return minSdkAtLeast(CHARSET_OVERLOAD_MIN_SDK.getOrDefault(key, MIN_SDK_STANDARD_CHARSETS));
	}

	/**
	 * Whether {@code call}, already shaped as a one-argument {@code forName} call, is
	 * {@code java.nio.charset.Charset.forName}: its receiver resolves to that class, or a bare name
	 * is its static import, in either case not taken by a declaration in the file
	 * ({@link #isNameTakenAt}). A simple {@code Charset} receiver also needs a single-type import of
	 * it when the file has a package or a {@code Charset.java} sits next to the file: a same-package
	 * class outranks an import-on-demand.
	 */
	@CheckReturnValue
	private boolean isProvenForName(@Nonnull DetailAST call) {
		final var callee = call.getFirstChild();
		if (callee.getType() == TokenTypes.IDENT)
			return forNameStaticallyImported && !isNameTakenAt(call, FOR_NAME, false);
		final var receiver = callee.getFirstChild();
		if (receiver.getType() == TokenTypes.IDENT) {
			return CHARSET_FQCN.equals(resolve(receiver.getText()))
					&& (charsetSingleTypeImported || !hasPackage && !siblingSourceFileExists(CHARSET_SIMPLE_NAME))
					&& !isNameTakenAt(call, CHARSET_SIMPLE_NAME, true);
		}
		return CHARSET_FQCN.equals(FullIdent.createFullIdent(receiver).getText());
	}

	/**
	 * Whether {@code StandardCharsets} at {@code node} would name something other than
	 * {@code java.nio.charset.StandardCharsets}: a declaration of that name in the file
	 * ({@link #isNameTakenAt}), an import of a different class by that name, or, without a
	 * single-type import of the JDK class, a {@code StandardCharsets.java} next to the file: a
	 * same-package class the checkstyle classpath cannot load, which outranks an import-on-demand and
	 * which the inserted import would shadow file-wide.
	 */
	@CheckReturnValue
	private boolean isStandardCharsetsNameTaken(@Nonnull DetailAST node) {
		if (standardCharsetsDeclaredTopLevel || isNameTakenAt(node, STANDARD_CHARSETS_SIMPLE_NAME, true))
			return true;
		final var resolved = resolve(STANDARD_CHARSETS_SIMPLE_NAME);
		if (resolved != null && !STANDARD_CHARSETS_FQCN.equals(resolved))
			return true;
		return !standardCharsetsSingleTypeImported && siblingSourceFileExists(STANDARD_CHARSETS_SIMPLE_NAME);
	}

	private void visitCall(@Nonnull DetailAST ast) {
		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return;

		final var argCount = AstQuery.countArguments(elist);
		final var fqcn = callTargetFqcn(ast);
		final var callName = callName(ast);
		final var parameter = fqcn == null || callName == null ? null : ReflectionUtil.findCharsetParameter(fqcn, callName, argCount);
		final var charsetArgIndex = parameter == null ? -1 : parameter.index();

		if (parameter != null && isOverloadAvailable(parameter, ast)) {
			final var arg = getArgExpr(elist, charsetArgIndex);
			if (arg != null && !isStandardCharsetsRef(arg)) {
				final var constant = matchCharsetLiteral(arg);
				if (constant != null) {
					final var name = arg.getText().substring(1, arg.getText().length() - 1);
					if (isStandardCharsetsNameTaken(arg)
							|| ReflectionUtil.charsetOverloadWidensCheckedExceptions(fqcn, callName, charsetArgIndex, argumentTypes(elist)))
						logWarning(arg, MSG_LITERAL, name, constant);
					else
						log(arg, MSG_SPECIFIC, name, constant);
				}
				else if (isStringIdent(arg))
					log(arg, MSG_GENERIC, arg.getText());
			}
		}

		var i = 0;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.EXPR)
				continue;
			if (i != charsetArgIndex) {
				final var expr = child.getFirstChild();
				if (expr != null) {
					final var constant = matchCharsetLiteral(expr);
					if (constant != null) {
						final var name = expr.getText().substring(1, expr.getText().length() - 1);
						if (forNameCallOf(expr) == ast && isProvenForName(ast) && !isStandardCharsetsNameTaken(expr))
							log(expr, MSG_FOR_NAME, name, constant);
						else
							logWarning(expr, MSG_LITERAL, name, constant);
					}
				}
			}
			++i;
		}
	}

	@Override
	protected void visitScopedToken(@Nonnull DetailAST ast) {
		if (minSdkAtLeast(MIN_SDK_STANDARD_CHARSETS))
			visitCall(ast);
	}
}