package com.etk2000.checkstyle.inputs.preferdowhile;

import java.util.List;

class InputPreferDoWhileClean {
	void bareBlockBefore(int i) {
		{
			++i;
		}
		while (i < 10)
			++i;
	}

	void blockBefore(int i) {
		if (i < 0)
			++i;
		while (i < 10)
			++i;
	}

	void controlBody(int i) {
		++i;
		while (i < 10) {
			if (i == 5)
				return;
			else
				--i;
		}
	}

	void declarationBefore(int n) {
		var i = 0;
		while (i < n)
			i = i + 1;
		System.out.println(i);
	}

	void differentArgs(int i) {
		System.out.println(1);
		while (i-- > 0)
			System.out.println(2);
	}

	void differentIdent(int i, int j) {
		++i;
		while (j < 10)
			++j;
	}

	void differentMethodName(List<Integer> a, int x) {
		a.add(x);
		while (a.size() < 10)
			a.contains(x);
	}

	void differentOperators(int i) {
		++i;
		while (i < 10)
			--i;
	}

	void emptyBody(int i) {
		++i;
		while (i++ < 10);
	}

	void emptyBracedBody(int i) {
		++i;
		while (i++ < 10) {
		}
	}

	void gappedPre(int i) {
		++i;
		System.out.println(i);
		while (i < 10)
			++i;
	}

	void multiStatementBody(int i, int n) {
		++i;
		while (i < n) {
			System.out.println(i);
			--i;
		}
	}

	void multiStatementLastMatches(int i, int n) {
		++i;
		while (i < n) {
			System.out.println(i);
			++i;
		}
	}

	void noPre(int x) {
		while (x > 0)
			--x;
	}

	void tryBefore(int i) {
		try {
			++i;
		}
		finally {
			System.out.println(i);
		}
		while (i < 10)
			++i;
	}

	void whileFirstInBlock(int i) {
		if (i > 0) {
			while (i < 10)
				++i;
		}
	}
}