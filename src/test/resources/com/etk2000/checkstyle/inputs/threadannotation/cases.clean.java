package com.etk2000.checkstyle.inputs.threadannotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@interface AnyThread {}

@Target(ElementType.TYPE)
@interface BinderThread {}

@Target(ElementType.TYPE)
@interface MainThread {}

@Target(ElementType.TYPE)
@interface UiThread {}

@Target(ElementType.TYPE)
@interface WorkerThread {}

@AnyThread
class InputThreadClean {
}

@BinderThread
class InputThreadAllAnnotationsClean {
}

@MainThread
class InputThreadMainThreadClean {
}

@UiThread
class InputThreadUiThreadClean {
}

@WorkerThread
enum InputThreadEnumClean {
}

@WorkerThread
interface InputThreadInterfaceClean {
}

@WorkerThread
record InputThreadRecordClean() {}

@MainThread
class InputThreadInnerClassSkipped {
	class Inner {}

	static class StaticInner {}

	enum InnerEnum {}

	interface InnerInterface {}
}