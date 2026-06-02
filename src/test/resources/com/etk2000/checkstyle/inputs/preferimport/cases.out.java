package com.etk2000.checkstyle.inputs.preferimport;

// === case: annotated_type_arg_fqn ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferImportAnnotatedTypeArgFqnSliceViolation {
	@interface Ann {}

	List<@Ann Map> field;
}
// === end ===

// === case: annotation_arg_fqn ===
class InputPreferImportAnnotationArgFqnSliceViolation {
	@interface Cap {
		int value();
	}

	@Cap(Integer.MAX_VALUE)
	void m() {}
}
// === end ===

// === case: annotation_fqn ===
@SuppressWarnings("unused")
class InputPreferImportAnnotationFqnSliceViolation {}
// === end ===

// === case: annotation_fqn_wildcard ===
// imports: java.util.*
// skip-reason: the short name is a java.lang type but a wildcard import in scope could bind the same simple name
@java.lang.SuppressWarnings("unused")
class InputPreferImportAnnotationFqnWildcardSliceViolation {}
// === end ===

// === case: array_of_generic_type_arg ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferImportArrayOfGenericTypeArgSliceViolation {
	List<Map>[] field;
}
// === end ===

// === case: block_comment_package_masked ===
// skip-reason: no single-type import, same-package type, or java.lang type binds the short name; adding a new import is the general PreferImport fixer's job
class InputPreferImportBlockCommentPackageMaskedSliceViolation {
	/*
package com.etk2000.checkstyle.gradle.fix;
	*/
	com.etk2000.checkstyle.gradle.fix.PreferImportFixer fixer;
}
// === end ===

// === case: block_comment_wildcard_java_lang_strips ===
class InputPreferImportBlockCommentWildcardJavaLangStripsSliceViolation {
	/*
import foo.*;
	*/
	Runnable task;
}
// === end ===

// === case: cast ===
// imports: java.util.List
class InputPreferImportCastSliceViolation {
	void m(Object obj) {
		System.out.println((List<?>) obj);
	}
}
// === end ===

// === case: cast_type_arg ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferImportCastTypeArgSliceViolation {
	void m(Object obj) {
		System.out.println((List<Map>) obj);
	}
}
// === end ===

// === case: class_literal_fqn ===
// imports: java.util.List
class InputPreferImportClassLiteralFqnSliceViolation {
	Class<?> type() {
		return List.class;
	}
}
// === end ===

// === case: collision_different_fqn ===
// imports: java.util.List
// skip-reason: the short name resolves to a different type than the qualified name (a single-type import, same-package type, or dependency type with the same simple name shadows it)
class InputPreferImportCollisionDifferentFqnSliceViolation {
	com.foo.List<String> field;
}
// === end ===

// === case: enclosing_type_collision_resolves ===
class java {
	Runnable f;
}
// === end ===

// === case: extends_fqn ===
// imports: java.util.ArrayList
class InputPreferImportExtendsFqnSliceViolation
		extends ArrayList<String> {}
// === end ===

// === case: field_type ===
// imports: java.util.Map
class InputPreferImportFieldTypeSliceViolation {
	Map<String, Integer> field;
}
// === end ===

// === case: generic_type_arg ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferImportGenericTypeArgSliceViolation {
	List<Map<String, Integer>> field;
}
// === end ===

// === case: implements_fqn ===
// imports: java.io.Serializable
class InputPreferImportImplementsFqnSliceViolation
		implements Serializable {}
// === end ===

// === case: instanceof_fqn ===
// imports: java.util.List
class InputPreferImportInstanceofSliceViolation {
	void m(Object obj) {
		if (obj instanceof List)
			System.out.println(obj);
	}
}
// === end ===

// === case: java_lang_type ===
class InputPreferImportJavaLangTypeSliceViolation {
	Runnable task;
}
// === end ===

// === case: leading_comment_line_java_lang_strips ===
// leading comment before any declaration
class InputPreferImportLeadingCommentLineJavaLangStripsSliceViolation {
	Runnable task;
}
// === end ===

// === case: local_var_type ===
// imports: java.util.List
class InputPreferImportLocalVarTypeSliceViolation {
	void m() {
		final List<String> local = null;
		System.out.println(local);
	}
}
// === end ===

// === case: mask_no_leak_next_line_wildcard ===
/*
comment
*/
// imports: foo.*
// skip-reason: the short name is a java.lang type but a wildcard import in scope could bind the same simple name
class InputPreferImportMaskNoLeakNextLineWildcardSliceViolation {
	java.lang.Runnable task;
}
// === end ===

