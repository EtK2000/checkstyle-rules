package com.etk2000.checkstyle.inputs.nounnecessarythis;

/**
 * Positions for {@code NoUnnecessaryThisLocatorTest}, which addresses them directly rather than
 * through a reported violation. Nothing here is a slice: the file carries no markers and is never
 * fix-run. The positions that matter are the ones the check never reports, since those are what a
 * stale column or a wrong conversion hands the fixer.
 */
public class InputLocatorPositions {
	class Inner {
		int field;
	}

	int alpha, beta, field;
	int[] values;
	Inner inner;
	String text;

	int arrayAccessMember() {
		return this.values[0];
	}

	void arrayElementAssignmentTarget() {
		this.values[0] = 1;
	}

	void assignmentTarget() {
		this.field = 1;
	}

	int bindingInALocalClass() {
		class Local {
			void print(Object field) {
				System.out.println(field);
			}
		}

		new Local().print(text);
		return this.field;
	}

	int bindingInAnAnonymousBody() {
		final var doubler = new InputLocatorPositions() {
			int doubled() {
				final var field = 2;
				return field;
			}
		};
		System.out.println(doubler.doubled());
		return this.field;
	}

	int castReceiver() {
		return ((Object) this).hashCode();
	}

	int commentAfterDot() {
		return this./* c */field;
	}

	int commentBeforeDot() {
		return this/* c */.field;
	}

	int compoundAssignmentTarget() {
		this.field += 1;
		return field;
	}

	int dotOnTheNextLine() {
		return this
				.field;
	}

	int identifierEndingInThis() {
		final var xthis = new Inner();
		return xthis.field;
	}

	int nestedDotReceiver() {
		return this.inner.field;
	}

	int parenthesizedThis() {
		return (this).field;
	}

	<X> X pick() {
		return null;
	}

	Inner qualifiedNew() {
		return this.new Inner();
	}

	int shadowedName() {
		final var field = 1;
		return this.field + field;
	}

	int spaceAfterDot() {
		return this. field;
	}

	int spaceBeforeDot() {
		return this .field;
	}

	int spacedDot() {
		return this . field;
	}

	String stringHoldingThisDot() {
		return "this.field" + this.text;
	}

	String supplementaryAhead() {
		final var 𝒜 = "x";
		return 𝒜 + this.text;
	}

	String textBlockHoldingThisDot() {
		return """
				this.field
				""" + this.text;
	}

	int tightDot() {
		return this.field;
	}

	int twoThisOnOneLine() {
		return this.alpha + this.beta;
	}

	<X> X typeWitness() {
		return this.<X>pick();
	}
}