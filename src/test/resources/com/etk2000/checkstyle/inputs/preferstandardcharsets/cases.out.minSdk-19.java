package com.etk2000.checkstyle.inputs.preferstandardcharsets;

// === case: anon_inherited_string_field ===
// imports: java.io.UnsupportedEncodingException
class InputStandardCharsetsAnonInheritedStringFieldSliceViolation {
	static class Base {
		static final String CHARSET_NAME = "UTF-8";
	}

	Base holder = new Base() {
		String decode(byte[] data) throws UnsupportedEncodingException {
			return new String(data, CHARSET_NAME);
		}
	};
}
// === end ===

// === case: anon_var_from_inherited_method ===
// imports: java.io.UnsupportedEncodingException
class InputStandardCharsetsAnonVarFromInheritedMethodSliceViolation {
	static class Base {
		String charsetName() {
			return "UTF-8";
		}
	}

	Base holder = new Base() {
		String decode(byte[] data) throws UnsupportedEncodingException {
			final var name = charsetName();
			return new String(data, name);
		}
	};
}
// === end ===

// === case: builtin_functions ===
// imports: java.io.ByteArrayInputStream
// imports: java.io.ByteArrayOutputStream
// imports: java.io.File
// imports: java.io.InputStreamReader
// imports: java.io.OutputStreamWriter
// imports: java.io.PrintStream
// imports: java.io.PrintWriter
// imports: java.net.URLDecoder
// imports: java.net.URLEncoder
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
// imports: java.util.Scanner
class InputStandardCharsetsBuiltinFunctionsSliceViolation {
	void m(byte[] data, File file) throws Exception {
		final var a = "hello".getBytes(StandardCharsets.UTF_8);
		final var b = new String(data, StandardCharsets.UTF_8);
		final var c = new String(data, 0, 1, StandardCharsets.UTF_8);
		final var d = new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8);
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), StandardCharsets.UTF_8);
		final var f = StandardCharsets.UTF_8;
		final var g = URLEncoder.encode("hello", "UTF-8");
		final var h = URLDecoder.decode("hello", "UTF-8");
		final var i = new ByteArrayOutputStream().toString("UTF-8");
		final var j = new PrintStream(file, "UTF-8");
		final var k = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
		final var l = new PrintWriter(file, "UTF-8");
		final var m = new Scanner(new ByteArrayInputStream(data), "UTF-8");
		final var n = new <String>String(data, StandardCharsets.UTF_8);
		final var o = new <String>ByteArrayOutputStream().toString("UTF-8");
	}
}
// === end ===

// === case: charset_api_33_file_name_and_channel_shapes ===
// imports: java.io.PrintStream
// imports: java.io.PrintWriter
// imports: java.nio.channels.Channels
// imports: java.nio.channels.ReadableByteChannel
// imports: java.nio.channels.WritableByteChannel
class InputStandardCharsetsCharsetApi33FileNameAndChannelShapesSliceViolation {
	void m(ReadableByteChannel in, WritableByteChannel out) throws Exception {
		final var a = new PrintStream("out.txt", "UTF-8");
		final var b = new PrintWriter("out.txt", "UTF-8");
		final var c = Channels.newReader(in, "UTF-8");
		final var d = Channels.newWriter(out, "UTF-8");
	}
}
// === end ===

// === case: charset_api_34_formatter_and_scanner_shapes ===
// imports: java.io.ByteArrayOutputStream
// imports: java.io.File
// imports: java.nio.channels.ReadableByteChannel
// imports: java.nio.file.Path
// imports: java.util.Formatter
// imports: java.util.Locale
// imports: java.util.Scanner
class InputStandardCharsetsCharsetApi34FormatterAndScannerShapesSliceViolation {
	void m(File file, ReadableByteChannel channel, Path path) throws Exception {
		final var a = new Formatter(file, "UTF-8", Locale.ROOT);
		final var b = new Formatter(new ByteArrayOutputStream(), "UTF-8", Locale.ROOT);
		final var c = new Formatter("out.txt", "UTF-8", Locale.ROOT);
		final var d = new Scanner(file, "UTF-8");
		final var e = new Scanner(channel, "UTF-8");
		final var f = new Scanner(path, "UTF-8");
	}
}
// === end ===

// === case: charset_api_35_store_to_xml ===
// imports: java.io.ByteArrayOutputStream
// imports: java.security.AuthProvider
// imports: java.security.Provider
// imports: java.util.Properties
class InputStandardCharsetsCharsetApi35StoreToXmlSliceViolation {
	void m(Properties props, java.util.Properties qualified, Provider provider, AuthProvider authProvider) throws Exception {
		final var out = new ByteArrayOutputStream();
		props.storeToXML(out, null, "UTF-8");
		qualified.storeToXML(out, null, "UTF-8");
		provider.storeToXML(out, null, "UTF-8");
		authProvider.storeToXML(out, null, "UTF-8");
		new Properties().storeToXML(out, null, "UTF-8");
	}
}
// === end ===