// === case: method_call_witness_type_arg ===
// imports: java.util.Map
class InputPreferImportMethodCallWitnessTypeArgSliceViolation {
	<T> T pick() {
		return null;
	}

	void useWitness() {
		this.<Map>pick();
	}
}
// === end ===

// === case: method_param_type ===
// imports: java.util.Set
class InputPreferImportMethodParamTypeSliceViolation {
	void m(Set<Integer> param) {
		System.out.println(param);
	}
}
// === end ===

// === case: method_ref_fqn ===
class InputPreferImportMethodRefFqnSliceViolation {
	Runnable r = Thread::yield;
}
// === end ===

// === case: method_ref_witness_type_arg ===
// imports: java.util.ArrayList
// imports: java.util.Map
// imports: java.util.function.Supplier
class InputPreferImportMethodRefWitnessTypeArgSliceViolation {
	Supplier<ArrayList<Map>> ref = ArrayList::<Map>new;
}
// === end ===

// === case: method_return_type ===
// imports: java.util.List
class InputPreferImportMethodReturnTypeSliceViolation {
	List<String> m() {
		return null;
	}
}
// === end ===

// === case: mixed_fqn_and_nested ===
// imports: java.util.Map
class InputPreferImportMixedFqnAndNestedSliceViolation {
	Map.Entry nestedFirst() {
		return null;
	}

	java.util.List<String> qualifiedSecond() {
		return null;
	}
}
// === end ===

// === case: nested_annotation ===
// imports: java.util.Map
// skip-reason: no single-type import, same-package type, or java.lang type binds the short name; adding a new import is the general PreferImport fixer's job
@Map.Entry
class InputPreferImportNestedAnnotationSliceViolation {}
// === end ===

// === case: nested_extends ===
// imports: java.util.Map
// skip-reason: no single-type import, same-package type, or java.lang type binds the short name; adding a new import is the general PreferImport fixer's job
class InputPreferImportNestedExtendsSliceViolation
		extends Map.Entry {}
// === end ===

// === case: nested_generic ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferImportNestedGenericSliceViolation {
	List<Map<String, Integer>> nested() {
		return null;
	}
}
// === end ===

// === case: nested_implements ===
// imports: java.util.Map
// skip-reason: no single-type import, same-package type, or java.lang type binds the short name; adding a new import is the general PreferImport fixer's job
class InputPreferImportNestedImplementsSliceViolation
		implements Map.Entry {}
// === end ===

// === case: nested_java_lang ===
// skip-reason: no single-type import, same-package type, or java.lang type binds the short name; adding a new import is the general PreferImport fixer's job
class InputPreferImportNestedJavaLangSliceViolation {
	Thread.State state;
}
// === end ===

// === case: nested_new ===
// imports: java.util.Map
// skip-reason: no single-type import, same-package type, or java.lang type binds the short name; adding a new import is the general PreferImport fixer's job
class InputPreferImportNestedNewSliceViolation {
	void m() {
		final var e = new Map.Entry();
		System.out.println(e);
	}
}
// === end ===

// === case: nested_same_file_decl_kinds ===
@interface NdkAnnRoot {
	class N {}
}

class NdkClassRoot {
	class N {}
}

enum NdkEnumRoot {
	VALUE;

	class N {}
}

interface NdkIfaceRoot {
	class N {}
}

record NdkRecRoot() {
	class N {}
}

class InputPreferImportNestedSameFileDeclKindsSliceViolation {
	NdkAnnRoot.N annotationRoot;
	NdkClassRoot.N classRoot;
	NdkEnumRoot.N enumRoot;
	NdkIfaceRoot.N interfaceRoot;
	NdkRecRoot.N recordRoot;
}
// === end ===

// === case: nested_single_import ===
// imports: java.util.Map
// skip-reason: no single-type import, same-package type, or java.lang type binds the short name; adding a new import is the general PreferImport fixer's job
class InputPreferImportNestedSingleImportSliceViolation {
	Map.Entry<String, Integer> entry;
}
// === end ===

// === case: nested_three_segment ===
// skip-reason: the short name is shadowed by a type declaration or type parameter in this file
class InputPreferImportNestedThreeSegmentOuter {
	static class Mid {
		static class Leaf {}
	}
}

class InputPreferImportNestedThreeSegmentSliceViolation {
	InputPreferImportNestedThreeSegmentOuter.Mid.Leaf field;
}
// === end ===

// === case: nested_throws ===
// imports: java.util.Map
// skip-reason: no single-type import, same-package type, or java.lang type binds the short name; adding a new import is the general PreferImport fixer's job
class InputPreferImportNestedThrowsSliceViolation {
	void m() throws Map.Entry {}
}
// === end ===

