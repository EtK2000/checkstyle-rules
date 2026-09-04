// `PreferImportCheck` imports the fully qualified `java.util.Properties` parameter type.
// === case: charset_api_35_store_to_xml ===
// imports: java.io.ByteArrayOutputStream
// imports: java.nio.charset.StandardCharsets
// imports: java.security.AuthProvider
// imports: java.security.Provider
// imports: java.util.Properties
class InputStandardCharsetsCharsetApi35StoreToXmlSliceViolation {
	void m(Properties props, Properties qualified, Provider provider, AuthProvider authProvider) throws Exception {
		final var out = new ByteArrayOutputStream();
		props.storeToXML(out, null, StandardCharsets.UTF_8);
		qualified.storeToXML(out, null, StandardCharsets.UTF_8);
		provider.storeToXML(out, null, StandardCharsets.UTF_8);
		authProvider.storeToXML(out, null, StandardCharsets.UTF_8);
		new Properties().storeToXML(out, null, StandardCharsets.UTF_8);
	}
}
// === end ===

// === case: fq_string_parameter ===
// skip-reason: cannot replace the charset name: the violation names a String variable, not a charset literal
// imports: java.net.URLDecoder
class InputStandardCharsetsFqStringParameterSliceViolation {
	void m(byte[] data, String encoding) throws Exception {
		final var a = "hello".getBytes(encoding);
		final var b = new String(data, encoding);
		final var c = URLDecoder.decode("hello", encoding);
	}
}
// === end ===

// `PreferImportCheck` shortens the fully qualified catch type the file already imports.
// === case: specific_caught_by_an_encoding_catch ===
// skip-reason: cannot replace the charset name: a catch of UnsupportedEncodingException or IOException around it would stop compiling
// imports: java.io.IOException
// imports: java.io.InputStream
// imports: java.io.InputStreamReader
// imports: java.io.UnsupportedEncodingException
class InputStandardCharsetsSpecificCaughtByAnEncodingCatchSliceViolation {
	Object fullyQualified(byte[] data) {
		try {
			return new String(data, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
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