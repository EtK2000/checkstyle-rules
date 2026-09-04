// === case: default_package_sibling_collision_main ===
// target: line=1 col=1
class Main {
	com.other.Sibling field;
}
// === end ===

// === case: same_package_entry_dep ===
package com.example;
class Entry {}
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

// === case: static_import_entry_on_demand_main ===
// target: line=3 col=1
package com.example;
import static java.util.Map.*;
class Main {
	com.example.Entry field;
}
// === end ===

// === case: static_import_entry_on_demand_unknown_main ===
// target: line=3 col=1
package com.example;
import static com.unknown.Missing.*;
class Main {
	com.example.Entry field;
}
// === end ===

// === case: static_import_entry_single_main ===
// target: line=3 col=1
package com.example;
import static java.util.Map.Entry;
class Main {
	com.example.Entry field;
}
// === end ===

// === case: static_import_entry_single_unknown_main ===
// target: line=3 col=1
package com.example;
import static com.unknown.Shapes.Entry;
class Main {
	com.example.Entry field;
}
// === end ===
