package com.etk2000.checkstyle.inputs.fieldconsolidation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

class L<T> {
}

@interface CleanMulti {
	int a() default 0;
	int b() default 0;
}

class InputFieldConsolidationCleanAlreadyCombined {
	int height, width;
}

@interface CleanAnnotationType {
	int alpha() default 0;
	int beta() default 0;
}

class InputFieldConsolidationCleanDifferentTypes {
	int count;
	String name;
}

class InputFieldConsolidationCleanFinalValueSplit {
	final int alpha = 1;
	final int beta;

	InputFieldConsolidationCleanFinalValueSplit() {
		beta = 0;
	}
}

class InputFieldConsolidationCleanDependencyCrossing {
	int alpha = 1;
	String label = "v" + alpha;
	int beta = label.length();
}

class InputFieldConsolidationCleanAnonymousClassBoundApart {
	final Object first = new Object() {
	};
	final Object plain = "p";
}

class InputFieldConsolidationCleanAnonymousClassReaderBoundApart {
	final Object first = new Object() {
	};
	final Object reader = first.toString(), plain = "p";
}

class InputFieldConsolidationCleanDependencyCrossingReader {
	int alpha;
	String label = "v" + this.beta;
	int beta = 2;
}

class InputFieldConsolidationCleanInitForwardRead {
	private int total = this.count * 2;
	private int count = 3;
}

class InputFieldConsolidationCleanInitForwardReadAnonymousFieldInitializer {
	private int total = new Object() {
		final int seen = InputFieldConsolidationCleanInitForwardReadAnonymousFieldInitializer.this.count;
	}.seen;
	private int count = 3;
}

class InputFieldConsolidationCleanPinnedRunsBetween {
	private int base = 10;
	private String report = new Report().text();
	private int max = 100;

	private class Report {
		String text() {
			return base + ".." + max;
		}
	}
}

class InputFieldConsolidationCleanPinnedRunsJoinerPastLiteral {
	private int base;
	private String label = "v";
	private int max = Integer.parseInt("5");
}

class InputFieldConsolidationCleanPinnedRunsJoinerPastRuns {
	private int base = Integer.parseInt("1");
	private String report = String.join("..", "a");
	private int max = Integer.parseInt("2");
}

class InputFieldConsolidationCleanPinnedLaterDeclaratorRunsBetween {
	private int base = 1;
	private String label = "x", report = String.join("..", "a");
	private int max = 2;
}

class InputFieldConsolidationCleanPinnedLastNameOfJoinerRunsBetween {
	private int base = 1;
	private String report = String.join("..", "a");
	private int max, zeta = Integer.parseInt("2");
}

class InputFieldConsolidationCleanPinnedFirstNameOfJoinerRunsBetween {
	private int base = 1;
	private String report = String.join("..", "a");
	private int max = 2, zeta;
}

class InputFieldConsolidationCleanPinnedAnonymousClassBoundRunsMergeApart {
	final Object first = new Object() {
	};
	final Object plain = "p";
	final Object second = new Object() {
	};
	final Object label = "l";
}

class InputFieldConsolidationCleanInitForwardReadInLambda {
	static IntSupplier total = () -> InputFieldConsolidationCleanInitForwardReadInLambda.count.getAsInt();
	static IntSupplier count = () -> 3;
}

class InputFieldConsolidationCleanInitForwardReadSameDeclaration {
	private int total = this.count * 2, count = 3;
	private int other;
}

class InputFieldConsolidationCleanInitForwardReadStaticQualified {
	private static int total = InputFieldConsolidationCleanInitForwardReadStaticQualified.count * 2;
	private static int count = 3;
}

class InputFieldConsolidationCleanFinalMixedValueDeclaration {
	final int alpha = 1, beta;
	final int gamma = 2;

	InputFieldConsolidationCleanFinalMixedValueDeclaration() {
		beta = 0;
	}
}

class InputFieldConsolidationCleanFirstFieldOnHeaderLine { int alpha;
	int beta;
}

class InputFieldConsolidationCleanLastFieldOnClosingBraceLine {
	int alpha;
	int beta; }

class InputFieldConsolidationCleanRecordComponentsAndLocals {
	record Pair(int alpha, int beta) {}

	void m() {
		final int alpha;
		final int beta;
		alpha = 1;
		beta = 2;
		System.out.println(alpha + beta);
	}
}

@SuppressWarnings("FieldSorting")
class InputFieldConsolidationCleanSortingSuppressedInterleaved {
	int alpha;
	String first;
	int beta;
	String second;
}

@SuppressWarnings("FieldSorting")
class InputFieldConsolidationCleanSortingSuppressedDocumentedBetween {
	int beta;
	/** The gamma. */
	int gamma;
	int alpha;
}

@SuppressWarnings("FieldSorting")
class InputFieldConsolidationCleanSortingSuppressedSharedLineBetween {
	int beta;
	int gamma; int delta;
	int alpha;
}

@SuppressWarnings("FieldSorting")
class InputFieldConsolidationCleanSortingSuppressedCommentLineBetween {
	int beta;
	// note
	int alpha;
}

