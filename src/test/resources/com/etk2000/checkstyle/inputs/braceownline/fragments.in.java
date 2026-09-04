// === case: stale_position_no_site_refused ===
// target: line=3 col=2
class T {
	void m(int x)
	{
		--x;
		++x;
	}
}
// === end ===

// === case: stale_position_refused ===
// target: line=3 col=2
class T {
	void m(int x)
	{
		if (x > 0) {
			--x;
			++x;
		}
	}
}
// === end ===

// === case: unparseable_buffer_refused ===
// target: col=1
	if (x > 0)
	{
		x = 5 6;
	}
// === end ===