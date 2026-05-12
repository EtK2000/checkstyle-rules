package com.etk2000.checkstyle.inputs.preferstaticimportconstant;

import com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass;

import java.util.List;

class InputPreferStaticImportConstantViolation {
	enum InnerEnum {
		A;

		private static final int ENUM_FIELD = AnchorClass.ENUM_FIELD; // violation: Replace 'ENUM_FIELD' alias of 'AnchorClass.ENUM_FIELD' with a static import.
	}

	record InnerRecord(int x) {
		private static final int RECORD_FIELD = AnchorClass.RECORD_FIELD; // violation: Replace 'RECORD_FIELD' alias of 'AnchorClass.RECORD_FIELD' with a static import.
	}

	static class NestedClass {
		private static final int NESTED = AnchorClass.NESTED; // violation: Replace 'NESTED' alias of 'AnchorClass.NESTED' with a static import.
	}

	private static final int DEEPLY_NESTED_PARENS_ALIAS = (((AnchorClass.X23))); // violation: Replace 'DEEPLY_NESTED_PARENS_ALIAS' alias of 'AnchorClass.X23' with a static import.
	private static final int FQ_ALIAS = com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X15; // violation: Replace 'FQ_ALIAS' alias of 'com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X15' with a static import.
	private static final int INT_ALIAS = AnchorClass.INT_ALIAS; // violation: Replace 'INT_ALIAS' alias of 'AnchorClass.INT_ALIAS' with a static import.
	private static final int lowercase_alias = AnchorClass.X7; // violation: Replace 'lowercase_alias' alias of 'AnchorClass.X7' with a static import.
	private static final int NESTED_CLASS_ALIAS = AnchorClass.Inner.X16; // violation: Replace 'NESTED_CLASS_ALIAS' alias of 'AnchorClass.Inner.X16' with a static import.
	private static final int PARENTHESIZED_ALIAS = (AnchorClass.X11); // violation: Replace 'PARENTHESIZED_ALIAS' alias of 'AnchorClass.X11' with a static import.
	private static final int RENAMED = AnchorClass.X6; // violation: Replace 'RENAMED' alias of 'AnchorClass.X6' with a static import.
	@Deprecated
	private static final int ANNOTATED_ALIAS = AnchorClass.ANNOTATED_ALIAS; // violation: Replace 'ANNOTATED_ALIAS' alias of 'AnchorClass.ANNOTATED_ALIAS' with a static import.
	@SuppressWarnings("unused")
	private static final int OTHER_SUPPRESS_KEY = AnchorClass.OTHER_SUPPRESS_KEY; // violation: Replace 'OTHER_SUPPRESS_KEY' alias of 'AnchorClass.OTHER_SUPPRESS_KEY' with a static import.
	private static final int[] ARRAY_ALIAS = AnchorClass.ARRAY_ALIAS; // violation: Replace 'ARRAY_ALIAS' alias of 'AnchorClass.ARRAY_ALIAS' with a static import.
	private static final List<String> GENERIC_ALIAS = AnchorClass.GENERIC_ALIAS; // violation: Replace 'GENERIC_ALIAS' alias of 'AnchorClass.GENERIC_ALIAS' with a static import.
	private static final String STRING_ALIAS = AnchorClass.STRING_ALIAS; // violation: Replace 'STRING_ALIAS' alias of 'AnchorClass.STRING_ALIAS' with a static import.
}