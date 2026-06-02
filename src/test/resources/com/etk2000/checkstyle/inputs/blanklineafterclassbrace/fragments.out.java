// === case: delete_mixed_whitespace_blanks ===
class T {
	int x;
// === end ===

// === case: delete_multiple_blanks_after_class_brace ===
class T {
	int x;
// === end ===

// === case: delete_single_blank_after_class_brace ===
class T {
	int x;
// === end ===

// === case: delete_whitespace_only_blank ===
class T {
	int x;
// === end ===

// === case: enum_keyword ===
enum E {
	A
// === end ===

// === case: interface_keyword ===
interface I {
	void f();
// === end ===

// === case: multi_line_declaration ===
class T
	extends Base {
	int x;
// === end ===

// === case: record_keyword ===
record R(int x) {
}
// === end ===