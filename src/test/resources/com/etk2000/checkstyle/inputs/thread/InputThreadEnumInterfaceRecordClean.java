package com.etk2000.checkstyle.inputs.thread;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@interface WorkerThread {}

@WorkerThread
enum InputThreadEnumClean {
}

@WorkerThread
interface InputThreadInterfaceClean {
}

@WorkerThread
record InputThreadRecordClean() {
}