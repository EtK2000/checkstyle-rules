// === case: fq_string_parameter ===
// skip-reason: the violation names a String variable, not a charset literal
// imports: java.net.URLDecoder
class InputStandardCharsetsFqStringParameterSliceViolation {
	void m(byte[] data, String encoding) throws Exception {
		final var a = "hello".getBytes(encoding);
		final var b = new String(data, encoding);
		final var c = URLDecoder.decode("hello", encoding);
	}
}
// === end ===