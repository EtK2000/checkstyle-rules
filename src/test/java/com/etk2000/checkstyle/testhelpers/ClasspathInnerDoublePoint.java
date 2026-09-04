package com.etk2000.checkstyle.testhelpers;

import java.awt.geom.Point2D;

/** Test helper whose non-static member type {@code Double} hides the static {@code Point2D.Double}, so a static import brings in neither. */
public abstract class ClasspathInnerDoublePoint extends Point2D {
	public class Double {}
}