// === case: charset_api_gated_qualified_and_type_argument_calls ===
// imports: java.io.ByteArrayInputStream
// imports: java.io.File
// imports: java.io.PrintStream
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsCharsetApiGatedQualifiedAndTypeArgumentCallsSliceViolation {
	void m(byte[] data, File file) throws Exception {
		final var a = new <String>PrintStream(file, "UTF-8");
		final var b = new java.util.Scanner(new ByteArrayInputStream(data), "UTF-8");
		final var c = java.net.URLEncoder.encode("x", "UTF-8");
		final var d = new java.io.InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: enum_constant_string_field ===
// imports: java.io.UnsupportedEncodingException
enum InputStandardCharsetsEnumConstantStringFieldSliceViolation {
	A {
		@Override
		String decode(byte[] data) throws UnsupportedEncodingException {
			return new String(data, charsetName);
		}
	};

	final String charsetName = "UTF-8";

	String decode(byte[] data) throws UnsupportedEncodingException {
		return null;
	}
}
// === end ===

// === case: for_name_and_literal_on_one_line ===
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
// imports: java.util.List
class InputStandardCharsetsForNameAndLiteralOnOneLineSliceViolation {
	List<Object> m(byte[] data, Charset fallback) throws Exception {
		final var a = List.of(StandardCharsets.UTF_8, new String(data, StandardCharsets.US_ASCII));
		return List.of(new String(data, StandardCharsets.UTF_8), StandardCharsets.US_ASCII, a, fallback);
	}
}
// === end ===

// === case: for_name_argument_on_its_own_line ===
// imports: java.nio.charset.Charset
class InputStandardCharsetsForNameArgumentOnItsOwnLineSliceViolation {
	Charset m() {
		return Charset.forName(
				"UTF-8"
		);
	}
}
// === end ===

// === case: for_name_as_charset_argument ===
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsForNameAsCharsetArgumentSliceViolation {
	String m(byte[] data, Charset fallback) {
		return new String(data, StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: for_name_chained ===
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsForNameChainedSliceViolation {
	Charset m() {
		final var name = StandardCharsets.UTF_8.name();
		return Charset.forName(name);
	}
}
// === end ===

// === case: for_name_chained_statement ===
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsForNameChainedStatementSliceViolation {
	void m(Charset fallback) {
		StandardCharsets.UTF_8.aliases();
	}
}
// === end ===

// === case: for_name_comment_inside_call ===
// imports: java.nio.charset.Charset
class InputStandardCharsetsForNameCommentInsideCallSliceViolation {
	void m(Charset fallback) {
		final var a = Charset.forName(/* c */ "UTF-8");
		final var b = Charset.forName("UTF-8" /* c */);
		final var c = Charset./* c */forName("UTF-8");
		final var d = Charset /* c */.forName("UTF-8");
		final var e = Charset.forName /* c */("UTF-8");
		final var f = java.nio./* c */charset.Charset.forName("UTF-8");
	}
}
// === end ===

// === case: for_name_comment_like_sibling_literals ===
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
// imports: java.util.List
class InputStandardCharsetsForNameCommentLikeSiblingLiteralsSliceViolation {
	List<Object> m(Charset fallback) {
		return List.of("/*", StandardCharsets.UTF_8, "*/", fallback);
	}
}
// === end ===

// === case: for_name_comments_outside_call ===
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsForNameCommentsOutsideCallSliceViolation {
	void m(Charset fallback) {
		final var a = /* d */ StandardCharsets.UTF_8;
		final var b = StandardCharsets.UTF_8 /* c */.name();
		final var c = StandardCharsets.UTF_8; /* trailing */
	}
}
// === end ===

// === case: for_name_every_constant_and_alias ===
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsForNameEveryConstantAndAliasSliceViolation {
	void m(Charset fallback) {
		final var a = StandardCharsets.US_ASCII;
		final var b = StandardCharsets.ISO_8859_1;
		final var c = StandardCharsets.UTF_8;
		final var d = StandardCharsets.UTF_16BE;
		final var e = StandardCharsets.UTF_16LE;
		final var f = StandardCharsets.UTF_16;
		final var g = StandardCharsets.ISO_8859_1;
		final var h = StandardCharsets.UTF_8;
	}
}
// === end ===

// === case: for_name_field_initializer_and_lambda ===
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
// imports: java.util.function.Supplier
class InputStandardCharsetsForNameFieldInitializerAndLambdaSliceViolation {
	static final Charset DEFAULT = StandardCharsets.UTF_8;

	final Supplier<String> supplier = () -> StandardCharsets.US_ASCII.name();
}
// === end ===

// === case: for_name_field_named_for_name_and_method_named_charset ===
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
// imports: static java.nio.charset.Charset.forName
class InputStandardCharsetsForNameFieldNamedForNameAndMethodNamedCharsetSliceViolation {
	final Object forName = "field";

	Charset Charset() {
		return StandardCharsets.UTF_8;
	}

	Charset m() {
		return StandardCharsets.US_ASCII;
	}
}
// === end ===

// === case: for_name_fully_qualified_receiver ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsForNameFullyQualifiedReceiverSliceViolation {
	Object m() {
		return StandardCharsets.UTF_8;
	}
}
// === end ===

// === case: for_name_inside_each_type_kind ===
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsForNameInsideEachTypeKindSliceViolation {
	enum InEnum {
		A;

		Charset m() {
			return StandardCharsets.UTF_8;
		}
	}

	interface InInterface {
		default Charset m() {
			return StandardCharsets.UTF_8;
		}
	}

	record InRecord() {
		Charset m() {
			return StandardCharsets.UTF_8;
		}
	}

	@interface InAnnotation {
		Charset DEFAULT = StandardCharsets.UTF_8;
	}
}
// === end ===

// === case: for_name_local_class_declared_after_call ===
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsForNameLocalClassDeclaredAfterCallSliceViolation {
	Object m(Charset fallback) {
		final var charset = StandardCharsets.UTF_8;
		class Charset {
			Object name() {
				return "local";
			}
		}

		return charset.equals(fallback) ? charset : new Charset().name();
	}
}
// === end ===

// === case: for_name_nested ===
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsForNameNestedSliceViolation {
	Charset m() {
		return Charset.forName(StandardCharsets.UTF_8.name());
	}
}
// === end ===

// === case: for_name_paren_char_literals ===
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
// imports: java.util.List
class InputStandardCharsetsForNameParenCharLiteralsSliceViolation {
	List<Object> m(Charset fallback) {
		return List.of('(', StandardCharsets.UTF_8, ')', fallback);
	}
}
// === end ===

// === case: for_name_qualifier_split_across_lines ===
class InputStandardCharsetsForNameQualifierSplitAcrossLinesSliceViolation {
	Object m() {
		return java.nio.charset
				.Charset.forName("UTF-8");
	}
}
// === end ===

// === case: for_name_receiver_on_its_own_line ===
// imports: java.nio.charset.Charset
class InputStandardCharsetsForNameReceiverOnItsOwnLineSliceViolation {
	Charset m() {
		return Charset
				.forName("UTF-8");
	}
}
// === end ===

// === case: for_name_sibling_member_declares_charset ===
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsForNameSiblingMemberDeclaresCharsetSliceViolation {
	static class Holder {
		static class Charset {
		}
	}

	Charset m() {
		return StandardCharsets.UTF_8;
	}
}
// === end ===

// === case: for_name_statement_positions ===
// imports: java.nio.charset.Charset
// imports: java.util.function.Supplier
class InputStandardCharsetsForNameStatementPositionsSliceViolation {
	static {
		Charset.forName("UTF-8");
	}

	final Runnable block = () -> {
		Charset.forName("UTF-8");
	};
	final Runnable runnable = () -> Charset.forName("UTF-8");
	final Supplier<Charset> supplier = () -> Charset.forName("US-ASCII");

	void m(boolean flag, int value) {
		Charset.forName("UTF-8");
		if (flag)
			Charset.forName("UTF-8");
		else
			Charset.forName("US-ASCII");
		while (flag)
			Charset.forName("UTF-8");
		for (Charset.forName("UTF-8"); flag; Charset.forName("US-ASCII"))
			System.out.println(value);
		for (var i = 0; i < value; ++i)
			Charset.forName("UTF-8");
		for (var item : new int[]{value})
			Charset.forName("US-ASCII");
		do Charset.forName("UTF-8");
		while (flag);
		label: Charset.forName("UTF-8");
		switch (value) {
			case 1 -> Charset.forName("UTF-8");
			default -> System.out.println(value);
		}
		switch (value) {
			case 1:
				Charset.forName("UTF-8");
			case 2:
				System.out.println(value);
				break;

			default:
				System.out.println(value);
		}
	}
}
// === end ===

// === case: for_name_static_import ===
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
// imports: static java.nio.charset.Charset.forName
class InputStandardCharsetsForNameStaticImportSliceViolation {
	Charset m() {
		return StandardCharsets.UTF_8;
	}
}
// === end ===

// === case: for_name_two_on_one_line ===
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
// imports: java.util.List
class InputStandardCharsetsForNameTwoOnOneLineSliceViolation {
	List<Charset> m(Charset fallback) {
		return List.of(StandardCharsets.UTF_8, StandardCharsets.US_ASCII, fallback);
	}
}
// === end ===

// === case: for_name_utf_8 ===
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsForNameUtf8SliceViolation {
	Charset m() {
		return StandardCharsets.UTF_8;
	}
}
// === end ===

// === case: for_name_whitespace_inside_call ===
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsForNameWhitespaceInsideCallSliceViolation {
	void m(Charset fallback) {
		final var a = StandardCharsets.UTF_8;
		final var b = StandardCharsets.UTF_8;
		final var c = StandardCharsets.UTF_8;
	}
}
// === end ===

// === case: fq_string_local ===
// imports: java.net.URLEncoder
class InputStandardCharsetsFqStringLocalSliceViolation {
	void m(byte[] data) throws Exception {
		final var encoding = "UTF-8";
		final var a = "hello".getBytes(encoding);
		final var b = new String(data, encoding);
		final var c = URLEncoder.encode("hello", encoding);
	}
}
// === end ===

// === case: fq_string_parameter ===
// imports: java.net.URLDecoder
class InputStandardCharsetsFqStringParameterSliceViolation {
	void m(byte[] data, java.lang.String encoding) throws Exception {
		final var a = "hello".getBytes(encoding);
		final var b = new String(data, encoding);
		final var c = URLDecoder.decode("hello", encoding);
	}
}
// === end ===

// === case: iso_8859_1 ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsIso88591SliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.ISO_8859_1);
	}
}
// === end ===

// === case: iso_8859_1_latin1 ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsIso88591Latin1SliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.ISO_8859_1);
	}
}
// === end ===