@SuppressWarnings("FieldSorting")
class InputFieldConsolidationCleanSortingSuppressedFinalSplit {
	final int alpha = 1;
	final int beta;

	InputFieldConsolidationCleanSortingSuppressedFinalSplit() {
		beta = 0;
	}
}

@SuppressWarnings("FieldSorting")
class InputFieldConsolidationCleanSortingSuppressedAnonymousSplit {
	final Object holder = new Object() {
	};
	final Object label = "w";
}

class InputFieldConsolidationCleanDirectiveInDocComment {
	int alpha;
	/** NOSONAR */ int beta;
}

class InputFieldConsolidationCleanDirectiveCheckstyleNextLinesAboveBase {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES
	private int a = 1;
	private int b = 2;
}

class InputFieldConsolidationCleanDirectiveCheckstyleNextLinesClosingOnBaseLine {
	/* CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES
	 */ private int a = 7;
	private int b = 8;
}

class InputFieldConsolidationCleanDirectiveLanguageAboveBase {
	// language=SQL
	private String query = "select 1";
	private String label = "x";
}

class InputFieldConsolidationCleanDirectiveStatementScopedAboveBase {
	//noinspection unused
	int alpha;
	int beta;
}

class InputFieldConsolidationCleanDirectiveStatementScopedAboveCarriedBlockComment {
	//noinspection unused
	/* legacy
	 */ int alpha;
	int beta;
}

class InputFieldConsolidationCleanDirectiveStatementScopedAboveSupplementaryCharComment {
	//noinspection unused
	/* 𝑥 */ int alpha;
	int beta;
}

class InputFieldConsolidationCleanDirectiveStatementScopedBetweenAnnotationAndType {
	@Deprecated
	//noinspection unused
	private String alpha;
	@Deprecated
	private String beta;
}

class InputFieldConsolidationCleanDirectiveLanguageBetweenAnnotationAndType {
	@Deprecated
	// language=SQL
	private String alpha = "select 1";
	@Deprecated
	private String beta = "x";
}

class InputFieldConsolidationCleanDirectiveNosonarBetweenAnnotationAndType {
	@Deprecated
	// NOSONAR
	private String alpha;
	@Deprecated
	private String beta;
}

class InputFieldConsolidationCleanDirectiveStatementScopedOnFirstAnnotationLine {
	@Deprecated //noinspection unused
	int alpha;
	@Deprecated
	int beta;
}

class InputFieldConsolidationCleanDirectiveStatementScopedTrailingLineAbove {
	String label; //noinspection unused
	int alpha;
	int beta;
}

class InputFieldConsolidationCleanDirectiveStatementScopedAboveBlankLine {
	//noinspection unused

	int alpha;
	int beta;
}

class InputFieldConsolidationCleanVisibility {
	private int priv;
	int pub;
}

class InputFieldConsolidationCleanStatic {
	static int shared;

	int local;
}

class InputFieldConsolidationCleanFinal {
	final int fixed;
	int flex;

	InputFieldConsolidationCleanFinal(int fixed) {
		this.fixed = fixed;
	}
}

class InputFieldConsolidationCleanDifferentAnnotations {
	@Deprecated
	int newer;
	@SuppressWarnings("unused")
	int old;
}

class InputFieldConsolidationCleanDifferentParams {
	@SuppressWarnings("a")
	int first;
	@SuppressWarnings("b")
	int second;
}

class InputFieldConsolidationCleanDifferentNamedParams {
	@CleanMulti(a = 1, b = 2)
	int first;
	@CleanMulti(a = 1, b = 3)
	int second;
}

class InputFieldConsolidationCleanSwappedArrayParam {
	@SuppressWarnings({"all", "unused"})
	int first;
	@SuppressWarnings({"unused", "all"})
	int second;
}

@interface CleanValue {
	int value();
}

class InputFieldConsolidationCleanNegatedAnnotationValue {
	@CleanValue(-1)
	int alpha;
	@CleanValue(1)
	int beta;
}

class InputFieldConsolidationCleanAnnotationValueSumVsLiteral {
	@CleanValue(1 + 2)
	int alpha;
	@CleanValue(12)
	int beta;
}

class InputFieldConsolidationCleanNegatedNamedAnnotationValue {
	@CleanMulti(a = -5)
	int alpha;
	@CleanMulti(a = 5)
	int beta;
}

class InputFieldConsolidationCleanQualifiedVsSimpleAnnotation {
	@Deprecated
	int alpha;
	@java.lang.Deprecated
	int beta;
}

class InputFieldConsolidationCleanSameSimpleNameAnnotations {
	@javax.annotation.Nullable
	String alpha;
	@org.jetbrains.annotations.Nullable
	String beta;
}

@interface CleanTag {
	String value();
}

class InputFieldConsolidationCleanRepeatedAnnotationOrder {
	@CleanTag("x")
	@CleanTag("y")
	int alpha;
	@CleanTag("y")
	@CleanTag("x")
	int beta;
}

class InputFieldConsolidationCleanAnnotationMismatch {
	int plain;
	@Deprecated
	int annotated;
}

