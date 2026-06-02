package com.etk2000.checkstyle.inputs.threadannotation;

// === case: enum ===
enum InputThreadEnumSliceViolation { // violation: Class 'InputThreadEnumSliceViolation' must have a thread annotation (@AnyThread, @MainThread, etc.).
}
// === end ===

// === case: interface ===
interface InputThreadInterfaceSliceViolation { // violation: Class 'InputThreadInterfaceSliceViolation' must have a thread annotation (@AnyThread, @MainThread, etc.).
}
// === end ===

// === case: main ===
class InputThreadSliceViolation { // violation: Class 'InputThreadSliceViolation' must have a thread annotation (@AnyThread, @MainThread, etc.).
}
// === end ===

// === case: record ===
record InputThreadRecordSliceViolation() {} // violation: Class 'InputThreadRecordSliceViolation' must have a thread annotation (@AnyThread, @MainThread, etc.).
// === end ===