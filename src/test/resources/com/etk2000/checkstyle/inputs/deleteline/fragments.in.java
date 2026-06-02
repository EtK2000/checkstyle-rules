// === case: delete_first_line ===
// target: col=0
import java.util.List;
class Foo {}
// === end ===

// === case: delete_first_line_blank_below ===
// target: col=0
import A;

class T {}
// === end ===

// === case: delete_import_blank_above_only ===
// target: line=2 col=0
import A;

import B;
import C;
// === end ===

// === case: delete_import_blank_below_only ===
// target: line=1 col=0
import A;
import B;

import C;
// === end ===

// === case: delete_last_line ===
// target: line=1 col=0
class Foo {}
import java.util.List;
// === end ===

// === case: delete_last_line_blank_above ===
// target: line=2 col=0
import A;

import B;
// === end ===

// === case: delete_middle_line ===
// target: line=1 col=0
line1
line2
line3
// === end ===

// === case: delete_orphaned_import_blank_above_and_below ===
// target: line=2 col=0
import A;

import B;

import C;
// === end ===