// === case: literal_cast_and_ternary_receivers ===
// imports: java.io.ByteArrayOutputStream
class InputStandardCharsetsLiteralCastAndTernaryReceiversSliceViolation {
	void m(Object stream, boolean flag, String first, String second) throws Exception {
		final var a = ((ByteArrayOutputStream) stream).toString("UTF-8");
		final var b = (flag ? first : second).getBytes("UTF-8");
	}
}
// === end ===

// === case: literal_charset_overload_widens_checked_exceptions ===
// imports: java.io.File
// imports: java.io.FileNotFoundException
// imports: java.io.PrintStream
// imports: java.io.PrintWriter
// imports: java.io.UnsupportedEncodingException
// imports: java.util.Formatter
// imports: java.util.Locale
// imports: java.util.Scanner
class InputStandardCharsetsLiteralCharsetOverloadWidensCheckedExceptionsSliceViolation {
	Object fileFormatter(File file) throws FileNotFoundException, UnsupportedEncodingException {
		return new Formatter(file, "UTF-8", Locale.ROOT);
	}

	Object filePrintStream(File file) throws FileNotFoundException, UnsupportedEncodingException {
		return new PrintStream(file, "UTF-8");
	}

	Object filePrintWriter(File file) throws FileNotFoundException, UnsupportedEncodingException {
		return new PrintWriter(file, "UTF-8");
	}

	Object fileScanner(File file) {
		try {
			return new Scanner(file, "UTF-8");
		}
		catch (FileNotFoundException e) {
			return null;
		}
	}

