package com.etk2000.checkstyle.inputs.preferstaticimportconstant.support;

/**
 * Second anchor class used by the conflict-violation fixture: when a file
 * already statically imports {@code OtherAnchorClass.X23} and locally aliases
 * {@code AnchorClass.X23}, the fixer can't add {@code import static
 * AnchorClass.X23} without a same-name clash, so it skips with the conflict
 * message.
 */
public class OtherAnchorClass {
	public static final int X23 = 0;
}