// === case: nested_type_arg ===
// imports: java.util.List
// imports: java.util.Map
// skip-reason: no single-type import, same-package type, or java.lang type binds the short name; adding a new import is the general PreferImport fixer's job
class InputPreferImportNestedTypeArgSliceViolation {
	List<Map.Entry> field;
}
// === end ===

// === case: nested_type_exact ===
// imports: java.util.Map
// imports: java.util.Map.Entry
class InputPreferImportNestedTypeExactSliceViolation {
	Entry<String, Integer> entry;
}
// === end ===

// === case: nested_type_prefix ===
// imports: java.util.Map
// skip-reason: no single-type import, same-package type, or java.lang type binds the short name; adding a new import is the general PreferImport fixer's job
class InputPreferImportNestedTypePrefixSliceViolation {
	java.util.Map.Entry<String, Integer> entry;
}
// === end ===

// === case: nested_wildcard_bound ===
// imports: java.util.List
// imports: java.util.Map
// skip-reason: no single-type import, same-package type, or java.lang type binds the short name; adding a new import is the general PreferImport fixer's job
class InputPreferImportNestedWildcardBoundSliceViolation {
	List<? extends Map.Entry> field;
}
// === end ===

// === case: nested_wildcard_import ===
// imports: java.util.*
// skip-reason: resolution depends on a wildcard import whose contents cannot be confirmed from this file
class InputPreferImportNestedWildcardImportSliceViolation {
	Map.Entry<String, Integer> entry;
}
// === end ===

// === case: nested_wildcard_no_bind ===
// imports: java.io.*
// skip-reason: resolution depends on a wildcard import whose contents cannot be confirmed from this file
class InputPreferImportNestedWildcardNoBindSliceViolation {
	Map.Entry<String, Integer> entry;
}
// === end ===

// === case: new_expression ===
// imports: java.util.ArrayList
class InputPreferImportNewExpressionSliceViolation {
	void m() {
		final var list = new ArrayList<String>();
		System.out.println(list);
	}
}
// === end ===

// === case: new_expression_nested_type_arg ===
// imports: java.util.ArrayList
// imports: java.util.Map
// skip-reason: no single-type import, same-package type, or java.lang type binds the short name; adding a new import is the general PreferImport fixer's job
class InputPreferImportNewExpressionNestedTypeArgSliceViolation {
	void m() {
		new ArrayList<java.util.Map.Entry>();
	}
}
// === end ===

// === case: new_expression_type_arg ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferImportNewExpressionTypeArgSliceViolation {
	void m() {
		new ArrayList<List>();
	}
}
// === end ===

// === case: new_expression_type_arg_offclasspath ===
// imports: java.util.ArrayList
// skip-reason: no single-type import, same-package type, or java.lang type binds the short name; adding a new import is the general PreferImport fixer's job
class InputPreferImportNewExpressionTypeArgOffclasspathSliceViolation {
	void m() {
		new ArrayList<com.foo.Unknown>();
	}
}
// === end ===

// === case: real_import_collision_unmasked ===
// imports: some.other.PreferImportFixer
// skip-reason: the short name resolves to a different type than the qualified name (a single-type import, same-package type, or dependency type with the same simple name shadows it)
class InputPreferImportRealImportCollisionUnmaskedSliceViolation {
	com.etk2000.checkstyle.gradle.fix.PreferImportFixer fixer;
}
// === end ===

// === case: real_wildcard_blocks_java_lang ===
// imports: foo.*
// skip-reason: the short name is a java.lang type but a wildcard import in scope could bind the same simple name
class InputPreferImportRealWildcardBlocksJavaLangSliceViolation {
	java.lang.Runnable task;
}
// === end ===

// === case: reflection_dependency_collision ===
// package: com.etk2000.checkstyle.gradle.fix
// skip-reason: the short name resolves to a different type than the qualified name (a single-type import, same-package type, or dependency type with the same simple name shadows it)
class InputPreferImportReflectionDependencyCollisionSliceViolation {
	some.other.PreferImportFixer fixer;
}
// === end ===

// === case: reflection_dependency_strip ===
// package: com.etk2000.checkstyle.gradle.fix
class InputPreferImportReflectionDependencyStripSliceViolation {
	PreferImportFixer fixer;
}
// === end ===

// === case: reflection_dependency_strip_block_comment ===
// package: com.etk2000.checkstyle.gradle.fix
class InputPreferImportReflectionDependencyStripBlockCommentSliceViolation {
	/*
import some.other.PreferImportFixer;
	*/
	PreferImportFixer fixer;
}
// === end ===

