// === case: leading_bom_unparseable ===
// target: line=2 col=1
﻿package com.example;
class T {
	com.foo.Bar field;
}
// === end ===

// === case: unparseable ===
// target: col=0
java.util.List x = (
// === end ===
