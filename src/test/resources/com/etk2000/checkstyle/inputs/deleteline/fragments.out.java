// === case: delete_first_line ===
class Foo {}
// === end ===

// === case: delete_first_line_blank_below ===

class T {}
// === end ===

// === case: delete_import_blank_above_only ===
import A;

import C;
// === end ===

// === case: delete_import_blank_below_only ===
import A;

import C;
// === end ===

// === case: delete_last_line ===
class Foo {}
// === end ===

// === case: delete_last_line_blank_above ===
import A;

// === end ===

// === case: delete_middle_line ===
line1
line3
// === end ===

// === case: delete_orphaned_import_blank_above_and_below ===
import A;

import C;
// === end ===