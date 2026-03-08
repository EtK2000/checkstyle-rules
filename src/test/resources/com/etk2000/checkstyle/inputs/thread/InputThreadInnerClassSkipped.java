package com.etk2000.checkstyle.inputs.thread;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@interface MainThread {}

@MainThread
class InputThreadInnerClassSkipped {
	class Inner {}

	static class StaticInner {}

	enum InnerEnum {}

	interface InnerInterface {}
}