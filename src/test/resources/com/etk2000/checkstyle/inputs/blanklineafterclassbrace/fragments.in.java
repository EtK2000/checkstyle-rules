// === case: delete_blank_after_brace_below_text_block ===
// target: line=5 col=0
class T {
	String s = """
		x
		""";
}
class U {

	int y;
}
// === end ===

// === case: delete_mixed_whitespace_blanks ===
// target: col=0
class T {

	
  
	int x;
// === end ===

// === case: delete_multiple_blanks_after_class_brace ===
// target: col=0
class T {


	int x;
// === end ===

// === case: delete_single_blank_after_class_brace ===
// target: col=0
class T {

	int x;
// === end ===

// === case: delete_whitespace_only_blank ===
// target: col=0
class T {
	
	int x;
// === end ===

// === case: enum_keyword ===
// target: col=0
enum E {

	A
// === end ===

// === case: interface_keyword ===
// target: col=0
interface I {

	void f();
// === end ===

// === case: multi_line_declaration ===
// target: col=0
class T
	extends Base {

	int x;
// === end ===

// === case: no_blank_after_brace ===
// target: col=0
class T {
	int x;
// === end ===

// === case: no_brace_found ===
// target: col=0
class T
// === end ===

// === case: record_keyword ===
// target: col=0
record R(int x) {

}
// === end ===

// === case: skip_brace_inside_text_block ===
// target: line=2 col=0
class T {
	String json = """
		class X {

		}
		""";
}
// === end ===