class InputFieldConsolidationCleanBlockCommentBetween {
	int alpha;
	/* separator */
	int beta;
}

class InputFieldConsolidationCleanJavadocBetween {
	int x;
	/** The Y coordinate */
	int y;
}

class InputFieldConsolidationCleanJavadocOnPrev {
	/** Height in pixels. */
	private int height;
	private int width;
}

class InputFieldConsolidationCleanSameLineJavadocOnPrev {
	/** Height in pixels. */ private int height;
	private int width;
}

class InputFieldConsolidationCleanJavadocBlankLineOnPrev {
	/** Height in pixels. */

	private int height;
	private int width;
}

class InputFieldConsolidationCleanDeprecatedJavadocOnPrev {
	/**
	 * Height in pixels.
	 *
	 * @deprecated use a size instead
	 */
	@Deprecated
	private int height;
	@Deprecated
	private int width;
}

class InputFieldConsolidationCleanJavadocOnClassHeaderLine { /** The alpha. */
	int alpha;
	int beta;
}

class InputFieldConsolidationCleanEmptyBlockCommentThenJavadocOnPrev {
	/**/ /** d */
	private int alpha;
	private int beta;
}

class InputFieldConsolidationCleanJavadocThenBlockCommentOnPrev {
	/** Width. */
	/* px */
	private int alpha;
	private int beta;
}

class InputFieldConsolidationCleanJavadocThenLineCommentOnPrev {
	/** d */
	// x
	private int alpha;
	private int beta;
}

class InputFieldConsolidationCleanMultiLineJavadocClosingOnPrevLine {
	/** a
	 b */ private int alpha;
	private int beta;
}

class InputFieldConsolidationCleanStarredJavadocOnPrev {
	/***/
	private int alpha;
	private int beta;
}

class InputFieldConsolidationCleanMarkdownDocCommentOnPrev {
	/// d
	private int alpha;
	private int beta;
}

class InputFieldConsolidationCleanQuadSlashDocCommentOnPrev {
	//// d
	private int alpha;
	private int beta;
}

class InputFieldConsolidationCleanMarkdownDocCommentOnCurr {
	private int alpha;
	/// d
	private int beta;
}

class InputFieldConsolidationCleanTrailingJavadocOnPrevLine {
	private int zeta; /**< the alpha */
	private int alpha;
}

class InputFieldConsolidationCleanSameLineJavadocAfterSupplementaryCharacter {
	/** 🔥🔥🔥 */ private int alpha;
	private int beta;
}

class InputFieldConsolidationCleanSameLineJavadocOnCurr {
	private int height;
	/** Width in pixels. */ private int width;
}

class InputFieldConsolidationCleanLineCommentBetween {
	int alpha;
	// separator comment
	int beta;
}

class InputFieldConsolidationCleanMultiLineBlockCommentBetween {
	int alpha;
	/*
	 * Multi-line separator
	 */
	int beta;
}

class InputFieldConsolidationCleanMultiLineBlockCommentNoStar {
	int alpha;
	/*
	   continued without star prefix
	*/
	int beta;
}

class InputFieldConsolidationCleanArrayMismatch {
	int scalar;
	int[] vector;
}

class InputFieldConsolidationCleanGenericMismatch {
	List<Integer> numbers;
	List<String> words;
}

class InputFieldConsolidationCleanSingle {
	int only;
}

class InputFieldConsolidationCleanArrayDimension {
	int[] flat;
	int[][] matrix;
}

class InputFieldConsolidationCleanCompoundArrayMismatch {
	String[] flat;
	String[] compound[];
}

class InputFieldConsolidationCleanBracketedSecondaryOnCurr {
	int alpha;
	int zebra, beta[];
}

class InputFieldConsolidationCleanBracketedSecondaryOnPrev {
	int alpha, beta[];
	int[] zebra;
}

class InputFieldConsolidationCleanPlainSecondaryOnCurr {
	int alpha[];
	int zebra[], gamma;
}

class InputFieldConsolidationCleanPlainSecondaryOnPrev {
	int alpha[], beta;
	int zebra;
}

enum InputFieldConsolidationCleanEnumConstantBodies {
	A {
		int x;
	},
	B {
		int x;
	}
}

enum InputFieldConsolidationCleanEnumSeparated {
	A,
	B;

	int beta;

	void doSomething() {}
}

class InputFieldConsolidationCleanFqnVsSimple {
	java.util.List<String> alpha;
	List<String> beta;
}

class InputFieldConsolidationCleanFqnVsSimpleNoGenerics {
	java.lang.String first;
	String second;
}

class InputFieldConsolidationCleanDifferentWildcards {
	List<? extends Number> bounded;
	List<? super Number> lower;
}

class InputFieldConsolidationCleanWildcardVsConcrete {
	List<Number> concrete;
	List<? extends Number> wildcard;
}

class InputFieldConsolidationCleanNestedGenericMismatch {
	Map<String, List<Integer>> intMap;
	Map<String, List<String>> strMap;
}

class InputFieldConsolidationCleanFqnAnnotation {
	@java.lang.Deprecated
	int alpha, beta;
}

