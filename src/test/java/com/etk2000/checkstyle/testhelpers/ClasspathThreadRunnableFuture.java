package com.etk2000.checkstyle.testhelpers;

import java.util.concurrent.RunnableFuture;

/** Test helper passing down {@code Thread.State} from its superclass and {@code Future.State} one interface further up. */
public abstract class ClasspathThreadRunnableFuture extends Thread implements RunnableFuture<String> {}