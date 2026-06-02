// === case: assert_false_instance_of_fully_qualified ===
// imports: static org.junit.jupiter.api.Assertions.assertNotInstanceOf
class AssertFalseInstanceOfFullyQualifiedSlice {
	void m(Object o) {
		assertNotInstanceOf(Integer.class, o);
	}
}
// === end ===

// === case: assert_true_instance_of_fully_qualified ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
class AssertTrueInstanceOfFullyQualifiedSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===