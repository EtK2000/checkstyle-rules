package com.etk2000.checkstyle.gradle.fix;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;

/**
 * Skip reason messages loaded from {@code messages.properties}.
 */
final class SkipMessages {
	private static final Map<Object, Object> PROPS = loadProps();

	static final String ANNOTATION_SYNTAX_SKIP = get("annotation.syntax.skip");
	static final String AVOID_SUPER_SKIP = get("avoid.super.skip");
	static final String COLLECTION_INTERFACE_SKIP = get("prefer.collection.interface.skip");
	static final String CONSTRUCTOR_ASSIGN_SKIP_COMMENT = get("constructor.assign.skip.comment");
	static final String CONSTRUCTOR_ASSIGN_SKIP_CYCLE = get("constructor.assign.skip.cycle");
	static final String CONSTRUCTOR_ASSIGN_SKIP_DUPLICATE_FIELD = get("constructor.assign.skip.duplicate.field");
	static final String CONSTRUCTOR_ASSIGN_SKIP_SHARED_LINE = get("constructor.assign.skip.shared.line");
	static final String CONSTRUCTOR_ASSIGN_SKIP_STATEMENT = get("constructor.assign.skip.statement");
	static final String CONSTRUCTOR_ASSIGN_SKIP_VAR_SIDE_EFFECT = get("constructor.assign.skip.var.side.effect");
	static final String CONTROL_FLOW_SKIP_BRACE_LINE_CONTENT = get("control.flow.skip.brace.line.content");
	static final String CONTROL_FLOW_SKIP_CLOSE_BRACE = get("control.flow.skip.close.brace");
	static final String CONTROL_FLOW_SKIP_COMMENT_ONLY = get("control.flow.skip.comment.only");
	static final String CONTROL_FLOW_SKIP_DECLARATION_BODY = get("control.flow.skip.declaration.body");
	static final String CONTROL_FLOW_SKIP_EMPTY_BODY = get("control.flow.skip.empty.body");
	static final String CONTROL_FLOW_SKIP_MULTILINE_BRACED = get("control.flow.skip.multiline.braced");
	static final String CONTROL_FLOW_SKIP_MULTILINE_HEADER = get("control.flow.skip.multiline.header");
	static final String CONTROL_FLOW_SKIP_NESTED_DO = get("control.flow.skip.nested.do");
	static final String CONTROL_FLOW_SKIP_NO_BODY = get("control.flow.skip.no.body");
	static final String CONTROL_FLOW_SKIP_NO_KEYWORD = get("control.flow.skip.no.keyword");
	static final String CONTROL_FLOW_SKIP_NO_SEMICOLON = get("control.flow.skip.no.semicolon");
	static final String CONTROL_FLOW_SKIP_NO_TIER = get("control.flow.skip.no.tier");
	static final String CONTROL_FLOW_SKIP_STALE_POSITION = get("control.flow.skip.stale.position");
	static final String CONTROL_FLOW_SKIP_TEXT_BLOCK = get("control.flow.skip.text.block");
	static final String CONTROL_FLOW_SKIP_UNTERMINATED_LITERAL = get("control.flow.skip.unterminated.literal");
	static final String CONTROL_FLOW_SKIP_WHILE_LINE_BODY = get("control.flow.skip.while.line.body");
	static final String EXPLICIT_INIT_SKIP = get("explicit.init.skip");
	static final String FIELD_SORTING_SKIP_ANON_CLASS_REFERENCED_FIELD = get("field.sorting.skip.anon.class.referenced.field");
	static final String FIELD_SORTING_SKIP_C_STYLE_ARRAY = get("field.sorting.skip.c.style.array");
	static final String FIELD_SORTING_SKIP_DEPENDENCY_CYCLE = get("field.sorting.skip.dependency.cycle");
	static final String FIELD_SORTING_SKIP_ENUM_SPLIT_COMMENT = get("field.sorting.skip.enum.split.comment");
	static final String FIELD_SORTING_SKIP_ENUM_TRAILING_MULTILINE = get("field.sorting.skip.enum.trailing.multiline");
	static final String FIELD_SORTING_SKIP_INTERLEAVED_STATIC = get("field.sorting.skip.interleaved.static");
	static final String FIELD_SORTING_SKIP_MULTI_VAR_COMMENT = get("field.sorting.skip.multi.var.comment");
	static final String FIELD_SORTING_SKIP_MULTI_VAR_DEPENDENCY = get("field.sorting.skip.multi.var.dependency");
	static final String FIELD_SORTING_SKIP_MULTI_VAR_INITIALIZED = get("field.sorting.skip.multi.var.initialized");
	static final String FIELD_SORTING_SKIP_MULTI_VAR_INTERLEAVED = get("field.sorting.skip.multi.var.interleaved");
	static final String FIELD_SORTING_SKIP_STATIC_FIELD = get("field.sorting.skip.static.field");
	static final String FINAL_LOCAL_ALREADY_FINAL = get("final.local.skip.already.final");
	static final String FINAL_LOCAL_MULTI_VAR = get("final.local.skip.multi.var");
	static final String FINAL_LOCAL_NO_TYPE_LINE = get("final.local.skip.no.type.line");
	static final String FIX_BOUNDS = get("fix.bounds");
	static final String FIX_ERROR = get("fix.error");
	static final String FIX_NO_FIXER = get("fix.no.fixer");
	static final String FIX_NOT_FIXABLE = get("fix.not.fixable");
	static final String FIX_SEVERITY = get("fix.severity");
	static final String FIX_SUPPRESSED = get("fix.suppressed");
	static final String IMPORT_SKIP_NAME_COLLISION = get("prefer.import.skip.name.collision");
	static final String IMPORT_SKIP_NON_CONTIGUOUS = get("prefer.import.skip.non.contiguous");
	static final String IMPORT_SKIP_SHADOW = get("prefer.import.skip.shadow");
	static final String IMPORT_SKIP_UNPARSEABLE = get("prefer.import.skip.unparseable");
	static final String IMPORT_SKIP_UNRESOLVABLE = get("prefer.import.skip.unresolvable");
	static final String IMPORT_SKIP_WILDCARD = get("prefer.import.skip.wildcard");
	static final String IMPORT_SKIP_WILDCARD_AMBIGUITY = get("prefer.import.skip.wildcard.ambiguity");
	static final String LAMBDA_PARAM_SKIP = get("lambda.param.skip");
	static final String MATH_METHOD_SKIP = get("prefer.math.method.skip");
	static final String MATH_METHOD_SKIP_IF = get("prefer.math.method.skip.if");
	static final String MULTILINE_PUT_SKIP_COMMENT = get("multiline.put.skip.comment");
	static final String MULTILINE_PUT_SKIP_COMMENT_JOIN = get("multiline.put.skip.comment.join");
	static final String MULTILINE_PUT_SKIP_STALE = get("multiline.put.skip.stale");
	static final String MULTILINE_PUT_SKIP_UNSUPPORTED = get("multiline.put.skip.unsupported");
	static final String MULTILINE_TERNARY_SKIP_COMMENT_JOIN = get("multiline.ternary.skip.comment.join");
	static final String PREFER_API_SKIP = get("prefer.api.skip");
	static final String PREFER_ASSERT_SKIP = get("prefer.assert.skip");
	static final String PREFER_PREFIX_SKIP_MULTILINE_OPERAND = get("prefer.prefix.skip.multiline.operand");
	static final String PREFER_STANDARD_CHARSETS_SKIP = get("prefer.standard.charsets.skip");
	static final String PREFER_STANDARD_CHARSETS_SKIP_VARIABLE = get("prefer.standard.charsets.skip.variable");
	static final String PREFER_STATIC_IMPORT_CONSTANT_SKIP = get("prefer.static.import.constant.skip");
	static final String PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT = get("prefer.static.import.constant.skip.cinit");
	static final String PREFER_STATIC_IMPORT_CONSTANT_SKIP_CONFLICT = get("prefer.static.import.constant.skip.conflict");
	static final String PREFER_STATIC_IMPORT_CONSTANT_SKIP_MULTI_VAR = get("prefer.static.import.constant.skip.multi.var");
	static final String PREFER_STATIC_IMPORT_CONSTANT_SKIP_RENAME_TARGET = get("prefer.static.import.constant.skip.rename.target");
	static final String PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW = get("prefer.static.import.constant.skip.shadow");
	static final String PREFER_STATIC_IMPORT_CONSTANT_SKIP_VISIBILITY = get("prefer.static.import.constant.skip.visibility");
	static final String PREFER_STATIC_IMPORT_SKIP = get("prefer.static.import.skip");
	static final String PREFER_VAR_ALREADY_VAR = get("prefer.var.skip.already.var");
	static final String PREFER_VAR_SKIP = get("prefer.var.skip");
	static final String PREFER_VAR_SKIP_SPLIT_ARRAY_INITIALIZER = get("prefer.var.skip.split.array.initializer");
	static final String PREFER_VAR_SKIP_STALE_COLUMN = get("prefer.var.skip.stale.column");
	static final String PREFER_VAR_SKIP_UNREACHABLE_DIAMOND = get("prefer.var.skip.unreachable.diamond");
	static final String PREFER_VAR_SKIP_UNRECOGNIZED = get("prefer.var.skip.unrecognized");
	static final String PREFER_VAR_SKIP_WRAPPED_TYPE_ARGUMENTS = get("prefer.var.skip.wrapped.type.arguments");
	static final String REDUNDANT_EQUALITY_SKIP_COMMENT = get("redundant.equality.skip.comment");
	static final String REDUNDANT_MODIFIER_STALE_COLUMN = get("redundant.modifier.skip.stale.column");
	static final String UNUSED_IMPORTS_MALFORMED = get("unused.imports.skip.malformed");
	static final String UNUSED_IMPORTS_NOW_USED = get("unused.imports.skip.now.used");

	@Nonnull
	static String get(@Nonnull String key) {
		final var value = (String) PROPS.get(key);
		if (value == null)
			throw new IllegalStateException("Missing message for key: " + key);
		return value;
	}

	@Nonnull
	private static Map<Object, Object> loadProps() {
		final var props = new Properties();
		try (var in = SkipMessages.class.getResourceAsStream("/com/etk2000/checkstyle/messages.properties")) {
			if (in != null)
				props.load(in);
		}
		catch (IOException ignored) {
		}
		return props;
	}
}