class InputFieldConsolidationCleanUnboundedVsBounded {
	List<? extends Number> bounded;
	List<?> unbounded;
}

class InputFieldConsolidationCleanMethodBetween {
	int alpha;

	void doSomething() {}

	int beta;
}

class InputFieldConsolidationCleanConstructorBetween {
	int alpha;

	InputFieldConsolidationCleanConstructorBetween() {}

	int beta;
}

class InputFieldConsolidationCleanStaticInitBetween {
	static int alpha;

	static {
		System.out.println();
	}

	static int beta;
}

class InputFieldConsolidationCleanInstanceInitBetween {
	int alpha;

	{
		System.out.println();
	}

	int beta;
}

class InputFieldConsolidationCleanInnerTypeBetween {
	int alpha;

	static class Inner {}

	int beta;
}

class InputFieldConsolidationCleanStraySemicolonBetween {
	int alpha;;
	int beta;
}

@interface TypeAnn {}

class InputFieldConsolidationCleanTypeUseAnnotation {
	List<String> plain;
	List<@TypeAnn String> annotated;
}

class InputFieldConsolidationCleanAnnotatedDimensionMismatch {
	String @TypeAnn [] alpha;
	String @CleanInner [] beta;
}

class InputFieldConsolidationCleanAnnotatedDimensionOrder {
	String @TypeAnn [] @CleanInner [] alpha;
	String @CleanInner [] @TypeAnn [] beta;
}

class InputFieldConsolidationCleanQualifiedTypeAnnotationMismatch {
	Outer.@TypeAnn Inner alpha;
	Outer.@CleanInner Inner beta;
}

class InputFieldConsolidationCleanQualifiedTypeArgumentAnnotationMismatch {
	List<Outer.@TypeAnn Inner> alpha;
	List<Outer.@CleanInner Inner> beta;
}

class InputFieldConsolidationCleanParameterizedQualifierMismatch {
	L<String>.Inner<String> alpha;
	L<String>.Inner<Integer> beta;
}

class InputFieldConsolidationCleanParameterizedQualifierArgumentMismatch {
	L<String>.Inner<Integer> alpha;
	L<Long>.Inner<Integer> beta;
}

class InputFieldConsolidationCleanParameterizedQualifierNestedMismatch {
	L<String>.Inner<Integer>.Leaf alpha;
	L<Long>.Inner<Integer>.Leaf beta;
}

class InputFieldConsolidationCleanAnnotatedDimensionVsPlain {
	String @TypeAnn [] alpha;
	String[] beta;
}

class InputFieldConsolidationCleanQualifiedTypeAnnotationVsPlain {
	Outer.@TypeAnn Inner alpha;
	Outer.Inner beta;
}

class InputFieldConsolidationCleanNestedQualifierAnnotationMismatch {
	Outer.@TypeAnn Middle.Inner alpha;
	Outer.@CleanInner Middle.Inner beta;
}

class InputFieldConsolidationCleanDeepQualifiedTypeAnnotationMismatch {
	Outer.Middle.@TypeAnn Inner alpha;
	Outer.Middle.@CleanInner Inner beta;
}

class InputFieldConsolidationCleanAnnotatedDimensionPosition {
	String @TypeAnn [][] alpha;
	String[] @TypeAnn [] beta;
}

class InputFieldConsolidationCleanTypeUseAnnotationValueMismatch {
	String @CleanValue(-1) [] alpha;
	String @CleanValue(1) [] beta;
}

class InputFieldConsolidationCleanCStyleAfterAnnotatedTypeDimension {
	int @TypeAnn [] alpha[];
	int @TypeAnn [][] beta;
}

// the same type as `int[] @TypeAnn []`, but not reported: see the coverage doc
class InputFieldConsolidationCleanCStyleAfterAnnotatedTypeDimensionSameType {
	int @TypeAnn [] alpha[];
	int[] @TypeAnn [] beta;
}

class InputFieldConsolidationCleanAnnotatedCStylePrevDeclarator {
	int alpha @TypeAnn [];
	int @TypeAnn [] beta;
}

class InputFieldConsolidationCleanAnnotatedCStyleCurrDeclarator {
	int @TypeAnn [] alpha;
	int beta @TypeAnn [];
}

class InputFieldConsolidationCleanAnnotatedCStyleSecondaryDeclarator {
	int @TypeAnn [] alpha;
	int @TypeAnn [] beta, gamma @TypeAnn [];
}

@interface CleanInner {}

@interface CleanOtherInner {}

@interface CleanContainer {
	CleanInner value();
}

@interface CleanContainerOther {
	CleanOtherInner value();
}

class InputFieldConsolidationCleanNestedAnnotationDiff {
	@CleanContainer(@CleanInner)
	int alpha;
	@CleanContainerOther(@CleanOtherInner)
	int beta;
}

class InputFieldConsolidationCleanNestedSameSimpleNameAnnotations {
	@CleanContainer(@first.CleanInner)
	int alpha;
	@CleanContainer(@second.CleanInner)
	int beta;
}

