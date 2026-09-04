package com.etk2000.checkstyle.testhelpers;

import java.util.concurrent.Future;

/** Test helper whose own member type {@code State} hides the {@code Future.State} it would inherit. */
public abstract class ClasspathHidingFuture implements Future<String> {
	public static class State {}
}