	Object nameFormatter() throws FileNotFoundException, UnsupportedEncodingException {
		return new Formatter("out.txt", "UTF-8", Locale.ROOT);
	}

	Object namePrintStream() throws FileNotFoundException, UnsupportedEncodingException {
		return new PrintStream("out.txt", "UTF-8");
	}

	Object namePrintWriter() throws FileNotFoundException, UnsupportedEncodingException {
		return new PrintWriter("out.txt", "UTF-8");
	}
}
// === end ===

// === case: literal_for_name_charset_classpath_supertype ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathCharsetHolder
// imports: java.nio.charset.Charset
class InputStandardCharsetsLiteralForNameCharsetClasspathSupertypeSliceViolation {
	static class Sub extends ClasspathCharsetHolder {
		Object lookup() {
			return Charset.forName("UTF-8");
		}
	}
}
// === end ===

// === case: literal_for_name_charset_enum_constant ===
// imports: java.nio.charset.Charset
class InputStandardCharsetsLiteralForNameCharsetEnumConstantSliceViolation {
	enum Lookup {
		Charset;

		Object forName(String name) {
			return name;
		}

		Object lookup() {
			return Charset.forName("UTF-8");
		}
	}
}
// === end ===

// === case: literal_for_name_charset_pattern_flow_scope ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathForName
// imports: java.nio.charset.Charset
class InputStandardCharsetsLiteralForNameCharsetPatternFlowScopeSliceViolation {
	Object lookup(Object o) {
		if (!(o instanceof ClasspathForName Charset))
			return null;
		return Charset.forName("UTF-8");
	}
}
// === end ===

// === case: literal_for_name_inherited_from_same_file_superclass ===
// imports: java.nio.charset.Charset
// imports: static java.nio.charset.Charset.forName
class InputStandardCharsetsLiteralForNameInheritedFromSameFileSuperclassSliceViolation {
	static class Base {
		static class Charset {
			static Object forName(String name) {
				return name;
			}
		}

		static class StandardCharsets {
			static final Object UTF_8 = "shim";
		}

		static Object forName(String name) {
			return name;
		}
	}

	static class Middle extends Base {
	}

	static class Derived extends Middle {
		Object bareForName() {
			return forName("UTF-8");
		}

		Object memberType() {
			return Charset.forName("UTF-8");
		}

		byte[] standardCharsetsMember(String s) throws Exception {
			return s.getBytes("UTF-8");
		}
	}
}
// === end ===

// === case: literal_for_name_near_misses ===
// imports: java.nio.charset.Charset
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsLiteralForNameNearMissesSliceViolation {
	void m(Charset fallback) {
		final var a = Charset.forName("UTF-8", StandardCharsets.US_ASCII);
		final var b = fallback.forName("UTF-8");
		final var c = Charset.<String>forName("UTF-8");
	}
}
// === end ===

// === case: literal_for_name_qualified_by_enclosing_class ===
// imports: java.nio.charset.Charset
class InputStandardCharsetsLiteralForNameQualifiedByEnclosingClassSliceViolation {
	static class Charset {
		static Object forName(String name) {
			return name;
		}
	}

	Object m() {
		return InputStandardCharsetsLiteralForNameQualifiedByEnclosingClassSliceViolation.Charset.forName("UTF-8");
	}

	Object n() {
		return java.nio.charset.Charset.defaultCharset().forName("UTF-8");
	}
}
// === end ===

// === case: literal_for_name_shadowed_by_local_class ===
// imports: java.nio.charset.Charset
class InputStandardCharsetsLiteralForNameShadowedByLocalClassSliceViolation {
	Object declaredBefore() {
		class Charset {
			static Object forName(String name) {
				return name;
			}
		}

		return Charset.forName("UTF-8");
	}

	Object insideIt() {
		class Charset {
			static Object forName(String name) {
				return name;
			}

			Object get() {
				return Charset.forName("UTF-8");
			}
		}

		return new Charset().get();
	}
}
// === end ===

// === case: literal_for_name_shadowed_by_local_class_in_outer_block ===
// imports: java.nio.charset.Charset
class InputStandardCharsetsLiteralForNameShadowedByLocalClassInOuterBlockSliceViolation {
	Object m(boolean flag) {
		class Charset {
			static Object forName(String name) {
				return name;
			}
		}

		if (flag) {
			System.out.println(flag);
			return Charset.forName("UTF-8");
		}
		return new Charset();
	}
}
// === end ===

// === case: literal_for_name_shadowed_by_member_type_kinds ===
// imports: java.nio.charset.Charset
class InputStandardCharsetsLiteralForNameShadowedByMemberTypeKindsSliceViolation {
	static class AnnotationMember {
		@interface Charset {}

		Object m() {
			return Charset.forName("UTF-8");
		}
	}

	static class EnumMember {
		enum Charset {
			A
		}

		Object m() {
			return Charset.forName("UTF-8");
		}
	}

	static class InterfaceMember {
		interface Charset {}

		Object m() {
			return Charset.forName("UTF-8");
		}
	}

	static class RecordMember {
		record Charset() {}

		Object m() {
			return Charset.forName("UTF-8");
		}
	}
}
// === end ===

// === case: literal_for_name_shadowed_from_outer_class ===
// imports: java.nio.charset.Charset
class InputStandardCharsetsLiteralForNameShadowedFromOuterClassSliceViolation {
	static class Charset {
		static Object forName(String name) {
			return name;
		}
	}

	static class Inner {
		Object m() {
			return Charset.forName("UTF-8");
		}
	}
}
// === end ===

