package com.etk2000.checkstyle.testhelpers;

import java.util.concurrent.Future;

/** Test helper passing down two different member types named {@code State} at one depth: {@code Thread.State} and {@code Future.State}. */
public abstract class ClasspathThreadFuture extends Thread implements Future<String> {}