class InputFieldConsolidationCleanNestedMemberValueSameSimpleNameAnnotations {
	@CleanContainer(value = @first.CleanInner)
	int alpha;
	@CleanContainer(value = @second.CleanInner)
	int beta;
}

class InputFieldConsolidationCleanTypeArgumentSameSimpleNameAnnotations {
	List<@first.TypeAnn String> alpha;
	List<@second.TypeAnn String> beta;
}

class InputFieldConsolidationCleanDimensionSameSimpleNameAnnotations {
	String @first.TypeAnn [] alpha;
	String @second.TypeAnn [] beta;
}

class InputFieldConsolidationCleanQualifiedSegmentSameSimpleNameAnnotations {
	Outer.@first.TypeAnn Inner alpha;
	Outer.@second.TypeAnn Inner beta;
}

class InputFieldConsolidationCleanWildcardBoundSameSimpleNameAnnotations {
	List<? extends @first.TypeAnn Number> alpha;
	List<? extends @second.TypeAnn Number> beta;
}

class InputFieldConsolidationCleanModifierAnnotationPastDepthCap {
	@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W))))))))))))))))))))))))))))))))))))))))))))))))))
	int alpha;
	@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W))))))))))))))))))))))))))))))))))))))))))))))))))
	int beta;
}

class InputFieldConsolidationCleanTypeUseAnnotationPastDepthCap {
	List<@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W)))))))))))))))))))))))))))))))))))))))))))))))))) String> alpha;
	List<@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W)))))))))))))))))))))))))))))))))))))))))))))))))) String> beta;
}

class InputFieldConsolidationCleanStackedDimensionAnnotationOrder {
	String @TypeAnn @CleanInner [] alpha;
	String @CleanInner @TypeAnn [] beta;
}

class InputFieldConsolidationCleanAnnotatedCStylePrevDeclaratorVsPlain {
	int alpha @TypeAnn [];
	int[] beta;
}

class InputFieldConsolidationCleanAnnotatedCStyleCurrDeclaratorVsPlain {
	int[] alpha;
	int beta @TypeAnn [];
}

class InputFieldConsolidationCleanCStyleOnLaterLineAfterAnnotatedTypeDimension {
	int @TypeAnn [][] alpha;
	int @TypeAnn [] beta
			[];
}

class InputFieldConsolidationCleanAnnotatedBoundMismatch {
	List<? extends Number> plain;
	List<? extends @TypeAnn Number> annotated;
}

class InputFieldConsolidationCleanAnnotatedLowerBoundMismatch {
	List<? super Number> plain;
	List<? super @TypeAnn Number> annotated;
}

class InputFieldConsolidationCleanGenericArrayVsPlain {
	List<String[]> array;
	List<String> plain;
}

class InputFieldConsolidationCleanMultiLineDeclaration {
	boolean areInvestmentFundsTreatedAsPensionLiquidity,
			arePensionsTreatedAsSeparateLiquidity,
			areUnvestedRsusExcludedFromSum,
			areUnvestedRsusTreatedAsSeparateLiquidity;
}

class InputFieldConsolidationCleanMultiVarThenDifferentType {
	int alpha, beta;
	String gamma;
}

class InputFieldConsolidationCleanWrappedTwoPerLine {
	boolean areInvestmentFundsTreatedAsPensionLiquidity, arePensionsTreatedAsSeparateLiquidity,
			areUnvestedRsusExcludedFromSum, areUnvestedRsusTreatedAsSeparateLiquidity;
}

class InputFieldConsolidationCleanSameLinePair {
	int alpha; int beta;
}

class InputFieldConsolidationCleanContinuationSemicolonFollowedByDeclaration {
	int alpha;
	int beta,
			gamma; int delta;
}

class InputFieldConsolidationCleanMultilineGenericSharedLine {
	Map<String, Integer> alpha; Map<String,
			Integer> beta;
}

class InputFieldConsolidationCleanPrevLineHoldsAnotherDeclaration {
	String s = "x"; int zebra;
	int alpha;
}

class InputFieldConsolidationCleanLineSecondDeclarationAfterSemicolon {
	int alpha;
	int beta; int gamma;
}

class InputFieldConsolidationCleanVolatileDiffers {
	volatile int alpha;
	int beta;
}

class InputFieldConsolidationCleanTypeDepthAboveCap {
	L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<String>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> alpha;
	L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<String>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> beta;
}

class InputFieldConsolidationCleanNoInitializerThenLong {
	int alpha;
	int beta = 1_000_000 + 2_000_000 + 3_000_000;
}

class InputFieldConsolidationCleanNoInitializerThenMultiLine {
	String alpha;
	String beta = """
			text
			""";
}

class InputFieldConsolidationCleanShortThenLong {
	int alpha = 1;
	int beta = 1_000_000 + 2_000_000 + 3_000_000;
}

class InputFieldConsolidationCleanShortThenMultiLine {
	int alpha = 1;
	int beta = 1 // c
			+ 2;
}

class InputFieldConsolidationCleanLongThenNoInitializer {
	int alpha = 1_000_000 + 2_000_000 + 3_000_000;
	int beta;
}