// === case: literal_for_name_shadowed_in_anonymous_class_body ===
// imports: java.nio.charset.Charset
// imports: static java.nio.charset.Charset.forName
class InputStandardCharsetsLiteralForNameShadowedInAnonymousClassBodySliceViolation {
	final Runnable runnable = new Runnable() {
		Charset forName(String name) {
			return Charset.defaultCharset();
		}

		@Override
		public void run() {
			System.out.println(forName("UTF-8"));
		}
	};
}
// === end ===

// === case: literal_for_name_shadowed_in_enum_constant_body ===
// imports: java.nio.charset.Charset
enum InputStandardCharsetsLiteralForNameShadowedInEnumConstantBodySliceViolation {
	A {
		static class Charset {
			static Object forName(String name) {
				return name;
			}
		}

		@Override
		Object charset() {
			return Charset.forName("UTF-8");
		}
	};

	abstract Object charset();
}
// === end ===

// === case: literal_for_name_static_import_shadowed_by_method ===
// imports: java.nio.charset.Charset
// imports: static java.nio.charset.Charset.forName
class InputStandardCharsetsLiteralForNameStaticImportShadowedByMethodSliceViolation {
	static Charset forName(String name) {
		return Charset.defaultCharset();
	}

	Charset m() {
		return forName("UTF-8");
	}
}
// === end ===

// === case: literal_for_name_user_charset_class ===
// imports: java.nio.charset.Charset
class InputStandardCharsetsLiteralForNameUserCharsetClassSliceViolation {
	static class Charset {
		static Charset forName(String name) {
			return new Charset();
		}
	}

	Charset m() {
		return Charset.forName("UTF-8");
	}
}
// === end ===

// === case: literal_for_name_variable_named_charset ===
// imports: java.nio.charset.Charset
class InputStandardCharsetsLiteralForNameVariableNamedCharsetSliceViolation {
	Charset m(Charset Charset) {
		return Charset.forName("UTF-8");
	}
}
// === end ===

// === case: literal_names_taken_by_fields_and_locals ===
// imports: java.nio.charset.Charset
// imports: java.util.List
class InputStandardCharsetsLiteralNamesTakenByFieldsAndLocalsSliceViolation {
	static class CharsetField {
		Charset Charset;

		Object m() {
			return Charset.forName("UTF-8");
		}
	}

	static class StandardCharsetsField {
		final Object StandardCharsets = "field";

		byte[] m(String s) throws Exception {
			return s.getBytes("UTF-8");
		}
	}

	Object charsetVarLocal(List<Charset> charsets) {
		final var Charset = charsets.getLast();
		return Charset.forName("UTF-8");
	}

	byte[] standardCharsetsLocal(String s) throws Exception {
		final Object StandardCharsets = "local";
		return s.getBytes("UTF-8");
	}

	byte[] standardCharsetsVarLocal(String s, List<Object> values) throws Exception {
		final var StandardCharsets = values.getLast();
		return s.getBytes("UTF-8");
	}
}
// === end ===

// === case: literal_plain_string_parameters ===
// imports: java.io.File
// imports: java.nio.charset.Charset
// imports: java.util.Formatter
class InputStandardCharsetsLiteralPlainStringParametersSliceViolation {
	void consume(String name) {
	}

	void m(File file, String name) throws Exception {
		final var a = Charset.isSupported("UTF-8");
		final var b = new IllegalArgumentException("UTF-8");
		consume("UTF-8");
		System.out.println("UTF-8");
		final var c = name.equals("UTF-8");
		final var d = new Formatter(file, "UTF-8");
		final var e = name.startsWith("UTF-8");
		final var f = Charset.isSupported("latin1");
		final var g = new <String>StringBuilder("UTF-8");
	}
}
// === end ===

// === case: literal_standard_charsets_classpath_supertype ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathStandardCharsetsHolder
class InputStandardCharsetsLiteralStandardCharsetsClasspathSupertypeSliceViolation {
	static class Leaf extends Middle {
		byte[] encode(String s) throws Exception {
			return s.getBytes("UTF-8");
		}
	}

	static class Middle extends ClasspathStandardCharsetsHolder {}

	static class Sub extends ClasspathStandardCharsetsHolder {
		class Inner {
			byte[] encode(String s) throws Exception {
				return s.getBytes("UTF-8");
			}
		}

		byte[] encode(String s) throws Exception {
			return s.getBytes("UTF-8");
		}
	}

	Object anonymous() {
		return new ClasspathStandardCharsetsHolder() {
			byte[] encode(String s) throws Exception {
				return s.getBytes("UTF-8");
			}
		};
	}
}
// === end ===

// === case: literal_standard_charsets_enum_constant ===
class InputStandardCharsetsLiteralStandardCharsetsEnumConstantSliceViolation {
	enum Names {
		StandardCharsets,
		WithBody {
			@Override
			byte[] encode(String s) throws Exception {
				return s.getBytes("UTF-8");
			}
		};

		byte[] encode(String s) throws Exception {
			return s.getBytes("UTF-8");
		}
	}
}
// === end ===

// === case: literal_standard_charsets_name_taken ===
// imports: java.nio.charset.Charset
class InputStandardCharsetsLiteralStandardCharsetsNameTakenSliceViolation {
	static final class StandardCharsets {
		static final Charset UTF_8 = Charset.forName("UTF-8");
	}

	byte[] m(String s) throws Exception {
		return s.getBytes("UTF-8");
	}
}
// === end ===

// === case: literal_standard_charsets_patterns ===
class InputStandardCharsetsLiteralStandardCharsetsPatternsSliceViolation {
	record Box(Object value) {}

	byte[] conservativeElseOfPositive(Object o, String s) throws Exception {
		if (o instanceof String StandardCharsets)
			System.out.println(StandardCharsets);
		else
			return s.getBytes("UTF-8");
		return new byte[0];
	}

