package com.etk2000.checkstyle.inputs.fieldsorting;

import java.util.List;
import java.util.Map;

@interface TAnnA {}

@interface TAnnB {}

class InputFieldSortingTypeArgAnnotationViolationAnnotatedBeforeUnannotated {
	List<@TAnnA String> annotated;
	List<String> plain; // violation: Field 'plain' (type argument unannotated) must appear before 'annotated' (type argument annotated @TAnnA), same type.
}

class InputFieldSortingTypeArgAnnotationViolationWrongOrder {
	List<@TAnnB String> bField;
	List<@TAnnA String> aField; // violation: Field 'aField' (type argument annotated @TAnnA) must appear before 'bField' (type argument annotated @TAnnB), same type.
}

class InputFieldSortingTypeArgAnnotationViolationMoreBeforeFewer {
	List<@TAnnA @TAnnB String> twoAnns;
	List<@TAnnA String> oneAnn; // violation: Field 'oneAnn' (type argument annotated @TAnnA) must appear before 'twoAnns' (type argument annotated @TAnnA), same type.
}

class InputFieldSortingTypeArgAnnotationViolationSecondArgAnnotated {
	Map<String, @TAnnA Integer> annotated;
	Map<String, Integer> plain; // violation: Field 'plain' (type argument unannotated) must appear before 'annotated' (type argument annotated @TAnnA), same type.
}

class InputFieldSortingTypeArgAnnotationViolationPositionAware {
	Map<@TAnnA String, String> firstArgAnnotated;
	Map<String, @TAnnA String> firstArgUnannotated; // violation: Field 'firstArgUnannotated' (type argument unannotated) must appear before 'firstArgAnnotated' (type argument annotated @TAnnA), same type.
}

class InputFieldSortingTypeArgAnnotationViolationWildcard {
	List<@TAnnB ? extends Number> bField;
	List<@TAnnA ? extends Number> aField; // violation: Field 'aField' (type argument annotated @TAnnA) must appear before 'bField' (type argument annotated @TAnnB), same type.
}

@interface TAnnParam {
	int value() default 0;
}

class InputFieldSortingTypeArgAnnotationViolationParameterized {
	List<@TAnnParam(2) String> higher;
	List<@TAnnParam(1) String> lower; // violation: Field 'lower' (type argument annotated @TAnnParam) must appear before 'higher' (type argument annotated @TAnnParam), same type.
}

@SuppressWarnings("PreferImport")
class InputFieldSortingTypeArgAnnotationViolationQualified {
	java.util.Set<@TAnnA String> annotated;
	java.util.Set<String> plain; // violation: Field 'plain' (type argument unannotated) must appear before 'annotated' (type argument annotated @TAnnA), same type.
}

class InputFieldSortingTypeArgAnnotationViolationWildcardBound {
	List<? extends @TAnnB Number> bField;
	List<? extends @TAnnA Number> aField; // violation: Field 'aField' (type argument annotated @TAnnA) must appear before 'bField' (type argument annotated @TAnnB), same type.
}

class InputFieldSortingTypeArgAnnotationViolationLowerBound {
	List<? super @TAnnB Number> bField;
	List<? super @TAnnA Number> aField; // violation: Field 'aField' (type argument annotated @TAnnA) must appear before 'bField' (type argument annotated @TAnnB), same type.
}

class InputFieldSortingTypeArgAnnotationViolationSameAnnotationsFallsToName {
	@Deprecated
	List<@TAnnA String> zebra = List.of();
	@Deprecated
	List<@TAnnA String> alpha = List.of(); // violation: Field 'alpha' must appear before 'zebra' (alphabetical order, same type).
}