class InputFieldConsolidationCleanLongThenShort {
	int alpha = 1_000_000 + 2_000_000 + 3_000_000;
	int beta = 1;
}

class InputFieldConsolidationCleanLongThenLong {
	int alpha = 1_000_000 + 2_000_000 + 3_000_000, beta = 1_000_000 + 2_000_000 + 3_000_000;
	int gamma = 1_000_000 + 2_000_000 + 3_000_000;
}

class InputFieldConsolidationCleanLongThenMultiLine {
	int alpha = 1_000_000 + 2_000_000 + 3_000_000;
	int beta = 1 // c
			+ 2;
}

class InputFieldConsolidationCleanMultiLineThenNoInitializer {
	String beta = """
			text
			""";
	String alpha;
}

class InputFieldConsolidationCleanMultiLineThenShort {
	Runnable alpha = () -> {
		System.gc();
	};
	Runnable beta = () -> {};
}

class InputFieldConsolidationCleanMultiLineThenLong {
	Runnable alpha = () -> {
		System.gc();
	};
	Runnable beta = () -> System.out.println("hello");
}

class InputFieldConsolidationCleanMultiLineThenMultiLine {
	Runnable alpha = () -> {
		System.gc();
	};
	Runnable beta = () -> {
		System.gc();
	};
}

class InputFieldConsolidationCleanMultiLineCommentInsideLaterInitializer {
	int alpha;
	int beta
			= /* a
			b */ 1;
}

class InputFieldConsolidationCleanSplitBlockCommentInValue {
	int alpha;
	int beta = 1 /* c */
			+ 2;
}

class InputFieldConsolidationCleanSplitSwitchExpression {
	int alpha;
	int beta = switch (alpha) {
		default -> 1;
	};
}

class InputFieldConsolidationCleanSplitAnonymousClassBody {
	final Object alpha = new Object() {};
	final Object beta = new Object() {
		int counter;
	};
}

class InputFieldConsolidationCleanLongAnonymousClassRunsCodeBetweenBound {
	final Object first = new Object() {
	};
	final Object second = new Object() {
		int counter;
	};
	final Object third = new Object() {
	};
}

class InputFieldConsolidationCleanLongSingleLineAnonymousClassRunsCodeBetweenBound {
	final Object first = new Object() {};
	final Object second = new Object() { int counter; };
	final Object third = new Object() {};
}

class InputFieldConsolidationCleanLongBetweenDependentReader {
	int alpha;
	int beta = 1_000_000 + 2_000_000 + 3_000_000;
	int gamma = beta;
}

class InputFieldConsolidationCleanLongReaderInitForwardRead {
	int alpha;
	int total = this.count * 2 + 10_000_000;
	int count = 3;
}

class InputFieldConsolidationCleanPinnedLongRunsBetween {
	private int base = 1;
	private int mid = Integer.parseInt("1234567890");
	private int max = 2;
}

@SuppressWarnings("FieldSorting")
class InputFieldConsolidationCleanSortingSuppressedLongBetween {
	int alpha;
	int beta = 1_000_000 + 2_000_000 + 3_000_000;
	int gamma;
}

class InputFieldConsolidationCleanLongFirstNameOfDeclarationThenShort {
	int alpha = 1_000_000 + 2_000_000 + 3_000_000, beta;
	int gamma;
}

class InputFieldConsolidationCleanLongLastNameOfDeclarationThenShort {
	int alpha, beta = 1_000_000 + 2_000_000 + 3_000_000;
	int gamma;
}

class InputFieldConsolidationCleanShortThenLongFirstNameOfDeclaration {
	int gamma;
	int alpha = 1_000_000 + 2_000_000 + 3_000_000, beta;
}

class InputFieldConsolidationCleanLongBetweenCommentLine {
	int alpha;
	// note
	int beta = 1_000_000 + 2_000_000 + 3_000_000;
	int gamma;
}

class InputFieldConsolidationCleanLongBetweenThenMethod {
	int alpha;
	int beta = 1_000_000 + 2_000_000 + 3_000_000;

	void method() {
	}

	int gamma;
}

class InputFieldConsolidationCleanInitForwardReadQualifiedStringConstantLong {
	private static final java.lang.String total = InputFieldConsolidationCleanInitForwardReadQualifiedStringConstantLong.label + "x";
	private static final java.lang.String label = "w";
}

class InputFieldConsolidationCleanInitForwardReadAnonymousFieldInitializerShort {
	private int t = new Object() { int s = c; }.s;
	private int c = 3;
}

class InputFieldConsolidationCleanInitForwardReadInLambdaShort {
	static class Q {
		static IntSupplier total = () -> Q.count.getAsInt();
		static IntSupplier count = () -> 3;
	}
}

class InputFieldConsolidationCleanInitForwardReadStaticQualifiedShort {
	static class Q {
		private static int total = Q.count * 2;
		private static int count = 3;
	}
}

class InputFieldConsolidationCleanLengthBoundaryArrayInitializer {
	int[] alpha;
	int[] abcd = {1, 2, 3, 4, 5, 6, 7, 8, 9};
}