	byte[] conservativeOtherCase(Object o, String s) throws Exception {
		return switch (o) {
			case String StandardCharsets -> new byte[0];
			default -> s.getBytes("UTF-8");
		};
	}

	byte[] flowScope(Object o, String s) throws Exception {
		if (!(o instanceof String StandardCharsets))
			return new byte[0];
		return s.getBytes("UTF-8");
	}

	byte[] recordPatternInIf(Object o, String s) throws Exception {
		if (o instanceof Box(Object StandardCharsets))
			return s.getBytes("UTF-8");
		return new byte[0];
	}

	byte[] switchGuard(Object o, String s) throws Exception {
		return switch (o) {
			case String StandardCharsets when s.isEmpty() -> s.getBytes("UTF-8");
			default -> new byte[0];
		};
	}

	byte[] switchRecordPattern(Object o, String s) throws Exception {
		return switch (o) {
			case Box(Object StandardCharsets) -> s.getBytes("UTF-8");
			default -> new byte[0];
		};
	}

	byte[] switchTypePattern(Object o, String s) throws Exception {
		return switch (o) {
			case String StandardCharsets -> s.getBytes("UTF-8");
			default -> new byte[0];
		};
	}

	byte[] ternary(Object o, String s) throws Exception {
		return o instanceof String StandardCharsets ? s.getBytes("UTF-8") : new byte[0];
	}
}
// === end ===

// === case: literal_standard_charsets_qualified_classpath_supertype ===
class InputStandardCharsetsLiteralStandardCharsetsQualifiedClasspathSupertypeSliceViolation {
	static class Qualified extends com.etk2000.checkstyle.testhelpers.ClasspathStandardCharsetsHolder {
		byte[] encode(String s) throws Exception {
			return s.getBytes("UTF-8");
		}
	}
}
// === end ===

// === case: literal_standard_charsets_supertype_through_wildcard_import ===
// imports: com.etk2000.checkstyle.testhelpers.*
class InputStandardCharsetsLiteralStandardCharsetsSupertypeThroughWildcardImportSliceViolation {
	static class Sub extends ClasspathStandardCharsetsHolder {
		byte[] encode(String s) throws Exception {
			return s.getBytes("UTF-8");
		}
	}
}
// === end ===

// === case: literal_standard_charsets_type_parameter ===
class InputStandardCharsetsLiteralStandardCharsetsTypeParameterSliceViolation {
	static class Generic<StandardCharsets> {
		byte[] m(String s) throws Exception {
			return s.getBytes("UTF-8");
		}
	}

	<StandardCharsets> byte[] m(String s) throws Exception {
		return s.getBytes("UTF-8");
	}
}
// === end ===

// === case: literal_standard_charsets_variable_in_scope ===
class InputStandardCharsetsLiteralStandardCharsetsVariableInScopeSliceViolation {
	byte[] m(String s, Object StandardCharsets) throws Exception {
		return s.getBytes("UTF-8");
	}
}
// === end ===

// === case: literal_unresolvable_receivers ===
// imports: java.io.ByteArrayOutputStream
// imports: java.util.Properties
// imports: static java.net.URLEncoder.encode
class InputStandardCharsetsLiteralUnresolvableReceiversSliceViolation {
	static class MyProperties extends Properties {
	}

	void m(UnknownType unknown) throws Exception {
		final var a = encode("x", "UTF-8");
		new MyProperties().storeToXML(new ByteArrayOutputStream(), null, "UTF-8");
		final var b = new Thread("UTF-8");
		unknown.call("UTF-8");
	}
}
// === end ===

// === case: other_charsets ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsOtherCharsetsSliceViolation {
	void m() throws Exception {
		final var a = "test".getBytes(StandardCharsets.ISO_8859_1);
		final var b = "test".getBytes(StandardCharsets.ISO_8859_1);
		final var c = "test".getBytes(StandardCharsets.US_ASCII);
		final var d = "test".getBytes(StandardCharsets.US_ASCII);
		final var e = "test".getBytes(StandardCharsets.UTF_16);
		final var f = "test".getBytes(StandardCharsets.UTF_16BE);
		final var g = "test".getBytes(StandardCharsets.UTF_16LE);
	}
}
// === end ===

// === case: reflected_call_other_args_scanned ===
// imports: java.io.PrintStream
// imports: java.net.URLEncoder
class InputStandardCharsetsReflectedCallOtherArgsScannedSliceViolation {
	void m(String encoding) throws Exception {
		final var a = URLEncoder.encode("UTF-8", "UTF-8");
		final var b = new PrintStream("UTF-8", "UTF-8");
		final var c = URLEncoder.encode(encoding, "UTF-8");
	}
}
// === end ===

// === case: resource_receiver_charset_literal ===
// imports: java.io.ByteArrayOutputStream
class InputStandardCharsetsResourceReceiverCharsetLiteralSliceViolation {
	void m() throws Exception {
		try (var out = new ByteArrayOutputStream()) {
			final var s = out.toString("UTF-8");
		}
	}
}
// === end ===

// === case: resource_receiver_string_charset ===
// skip-reason: cannot replace the charset name: the violation names a String variable, not a charset literal
// imports: java.io.ByteArrayOutputStream
class InputStandardCharsetsResourceReceiverStringCharsetSliceViolation {
	void m(String encoding) throws Exception {
		try (var out = new ByteArrayOutputStream()) {
			final var s = out.toString(encoding);
		}
	}
}
// === end ===

// === case: specific_caught_by_an_encoding_catch ===
// imports: java.io.IOException
// imports: java.io.InputStream
// imports: java.io.InputStreamReader
// imports: java.io.UnsupportedEncodingException
class InputStandardCharsetsSpecificCaughtByAnEncodingCatchSliceViolation {
	Object fullyQualified(byte[] data) {
		try {
			return new String(data, "UTF-8");
		}
		catch (java.io.UnsupportedEncodingException e) {
			return null;
		}
	}

