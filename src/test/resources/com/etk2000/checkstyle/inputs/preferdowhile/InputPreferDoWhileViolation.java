package com.etk2000.checkstyle.inputs.preferdowhile;

import java.util.List;

class InputPreferDoWhileViolation {
	static class Node {
		Node next() {
			return null;
		}
	}

	void arrayAssign(int[] arr, int i) {
		arr[i] = 0;
		while (i-- > 0) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			arr[i] = 0;
	}

	void bareCallNoArgs() {
		foo();
		while (cond()) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			foo();
	}

	void bareMethodCall(int i) {
		System.out.println(i);
		while (--i > 0) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			System.out.println(i);
	}

	void bracedBody(int i) {
		++i;
		while (i < 10) { // violation: Replace pre-loop statement and 'while' with 'do-while'.
			++i;
		}
	}

	void commentBetween(int i) {
		++i;
		// walking forward
		while (i < 10) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			++i;
	}

	void compoundAssign(int i) {
		i += 2;
		while (i < 100) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			i += 2;
	}

	boolean cond() {
		return false;
	}

	void foo() {
	}

	void linkedListWalk(Node node) {
		node = node.next();
		while (node != null) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			node = node.next();
	}

	void nestedInForBody(int n) {
		for (var k = 0; k < n; ++k) {
			var i = 0;
			++i;
			while (i < n) // violation: Replace pre-loop statement and 'while' with 'do-while'.
				++i;
		}
	}

	void prefixDec(int i) {
		--i;
		while (i > 0) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			--i;
	}

	void prefixInc(int i, String param) {
		++i;
		while (i < param.length() && Character.isLetterOrDigit(param.charAt(i))) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			++i;
	}

	void sameMethodArg(List<Integer> list) {
		list.add(1);
		while (list.size() < 10) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			list.add(1);
	}
}