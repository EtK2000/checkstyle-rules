package com.etk2000.checkstyle.testhelpers;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Test helper reaching {@code Map.Entry} along two paths: through {@code LinkedHashMap}, whose
 * package-private {@code Entry} hides it, and directly through {@code Map}.
 */
public class ClasspathLinkedEntryMap<K, V> extends LinkedHashMap<K, V> implements Map<K, V> {}