	Object innerCatchBody(byte[] data) {
		try {
			try {
				return data.clone();
			}
			catch (IllegalStateException e) {
				return new String(data, "UTF-8");
			}
		}
		catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	Object innerFinallyBody(byte[] data) {
		try {
			try {
				System.out.println(data.length);
			}
			finally {
				System.out.println(new String(data, "UTF-8"));
			}
			return null;
		}
		catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	Object ioException(InputStream in) {
		try {
			return new InputStreamReader(in, "UTF-8");
		}
		catch (IOException e) {
			return null;
		}
	}

	Object multiCatch(byte[] data) {
		try {
			return new String(data, "UTF-8");
		}
		catch (IllegalStateException | UnsupportedEncodingException e) {
			return null;
		}
	}

	Object multiCatchFirst(byte[] data) {
		try {
			return new String(data, "UTF-8");
		}
		catch (UnsupportedEncodingException | IllegalStateException e) {
			return null;
		}
	}

	Object multiCatchOfThree(byte[] data) {
		try {
			return new String(data, "UTF-8");
		}
		catch (UnsupportedEncodingException | IllegalStateException | IllegalArgumentException e) {
			return null;
		}
	}

	Object multiCatchQualified(byte[] data) {
		try {
			return new String(data, "UTF-8");
		}
		catch (IllegalStateException | java.io.UnsupportedEncodingException e) {
			return null;
		}
	}

	Object nestedTry(byte[] data) {
		try {
			try {
				return new String(data, "UTF-8");
			}
			finally {
				System.out.println(data.length);
			}
		}
		catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	Object resource(InputStream in) {
		try (var reader = new InputStreamReader(in, "UTF-8")) {
			return reader.read();
		}
		catch (IOException e) {
			return null;
		}
	}

	Object secondCatch(byte[] data) {
		try {
			return new String(data, "UTF-8");
		}
		catch (IllegalStateException e) {
			return null;
		}
		catch (UnsupportedEncodingException e) {
			return data;
		}
	}

	Object simple(byte[] data) {
		try {
			return new String(data, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			return null;
		}
	}
}
// === end ===

// === case: specific_literal_on_continuation_line ===
// imports: java.io.ByteArrayInputStream
// imports: java.io.InputStreamReader
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsSpecificLiteralOnContinuationLineSliceViolation {
	void m(byte[] payloadBytesReadFromTheIncomingConnection) throws Exception {
		final var reader = new InputStreamReader(
				new ByteArrayInputStream(payloadBytesReadFromTheIncomingConnection, 0, payloadBytesReadFromTheIncomingConnection.length),
				StandardCharsets.UTF_8
		);
	}
}
// === end ===

// === case: specific_not_caught_by_an_encoding_catch ===
// imports: java.io.UnsupportedEncodingException
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsSpecificNotCaughtByAnEncodingCatchSliceViolation {
	Object broadCatch(byte[] data) {
		try {
			return new String(data, StandardCharsets.UTF_8);
		}
		catch (Exception e) {
			return null;
		}
	}

	Object inCatchBody(byte[] data) throws Exception {
		try {
			return data.clone();
		}
		catch (UnsupportedEncodingException e) {
			return new String(data, StandardCharsets.UTF_8);
		}
	}
}
// === end ===

// === case: specific_quote_in_trailing_comment ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsSpecificQuoteInTrailingCommentSliceViolation {
	byte[] m(String s) throws Exception {
		return s.getBytes(StandardCharsets.UTF_8 /* "US-ASCII" */);
	}
}
// === end ===

// === case: specific_scanner_path_keeps_io_exception ===
// imports: java.io.IOException
// imports: java.nio.file.Path
// imports: java.util.Scanner
class InputStandardCharsetsSpecificScannerPathKeepsIoExceptionSliceViolation {
	Object m(Path path) throws IOException {
		return new Scanner(path, "UTF-8");
	}
}
// === end ===

// === case: specific_two_on_one_line_and_chained ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsSpecificTwoOnOneLineAndChainedSliceViolation {
	int m(byte[] data) throws Exception {
		final var a = new String(data, StandardCharsets.UTF_8).getBytes(StandardCharsets.US_ASCII);
		return "a".getBytes(StandardCharsets.UTF_8).length + "b".getBytes(StandardCharsets.US_ASCII).length + a.length;
	}
}
// === end ===

// === case: string_field ===
// imports: java.io.ByteArrayInputStream
// imports: java.io.ByteArrayOutputStream
// imports: java.io.File
// imports: java.io.InputStreamReader
// imports: java.io.OutputStreamWriter
// imports: java.io.PrintStream
// imports: java.io.PrintWriter
// imports: java.net.URLDecoder
// imports: java.net.URLEncoder
// imports: java.util.Scanner
class InputStandardCharsetsStringFieldSliceViolation {
	static final String CHARSET_NAME = "UTF-8";

	void m(byte[] data, File file) throws Exception {
		final var a = "hello".getBytes(CHARSET_NAME);
		final var b = new String(data, CHARSET_NAME);
		final var c = new String(data, 0, 1, CHARSET_NAME);
		final var d = new InputStreamReader(new ByteArrayInputStream(data), CHARSET_NAME);
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), CHARSET_NAME);
		final var f = URLEncoder.encode("hello", CHARSET_NAME);
		final var g = URLDecoder.decode("hello", CHARSET_NAME);
		final var h = new ByteArrayOutputStream().toString(CHARSET_NAME);
		final var i = new PrintStream(file, CHARSET_NAME);
		final var j = new PrintStream(new ByteArrayOutputStream(), true, CHARSET_NAME);
		final var k = new PrintWriter(file, CHARSET_NAME);
		final var l = new Scanner(new ByteArrayInputStream(data), CHARSET_NAME);
	}
}
// === end ===