class InputFieldConsolidationCleanLengthBoundaryCStyleBracketsNotCounted {
	int[] alpha;
	int n12345678901234567890123456[] = {12};
}

class InputFieldConsolidationCleanLengthBoundaryCast {
	int alpha;
	int n1234567890123 = (int) 123456789.0;
}

class InputFieldConsolidationCleanLengthBoundaryCharEscape {
	char alpha;
	char n12345678901234567890123456 = '\n';
}

class InputFieldConsolidationCleanLengthBoundaryCommentCounted {
	int alpha;
	int n12345678901234567 = 1 /* c */ + 2;
}

class InputFieldConsolidationCleanLengthBoundaryDiamond {
	List<String> first;
	List<String> n1234567890123 = new ArrayList<>();
}

class InputFieldConsolidationCleanLengthBoundaryFirstDeclaration {
	String s = "1234567890123456789012345678";
	String t;
}

class InputFieldConsolidationCleanLengthBoundaryHexLiteral {
	int alpha;
	int n1234567890123456789 = 0xFF_FF_FFF;
}

class InputFieldConsolidationCleanLengthBoundaryLambda {
	Runnable first;
	Runnable r = () -> System.out.println("hi");
}

class InputFieldConsolidationCleanLengthBoundaryLeadingCommentCounted {
	int alpha;
	int n123456789012345678901 = /* c */ 1;
}

class InputFieldConsolidationCleanLengthBoundaryLongName {
	int alpha;
	int n1234567890123456789012345678 = 12;
}

class InputFieldConsolidationCleanLengthBoundaryMethodCall {
	String first;
	String s = String.valueOf(1234567890123L);
}

class InputFieldConsolidationCleanLengthBoundaryMethodReference {
	Runnable first;
	Runnable printerRefsX = System.out::println;
}

class InputFieldConsolidationCleanLengthBoundaryNewWithArguments {
	Object first;
	Object o = new StringBuilder("123456789");
}

class InputFieldConsolidationCleanLengthBoundaryNumericSuffix {
	long alpha;
	long n123456789012345 = 99_999_999_999L;
}

class InputFieldConsolidationCleanLengthBoundaryOneCharName {
	String first;
	String s = "1234567890123456789012345678";
}

class InputFieldConsolidationCleanLengthBoundaryQualifiedConstant {
	int alpha;
	int n12345678901 = Thread.MAX_PRIORITY;
}

class InputFieldConsolidationCleanLengthBoundarySingleLineAnonymousClass {
	final Object first = new Object() {};
	final Object n123456789012345 = new Object() {};
}

class InputFieldConsolidationCleanLengthBoundarySplitValueJoined {
	String first;
	String s = "1234567890123456789012"
			+ "x";
}

class InputFieldConsolidationCleanLengthBoundarySplitCommentBetweenNameAndEquals {
	int alpha;
	int beta /* c */
			= 5;
}

class InputFieldConsolidationCleanLengthBoundarySplitValueTightJoin {
	String first;
	String b = "123456789012345678901"
			.trim();
}

class InputFieldConsolidationCleanLengthBoundaryStringEscapes {
	String first;
	String s = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n";
}

class InputFieldConsolidationCleanLengthBoundarySupplementaryCodePoints {
	String first;
	String s = "𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥";
}

class InputFieldConsolidationCleanLengthBoundaryTernary {
	int alpha;
	int n123 = 1 > 0 ? 1_000_000 : 999_999;
}

class InputFieldConsolidationCleanLengthBoundaryTypeWitness {
	List<Integer> first;
	List<Integer> n12345678 = List.<Integer>of(1, 2);
}

class InputFieldConsolidationCleanLengthBoundaryUnderscores {
	int alpha;
	int n12345678901234 = 100_000 + 10_000;
}

class InputFieldConsolidationCleanLengthBoundaryWhitespaceAroundEquals {
	int alpha;
	int n1234567890123456789012345678=12;
}

class InputFieldConsolidationCleanConstants {
	static final int ALPHA = 1;
	static final int BETA = 2;
}

class InputFieldConsolidationCleanConstantVisibilities {
	private static final int A = 1;
	private static final int B = 2;
	protected static final int C = 3;
	protected static final int D = 4;
	public static final int E = 5;
	public static final int F = 6;
}

class InputFieldConsolidationCleanConstantTransient {
	private static final transient int A = 1;
	private static final transient int B = 2;
}

class InputFieldConsolidationCleanConstantAnnotated {
	@Deprecated static final int A = 1;
	@Deprecated static final int B = 2;
}

class InputFieldConsolidationCleanConstantMultiNameFirst {
	static final int A = 1, B = 2;
	static final int C = 3;
}

class InputFieldConsolidationCleanConstantMultiNameLast {
	static final int A = 1;
	static final int B = 2, C = 3;
}

class InputFieldConsolidationCleanConstantFirstOfMixedDeclaration {
	static final int A = 1, B;
	static final int C;

	static {
		B = 2;
		C = 3;
	}
}

class InputFieldConsolidationCleanConstantLastOfMixedDeclaration {
	static final int B, A = 1;
	static final int C;

	static {
		B = 2;
		C = 3;
	}
}