// === case: reflection_dependency_strip_text_block ===
// package: com.etk2000.checkstyle.gradle.fix
class InputPreferImportReflectionDependencyStripTextBlockSliceViolation {
	PreferImportFixer fixer;
	String doc = """
import some.other.PreferImportFixer;
""";
}
// === end ===

// === case: shadow_annotation_type ===
// skip-reason: the short name is shadowed by a type declaration or type parameter in this file
class InputPreferImportShadowAnnotationTypeSliceViolation {
	@interface Map {}

	java.util.Map<String, Integer> field;
}
// === end ===

// === case: shadow_enum_type ===
// skip-reason: the short name is shadowed by a type declaration or type parameter in this file
class InputPreferImportShadowEnumTypeSliceViolation {
	enum Map { A }

	java.util.Map<String, Integer> field;
}
// === end ===

// === case: shadow_interface_type ===
// skip-reason: the short name is shadowed by a type declaration or type parameter in this file
class InputPreferImportShadowInterfaceTypeSliceViolation {
	interface List {}

	java.util.List<String> field;
}
// === end ===

// === case: shadow_nested_type ===
// skip-reason: the short name is shadowed by a type declaration or type parameter in this file
class InputPreferImportShadowNestedTypeSliceViolation {
	static final class List {}

	java.util.List<String> field;
}
// === end ===

// === case: shadow_record_type ===
// skip-reason: the short name is shadowed by a type declaration or type parameter in this file
class InputPreferImportShadowRecordTypeSliceViolation {
	record List() {}

	java.util.List<String> field;
}
// === end ===

// === case: shadow_type_param ===
// skip-reason: the short name is shadowed by a type declaration or type parameter in this file
class InputPreferImportShadowTypeParamSliceViolation<List> {
	java.util.List<String> field;
}
// === end ===

// === case: static_call_fqn ===
// imports: java.util.List
class InputPreferImportStaticCallFqnSliceViolation {
	void m() {
		List.of(1, 2);
	}
}
// === end ===

// === case: static_call_type_witness ===
// imports: java.util.List
class InputPreferImportStaticCallTypeWitnessSliceViolation {
	void m() {
		List.<Integer>of(1);
	}
}
// === end ===

// === case: static_call_witness_fqn ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferImportStaticCallWitnessFqnSliceViolation {
	void m() {
		List.<Map>of();
	}
}
// === end ===

// === case: static_field_fqn ===
class InputPreferImportStaticFieldFqnSliceViolation {
	int max() {
		return Integer.MAX_VALUE;
	}
}
// === end ===

// === case: text_block_package_masked ===
// skip-reason: no single-type import, same-package type, or java.lang type binds the short name; adding a new import is the general PreferImport fixer's job
class InputPreferImportTextBlockPackageMaskedSliceViolation {
	com.etk2000.checkstyle.gradle.fix.PreferImportFixer fixer;
	String s = """
package com.etk2000.checkstyle.gradle.fix;
""";
}
// === end ===

// === case: text_block_wildcard_java_lang_strips ===
class InputPreferImportTextBlockWildcardJavaLangStripsSliceViolation {
	Runnable task;
	String s = """
import foo.*;
""";
}
// === end ===

// === case: throws_fqn ===
// imports: java.io.IOException
class InputPreferImportThrowsSliceViolation {
	void m() throws IOException {}
}
// === end ===

// === case: type_arg_two_fqns_one_line ===
// imports: java.util.List
// imports: java.util.Map
// imports: java.util.Set
class InputPreferImportTypeArgTwoFqnsOneLineSliceViolation {
	Map<List, Set> field;
}
// === end ===

// === case: whitespace_split_qualified_name ===
// skip-reason: the qualified name is not contiguous at the violation column (comments or unusual whitespace split the dotted run)
class InputPreferImportWhitespaceSplitSliceViolation {
	java . util . List field;
}
// === end ===

// === case: wildcard_lower_bound_type_arg ===
// imports: java.util.List
// imports: java.util.Set
class InputPreferImportWildcardLowerBoundTypeArgSliceViolation {
	List<? super Set> field;
}
// === end ===

// === case: wildcard_only ===
// imports: java.util.*
// skip-reason: resolution depends on a wildcard import whose contents cannot be confirmed from this file
class InputPreferImportWildcardOnlySliceViolation {
	java.util.Map<String, Integer> field;
}
// === end ===

// === case: wildcard_upper_bound_type_arg ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferImportWildcardUpperBoundTypeArgSliceViolation {
	List<? extends Map> field;
}
// === end ===