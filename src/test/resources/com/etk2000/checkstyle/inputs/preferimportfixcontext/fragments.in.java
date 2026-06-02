// === case: default_package_sibling_collision_main ===
// target: line=1 col=1
class Main {
	com.other.Sibling field;
}
// === end ===

// === case: same_package_sibling_collision_main ===
// target: line=2 col=1
package com.example;
class Main {
	com.other.Sibling field;
}
// === end ===

// === case: same_package_sibling_dep ===
package com.example;
class Sibling {}
// === end ===

// === case: same_package_sibling_main ===
// target: line=2 col=1
package com.example;
class Main {
	com.example.Sibling field;
}
// === end ===
