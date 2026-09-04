// === case: column_at_line_end_returns_null ===
// target: line=3 col=34
import java.util.Objects;

class F {
	boolean a = Objects.isNull(null);
	boolean b = Objects.isNull(null);
}
// === end ===

// === case: dot_with_no_method_ident ===
// target: col=2
		Objects.(arg);
// === end ===

// === case: no_dot_after_candidate_class ===
// target: line=0 col=17
import java.util.Objects;

class F {
	boolean a = Objects.isNull(null);
	boolean b = Objects.isNull(null);
}
// === end ===

// === case: receiver_column_inside_identifier ===
// target: line=3 col=14
import java.util.Objects;

class F {
	boolean a = Objects.isNull(null);
	boolean b = Objects.isNull(null);
}
// === end ===

// === case: receiver_runs_to_end_of_line ===
// target: col=2
		Objects
// === end ===

// === case: unknown_simple_class ===
// target: line=3 col=1
import java.util.Objects;

class F {
	boolean a = Objects.isNull(null);
	boolean b = Objects.isNull(null);
}
// === end ===