class InputFieldConsolidationCleanConstantBeforeNonConstants {
	static final int A = 1;
	static final int B;
	static int c = 1;
	final int d = 1;

	static {
		B = 2;
	}
}

class InputFieldConsolidationCleanConstantAfterNonConstants {
	final int d = 1;
	static int c = 1;
	static final int B;
	static final int A = 1;

	static {
		B = 2;
	}
}

class InputFieldConsolidationCleanConstantObjects {
	static final Object LOCK = new Object();
	static final Object OTHER = new Object();
}

class InputFieldConsolidationCleanConstantReadAtInitBlocksMerge {
	static int alpha;
	static final int LIMIT = InputFieldConsolidationCleanConstantReadAtInitBlocksMerge.gamma + 1;
	static int gamma = 2;
}

class InputFieldConsolidationCleanConstantReadByRunMemberBlocksMerge {
	static int alpha;
	static final int LIMIT = 5;
	static int gamma = LIMIT + 1;
}

class InputFieldConsolidationCleanConstantRunningCodePinsInitializedNeighbour {
	static int alpha = 1;
	static final int LIMIT = Integer.parseInt("5");
	static int gamma = 2;
}

@SuppressWarnings("FieldSorting")
class InputFieldConsolidationCleanConstantBetweenInOrder {
	static int beta;
	static final int LIMIT = 5;
	static int alpha;
}

class InputFieldConsolidationCleanConstantJavadocEndsStretch {
	static int alpha;
	/** The limit. */
	static final int LIMIT = 5;
	static int gamma;
}

interface InputFieldConsolidationCleanInterfaceConstants {
	int ALPHA = 1;
	int BETA = 2;
}

interface InputFieldConsolidationCleanInterfaceConstantsMultiName {
	int A = 1, B = 2;
	int C = 3;
}

interface InputFieldConsolidationCleanInterfaceConstantsGeneric {
	List<String> NAMES = List.of("a");
	List<String> OTHERS = List.of("b");
}

interface InputFieldConsolidationCleanInterfaceExplicitConstants {
	public static final int A = 1;
	public static final int B = 2;
}

@interface InputFieldConsolidationCleanAnnotationTypeConstants {
	int ALPHA = 1;
	int BETA = 2;
}

@interface InputFieldConsolidationCleanAnnotationTypeConstantsBesideElement {
	int ALPHA = 1;
	int BETA = 2;

	int value() default 0;
}

class InputFieldConsolidationCleanInterfaceNestedInClass {
	interface Nested {
		int A = 1;
		int B = 2;
	}
}

enum InputFieldConsolidationCleanEnumConstants {
	ONE;

	static final int MAX = 10;
	static final int MIN = 1;
}

enum InputFieldConsolidationCleanEnumConstantBodyConstants {
	ONE {
		static final int A = 1;
		static final int B = 2;
	}
}

record InputFieldConsolidationCleanRecordConstants(int x) {
	static final int A = 1;
	static final int B = 2;
}

class InputFieldConsolidationCleanLocalClassConstants {
	void run() {
		class Local {
			static final int A = 1;
			static final int B = 2;
		}
	}
}

class InputFieldConsolidationCleanAnonymousBodyConstants {
	Object holder = new Object() {
		static final int A = 1;
		static final int B = 2;
	};
}

class InputFieldConsolidationCleanInnerClassConstants {
	class Inner {
		static final int A = 1;
		static final int B = 2;
	}
}

interface InputFieldConsolidationCleanClassNestedInInterfaceConstants {
	class Impl {
		static final int A = 1;
		static final int B = 2;
	}
}

class InputFieldConsolidationCleanConstantCStyleBrackets {
	static final int A[] = {1};
	static final int[] B = {2};
}

class InputFieldConsolidationCleanConstantSplit {
	static final int A =
			1;
	static final int B = 2;
}

class InputFieldConsolidationCleanConstantAnonymousValues {
	static final Object FIRST = new Object() {
	};
	static final Object SECOND = new Object() {
	};
}

class InputFieldConsolidationCleanConstantFinalStaticOrder {
	final static int A = 1;
	final static int B = 2;
}

class InputFieldConsolidationCleanConstantMixedModifierOrder {
	static final int A = 1;
	final static int B = 2;
}

class InputFieldConsolidationCleanInitForwardReadQualifiedStringConstant {
	static class Q {
		private static final java.lang.String total = Q.label + "x";
		private static final java.lang.String label = "w";
	}
}

class InputFieldConsolidationCleanPinnedJdkCallBetween {
	private int base = 1;
	private String label = String.valueOf(1);
	private int max = 3;
}

class InputFieldConsolidationCleanLambdaLocalWithNamedAnnotationElement {
	private final Runnable task = () -> {
		@SuppressWarnings(value = "unused") final var unused = 0;
	};
	private final Runnable other = () -> {
	};
}

final class InputFieldConsolidationCleanPinnedThisInStringConcatenation {
	private final String user = System.getProperty("user.name");
	private final Object lock = new Object();
	private final String tag = "session:" + this;

	@Override
	public String toString() {
		return user + lock;
	}
}