// === case: assert_import_whitespace_tolerant ===
// target: line=2 col=2
import static org . junit . Assert . assertEquals ;

		assertEquals(false, result);
// === end ===

// === case: index_of_char_refuses_invalid_escape ===
// target: col=16
		final var i = s.indexOf("\z");
// === end ===

// === case: index_of_char_refuses_invalid_unicode_escape ===
// target: col=16
		final var i = s.indexOf("\uABCG");
// === end ===

// === case: unmodifiable_as_list_unbalanced ===
// target: col=0
		List<String> result = Collections.unmodifiableList(Arrays.asList(list);
// === end ===