package com.etk2000.checkstyle;

import javax.annotation.Nonnull;

/**
 * A {@code String} charset parameter with a {@code Charset} sibling overload: its argument index,
 * and the fully qualified name of the class declaring that sibling (an inherited overload is
 * declared by the superclass).
 */
record CharsetParameter(int index, @Nonnull String overloadOwner) {}