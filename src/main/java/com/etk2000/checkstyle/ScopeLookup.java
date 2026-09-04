package com.etk2000.checkstyle;

/**
 * What a {@link VariableScope} lookup found: a binding of the name, none, or no answer because a
 * class the answer depends on cannot be read.
 */
public enum ScopeLookup {
	ABSENT,
	FOUND,
	UNKNOWN
}