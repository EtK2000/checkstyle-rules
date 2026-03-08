package com.etk2000.checkstyle.inputs.thread;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@interface AnyThread {}

@AnyThread
class InputThreadClean {
}