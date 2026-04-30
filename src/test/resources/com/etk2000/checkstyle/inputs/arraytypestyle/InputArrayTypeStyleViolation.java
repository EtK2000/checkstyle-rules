package com.etk2000.checkstyle.inputs.arraytypestyle;

import java.util.List;

@SuppressWarnings("unused")
class InputArrayTypeStyleViolation {
	record InnerRec(int comp[]) {} // violation: Array brackets must be on the type, not after 'comp'.

	static int sb[]; // violation: Array brackets must be on the type, not after 'sb'.

	@Deprecated
	int annotatedField[]; // violation: Array brackets must be on the type, not after 'annotatedField'.

	int ib[]; // violation: Array brackets must be on the type, not after 'ib'.
	int ic[][]; // violation: Array brackets must be on the type, not after 'ic'.
	List<String> gs[]; // violation: Array brackets must be on the type, not after 'gs'.

	InputArrayTypeStyleViolation(int ctorParam[]) {} // violation: Array brackets must be on the type, not after 'ctorParam'.

	void annotatedParam(@Deprecated int p[]) { // violation: Array brackets must be on the type, not after 'p'.
		p[0] = 1;
	}

	void compoundLocal() {
		final int lc[][] = {{1}}; // violation: Array brackets must be on the type, not after 'lc'.
		lc[0][0] = 1;
	}

	void methodCompoundParam(int c[][]) { // violation: Array brackets must be on the type, not after 'c'.
		c[0][0] = 1;
	}

	int methodMixedReturn()[][] { // violation: Array brackets must be on the type, not after 'methodMixedReturn'.
		return null;
	}

	int methodReturnCStyle()[] { // violation: Array brackets must be on the type, not after 'methodReturnCStyle'.
		return null;
	}

	int methodReturnWithThrows()[] throws Exception { // violation: Array brackets must be on the type, not after 'methodReturnWithThrows'.
		return null;
	}

	void methodSimpleParam(int b[]) { // violation: Array brackets must be on the type, not after 'b'.
		b[0] = 1;
	}

	void mixedDeclaration() {
		final int[] mx[] = {{1}}; // violation: Array brackets must be on the type, not after 'mx'.
		mx[0][0] = 1;
	}

	void multiVarMixed() {
		final int gamma[] = {1}, delta = 0; // violation: Array brackets must be on the type, not after 'gamma'.
		gamma[0] = delta;
	}

	void multiVarMixedReversed() {
		final int epsilon = 0, zeta[] = {1}; // violation: Array brackets must be on the type, not after 'zeta'.
		zeta[0] = epsilon;
	}

	void multiVarSame() {
		final int alpha[] = {1}, beta[] = {2}; // violation: Array brackets must be on the type, not after 'alpha'. // violation: Array brackets must be on the type, not after 'beta'.
		alpha[0] = beta[0];
	}

	void simpleLocal() {
		final int lb[] = {1}; // violation: Array brackets must be on the type, not after 'lb'.
		lb[0] = 0;
	}
}