package com.etk2000.checkstyle.inputs.emptybody;

import java.util.List;

class InputEmptyBodyClean {
	static {
		System.out.println("init");
	}

	{
		System.out.println("init");
	}

	void bracedDoWhile(int x) {
		do {
			--x;
		} while (x > 0);
	}

	void bracedFor(int x) {
		for (int i = 0; i < x; ++i) {
			System.out.println(i);
		}
	}

	void bracedIf(int x) {
		if (x > 0) {
			System.out.println("positive");
		}
	}

	void bracedIfElse(int x) {
		if (x > 0) {
			System.out.println("positive");
		}
		else {
			System.out.println("non-positive");
		}
	}

	void bracedIfElseIf(int x) {
		if (x > 0) {
			System.out.println("positive");
		}
		else if (x < 0) {
			System.out.println("negative");
		}
		else {
			System.out.println("zero");
		}
	}

	void commentedBlockCommentIfBody(int x) {
		if (x > 0)
			; /* handled by the caller */
	}

	void commentedDoBody(int x) {
		do; // deliberately does nothing
		while (x > 0);
	}

	void commentedElseBody(int x) {
		if (x > 0)
			System.out.println("positive");
		else
			; // nothing to do for non-positive
	}

	void commentedElseIfBody(int x) {
		if (x > 0)
			System.out.println("positive");
		else if (x < 0)
			; // negatives are the caller's problem
	}

	void commentedForBody(int x) {
		for (var i = 0; i < x; ++i)
			; // the update is the whole point
	}

	void commentedForEachBody(List<String> list) {
		for (var s : list)
			; // walking the iterator is the whole point
	}

	void commentedIfBodyOwnLine(int x) {
		if (x > 0)
			; // handled by the caller
	}

	void commentedIfBodySameLine(int x) {
		if (x > 0); // handled by the caller
	}

	void commentedWhileBody(int x) {
		while (x > 0); // spin until another thread clears it
	}

	void labeledEmptyStatement() {
		done: ;
	}

	void normalIf(int x) {
		if (x > 0)
			System.out.println("positive");
	}

	void normalIfElse(int x) {
		if (x > 0)
			System.out.println("positive");
		else
			System.out.println("non-positive");
	}

	void normalIfElseIf(int x) {
		if (x > 0)
			System.out.println("positive");
		else if (x < 0)
			System.out.println("negative");
		else
			System.out.println("zero");
	}

	void normalWhile(int x) {
		while (x > 0)
			--x;
	}

	void singleLineDoWhile(int x) {
		do
			--x;
		while (x > 0);
	}

	void singleLineFor(int x) {
		for (int i = 0; i < x; ++i)
			System.out.println(i);
	}
}