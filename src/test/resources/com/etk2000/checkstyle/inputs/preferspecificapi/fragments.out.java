// === case: assert_import_whitespace_tolerant ===
import static org . junit . Assert . assertEquals ;

		assertEquals(false, result);
// === end ===

// === case: index_of_char_refuses_invalid_escape ===
		final var i = s.indexOf("\z");
// === end ===

// === case: index_of_char_refuses_invalid_unicode_escape ===
		final var i = s.indexOf("\uABCG");
// === end ===

// === case: unmodifiable_as_list_unbalanced ===
		List<String> result = Collections.unmodifiableList(Arrays.asList(list);
// === end ===