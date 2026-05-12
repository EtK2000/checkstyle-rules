package com.etk2000.checkstyle.gradle.fix;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;

/**
 * Skip reason messages loaded from {@code messages.properties}.
 * Fixer-specific reasons use the check's message prefix with a {@code .skip}
 * suffix; pipeline-level reasons use a {@code fix.} prefix.
 */
final class SkipMessages {
	private static final Map<Object, Object> PROPS = loadProps();

	static final String ANNOTATION_SYNTAX_SKIP = get("annotation.syntax.skip");
	static final String COLLECTION_INTERFACE_SKIP = get("prefer.collection.interface.skip");
	static final String CONTROL_FLOW_SKIP = get("control.flow.skip");
	static final String EXPLICIT_INIT_SKIP = get("explicit.init.skip");
	static final String FIELD_SORT_SKIP = get("field.sort.skip");
	static final String FIX_BOUNDS = get("fix.bounds");
	static final String FIX_NO_FIXER = get("fix.no.fixer");
	static final String FIX_NOT_FIXABLE = get("fix.not.fixable");
	static final String FIX_SEVERITY = get("fix.severity");
	static final String FIX_SUPPRESSED = get("fix.suppressed");
	static final String LAMBDA_PARAM_SKIP = get("lambda.param.skip");
	static final String MATH_METHOD_SKIP = get("prefer.math.method.skip");
	static final String MATH_METHOD_SKIP_IF = get("prefer.math.method.skip.if");
	static final String PREFER_API_SKIP = get("prefer.api.skip");
	static final String PREFER_ASSERT_SKIP = get("prefer.assert.skip");
	static final String PREFER_BULK_SKIP = get("prefer.bulk.skip");
	static final String PREFER_STANDARD_CHARSETS_SKIP = get("prefer.standard.charsets.skip");
	static final String PREFER_STATIC_IMPORT_CONSTANT_SKIP = get("prefer.static.import.constant.skip");
	static final String PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT = get("prefer.static.import.constant.skip.cinit");
	static final String PREFER_STATIC_IMPORT_CONSTANT_SKIP_CONFLICT = get("prefer.static.import.constant.skip.conflict");
	static final String PREFER_STATIC_IMPORT_CONSTANT_SKIP_MULTI_VAR = get("prefer.static.import.constant.skip.multi.var");
	static final String PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW = get("prefer.static.import.constant.skip.shadow");
	static final String PREFER_STATIC_IMPORT_CONSTANT_SKIP_VISIBILITY = get("prefer.static.import.constant.skip.visibility");
	static final String PREFER_STATIC_IMPORT_SKIP = get("prefer.static.import.skip");
	static final String PREFER_VAR_SKIP = get("prefer.var.skip");

	@Nonnull
	private static String get(@Nonnull String key) {
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