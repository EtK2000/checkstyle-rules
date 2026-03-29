package com.etk2000.checkstyle.inputs.thread;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@interface BinderThread {}

@Target(ElementType.TYPE)
@interface MainThread {}

@Target(ElementType.TYPE)
@interface UiThread {}

@BinderThread
class InputThreadAllAnnotationsClean {
}

@MainThread
class InputThreadMainThreadClean {
}

@UiThread
class InputThreadUiThreadClean {
}