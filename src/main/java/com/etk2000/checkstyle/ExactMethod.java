package com.etk2000.checkstyle;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * A method a call may bind to, its types read as {@link ProvenTypes} keys: the parameter types in
 * order, a varargs one as its array, the return type, and whether it takes a variable number of
 * arguments. A nearer method overrides a farther one exactly when their parameter types are the
 * same exact keys.
 */
record ExactMethod(@Nonnull List<String> parameters, @Nonnull String returnType, boolean varargs) {}