package com.etk2000.checkstyle.inputs.thread;

enum InputThreadEnumViolation { // violation: Class 'InputThreadEnumViolation' must have a thread annotation (@AnyThread, @MainThread, etc.).
}

interface InputThreadInterfaceViolation { // violation: Class 'InputThreadInterfaceViolation' must have a thread annotation (@AnyThread, @MainThread, etc.).
}

record InputThreadRecordViolation() { // violation: Class 'InputThreadRecordViolation' must have a thread annotation (@AnyThread, @MainThread, etc.).
}