// === case: string_local ===
// imports: java.io.ByteArrayInputStream
// imports: java.io.ByteArrayOutputStream
// imports: java.io.File
// imports: java.io.InputStreamReader
// imports: java.io.OutputStreamWriter
// imports: java.io.PrintStream
// imports: java.io.PrintWriter
// imports: java.net.URLDecoder
// imports: java.net.URLEncoder
// imports: java.util.Scanner
class InputStandardCharsetsStringLocalSliceViolation {
	void m(byte[] data, File file) throws Exception {
		final var encoding = "UTF-8";
		final var a = "hello".getBytes(encoding);
		final var b = new String(data, encoding);
		final var c = new String(data, 0, 1, encoding);
		final var d = new InputStreamReader(new ByteArrayInputStream(data), encoding);
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), encoding);
		final var f = URLEncoder.encode("hello", encoding);
		final var g = URLDecoder.decode("hello", encoding);
		final var h = new ByteArrayOutputStream().toString(encoding);
		final var i = new PrintStream(file, encoding);
		final var j = new PrintStream(new ByteArrayOutputStream(), true, encoding);
		final var k = new PrintWriter(file, encoding);
		final var l = new Scanner(new ByteArrayInputStream(data), encoding);
	}
}
// === end ===

// === case: string_parameter ===
// imports: java.io.ByteArrayInputStream
// imports: java.io.ByteArrayOutputStream
// imports: java.io.File
// imports: java.io.InputStreamReader
// imports: java.io.OutputStreamWriter
// imports: java.io.PrintStream
// imports: java.io.PrintWriter
// imports: java.net.URLDecoder
// imports: java.net.URLEncoder
// imports: java.util.Scanner
class InputStandardCharsetsStringParameterSliceViolation {
	void m(byte[] data, String encoding, File file) throws Exception {
		final var a = "hello".getBytes(encoding);
		final var b = new String(data, encoding);
		final var c = new String(data, 0, 1, encoding);
		final var d = new InputStreamReader(new ByteArrayInputStream(data), encoding);
		final var e = new OutputStreamWriter(new ByteArrayOutputStream(), encoding);
		final var f = URLEncoder.encode("hello", encoding);
		final var g = URLDecoder.decode("hello", encoding);
		final var h = new ByteArrayOutputStream().toString(encoding);
		final var i = new PrintStream(file, encoding);
		final var j = new PrintStream(new ByteArrayOutputStream(), true, encoding);
		final var k = new PrintWriter(file, encoding);
		final var l = new Scanner(new ByteArrayInputStream(data), encoding);
	}
}
// === end ===

// === case: string_variable_new_api_shapes ===
// imports: java.io.ByteArrayOutputStream
// imports: java.io.File
// imports: java.io.PrintStream
// imports: java.nio.channels.Channels
// imports: java.nio.channels.ReadableByteChannel
// imports: java.nio.file.Path
// imports: java.util.Formatter
// imports: java.util.Locale
// imports: java.util.Properties
// imports: java.util.Scanner
class InputStandardCharsetsStringVariableNewApiShapesSliceViolation {
	void m(String encoding, File file, ReadableByteChannel in, Path path, Properties props) throws Exception {
		final var a = new PrintStream("out.txt", encoding);
		final var b = Channels.newReader(in, encoding);
		final var c = new Formatter(file, encoding, Locale.ROOT);
		final var d = new Scanner(path, encoding);
		props.storeToXML(new ByteArrayOutputStream(), null, encoding);
	}
}
// === end ===

// === case: supplementary_char_before_literal ===
class InputStandardCharsetsSupplementaryCharBeforeLiteralSliceViolation {
	void m(String s) throws Exception {
		final var bytes = ("𠀀" + s).getBytes("UTF-8");
	}
}
// === end ===

// === case: supplementary_char_in_string_receiver ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsSupplementaryCharInStringReceiverSliceViolation {
	byte[] m() throws Exception {
		return "𠀀".getBytes(StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: us_ascii ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUsAsciiSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.US_ASCII);
	}
}
// === end ===

// === case: us_ascii_short ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUsAsciiShortSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.US_ASCII);
	}
}
// === end ===

// === case: utf8_alias_canonical ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8CanonicalSliceViolation {
	void m() throws Exception {
		final var a = "hello".getBytes(StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: utf8_alias_capitalized ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8CapitalizedSliceViolation {
	void m(byte[] data) throws Exception {
		final var a = new String(data, StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: utf8_alias_lowercase_hyphen ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8LowercaseHyphenSliceViolation {
	void m(byte[] data) throws Exception {
		final var a = new String(data, StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: utf8_alias_lowercase_no_hyphen ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8LowercaseNoHyphenSliceViolation {
	void m(byte[] data) throws Exception {
		final var a = new String(data, StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: utf8_alias_uppercase_no_hyphen ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8UppercaseNoHyphenSliceViolation {
	void m() throws Exception {
		final var a = "test".getBytes(StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: utf_16 ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf16SliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.UTF_16);
	}
}
// === end ===

// === case: utf_16_be ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf16BeSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.UTF_16BE);
	}
}
// === end ===

// === case: utf_16_le ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf16LeSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.UTF_16LE);
	}
}
// === end ===

// === case: utf_8 ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8SliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: utf_8_alt ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8AltSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: utf_8_alt_lc ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8AltLcSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: utf_8_cap ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8CapSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: utf_8_lc ===
// imports: java.nio.charset.StandardCharsets
class InputStandardCharsetsUtf8LcSliceViolation {
	void m(String s) throws Exception {
		final var bytes = s.getBytes(StandardCharsets.UTF_8);
	}
}
// === end ===