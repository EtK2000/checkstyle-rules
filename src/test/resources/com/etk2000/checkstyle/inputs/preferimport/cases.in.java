package com.etk2000.checkstyle.inputs.preferimport;

// === case: annotated_type_arg_fqn ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferImportAnnotatedTypeArgFqnSliceViolation {
	@interface Ann {}

	List<@Ann java.util.Map> field; // violation: Use an import instead of fully qualified name 'java.util.Map'.
}
// === end ===

// === case: annotation_arg_fqn ===
class InputPreferImportAnnotationArgFqnSliceViolation {
	@interface Cap {
		int value();
	}

	@Cap(java.lang.Integer.MAX_VALUE) // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	void m() {}
}
// === end ===

// === case: annotation_fqn ===
@java.lang.SuppressWarnings("unused") // violation: Use an import instead of fully qualified name 'java.lang.SuppressWarnings'.
class InputPreferImportAnnotationFqnSliceViolation {}
// === end ===

// === case: annotation_fqn_wildcard ===
// imports: java.util.*
// skip-reason: cannot shorten the name: the short name is a java.lang type but a wildcard import in scope could bind the same simple name
@java.lang.SuppressWarnings("unused") // violation: Use an import instead of fully qualified name 'java.lang.SuppressWarnings'.
class InputPreferImportAnnotationFqnWildcardSliceViolation {}
// === end ===

// === case: array_of_generic_type_arg ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferImportArrayOfGenericTypeArgSliceViolation {
	List<java.util.Map>[] field; // violation: Use an import instead of fully qualified name 'java.util.Map'.
}
// === end ===

// === case: block_comment_package_masked ===
// skip-reason: cannot shorten the name: no single-type import, same-package type, or java.lang type binds the short name (adding an import is the general PreferImport fixer's job)
class InputPreferImportBlockCommentPackageMaskedSliceViolation {
	/*
package com.etk2000.checkstyle.gradle.fix;
	*/
	com.etk2000.checkstyle.gradle.fix.PreferImportFixer fixer; // violation: Use an import instead of fully qualified name 'com.etk2000.checkstyle.gradle.fix.PreferImportFixer'.
}
// === end ===

// === case: block_comment_wildcard_java_lang_strips ===
class InputPreferImportBlockCommentWildcardJavaLangStripsSliceViolation {
	/*
import foo.*;
	*/
	java.lang.Runnable task; // violation: Use an import instead of fully qualified name 'java.lang.Runnable'.
}
// === end ===

// === case: cast ===
// imports: java.util.List
class InputPreferImportCastSliceViolation {
	void m(Object obj) {
		System.out.println((java.util.List<?>) obj); // violation: Use an import instead of fully qualified name 'java.util.List'.
	}
}
// === end ===

// === case: cast_type_arg ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferImportCastTypeArgSliceViolation {
	void m(Object obj) {
		System.out.println((java.util.List<java.util.Map>) obj); // violation: Use an import instead of fully qualified name 'java.util.List'. // violation: Use an import instead of fully qualified name 'java.util.Map'.
	}
}
// === end ===

// === case: class_literal_fqn ===
// imports: java.util.List
class InputPreferImportClassLiteralFqnSliceViolation {
	Class<?> type() {
		return java.util.List.class; // violation: Use an import instead of fully qualified name 'java.util.List'.
	}
}
// === end ===

// === case: collision_different_fqn ===
// imports: java.util.List
// skip-reason: cannot shorten the name: the short name resolves to a different type than the qualified name (a single-type import, same-package type, or dependency type with the same simple name shadows it)
class InputPreferImportCollisionDifferentFqnSliceViolation {
	com.foo.List<String> field; // violation: Use an import instead of fully qualified name 'com.foo.List'.
}
// === end ===

// === case: enclosing_type_collision_resolves ===
class java {
	java.lang.Runnable f; // violation: Use an import instead of fully qualified name 'java.lang.Runnable'.
}
// === end ===

// === case: extends_fqn ===
// imports: java.util.ArrayList
class InputPreferImportExtendsFqnSliceViolation
		extends java.util.ArrayList<String> {} // violation: Use an import instead of fully qualified name 'java.util.ArrayList'.
// === end ===

// === case: field_type ===
// imports: java.util.Map
class InputPreferImportFieldTypeSliceViolation {
	java.util.Map<String, Integer> field; // violation: Use an import instead of fully qualified name 'java.util.Map'.
}
// === end ===

// === case: generic_type_arg ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferImportGenericTypeArgSliceViolation {
	List<java.util.Map<String, Integer>> field; // violation: Use an import instead of fully qualified name 'java.util.Map'.
}
// === end ===

// === case: implements_fqn ===
// imports: java.io.Serializable
class InputPreferImportImplementsFqnSliceViolation
		implements java.io.Serializable {} // violation: Use an import instead of fully qualified name 'java.io.Serializable'.
// === end ===

// === case: inherited_member_type_access_skip ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes
// skip-reason: cannot shorten the name: a supertype declared outside this file passes down a member type of the same name
class InputPreferImportInheritedMemberTypeAccessSkipSliceViolation {
	static class Sub extends ClasspathMemberTypes {
		java.lang.Short protectedMember; // violation: Use an import instead of fully qualified name 'java.lang.Short'.
	}
}
// === end ===

// === case: inherited_member_type_access_strips ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes
class InputPreferImportInheritedMemberTypeAccessStripsSliceViolation {
	static class Sub extends ClasspathMemberTypes {
		java.lang.Byte packagePrivate; // violation: Use an import instead of fully qualified name 'java.lang.Byte'.
		java.lang.Character privateMember; // violation: Use an import instead of fully qualified name 'java.lang.Character'.

		byte max() {
			return java.lang.Byte.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Byte'.
		}
	}
}
// === end ===

// === case: inherited_member_type_ambiguous_skip ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathThreadFuture
// imports: com.etk2000.checkstyle.testhelpers.ClasspathThreadRunnableFuture
// imports: java.lang.Thread.State
// imports: java.util.List
// imports: java.util.function.Function
// skip-reason: cannot shorten the name: a supertype declared outside this file passes down a member type of the same name
class InputPreferImportInheritedMemberTypeAmbiguousSkipSliceViolation {
	abstract static class DifferentDepth extends ClasspathThreadRunnableFuture {
		java.lang.Thread.State state; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
	}

	abstract static class SameDepth extends ClasspathThreadFuture {
		abstract static class NestedHeader implements Comparable<java.lang.Thread.State> {} // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.

		java.lang.Thread.State state; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.

		Object array() {
			return new java.lang.Thread.State[0]; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
		}

		Object cast(Object value) {
			return (java.lang.Thread.State) value; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
		}

		Object classLiteral() {
			return java.lang.Thread.State.class; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
		}

		Object constant() {
			return java.lang.Thread.State.NEW; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
		}

		Object local() {
			class Local {
				java.lang.Thread.State state; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
			}

			return new Local();
		}

		Function<String, Object> methodReference() {
			return java.lang.Thread.State::valueOf; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
		}

		List<java.lang.Thread.State> typeArgument() { // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
			return null;
		}
	}

	State used() {
		return null;
	}
}
// === end ===

// === case: inherited_member_type_boundaries_strip ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathThreadFuture
// imports: java.lang.Thread.State
// imports: java.util.ArrayList
// imports: java.util.concurrent.Future
class InputPreferImportInheritedMemberTypeBoundariesStripSliceViolation {
	abstract static class AmbiguousHeader extends ClasspathThreadFuture implements Comparable<java.lang.Thread.State> {} // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.

	abstract static class Header implements Future<java.lang.Thread.State> {} // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.

	static class NoMemberType extends ArrayList<String> {
		java.lang.Thread.State state; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
	}

	java.lang.Thread.State plain; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
}
// === end ===

// === case: inherited_member_type_diamond_itself_strips ===
// imports: java.util.concurrent.Future.State
// imports: java.util.concurrent.RunnableScheduledFuture
class InputPreferImportInheritedMemberTypeDiamondItselfStripsSliceViolation {
	abstract static class Diamond implements RunnableScheduledFuture<String> {
		java.util.concurrent.Future.State state; // violation: Use an import instead of fully qualified name 'java.util.concurrent.Future.State'.
	}
}
// === end ===

// === case: inherited_member_type_hiding_itself_strips ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathHidingFuture
// imports: com.etk2000.checkstyle.testhelpers.ClasspathHidingFuture.State
class InputPreferImportInheritedMemberTypeHidingItselfStripsSliceViolation {
	abstract static class Hiding extends ClasspathHidingFuture {
		com.etk2000.checkstyle.testhelpers.ClasspathHidingFuture.State state; // violation: Use an import instead of fully qualified name 'com.etk2000.checkstyle.testhelpers.ClasspathHidingFuture.State'.
	}
}
// === end ===

// === case: inherited_member_type_hiding_other_skip ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathHidingFuture
// imports: java.util.concurrent.Future.State
// skip-reason: cannot shorten the name: a supertype declared outside this file passes down a member type of the same name
class InputPreferImportInheritedMemberTypeHidingOtherSkipSliceViolation {
	abstract static class Hiding extends ClasspathHidingFuture {
		java.util.concurrent.Future.State state; // violation: Use an import instead of fully qualified name 'java.util.concurrent.Future.State'.
	}

	State used() {
		return null;
	}
}
// === end ===

// === case: inherited_member_type_in_file_shadow_wins ===
// imports: java.lang.Thread.State
// imports: java.util.concurrent.Future
// skip-reason: cannot shorten the name: the short name is shadowed by a type declaration or type parameter in this file
class InputPreferImportInheritedMemberTypeInFileShadowWinsSliceViolation {
	enum State {
		A
	}

	abstract static class Task implements Future<String> {
		java.lang.Thread.State state; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
	}
}
// === end ===

// === case: inherited_member_type_inaccessible_hider_other_path_skip ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathLinkedEntryMap
// imports: java.security.KeyStore.Entry
// imports: java.util.HashMap
// skip-reason: cannot shorten the name: a supertype declared outside this file passes down a member type of the same name
class InputPreferImportInheritedMemberTypeInaccessibleHiderOtherPathSkipSliceViolation {
	static class Linked extends ClasspathLinkedEntryMap<String, String> {
		java.security.KeyStore.Entry entry; // violation: Use an import instead of fully qualified name 'java.security.KeyStore.Entry'.
	}

	static class Plain extends HashMap<String, String> {
		java.security.KeyStore.Entry entry; // violation: Use an import instead of fully qualified name 'java.security.KeyStore.Entry'.
	}

	Entry used() {
		return null;
	}
}
// === end ===

// === case: inherited_member_type_inaccessible_hider_strips ===
// imports: java.security.KeyStore.Entry
// imports: java.util.LinkedHashMap
// imports: java.util.Properties
// imports: java.util.WeakHashMap
class InputPreferImportInheritedMemberTypeInaccessibleHiderStripsSliceViolation {
	static class Linked extends LinkedHashMap<String, String> {
		java.security.KeyStore.Entry entry; // violation: Use an import instead of fully qualified name 'java.security.KeyStore.Entry'.

		Object attributes() {
			return entry.getAttributes();
		}
	}

	static class Props extends Properties {
		java.security.KeyStore.Entry entry; // violation: Use an import instead of fully qualified name 'java.security.KeyStore.Entry'.

		Object attributes() {
			return entry.getAttributes();
		}
	}

	static class Weak extends WeakHashMap<String, String> {
		java.security.KeyStore.Entry entry; // violation: Use an import instead of fully qualified name 'java.security.KeyStore.Entry'.

		Object attributes() {
			return entry.getAttributes();
		}
	}

	Entry used() {
		return null;
	}
}
// === end ===

// === case: inherited_member_type_nearer_itself_strips ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathThreadFuture
// imports: java.lang.Thread.State
// imports: java.util.concurrent.Future
class InputPreferImportInheritedMemberTypeNearerItselfStripsSliceViolation {
	abstract static class OuterAmbiguous extends ClasspathThreadFuture {
		static class Worker extends Thread {
			java.lang.Thread.State state; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
		}
	}

	abstract static class Task implements Future<String> {
		static class InnerThread extends Thread {
			java.lang.Thread.State state; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
		}
	}

	State used() {
		return null;
	}
}
// === end ===

// === case: inherited_member_type_same_class ===
// imports: java.lang.Thread.State
class InputPreferImportInheritedMemberTypeSameClassSliceViolation {
	static class Worker extends Thread {
		java.lang.Thread.State state; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
	}
}
// === end ===

// === case: inherited_member_type_same_file_diamond_itself_strips ===
// imports: java.util.concurrent.Future
// imports: java.util.concurrent.Future.State
class InputPreferImportInheritedMemberTypeSameFileDiamondItselfStripsSliceViolation {
	abstract static class Diamond implements Left, Right {
		java.util.concurrent.Future.State state; // violation: Use an import instead of fully qualified name 'java.util.concurrent.Future.State'.
	}

	interface Left extends Future<String> {}

	interface Right extends Future<String> {}
}
// === end ===

// === case: inherited_member_type_same_package_skip ===
// package: com.etk2000.checkstyle.testhelpers
// skip-reason: cannot shorten the name: a supertype declared outside this file passes down a member type of the same name
class InputPreferImportInheritedMemberTypeSamePackageSkipSliceViolation {
	static class Sub extends ClasspathMemberTypes {
		java.lang.Byte typed; // violation: Use an import instead of fully qualified name 'java.lang.Byte'.

		byte max() {
			return java.lang.Byte.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Byte'.
		}
	}
}
// === end ===

// === case: inherited_member_type_same_package_strips ===
// package: com.etk2000.checkstyle.testhelpers
class InputPreferImportInheritedMemberTypeSamePackageStripsSliceViolation {
	static class Sub extends ClasspathMemberTypes {
		java.lang.Character privateMember; // violation: Use an import instead of fully qualified name 'java.lang.Character'.
	}
}
// === end ===

// === case: inherited_member_type_skip_expression_positions ===
// imports: java.lang.Thread.State
// imports: java.util.concurrent.Future
// imports: java.util.function.Function
// skip-reason: cannot shorten the name: a supertype declared outside this file passes down a member type of the same name
class InputPreferImportInheritedMemberTypeSkipExpressionPositionsSliceViolation {
	abstract static class Task implements Future<String> {
		Object classLiteral() {
			return java.lang.Thread.State.class; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
		}

		Object field() {
			return java.lang.Thread.State.NEW; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
		}

		Object methodCall() {
			return java.lang.Thread.State.valueOf("NEW"); // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
		}

		Function<String, ?> methodRef() {
			return java.lang.Thread.State::valueOf; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
		}
	}

	State used() {
		return null;
	}
}
// === end ===

// === case: inherited_member_type_skip_type_positions ===
// imports: java.lang.Thread.State
// imports: java.util.List
// imports: java.util.concurrent.Future
// imports: java.util.concurrent.FutureTask
// skip-reason: cannot shorten the name: a supertype declared outside this file passes down a member type of the same name
class InputPreferImportInheritedMemberTypeSkipTypePositionsSliceViolation {
	abstract static class Implements implements Future<String> {
		java.lang.Thread.State state; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.

		Object array() {
			return new java.lang.Thread.State[0]; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
		}

		Object cast(Object value) {
			return (java.lang.Thread.State) value; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
		}

		List<java.lang.Thread.State> typeArgument() { // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
			return null;
		}
	}

	static class Leaf extends Middle {
		java.lang.Thread.State state; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
	}

	static class Middle extends FutureTask<String> {
		Middle() {
			super(() -> "");
		}
	}

	abstract static class Task implements Future<String> {
		abstract static class Header implements Comparable<java.lang.Thread.State> {} // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.

		static class Nested {
			java.lang.Thread.State state; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
		}
	}

	abstract static class Worker extends Thread implements Future<String> {
		java.lang.Thread.State state; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
	}

	Object anonymous() {
		return new FutureTask<String>(() -> "") {
			java.lang.Thread.State state; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
		};
	}

	State used() {
		return null;
	}
}
// === end ===

// === case: inherited_member_type_static_imported_supertype_skip ===
// imports: static java.awt.geom.Point2D.Double
// skip-reason: cannot shorten the name: a supertype declared outside this file passes down a member type of the same name
class InputPreferImportInheritedMemberTypeStaticImportedSupertypeSkipSliceViolation {
	static class Shape extends Double {
		java.lang.Float value; // violation: Use an import instead of fully qualified name 'java.lang.Float'.

		float max() {
			return java.lang.Float.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Float'.
		}
	}
}
// === end ===

// === case: inherited_member_type_unknown_skip ===
// imports: com.unknown.Missing
// imports: java.lang.Thread.State
// skip-reason: cannot shorten the name: a supertype that cannot be read may pass down a member type of the same name
class InputPreferImportInheritedMemberTypeUnknownSkipSliceViolation {
	static class Sub extends MissingSupertype {
		java.lang.Thread.State state; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.

		Object classLiteral() {
			return java.lang.Thread.State.class; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
		}
	}

	static class Unloadable extends Missing {
		java.lang.Thread.State state; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
	}

	State used() {
		return null;
	}
}
// === end ===

// === case: instanceof_fqn ===
// imports: java.util.List
class InputPreferImportInstanceofSliceViolation {
	void m(Object obj) {
		if (obj instanceof java.util.List) // violation: Use an import instead of fully qualified name 'java.util.List'.
			System.out.println(obj);
	}
}
// === end ===

// === case: java_lang_type ===
class InputPreferImportJavaLangTypeSliceViolation {
	java.lang.Runnable task; // violation: Use an import instead of fully qualified name 'java.lang.Runnable'.
}
// === end ===

// === case: leading_comment_line_java_lang_strips ===
// leading comment before any declaration
class InputPreferImportLeadingCommentLineJavaLangStripsSliceViolation {
	java.lang.Runnable task; // violation: Use an import instead of fully qualified name 'java.lang.Runnable'.
}
// === end ===

// === case: local_var_type ===
// imports: java.util.List
class InputPreferImportLocalVarTypeSliceViolation {
	void m() {
		final java.util.List<String> local = null; // violation: Use an import instead of fully qualified name 'java.util.List'.
		System.out.println(local);
	}
}
// === end ===

// === case: mask_no_leak_next_line_wildcard ===
/*
comment
*/
// imports: foo.*
// skip-reason: cannot shorten the name: the short name is a java.lang type but a wildcard import in scope could bind the same simple name
class InputPreferImportMaskNoLeakNextLineWildcardSliceViolation {
	java.lang.Runnable task; // violation: Use an import instead of fully qualified name 'java.lang.Runnable'.
}
// === end ===

// === case: method_call_witness_type_arg ===
// imports: java.util.Map
class InputPreferImportMethodCallWitnessTypeArgSliceViolation {
	<T> T pick() {
		return null;
	}

	void useWitness() {
		this.<java.util.Map>pick(); // violation: Use an import instead of fully qualified name 'java.util.Map'.
	}
}
// === end ===

// === case: method_param_type ===
// imports: java.util.Set
class InputPreferImportMethodParamTypeSliceViolation {
	void m(java.util.Set<Integer> param) { // violation: Use an import instead of fully qualified name 'java.util.Set'.
		System.out.println(param);
	}
}
// === end ===

// === case: method_ref_fqn ===
class InputPreferImportMethodRefFqnSliceViolation {
	Runnable r = java.lang.Thread::yield; // violation: Use an import instead of fully qualified name 'java.lang.Thread'.
}
// === end ===

// === case: method_ref_witness_type_arg ===
// imports: java.util.ArrayList
// imports: java.util.Map
// imports: java.util.function.Supplier
class InputPreferImportMethodRefWitnessTypeArgSliceViolation {
	Supplier<ArrayList<Map>> ref = ArrayList::<java.util.Map>new; // violation: Use an import instead of fully qualified name 'java.util.Map'.
}
// === end ===

// === case: method_return_type ===
// imports: java.util.List
class InputPreferImportMethodReturnTypeSliceViolation {
	java.util.List<String> m() { // violation: Use an import instead of fully qualified name 'java.util.List'.
		return null;
	}
}
// === end ===

// === case: mixed_fqn_and_nested ===
// imports: java.util.Map
class InputPreferImportMixedFqnAndNestedSliceViolation {
	Map.Entry nestedFirst() { // violation (warning): Consider importing the nested type 'Entry' instead of qualifying it through 'Map'.
		return null;
	}

	java.util.List<String> qualifiedSecond() { // violation: Use an import instead of fully qualified name 'java.util.List'.
		return null;
	}
}
// === end ===

// === case: nested_annotation_warns ===
// imports: java.util.Map
@Map.Entry // violation (warning): Consider importing the nested type 'Entry' instead of qualifying it through 'Map'.
class InputPreferImportNestedAnnotationSliceViolation {}
// === end ===

// === case: nested_extends_warns ===
// imports: java.util.Map
class InputPreferImportNestedExtendsSliceViolation
		extends Map.Entry {} // violation (warning): Consider importing the nested type 'Entry' instead of qualifying it through 'Map'.
// === end ===

// === case: nested_generic ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferImportNestedGenericSliceViolation {
	java.util.List<java.util.Map<String, Integer>> nested() { // violation: Use an import instead of fully qualified name 'java.util.List'. // violation: Use an import instead of fully qualified name 'java.util.Map'.
		return null;
	}
}
// === end ===

// === case: nested_implements_warns ===
// imports: java.util.Map
class InputPreferImportNestedImplementsSliceViolation
		implements Map.Entry {} // violation (warning): Consider importing the nested type 'Entry' instead of qualifying it through 'Map'.
// === end ===

// === case: nested_java_lang_warns ===
class InputPreferImportNestedJavaLangSliceViolation {
	Thread.State state; // violation (warning): Consider importing the nested type 'State' instead of qualifying it through 'Thread'.
}
// === end ===

// === case: nested_new_warns ===
// imports: java.util.Map
class InputPreferImportNestedNewSliceViolation {
	void m() {
		final var e = new Map.Entry(); // violation (warning): Consider importing the nested type 'Entry' instead of qualifying it through 'Map'.
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
	NdkAnnRoot.N annotationRoot; // violation (warning): Consider importing the nested type 'N' instead of qualifying it through 'NdkAnnRoot'.
	NdkClassRoot.N classRoot; // violation (warning): Consider importing the nested type 'N' instead of qualifying it through 'NdkClassRoot'.
	NdkEnumRoot.N enumRoot; // violation (warning): Consider importing the nested type 'N' instead of qualifying it through 'NdkEnumRoot'.
	NdkIfaceRoot.N interfaceRoot; // violation (warning): Consider importing the nested type 'N' instead of qualifying it through 'NdkIfaceRoot'.
	NdkRecRoot.N recordRoot; // violation (warning): Consider importing the nested type 'N' instead of qualifying it through 'NdkRecRoot'.
}
// === end ===

// === case: nested_single_import_warns ===
// imports: java.util.Map
class InputPreferImportNestedSingleImportSliceViolation {
	Map.Entry<String, Integer> entry; // violation (warning): Consider importing the nested type 'Entry' instead of qualifying it through 'Map'.
}
// === end ===

// === case: nested_three_segment_warns ===
class InputPreferImportNestedThreeSegmentOuter {
	static class Mid {
		static class Leaf {}
	}
}

class InputPreferImportNestedThreeSegmentSliceViolation {
	InputPreferImportNestedThreeSegmentOuter.Mid.Leaf field; // violation (warning): Consider importing the nested type 'Leaf' instead of qualifying it through 'InputPreferImportNestedThreeSegmentOuter.Mid'.
}
// === end ===

// === case: nested_throws_warns ===
// imports: java.util.Map
class InputPreferImportNestedThrowsSliceViolation {
	void m() throws Map.Entry {} // violation (warning): Consider importing the nested type 'Entry' instead of qualifying it through 'Map'.
}
// === end ===

// === case: nested_type_arg_warns ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferImportNestedTypeArgSliceViolation {
	List<Map.Entry> field; // violation (warning): Consider importing the nested type 'Entry' instead of qualifying it through 'Map'.
}
// === end ===

// === case: nested_type_exact ===
// imports: java.util.Map
// imports: java.util.Map.Entry
class InputPreferImportNestedTypeExactSliceViolation {
	java.util.Map.Entry<String, Integer> entry; // violation: Use an import instead of fully qualified name 'java.util.Map.Entry'.
}
// === end ===

// === case: nested_type_prefix ===
// imports: java.util.Map
// skip-reason: cannot shorten the name: no single-type import, same-package type, or java.lang type binds the short name (adding an import is the general PreferImport fixer's job)
class InputPreferImportNestedTypePrefixSliceViolation {
	java.util.Map.Entry<String, Integer> entry; // violation: Use an import instead of fully qualified name 'java.util.Map.Entry'.
}
// === end ===

// === case: nested_wildcard_bound_warns ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferImportNestedWildcardBoundSliceViolation {
	List<? extends Map.Entry> field; // violation (warning): Consider importing the nested type 'Entry' instead of qualifying it through 'Map'.
}
// === end ===

// === case: nested_wildcard_import_warns ===
// imports: java.util.*
class InputPreferImportNestedWildcardImportSliceViolation {
	Map.Entry<String, Integer> entry; // violation (warning): Consider importing the nested type 'Entry' instead of qualifying it through 'Map'.
}
// === end ===

// === case: nested_wildcard_no_bind ===
// imports: java.io.*
// skip-reason: cannot shorten the name: resolution depends on a wildcard import whose contents cannot be confirmed from this file
class InputPreferImportNestedWildcardNoBindSliceViolation {
	Map.Entry<String, Integer> entry; // violation: Use an import instead of fully qualified name 'Map.Entry'.
}
// === end ===

// === case: new_expression ===
// imports: java.util.ArrayList
class InputPreferImportNewExpressionSliceViolation {
	void m() {
		final var list = new java.util.ArrayList<String>(); // violation: Use an import instead of fully qualified name 'java.util.ArrayList'.
		System.out.println(list);
	}
}
// === end ===

// === case: new_expression_nested_type_arg ===
// imports: java.util.ArrayList
// imports: java.util.Map
// skip-reason: cannot shorten the name: no single-type import, same-package type, or java.lang type binds the short name (adding an import is the general PreferImport fixer's job)
class InputPreferImportNewExpressionNestedTypeArgSliceViolation {
	void m() {
		new ArrayList<java.util.Map.Entry>(); // violation: Use an import instead of fully qualified name 'java.util.Map.Entry'.
	}
}
// === end ===

// === case: new_expression_type_arg ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferImportNewExpressionTypeArgSliceViolation {
	void m() {
		new ArrayList<java.util.List>(); // violation: Use an import instead of fully qualified name 'java.util.List'.
	}
}
// === end ===

// === case: new_expression_type_arg_offclasspath ===
// imports: java.util.ArrayList
// skip-reason: cannot shorten the name: no single-type import, same-package type, or java.lang type binds the short name (adding an import is the general PreferImport fixer's job)
class InputPreferImportNewExpressionTypeArgOffclasspathSliceViolation {
	void m() {
		new ArrayList<com.foo.Unknown>(); // violation: Use an import instead of fully qualified name 'com.foo.Unknown'.
	}
}
// === end ===

// === case: obscure_boundary_annotation_of_other_local ===
class InputPreferImportObscureBoundaryAnnotationOfOtherLocalSliceViolation {
	@interface Cap {
		int value();
	}

	void m() {
		@Cap(java.lang.Integer.MAX_VALUE) // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		final Object other = null;
		final Object Integer = null;
		System.out.println(other);
		System.out.println(Integer);
	}
}
// === end ===

// === case: obscure_boundary_anonymous_field_outside_body ===
class InputPreferImportObscureBoundaryAnonymousFieldOutsideBodySliceViolation {
	int max() {
		final var anon = new Object() {
			Object Integer;
		};
		System.out.println(anon);
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_boundary_arrow_case_block_local_other_case ===
class InputPreferImportObscureBoundaryArrowCaseBlockLocalOtherCaseSliceViolation {
	void m(int k) {
		switch (k) {
			case 1 -> {
				final Object Integer = null;
				System.out.println(Integer);
			}
			default -> System.out.println(java.lang.Integer.MAX_VALUE); // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		}
	}
}
// === end ===

// === case: obscure_boundary_case_group_local_in_nested_block ===
class InputPreferImportObscureBoundaryCaseGroupLocalInNestedBlockSliceViolation {
	int max(int k) {
		switch (k) {
			case 1: {
				final Object Integer = null;
				System.out.println(Integer);
			}
			case 2:
				return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
			default:
				return 0;
		}
	}
}
// === end ===

// === case: obscure_boundary_catch_param_outside_catch ===
class InputPreferImportObscureBoundaryCatchParamOutsideCatchSliceViolation {
	int max() {
		try {
			System.out.println(java.lang.Integer.MIN_VALUE); // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		}
		catch (RuntimeException Integer) {
			System.out.println(Integer);
		}
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_boundary_class_annotation_own_field ===
class InputPreferImportObscureBoundaryClassAnnotationOwnFieldSliceViolation {
	@interface Cap {
		int value();
	}

	@Cap(java.lang.Integer.MAX_VALUE) // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	static class Annotated {
		Object Integer;
	}
}
// === end ===

// === case: obscure_boundary_classpath_supertype_shadowed_by_member_type ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeBase
class InputPreferImportObscureBoundaryClasspathSupertypeShadowedByMemberTypeSliceViolation {
	static class ClasspathScopeBase {}

	static class Sub extends ClasspathScopeBase {
		int max() {
			return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		}
	}
}
// === end ===

// === case: obscure_boundary_classpath_supertype_unresolvable ===
class InputPreferImportObscureBoundaryClasspathSupertypeUnresolvableSliceViolation {
	static class Sub extends MissingSupertype {
		int max() {
			return java.lang.Integer.MAX_VALUE; // violation (warning): Consider an import instead of fully qualified name 'java.lang.Integer'; a class this file cannot read may give the short name another meaning.
		}
	}
}
// === end ===

// === case: obscure_boundary_enum_constant_outside_enum ===
class InputPreferImportObscureBoundaryEnumConstantOutsideEnumSliceViolation {
	enum Kind {
		Integer
	}

	int max() {
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_boundary_field_nested_class_from_outer ===
class InputPreferImportObscureBoundaryFieldNestedClassFromOuterSliceViolation {
	static class Inner {
		Object Integer;
	}

	int max() {
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_boundary_field_non_enclosing_class ===
class InputPreferImportObscureBoundaryFieldNonEnclosingClassSliceViolation {
	static class Other {
		Object Integer;
	}

	static class User {
		int max() {
			return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		}
	}
}
// === end ===

// === case: obscure_boundary_field_subclass_from_superclass ===
class InputPreferImportObscureBoundaryFieldSubclassFromSuperclassSliceViolation {
	static class Base {
		int max() {
			return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		}
	}

	static class Sub extends Base {
		Object Integer;
	}
}
// === end ===

// === case: obscure_boundary_for_each_var_after_loop ===
// imports: java.util.List
class InputPreferImportObscureBoundaryForEachVarAfterLoopSliceViolation {
	int max(List<Object> items) {
		for (Object Integer : items)
			System.out.println(Integer);
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_boundary_for_init_var_after_loop ===
class InputPreferImportObscureBoundaryForInitVarAfterLoopSliceViolation {
	int max() {
		for (var Integer = 0; Integer < 1; ++Integer)
			System.out.println(Integer);
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_boundary_interface_constant_not_implemented ===
class InputPreferImportObscureBoundaryInterfaceConstantNotImplementedSliceViolation {
	interface Constants {
		Object Integer = null;
	}

	int max() {
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_boundary_label_named_like_type ===
class InputPreferImportObscureBoundaryLabelNamedLikeTypeSliceViolation {
	void m() {
		Integer:
		for (var i = 0; i < java.lang.Integer.MAX_VALUE; ++i) // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
			break Integer;
	}
}
// === end ===

// === case: obscure_boundary_local_declared_after_use ===
class InputPreferImportObscureBoundaryLocalDeclaredAfterUseSliceViolation {
	void m() {
		System.out.println(java.lang.Integer.MAX_VALUE); // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		final Object Integer = null;
		System.out.println(Integer);
	}
}
// === end ===

// === case: obscure_boundary_local_multi_declarator_binder_after ===
class InputPreferImportObscureBoundaryLocalMultiDeclaratorBinderAfterSliceViolation {
	int max() {
		final int max = java.lang.Integer.MAX_VALUE, Integer = 0; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		return max + Integer;
	}
}
// === end ===

// === case: obscure_boundary_local_sibling_block ===
class InputPreferImportObscureBoundaryLocalSiblingBlockSliceViolation {
	int max(boolean flag) {
		if (flag) {
			final Object Integer = null;
			System.out.println(Integer);
		}
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_boundary_lowercase_name ===
// imports: java.nio.charset.Charset
class InputPreferImportObscureBoundaryLowercaseNameSliceViolation {
	Object m() {
		final Object charset = null;
		System.out.println(charset);
		return java.nio.charset.Charset.defaultCharset(); // violation: Use an import instead of fully qualified name 'java.nio.charset.Charset'.
	}
}
// === end ===

// === case: obscure_boundary_method_named_like_type ===
class InputPreferImportObscureBoundaryMethodNamedLikeTypeSliceViolation {
	int Integer() {
		return 0;
	}

	int max() {
		return java.lang.Integer.MAX_VALUE + Integer(); // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_boundary_non_leading_segment_name ===
// imports: java.util.Map.Entry
class InputPreferImportObscureBoundaryNonLeadingSegmentNameSliceViolation {
	Object entry(Object Map) {
		return java.util.Map.Entry.comparingByKey(); // violation: Use an import instead of fully qualified name 'java.util.Map.Entry'.
	}

	int field(Object MAX_VALUE) {
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}

	int lang(Object lang) {
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}

	void out(Object out) {
		java.lang.System.out.println(out); // violation: Use an import instead of fully qualified name 'java.lang.System'.
	}
}
// === end ===

// === case: obscure_boundary_param_other_method ===
class InputPreferImportObscureBoundaryParamOtherMethodSliceViolation {
	void holder(Object Integer) {
		System.out.println(Integer);
	}

	int user() {
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_boundary_pattern_other_member ===
class InputPreferImportObscureBoundaryPatternOtherMemberSliceViolation {
	boolean matches = new Object() instanceof String Integer && Integer.isEmpty();

	int matcher(Object o) {
		if (o instanceof String Integer)
			System.out.println(Integer);
		return 0;
	}

	int max() {
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_boundary_record_component_other_record ===
class InputPreferImportObscureBoundaryRecordComponentOtherRecordSliceViolation {
	record Box(Object Integer) {}

	int max() {
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_boundary_resource_var_in_catch_and_finally ===
// imports: java.io.StringReader
class InputPreferImportObscureBoundaryResourceVarInCatchAndFinallySliceViolation {
	void m() {
		try (var Integer = new StringReader("")) {
			System.out.println(Integer);
		}
		catch (RuntimeException e) {
			System.out.println(java.lang.Integer.MIN_VALUE); // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		}
		finally {
			System.out.println(java.lang.Integer.MAX_VALUE); // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		}
	}
}
// === end ===

// === case: obscure_boundary_sibling_lambda_param ===
// imports: java.util.function.IntSupplier
// imports: java.util.function.IntUnaryOperator
class InputPreferImportObscureBoundarySiblingLambdaParamSliceViolation {
	void m() {
		final IntUnaryOperator first = Integer -> Integer;
		final IntSupplier second = () -> java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		System.out.println(first.applyAsInt(second.getAsInt()));
	}
}
// === end ===

// === case: obscure_boundary_static_import_on_demand ===
// imports: static java.lang.Math.*
class InputPreferImportObscureBoundaryStaticImportOnDemandSliceViolation {
	int max() {
		return java.lang.Integer.MAX_VALUE - abs(1); // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_boundary_static_import_on_demand_unresolvable ===
// imports: static com.unknown.Missing.*
class InputPreferImportObscureBoundaryStaticImportOnDemandUnresolvableSliceViolation {
	int max() {
		return java.lang.Integer.MAX_VALUE; // violation (warning): Consider an import instead of fully qualified name 'java.lang.Integer'; a class this file cannot read may give the short name another meaning.
	}
}
// === end ===

// === case: obscure_boundary_static_import_other_member ===
// imports: java.nio.charset.Charset
// imports: static java.nio.charset.Charset.defaultCharset
class InputPreferImportObscureBoundaryStaticImportOtherMemberSliceViolation {
	Object m() {
		return java.nio.charset.Charset.defaultCharset(); // violation: Use an import instead of fully qualified name 'java.nio.charset.Charset'.
	}
}
// === end ===

// === case: obscure_boundary_type_parameter_is_not_variable ===
// skip-reason: cannot shorten the name: the short name is shadowed by a type declaration or type parameter in this file
class InputPreferImportObscureBoundaryTypeParameterIsNotVariableSliceViolation {
	<Integer> int max() {
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_class_literal_annotation_arg_still_reported ===
class InputPreferImportObscureClassLiteralAnnotationArgStillReportedSliceViolation {
	@interface Type {
		Class<?> value();
	}

	Object String;

	@Type(java.lang.String.class) // violation: Use an import instead of fully qualified name 'java.lang.String'.
	void m() {}
}
// === end ===

// === case: obscure_class_literal_still_reported ===
// imports: java.nio.charset.Charset
class InputPreferImportObscureClassLiteralStillReportedSliceViolation {
	Object name(Object Charset) {
		return java.nio.charset.Charset.class.getName(); // violation: Use an import instead of fully qualified name 'java.nio.charset.Charset'.
	}

	Class<?> type(Object Charset) {
		return java.nio.charset.Charset.class; // violation: Use an import instead of fully qualified name 'java.nio.charset.Charset'.
	}
}
// === end ===

// === case: obscure_classpath_cyclic_hierarchy_terminates ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeConstants
class InputPreferImportObscureClasspathCyclicHierarchyTerminatesSliceViolation {
	static class CycleA extends CycleB implements ClasspathScopeConstants {}

	static class CycleB extends CycleA {
		int max() {
			return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		}

		String separator() {
			return java.lang.System.lineSeparator();
		}
	}
}
// === end ===

// === case: obscure_classpath_interface_constant ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeConstants
class InputPreferImportObscureClasspathInterfaceConstantSliceViolation {
	interface Extending extends ClasspathScopeConstants {
		default String separator() {
			return java.lang.System.lineSeparator();
		}
	}

	static class Implementor implements ClasspathScopeConstants {
		String separator() {
			return java.lang.System.lineSeparator();
		}
	}

	enum Kind implements ClasspathScopeConstants {
		WithBody {
			@Override
			String separator() {
				return java.lang.System.lineSeparator();
			}
		};

		abstract String separator();

		String twice() {
			return java.lang.System.lineSeparator() + separator();
		}
	}

	record Implementing() implements ClasspathScopeConstants {
		String separator() {
			return java.lang.System.lineSeparator();
		}
	}

	String unobscured() {
		return java.lang.System.lineSeparator(); // violation: Use an import instead of fully qualified name 'java.lang.System'.
	}
}
// === end ===

// === case: obscure_classpath_nested_supertype ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter.Inner
class InputPreferImportObscureClasspathNestedSupertypeSliceViolation {
	static class FullyQualified extends com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter.Inner { // violation: Use an import instead of fully qualified name 'com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter.Inner'.
		Thread current() {
			return java.lang.Thread.currentThread();
		}
	}

	static class Imported extends Inner {
		Thread current() {
			return java.lang.Thread.currentThread();
		}
	}

	Thread unobscured() {
		return java.lang.Thread.currentThread(); // violation: Use an import instead of fully qualified name 'java.lang.Thread'.
	}
}
// === end ===

// === case: obscure_classpath_nested_supertype_through_outer ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter
class InputPreferImportObscureClasspathNestedSupertypeThroughOuterSliceViolation {
	static class ThroughOuter extends ClasspathScopeOuter.Inner { // violation (warning): Consider importing the nested type 'Inner' instead of qualifying it through 'ClasspathScopeOuter'.
		Thread current() {
			return java.lang.Thread.currentThread();
		}
	}
}
// === end ===

// === case: obscure_classpath_supertype_dotted_same_file_head ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter
class InputPreferImportObscureClasspathSupertypeDottedSameFileHeadSliceViolation {
	static class Holder extends ClasspathScopeOuter {}

	static class ThroughHolder extends Holder.Inner { // violation (warning): Consider importing the nested type 'Inner' instead of qualifying it through 'Holder'.
		Thread current() {
			return java.lang.Thread.currentThread();
		}
	}

	static class ThroughUnreadable extends Unreadable.Inner { // violation (warning): Consider importing the nested type 'Inner' instead of qualifying it through 'Unreadable'.
		Thread current() {
			return java.lang.Thread.currentThread(); // violation (warning): Consider an import instead of fully qualified name 'java.lang.Thread'; a class this file cannot read may give the short name another meaning.
		}
	}

	static class Unreadable extends MissingSupertype {}
}
// === end ===

// === case: obscure_classpath_supertype_expression_kinds ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeBase
// imports: java.util.function.IntUnaryOperator
class InputPreferImportObscureClasspathSupertypeExpressionKindsSliceViolation {
	@interface Cap {
		int value();
	}

	@Cap(java.lang.Integer.MAX_VALUE) // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	static class Sub extends ClasspathScopeBase {
		@Cap(java.lang.Integer.MAX_VALUE)
		void annotationArgument() {}

		Class<?> classLiteral() {
			return java.lang.Integer.class; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		}

		IntUnaryOperator methodRef() {
			return java.lang.Integer::reverse;
		}

		int staticCall() {
			return java.lang.Integer.signum(1);
		}

		int staticField() {
			return java.lang.Integer.MAX_VALUE;
		}
	}
}
// === end ===

// === case: obscure_classpath_supertype_frames ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeBase
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeGenericBase
// imports: java.util.function.IntSupplier
class InputPreferImportObscureClasspathSupertypeFramesSliceViolation {
	static class Outer {
		static class InnerSub extends ClasspathScopeBase {}

		int unobscured() {
			return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		}
	}

	static class Sub extends ClasspathScopeBase {
		@interface Limit {
			int max() default java.lang.Integer.MAX_VALUE;
		}

		class Inner {
			int max() {
				return java.lang.Integer.MAX_VALUE;
			}
		}

		static class Nested {
			int max() {
				return java.lang.Integer.MAX_VALUE;
			}
		}

		Object anonymousOfUnrelatedType() {
			return new Object() {
				@Override
				public int hashCode() {
					return java.lang.Integer.MAX_VALUE;
				}
			};
		}

		int lambda() {
			final IntSupplier max = () -> java.lang.Integer.MAX_VALUE;
			return max.getAsInt();
		}

		Object localClass() {
			class Local {
				int max() {
					return java.lang.Integer.MAX_VALUE;
				}
			}

			return new Local();
		}
	}

	Object anonymousDiamond() {
		return new ClasspathScopeGenericBase<>() {
			@Override
			public String toString() {
				return java.lang.String.valueOf(2);
			}
		};
	}

	Object anonymousGeneric() {
		return new ClasspathScopeGenericBase<String>() {
			@Override
			public String toString() {
				return java.lang.String.valueOf(1);
			}
		};
	}

	Object anonymousRaw() {
		return new ClasspathScopeBase() {
			@Override
			public int hashCode() {
				return java.lang.Integer.MAX_VALUE;
			}
		};
	}

	Object[] outsideAnonymousBody() {
		return new Object[]{java.lang.Integer.MAX_VALUE, new ClasspathScopeBase() {}}; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_classpath_supertype_inherited_member_type ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter
class InputPreferImportObscureClasspathSupertypeInheritedMemberTypeSliceViolation {
	static class Outer extends ClasspathScopeOuter {
		static class OwnMember extends Inner {
			static class Inner {}

			Thread current() {
				return java.lang.Thread.currentThread();
			}
		}

		static class Sub extends Inner {
			Thread current() {
				return java.lang.Thread.currentThread();
			}
		}

		Object anonymous() {
			return new Inner() {
				Thread current() {
					return java.lang.Thread.currentThread();
				}
			};
		}

		Thread outerOwn() {
			return java.lang.Thread.currentThread(); // violation: Use an import instead of fully qualified name 'java.lang.Thread'.
		}
	}

	static class Shadowed extends ClasspathScopeOuter {
		static class Inner {}

		static class Sub extends Inner {
			Thread current() {
				return java.lang.Thread.currentThread(); // violation: Use an import instead of fully qualified name 'java.lang.Thread'.
			}
		}
	}
}
// === end ===

// === case: obscure_classpath_supertype_package_beats_static_on_demand ===
// package: com.etk2000.checkstyle.testhelpers
// imports: static com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes.*
class InputPreferImportObscureClasspathSupertypePackageBeatsStaticOnDemandSliceViolation {
	static class Sub extends ClasspathScopeBase {
		long max() {
			return java.lang.Long.MAX_VALUE;
		}
	}

	long outside() {
		return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
	}
}
// === end ===

// === case: obscure_classpath_supertype_qualified ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeBase
class InputPreferImportObscureClasspathSupertypeQualifiedSliceViolation {
	static class Sub extends com.etk2000.checkstyle.testhelpers.ClasspathScopeBase { // violation: Use an import instead of fully qualified name 'com.etk2000.checkstyle.testhelpers.ClasspathScopeBase'.
		int max() {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	Object anonymous() {
		return new com.etk2000.checkstyle.testhelpers.ClasspathScopeBase() { // violation: Use an import instead of fully qualified name 'com.etk2000.checkstyle.testhelpers.ClasspathScopeBase'.
			@Override
			public int hashCode() {
				return java.lang.Integer.MAX_VALUE;
			}
		};
	}

	int unobscured() {
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_classpath_supertype_same_file_single_static_import ===
// package: com.etk2000.checkstyle.inputs.preferimport
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeBase
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeConstants
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportObscureClasspathSupertypeSameFileSingleStaticImportSliceViolation.Holder.Base
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportObscureClasspathSupertypeSameFileSingleStaticImportSliceViolation.Holder.Shape
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportObscureClasspathSupertypeSameFileSingleStaticImportSliceViolation.Holder.Thread
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportObscureClasspathSupertypeSameFileSingleStaticImportSliceViolation.Lost.Base2
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportObscureClasspathSupertypeSameFileSingleStaticImportSliceViolation.Shapes.Inner
class InputPreferImportObscureClasspathSupertypeSameFileSingleStaticImportSliceViolation {
	static class Holder {
		static class Base extends ClasspathScopeBase {}

		interface Shape extends ClasspathScopeConstants {}

		class Thread {}

		static final Object Thread = null;
	}

	static class Lost extends MissingSupertype {}

	interface Shapes {
		class Inner extends ClasspathScopeBase {}
	}

	static class SubBase extends Base {
		long max() {
			return java.lang.Long.MAX_VALUE;
		}
	}

	static class SubBase2 extends Base2 {
		long max() {
			return java.lang.Long.MAX_VALUE; // violation (warning): Consider an import instead of fully qualified name 'java.lang.Long'; a class this file cannot read may give the short name another meaning.
		}
	}

	static class SubInner extends Inner {
		long max() {
			return java.lang.Long.MAX_VALUE;
		}
	}

	static class SubShape implements Shape {
		String separator() {
			return java.lang.System.lineSeparator();
		}
	}

	static class SubThread extends Thread {
		int max() {
			return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		}
	}
}
// === end ===

// === case: obscure_classpath_supertype_shapes ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeBase
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeGenericBase
// imports: java.util.ArrayList
class InputPreferImportObscureClasspathSupertypeShapesSliceViolation {
	static class Generic extends ClasspathScopeGenericBase<String> {
		String text() {
			return java.lang.String.valueOf(1);
		}
	}

	static class LeafOfMiddle extends Middle {
		int max() {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	static class LeafOfQualifiedMiddle extends InputPreferImportObscureClasspathSupertypeShapesSliceViolation.Middle {
		int max() {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	static class Middle extends ClasspathScopeBase {}

	static class WithoutTheField extends ArrayList<String> {
		int max() {
			return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		}
	}
}
// === end ===

// === case: obscure_classpath_supertype_static_import_outranks_java_lang ===
// imports: static com.etk2000.checkstyle.testhelpers.ClasspathStaticImportShadows.Thread
class InputPreferImportObscureClasspathSupertypeStaticImportOutranksJavaLangSliceViolation {
	static class Deep extends Thread.Nested { // violation (warning): Consider importing the nested type 'Nested' instead of qualifying it through 'Thread'.
		long max() {
			return java.lang.Long.MAX_VALUE;
		}
	}

	static class Sub extends Thread {
		int max() {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	int unobscured() {
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_classpath_supertype_static_on_demand_ambiguous_warns ===
// imports: static com.etk2000.checkstyle.testhelpers.ClasspathStaticImportShadows.*
class InputPreferImportObscureClasspathSupertypeStaticOnDemandAmbiguousWarnsSliceViolation {
	static class Sub extends Thread {
		long max() {
			return java.lang.Long.MAX_VALUE; // violation (warning): Consider an import instead of fully qualified name 'java.lang.Long'; a class this file cannot read may give the short name another meaning.
		}
	}
}
// === end ===

// === case: obscure_classpath_supertype_static_on_demand_interface ===
// imports: static java.util.Map.*
class InputPreferImportObscureClasspathSupertypeStaticOnDemandInterfaceSliceViolation {
	abstract static class Pair implements Entry<String, String> {
		int max() {
			return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		}
	}
}
// === end ===

// === case: obscure_classpath_supertype_static_on_demand_member_type ===
// imports: static com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes.*
class InputPreferImportObscureClasspathSupertypeStaticOnDemandMemberTypeSliceViolation {
	static class Sub extends ClasspathScopeBase {
		long max() {
			return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
		}
	}
}
// === end ===

// === case: obscure_classpath_supertype_through_single_static_import ===
// imports: static com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter.Inner
class InputPreferImportObscureClasspathSupertypeThroughSingleStaticImportSliceViolation {
	static class Sub extends Inner {
		Thread current() {
			return java.lang.Thread.currentThread();
		}
	}

	static class Unmatched extends InnerMissing {
		Thread current() {
			return java.lang.Thread.currentThread(); // violation (warning): Consider an import instead of fully qualified name 'java.lang.Thread'; a class this file cannot read may give the short name another meaning.
		}
	}

	Object anonymous() {
		return new Inner() {
			Thread current() {
				return java.lang.Thread.currentThread();
			}
		};
	}
}
// === end ===

// === case: obscure_classpath_supertype_through_wildcard_import ===
// imports: com.etk2000.checkstyle.testhelpers.*
// skip-reason: cannot shorten the name: the short name is a java.lang type but a wildcard import in scope could bind the same simple name
class InputPreferImportObscureClasspathSupertypeThroughWildcardImportSliceViolation {
	static class Sub extends ClasspathScopeBase {
		int max() {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	int unobscured() {
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_classpath_supertype_visibility ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeBase
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeMiddle
class InputPreferImportObscureClasspathSupertypeVisibilitySliceViolation {
	static class Sub extends ClasspathScopeBase {
		short packagePrivateInstance() {
			return java.lang.Short.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Short'.
		}

		byte packagePrivateStatic() {
			return java.lang.Byte.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Byte'.
		}

		double privateInstance() {
			return java.lang.Double.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
		}

		char privateStatic() {
			return java.lang.Character.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Character'.
		}

		int protectedInstance() {
			return java.lang.Integer.MAX_VALUE;
		}

		long protectedStatic() {
			return java.lang.Long.MAX_VALUE;
		}

		float publicInstance() {
			return java.lang.Float.MAX_VALUE;
		}

		Boolean publicStatic() {
			return java.lang.Boolean.TRUE;
		}
	}

	static class SubOfMiddle extends ClasspathScopeMiddle {
		short grandparentPackagePrivate() {
			return java.lang.Short.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Short'.
		}

		int grandparentProtected() {
			return java.lang.Integer.MAX_VALUE;
		}
	}
}
// === end ===

// === case: obscure_classpath_supertype_visibility_same_package ===
// package: com.etk2000.checkstyle.testhelpers
class InputPreferImportObscureClasspathSupertypeVisibilitySamePackageSliceViolation {
	static class Sub extends ClasspathScopeBase {
		short packagePrivateInstance() {
			return java.lang.Short.MAX_VALUE;
		}

		byte packagePrivateStatic() {
			return java.lang.Byte.MAX_VALUE;
		}

		double privateInstance() {
			return java.lang.Double.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
		}

		char privateStatic() {
			return java.lang.Character.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Character'.
		}
	}

	static class SubOfMiddle extends ClasspathScopeMiddle {
		short grandparentPackagePrivate() {
			return java.lang.Short.MAX_VALUE;
		}
	}
}
// === end ===

// === case: obscure_ctor_ref_still_reported ===
// imports: java.util.function.Supplier
class InputPreferImportObscureCtorRefStillReportedSliceViolation {
	Supplier<Object> make(Object StringBuilder) {
		return java.lang.StringBuilder::new; // violation: Use an import instead of fully qualified name 'java.lang.StringBuilder'.
	}
}
// === end ===

// === case: obscure_enum_constant_argument_pattern ===
class InputPreferImportObscureEnumConstantArgumentPatternSliceViolation {
	enum Mode {
		A(new Object() instanceof String Integer ? java.lang.Integer.MAX_VALUE : 0),
		B(new Object() instanceof String Text ? java.lang.Integer.MAX_VALUE : 0), // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		C(java.lang.Integer.MAX_VALUE), // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		D(0) {
			@Override
			int max(Object value) {
				return value instanceof String Integer ? 0 : java.lang.Integer.MAX_VALUE;
			}
		},
		E(java.lang.Integer.MAX_VALUE) { // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
			@Override
			int max(Object value) {
				return value instanceof String Integer ? 1 : 0;
			}
		};

		Mode(int value) {
			System.out.println(value);
		}

		int max(Object value) {
			return 0;
		}
	}
}
// === end ===

// === case: obscure_mixed_java_lang ===
class InputPreferImportObscureMixedJavaLangSliceViolation {
	int obscured(Object Integer) {
		return java.lang.Integer.MAX_VALUE;
	}

	int unobscured() {
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_mixed_nested_type_import ===
// imports: java.util.Map.Entry
class InputPreferImportObscureMixedNestedTypeImportSliceViolation {
	Object obscured(Object Entry) {
		return java.util.Map.Entry.comparingByKey();
	}

	Object unobscured() {
		return java.util.Map.Entry.comparingByKey(); // violation: Use an import instead of fully qualified name 'java.util.Map.Entry'.
	}
}
// === end ===

// === case: obscure_mixed_same_package ===
// package: com.etk2000.checkstyle.gradle.fix
class InputPreferImportObscureMixedSamePackageSliceViolation {
	Object obscured(Object SkipMessages) {
		return com.etk2000.checkstyle.gradle.fix.SkipMessages.FIX_BOUNDS;
	}

	Object unobscured() {
		return com.etk2000.checkstyle.gradle.fix.SkipMessages.FIX_BOUNDS; // violation: Use an import instead of fully qualified name 'com.etk2000.checkstyle.gradle.fix.SkipMessages'.
	}
}
// === end ===

// === case: obscure_mixed_single_type_import ===
// imports: java.nio.charset.Charset
class InputPreferImportObscureMixedSingleTypeImportSliceViolation {
	Object obscured(Object Charset) {
		return java.nio.charset.Charset.defaultCharset();
	}

	Object unobscured() {
		return java.nio.charset.Charset.defaultCharset(); // violation: Use an import instead of fully qualified name 'java.nio.charset.Charset'.
	}
}
// === end ===

// === case: obscure_mixed_wildcard_skip ===
// imports: java.util.*
// skip-reason: cannot shorten the name: resolution depends on a wildcard import whose contents cannot be confirmed from this file
class InputPreferImportObscureMixedWildcardSkipSliceViolation {
	Object obscured(Object List) {
		return java.util.List.of();
	}

	Object unobscured() {
		return java.util.List.of(); // violation: Use an import instead of fully qualified name 'java.util.List'.
	}
}
// === end ===

// === case: obscure_nearer_member_type_classpath_mid ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathGenericOuter
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeBase
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter
class InputPreferImportObscureNearerMemberTypeClasspathMidSliceViolation {
	static class ClasspathOuter extends ClasspathGenericOuter<String> {
		static class Mid extends ClasspathScopeOuter {
			static class Sub extends Inner {
				Thread current() {
					return java.lang.Thread.currentThread();
				}

				long max() {
					return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
				}
			}
		}
	}

	static class DeclaredOuter {
		static class Inner extends ClasspathScopeBase {}

		static class Mid extends ClasspathScopeOuter {
			static class Sub extends Inner {
				Thread current() {
					return java.lang.Thread.currentThread();
				}

				long max() {
					return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
				}
			}
		}
	}

	static class InheritedOuter extends SameFileBase {
		static class Mid extends ClasspathScopeOuter {
			static class Sub extends Inner {
				Thread current() {
					return java.lang.Thread.currentThread();
				}

				long max() {
					return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
				}
			}
		}
	}

	static class SameFileBase {
		static class Inner extends ClasspathScopeBase {}
	}
}
// === end ===

// === case: obscure_nearer_member_type_hiding ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeBase
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter
class InputPreferImportObscureNearerMemberTypeHidingSliceViolation {
	static class Hidden extends HidingBase {
		static class Sub extends Inner {
			Thread current() {
				return java.lang.Thread.currentThread(); // violation: Use an import instead of fully qualified name 'java.lang.Thread'.
			}

			long max() {
				return java.lang.Long.MAX_VALUE;
			}
		}
	}

	static class HidingBase extends ClasspathScopeOuter {
		static class Inner extends ClasspathScopeBase {}
	}

	static class Inner extends ClasspathScopeBase {}

	static class OwnPrivate extends ClasspathScopeOuter {
		private static class Inner extends ClasspathScopeBase {}

		static class Sub extends Inner {
			Thread current() {
				return java.lang.Thread.currentThread(); // violation: Use an import instead of fully qualified name 'java.lang.Thread'.
			}

			long max() {
				return java.lang.Long.MAX_VALUE;
			}
		}
	}

	static class PrivateInBase extends ClasspathScopeOuter {
		private static class Inner {}
	}

	static class SkipsPrivate extends PrivateInBase {
		static class Sub extends Inner {
			Thread current() {
				return java.lang.Thread.currentThread(); // violation: Use an import instead of fully qualified name 'java.lang.Thread'.
			}

			long max() {
				return java.lang.Long.MAX_VALUE;
			}
		}
	}
}
// === end ===

// === case: obscure_nearer_member_type_local_class ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeBase
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeConstants
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter
class InputPreferImportObscureNearerMemberTypeLocalClassSliceViolation {
	static class Outer extends ClasspathScopeOuter {
		Object anonymous() {
			class Inner extends ClasspathScopeBase {}

			return new Inner() {
				long max() {
					return java.lang.Long.MAX_VALUE;
				}
			};
		}

		Object declaredAfter() {
			class Sub extends Inner {
				Thread current() {
					return java.lang.Thread.currentThread();
				}
			}

			class Inner extends ClasspathScopeBase {}

			return new Sub();
		}

		Object declaredBefore() {
			class Inner extends ClasspathScopeBase {}

			class Sub extends Inner {
				Thread current() {
					return java.lang.Thread.currentThread(); // violation: Use an import instead of fully qualified name 'java.lang.Thread'.
				}

				long max() {
					return java.lang.Long.MAX_VALUE;
				}
			}

			return new Sub();
		}

		Runnable lambdaBody() {
			return () -> {
				class Inner extends ClasspathScopeBase {}

				class Sub extends Inner {
					long max() {
						return java.lang.Long.MAX_VALUE;
					}
				}

				new Sub().max();
			};
		}

		Object localInterface() {
			interface Inner extends ClasspathScopeConstants {}

			class Sub implements Inner {
				String separator() {
					return java.lang.System.lineSeparator();
				}
			}

			return new Sub();
		}
	}
}
// === end ===

// === case: obscure_nearer_member_type_repro_declared_outer ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter
class InputPreferImportObscureNearerMemberTypeReproDeclaredOuterSliceViolation {
	static class Inner {}

	static class Mid extends ClasspathScopeOuter {
		static class Sub extends Inner {
			Thread current() {
				return java.lang.Thread.currentThread();
			}
		}

		Thread midOwn() {
			return java.lang.Thread.currentThread(); // violation: Use an import instead of fully qualified name 'java.lang.Thread'.
		}
	}
}
// === end ===

// === case: obscure_nearer_member_type_repro_generic_outer ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathGenericOuter
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter
class InputPreferImportObscureNearerMemberTypeReproGenericOuterSliceViolation extends ClasspathGenericOuter<String> {
	static class Base extends ClasspathScopeOuter {}

	static class Mid extends Base {
		static class Sub extends Inner {
			Thread current() {
				return java.lang.Thread.currentThread();
			}
		}

		Thread midOwn() {
			return java.lang.Thread.currentThread(); // violation: Use an import instead of fully qualified name 'java.lang.Thread'.
		}
	}
}
// === end ===

// === case: obscure_nearer_member_type_same_file_chain_mid ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathGenericOuter
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeBase
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter
class InputPreferImportObscureNearerMemberTypeSameFileChainMidSliceViolation {
	static class Base extends ClasspathScopeOuter {}

	static class DeclaredOuter {
		static class Inner extends ClasspathScopeBase {}

		static class Mid extends Base {
			static class Sub extends Inner {
				Thread current() {
					return java.lang.Thread.currentThread();
				}

				long max() {
					return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
				}
			}
		}
	}

	static class InheritedOuter extends SameFileBase {
		static class Mid extends Base {
			static class Sub extends Inner {
				Thread current() {
					return java.lang.Thread.currentThread();
				}

				long max() {
					return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
				}
			}
		}
	}

	static class ReverseClasspathOuter extends ClasspathScopeOuter {
		static class GenericBase extends ClasspathGenericOuter<String> {}

		static class Mid extends GenericBase {
			class Sub extends Inner {
				Thread current() {
					return java.lang.Thread.currentThread(); // violation: Use an import instead of fully qualified name 'java.lang.Thread'.
				}
			}
		}
	}

	static class SameFileBase {
		static class Inner extends ClasspathScopeBase {}
	}
}
// === end ===

// === case: obscure_nearer_member_type_same_file_mid ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeBase
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeConstants
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter
class InputPreferImportObscureNearerMemberTypeSameFileMidSliceViolation {
	interface BridgeI {
		class Inner implements ClasspathScopeConstants {}
	}

	static class ClasspathOuter extends ClasspathScopeOuter {
		static class Mid extends MidBase {
			static class Sub extends Inner {
				Thread current() {
					return java.lang.Thread.currentThread(); // violation: Use an import instead of fully qualified name 'java.lang.Thread'.
				}

				String separator() {
					return java.lang.System.lineSeparator();
				}
			}
		}
	}

	enum E implements BridgeI {
		A;

		static class Sub extends Inner {
			long max() {
				return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
			}

			String separator() {
				return java.lang.System.lineSeparator();
			}
		}
	}

	static class InheritedOuter extends OuterBase {
		static class Mid extends MidBase {
			static class Sub extends Inner {
				long max() {
					return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
				}

				String separator() {
					return java.lang.System.lineSeparator();
				}
			}
		}
	}

	static class Inner extends ClasspathScopeBase {}

	static class MidBase {
		static class Inner implements ClasspathScopeConstants {}
	}

	static class MidClass extends MidBase {
		static class Sub extends Inner {
			long max() {
				return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
			}

			String separator() {
				return java.lang.System.lineSeparator();
			}
		}
	}

	interface MidI extends BridgeI {
		class Sub extends Inner {
			long max() {
				return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
			}

			String separator() {
				return java.lang.System.lineSeparator();
			}
		}
	}

	static class OuterBase {
		static class Inner extends ClasspathScopeBase {}
	}

	record R() implements BridgeI {
		static class Sub extends Inner {
			long max() {
				return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
			}

			String separator() {
				return java.lang.System.lineSeparator();
			}
		}
	}
}
// === end ===

// === case: obscure_nearer_member_type_unknown_level_warns ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter
class InputPreferImportObscureNearerMemberTypeUnknownLevelWarnsSliceViolation {
	static class Outer extends ClasspathScopeOuter {
		static class Mid extends MissingSupertype {
			static class Sub extends Inner {
				Thread current() {
					return java.lang.Thread.currentThread(); // violation (warning): Consider an import instead of fully qualified name 'java.lang.Thread'; a class this file cannot read may give the short name another meaning.
				}
			}
		}
	}
}
// === end ===

// === case: obscure_same_line_different_names ===
class InputPreferImportObscureSameLineDifferentNamesSliceViolation {
	long obscuredFirst(Object Integer) {
		return java.lang.Integer.MAX_VALUE + java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
	}

	long obscuredLast(Object Integer) {
		return java.lang.Long.MAX_VALUE + java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
	}
}
// === end ===

// === case: obscure_same_line_lambda_and_sibling_arg ===
// imports: java.util.function.IntUnaryOperator
class InputPreferImportObscureSameLineLambdaAndSiblingArgSliceViolation {
	void first(int value, IntUnaryOperator op) {}

	void last(IntUnaryOperator op, int value) {}

	void m() {
		first(java.lang.Integer.MIN_VALUE, Integer -> java.lang.Integer.MAX_VALUE); // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		last(Integer -> java.lang.Integer.MAX_VALUE, java.lang.Integer.MIN_VALUE); // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_static_import_method_conservative ===
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportObscureStaticImportMethodConservativeSliceViolation.Holder.Integer
class InputPreferImportObscureStaticImportMethodConservativeSliceViolation {
	static class Holder {
		static int Integer() {
			return 0;
		}
	}

	int obscured() {
		return java.lang.Integer.MAX_VALUE + Integer();
	}

	long unobscured() {
		return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
	}
}
// === end ===

// === case: obscure_static_import_nested_type_conservative ===
// imports: static java.util.Map.Entry
class InputPreferImportObscureStaticImportNestedTypeConservativeSliceViolation {
	Entry<String, String> entry() {
		return null;
	}

	Object obscured() {
		return java.util.Map.Entry.comparingByKey();
	}

	long unobscured() {
		return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
	}
}
// === end ===

// === case: obscure_static_import_on_demand_inherited_static ===
// imports: static com.etk2000.checkstyle.testhelpers.ClasspathScopeMiddle.*
class InputPreferImportObscureStaticImportOnDemandInheritedStaticSliceViolation {
	long protectedStatic() {
		return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
	}

	Boolean publicStatic() {
		return java.lang.Boolean.TRUE;
	}
}
// === end ===

// === case: obscure_static_import_on_demand_jdk_field ===
// imports: java.util.function.ToIntFunction
// imports: javax.swing.text.TabSet
// imports: static javax.swing.text.StyleConstants.*
class InputPreferImportObscureStaticImportOnDemandJdkFieldSliceViolation {
	ToIntFunction<TabSet> obscured() {
		return javax.swing.text.TabSet::getTabCount;
	}

	long unobscured() {
		return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
	}
}
// === end ===

// === case: obscure_static_import_on_demand_nested_owner_and_interface ===
// imports: static com.etk2000.checkstyle.testhelpers.ClasspathScopeConstants.*
// imports: static com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter.Inner.*
class InputPreferImportObscureStaticImportOnDemandNestedOwnerAndInterfaceSliceViolation {
	Thread instanceFieldIsNotImported() {
		return java.lang.Thread.currentThread(); // violation: Use an import instead of fully qualified name 'java.lang.Thread'.
	}

	String interfaceConstant() {
		return java.lang.System.lineSeparator();
	}

	Runtime nestedOwnerStatic() {
		return java.lang.Runtime.getRuntime();
	}
}
// === end ===

// === case: obscure_static_import_on_demand_same_file_enum_constants ===
// package: com.etk2000.checkstyle.inputs.preferimport
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportObscureStaticImportOnDemandSameFileEnumConstantsSliceViolation.Kind.*
class InputPreferImportObscureStaticImportOnDemandSameFileEnumConstantsSliceViolation {
	enum Kind {
		Integer,
		NAME
	}

	long constantOfAnotherName() {
		return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
	}

	int constantOfTheName() {
		return java.lang.Integer.MAX_VALUE;
	}
}
// === end ===

// === case: obscure_static_import_on_demand_same_file_owner ===
// package: com.etk2000.checkstyle.inputs.preferimport
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportObscureStaticImportOnDemandSameFileOwnerSliceViolation.Holder.*
class InputPreferImportObscureStaticImportOnDemandSameFileOwnerSliceViolation {
	static class Holder {
		static Object Integer;
	}

	int obscured() {
		return java.lang.Integer.MAX_VALUE;
	}

	long unobscured() {
		return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
	}
}
// === end ===

// === case: obscure_static_import_on_demand_same_file_owner_classpath_supertype ===
// package: com.etk2000.checkstyle.inputs.preferimport
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeBase
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeConstants
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter.Inner
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportObscureStaticImportOnDemandSameFileOwnerClasspathSupertypeSliceViolation.ChainHolder.*
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportObscureStaticImportOnDemandSameFileOwnerClasspathSupertypeSliceViolation.ClassHolder.*
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportObscureStaticImportOnDemandSameFileOwnerClasspathSupertypeSliceViolation.InterfaceHolder.*
class InputPreferImportObscureStaticImportOnDemandSameFileOwnerClasspathSupertypeSliceViolation {
	static class ChainBase extends Inner {
		static Object Short;
	}

	static class ChainHolder extends ChainBase {}

	static class ClassHolder extends ClasspathScopeBase {}

	static class InterfaceHolder implements ClasspathScopeConstants {}

	Thread chainInstance() {
		return java.lang.Thread.currentThread(); // violation: Use an import instead of fully qualified name 'java.lang.Thread'.
	}

	Runtime chainStatic() {
		return java.lang.Runtime.getRuntime();
	}

	String interfaceConstant() {
		return java.lang.System.lineSeparator();
	}

	byte packagePrivateStatic() {
		return java.lang.Byte.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Byte'.
	}

	char privateStatic() {
		return java.lang.Character.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Character'.
	}

	long protectedStatic() {
		return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
	}

	float publicInstance() {
		return java.lang.Float.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Float'.
	}

	Boolean publicStatic() {
		return java.lang.Boolean.TRUE;
	}

	short sameFileStatic() {
		return java.lang.Short.MAX_VALUE;
	}
}
// === end ===

// === case: obscure_static_import_on_demand_same_file_owner_nearer_member_type ===
// package: com.etk2000.checkstyle.inputs.preferimport
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeBase
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeOuter
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportObscureStaticImportOnDemandSameFileOwnerNearerMemberTypeSliceViolation.Mid.Holder.*
class InputPreferImportObscureStaticImportOnDemandSameFileOwnerNearerMemberTypeSliceViolation {
	static class Inner extends ClasspathScopeBase {}

	static class Mid extends ClasspathScopeOuter {
		static class Holder extends Inner {}
	}

	boolean flag() {
		return java.lang.Boolean.TRUE; // violation: Use an import instead of fully qualified name 'java.lang.Boolean'.
	}

	Object runtime() {
		return java.lang.Runtime.getRuntime();
	}
}
// === end ===

// === case: obscure_static_import_on_demand_same_file_owner_unknown_supertype ===
// package: com.etk2000.checkstyle.inputs.preferimport
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportObscureStaticImportOnDemandSameFileOwnerUnknownSupertypeSliceViolation.Holder.*
class InputPreferImportObscureStaticImportOnDemandSameFileOwnerUnknownSupertypeSliceViolation {
	static class Holder extends MissingSupertype {
		static Object Integer;
	}

	int declared() {
		return java.lang.Integer.MAX_VALUE;
	}

	long unknown() {
		return java.lang.Long.MAX_VALUE; // violation (warning): Consider an import instead of fully qualified name 'java.lang.Long'; a class this file cannot read may give the short name another meaning.
	}
}
// === end ===

// === case: obscure_static_import_on_demand_visibility ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeBase
// imports: static com.etk2000.checkstyle.testhelpers.ClasspathScopeBase.*
class InputPreferImportObscureStaticImportOnDemandVisibilitySliceViolation {
	static class Sub extends ClasspathScopeBase {
		long protectedStaticInherited() {
			return java.lang.Long.MAX_VALUE;
		}
	}

	short packagePrivateInstance() {
		return java.lang.Short.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Short'.
	}

	byte packagePrivateStatic() {
		return java.lang.Byte.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Byte'.
	}

	double privateInstance() {
		return java.lang.Double.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
	}

	char privateStatic() {
		return java.lang.Character.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Character'.
	}

	int protectedInstance() {
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}

	long protectedStatic() {
		return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
	}

	float publicInstance() {
		return java.lang.Float.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Float'.
	}

	Boolean publicStatic() {
		return java.lang.Boolean.TRUE;
	}
}
// === end ===

// === case: obscure_static_import_on_demand_visibility_same_package ===
// package: com.etk2000.checkstyle.testhelpers
// imports: static com.etk2000.checkstyle.testhelpers.ClasspathScopeBase.*
class InputPreferImportObscureStaticImportOnDemandVisibilitySamePackageSliceViolation {
	short packagePrivateInstance() {
		return java.lang.Short.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Short'.
	}

	byte packagePrivateStatic() {
		return java.lang.Byte.MAX_VALUE;
	}

	char privateStatic() {
		return java.lang.Character.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Character'.
	}

	long protectedStatic() {
		return java.lang.Long.MAX_VALUE;
	}
}
// === end ===

// === case: obscure_static_import_single_field ===
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportObscureStaticImportSingleFieldSliceViolation.Holder.Integer
class InputPreferImportObscureStaticImportSingleFieldSliceViolation {
	static class Holder {
		static Object Integer;
	}

	int obscured() {
		return java.lang.Integer.MAX_VALUE;
	}

	long unobscured() {
		return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
	}

	void use() {
		System.out.println(Integer);
	}
}
// === end ===

// === case: obscure_type_positions_still_reported ===
// imports: java.util.Collections
// imports: java.util.List
class InputPreferImportObscureTypePositionsStillReportedSliceViolation {
	static class Sub extends java.lang.Thread {} // violation: Use an import instead of fully qualified name 'java.lang.Thread'.

	static class Task implements java.lang.Runnable { // violation: Use an import instead of fully qualified name 'java.lang.Runnable'.
		@Override
		public void run() {}
	}

	Object Deprecated, Exception, Integer, Runnable, StringBuilder, Thread;

	Object arrayCreation() {
		return new java.lang.Integer[0]; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}

	Object cast(Object value) {
		return (java.lang.Integer) value; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}

	@java.lang.Deprecated // violation: Use an import instead of fully qualified name 'java.lang.Deprecated'.
	void deprecated() {}

	boolean instanceOf(Object value) {
		return value instanceof java.lang.Integer; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}

	Object localType() {
		final java.lang.Integer boxed = null; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		return boxed;
	}

	Object newExpression() {
		return new java.lang.StringBuilder(); // violation: Use an import instead of fully qualified name 'java.lang.StringBuilder'.
	}

	Object ownDeclaredType() {
		final java.lang.Integer Integer = null; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		return Integer;
	}

	void paramType(java.lang.Integer value) {} // violation: Use an import instead of fully qualified name 'java.lang.Integer'.

	java.lang.Integer returnType() { // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		return null;
	}

	void throwsClause() throws java.lang.Exception {} // violation: Use an import instead of fully qualified name 'java.lang.Exception'.

	List<java.lang.Integer> typeArgument() { // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
		return null;
	}

	Object witness() {
		return Collections.<java.lang.Integer>emptyList(); // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_unknown_supertype_owner_kinds ===
// imports: java.lang.annotation.ElementType
// imports: java.lang.annotation.Target
class InputPreferImportObscureUnknownSupertypeOwnerKindsSliceViolation {
	@Target(ElementType.TYPE_USE)
	@interface Ann {}

	static class Annotated extends @Ann MissingSupertype {
		int max() {
			return java.lang.Integer.MAX_VALUE; // violation (warning): Consider an import instead of fully qualified name 'java.lang.Integer'; a class this file cannot read may give the short name another meaning.
		}
	}

	static class ClassImplements implements MissingInterface {
		int max() {
			return java.lang.Integer.MAX_VALUE; // violation (warning): Consider an import instead of fully qualified name 'java.lang.Integer'; a class this file cannot read may give the short name another meaning.
		}
	}

	enum EnumImplements implements MissingInterface {
		A;

		int max() {
			return java.lang.Integer.MAX_VALUE; // violation (warning): Consider an import instead of fully qualified name 'java.lang.Integer'; a class this file cannot read may give the short name another meaning.
		}
	}

	static class Generic extends MissingGeneric<String> {
		int max() {
			return java.lang.Integer.MAX_VALUE; // violation (warning): Consider an import instead of fully qualified name 'java.lang.Integer'; a class this file cannot read may give the short name another meaning.
		}
	}

	class InnerNonStatic extends MissingSupertype {
		int max() {
			return java.lang.Integer.MAX_VALUE; // violation (warning): Consider an import instead of fully qualified name 'java.lang.Integer'; a class this file cannot read may give the short name another meaning.
		}
	}

	interface InterfaceExtends extends MissingInterface {
		default int max() {
			return java.lang.Integer.MAX_VALUE; // violation (warning): Consider an import instead of fully qualified name 'java.lang.Integer'; a class this file cannot read may give the short name another meaning.
		}
	}

	static class Leaf extends Middle {
		int max() {
			return java.lang.Integer.MAX_VALUE; // violation (warning): Consider an import instead of fully qualified name 'java.lang.Integer'; a class this file cannot read may give the short name another meaning.
		}
	}

	static class Middle extends MissingSupertype {}

	static class OfSubclass extends MissingSupertype {
		static class Member {
			int max() {
				return java.lang.Integer.MAX_VALUE; // violation (warning): Consider an import instead of fully qualified name 'java.lang.Integer'; a class this file cannot read may give the short name another meaning.
			}
		}
	}

	record RecordImplements() implements MissingInterface {
		int max() {
			return java.lang.Integer.MAX_VALUE; // violation (warning): Consider an import instead of fully qualified name 'java.lang.Integer'; a class this file cannot read may give the short name another meaning.
		}
	}

	Object anonymous() {
		return new MissingSupertype() {
			@Override
			public int hashCode() {
				return java.lang.Integer.MAX_VALUE; // violation (warning): Consider an import instead of fully qualified name 'java.lang.Integer'; a class this file cannot read may give the short name another meaning.
			}
		};
	}

	Object anonymousGeneric() {
		return new MissingGeneric<String>() {
			@Override
			public int hashCode() {
				return java.lang.Integer.MAX_VALUE; // violation (warning): Consider an import instead of fully qualified name 'java.lang.Integer'; a class this file cannot read may give the short name another meaning.
			}
		};
	}

	Object local() {
		class Local extends MissingSupertype {
			int max() {
				return java.lang.Integer.MAX_VALUE; // violation (warning): Consider an import instead of fully qualified name 'java.lang.Integer'; a class this file cannot read may give the short name another meaning.
			}
		}

		return new Local();
	}

	int unobscured() {
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: obscure_unknown_supertype_proven_binder_wins ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeBase
// imports: com.etk2000.checkstyle.testhelpers.ClasspathScopeConstants
class InputPreferImportObscureUnknownSupertypeProvenBinderWinsSliceViolation {
	static class ClasspathInsideUnknown extends MissingSupertype {
		static class Sub extends ClasspathScopeBase {
			Boolean flag() {
				return java.lang.Boolean.TRUE; // violation (warning): Consider an import instead of fully qualified name 'java.lang.Boolean'; a class this file cannot read may give the short name another meaning.
			}

			Thread thread() {
				return java.lang.Thread.currentThread(); // violation (warning): Consider an import instead of fully qualified name 'java.lang.Thread'; a class this file cannot read may give the short name another meaning.
			}
		}
	}

	static class LocalBinder extends MissingSupertype {
		int parameter(Object Integer) {
			return java.lang.Integer.MAX_VALUE;
		}
	}

	static class UnknownFirst implements MissingInterface, ClasspathScopeConstants {
		String separator() {
			return java.lang.System.lineSeparator();
		}
	}

	static class UnknownInsideClasspath extends ClasspathScopeBase {
		static class Sub extends MissingSupertype {
			Boolean flag() {
				return java.lang.Boolean.TRUE;
			}
		}
	}

	static class UnknownLast implements ClasspathScopeConstants, MissingInterface {
		String separator() {
			return java.lang.System.lineSeparator();
		}
	}
}
// === end ===

// === case: obscure_unknown_supertype_unloadable_import ===
// imports: com.unknown.Missing
class InputPreferImportObscureUnknownSupertypeUnloadableImportSliceViolation {
	static class Sub extends Missing {
		int max() {
			return java.lang.Integer.MAX_VALUE; // violation (warning): Consider an import instead of fully qualified name 'java.lang.Integer'; a class this file cannot read may give the short name another meaning.
		}
	}

	long unobscured() {
		return java.lang.Long.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Long'.
	}
}
// === end ===

// === case: real_import_collision_unmasked ===
// imports: some.other.PreferImportFixer
// skip-reason: cannot shorten the name: the short name resolves to a different type than the qualified name (a single-type import, same-package type, or dependency type with the same simple name shadows it)
class InputPreferImportRealImportCollisionUnmaskedSliceViolation {
	com.etk2000.checkstyle.gradle.fix.PreferImportFixer fixer; // violation: Use an import instead of fully qualified name 'com.etk2000.checkstyle.gradle.fix.PreferImportFixer'.
}
// === end ===

// === case: real_wildcard_blocks_java_lang ===
// imports: foo.*
// skip-reason: cannot shorten the name: the short name is a java.lang type but a wildcard import in scope could bind the same simple name
class InputPreferImportRealWildcardBlocksJavaLangSliceViolation {
	java.lang.Runnable task; // violation: Use an import instead of fully qualified name 'java.lang.Runnable'.
}
// === end ===

// === case: reflection_dependency_collision ===
// package: com.etk2000.checkstyle.gradle.fix
// skip-reason: cannot shorten the name: the short name resolves to a different type than the qualified name (a single-type import, same-package type, or dependency type with the same simple name shadows it)
class InputPreferImportReflectionDependencyCollisionSliceViolation {
	some.other.PreferImportFixer fixer; // violation: Use an import instead of fully qualified name 'some.other.PreferImportFixer'.
}
// === end ===

// === case: reflection_dependency_strip ===
// package: com.etk2000.checkstyle.gradle.fix
class InputPreferImportReflectionDependencyStripSliceViolation {
	com.etk2000.checkstyle.gradle.fix.PreferImportFixer fixer; // violation: Use an import instead of fully qualified name 'com.etk2000.checkstyle.gradle.fix.PreferImportFixer'.
}
// === end ===

// === case: reflection_dependency_strip_block_comment ===
// package: com.etk2000.checkstyle.gradle.fix
class InputPreferImportReflectionDependencyStripBlockCommentSliceViolation {
	/*
import some.other.PreferImportFixer;
	*/
	com.etk2000.checkstyle.gradle.fix.PreferImportFixer fixer; // violation: Use an import instead of fully qualified name 'com.etk2000.checkstyle.gradle.fix.PreferImportFixer'.
}
// === end ===

// === case: reflection_dependency_strip_text_block ===
// package: com.etk2000.checkstyle.gradle.fix
class InputPreferImportReflectionDependencyStripTextBlockSliceViolation {
	com.etk2000.checkstyle.gradle.fix.PreferImportFixer fixer; // violation: Use an import instead of fully qualified name 'com.etk2000.checkstyle.gradle.fix.PreferImportFixer'.
	String doc = """
import some.other.PreferImportFixer;
""";
}
// === end ===

// === case: shadow_annotation_type ===
// skip-reason: cannot shorten the name: the short name is shadowed by a type declaration or type parameter in this file
class InputPreferImportShadowAnnotationTypeSliceViolation {
	@interface Map {}

	java.util.Map<String, Integer> field; // violation: Use an import instead of fully qualified name 'java.util.Map'.
}
// === end ===

// === case: shadow_enum_type ===
// skip-reason: cannot shorten the name: the short name is shadowed by a type declaration or type parameter in this file
class InputPreferImportShadowEnumTypeSliceViolation {
	enum Map { A }

	java.util.Map<String, Integer> field; // violation: Use an import instead of fully qualified name 'java.util.Map'.
}
// === end ===

// === case: shadow_interface_type ===
// skip-reason: cannot shorten the name: the short name is shadowed by a type declaration or type parameter in this file
class InputPreferImportShadowInterfaceTypeSliceViolation {
	interface List {}

	java.util.List<String> field; // violation: Use an import instead of fully qualified name 'java.util.List'.
}
// === end ===

// === case: shadow_nested_type ===
// skip-reason: cannot shorten the name: the short name is shadowed by a type declaration or type parameter in this file
class InputPreferImportShadowNestedTypeSliceViolation {
	static final class List {}

	java.util.List<String> field; // violation: Use an import instead of fully qualified name 'java.util.List'.
}
// === end ===

// === case: shadow_record_type ===
// skip-reason: cannot shorten the name: the short name is shadowed by a type declaration or type parameter in this file
class InputPreferImportShadowRecordTypeSliceViolation {
	record List() {}

	java.util.List<String> field; // violation: Use an import instead of fully qualified name 'java.util.List'.
}
// === end ===

// === case: shadow_type_param ===
// skip-reason: cannot shorten the name: the short name is shadowed by a type declaration or type parameter in this file
class InputPreferImportShadowTypeParamSliceViolation<List> {
	java.util.List<String> field; // violation: Use an import instead of fully qualified name 'java.util.List'.
}
// === end ===

// === case: static_call_fqn ===
// imports: java.util.List
class InputPreferImportStaticCallFqnSliceViolation {
	void m() {
		java.util.List.of(1, 2); // violation: Use an import instead of fully qualified name 'java.util.List'.
	}
}
// === end ===

// === case: static_call_type_witness ===
// imports: java.util.List
class InputPreferImportStaticCallTypeWitnessSliceViolation {
	void m() {
		java.util.List.<Integer>of(1); // violation: Use an import instead of fully qualified name 'java.util.List'.
	}
}
// === end ===

// === case: static_call_witness_fqn ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferImportStaticCallWitnessFqnSliceViolation {
	void m() {
		java.util.List.<java.util.Map>of(); // violation: Use an import instead of fully qualified name 'java.util.List'. // violation: Use an import instead of fully qualified name 'java.util.Map'.
	}
}
// === end ===

// === case: static_field_fqn ===
class InputPreferImportStaticFieldFqnSliceViolation {
	int max() {
		return java.lang.Integer.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.
	}
}
// === end ===

// === case: static_import_member_type_explicit_import_wins_strips ===
// imports: java.lang.Thread.State
// imports: static com.unknown.Missing.*
// imports: static java.util.concurrent.Future.*
class InputPreferImportStaticImportMemberTypeExplicitImportWinsStripsSliceViolation {
	java.lang.Thread.State state; // violation: Use an import instead of fully qualified name 'java.lang.Thread.State'.
}
// === end ===

// === case: static_import_member_type_inherited_reason_wins_skip ===
// imports: java.awt.geom.Point2D
// imports: static java.awt.geom.Point2D.*
// skip-reason: cannot shorten the name: a supertype declared outside this file passes down a member type of the same name
class InputPreferImportStaticImportMemberTypeInheritedReasonWinsSkipSliceViolation {
	abstract static class Shape extends Point2D {
		java.lang.Double value; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
	}
}
// === end ===

// === case: static_import_member_type_on_demand_boundaries_strip ===
// imports: static com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes.*
class InputPreferImportStaticImportMemberTypeOnDemandBoundariesStripSliceViolation {
	java.lang.Byte packagePrivate; // violation: Use an import instead of fully qualified name 'java.lang.Byte'.
	java.lang.Character privateMember; // violation: Use an import instead of fully qualified name 'java.lang.Character'.
	java.lang.Double nonStatic; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
	java.lang.Short protectedMember; // violation: Use an import instead of fully qualified name 'java.lang.Short'.
}
// === end ===

// === case: static_import_member_type_on_demand_hidden_by_inner_class_strips ===
// imports: static com.etk2000.checkstyle.testhelpers.ClasspathInnerDoublePoint.*
class InputPreferImportStaticImportMemberTypeOnDemandHiddenByInnerClassStripsSliceViolation {
	java.lang.Double field; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
}
// === end ===

// === case: static_import_member_type_on_demand_kinds_skip ===
// imports: java.util.function.Supplier
// imports: static com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes.*
// skip-reason: cannot shorten the name: a static import brings in a member type of the same name
class InputPreferImportStaticImportMemberTypeOnDemandKindsSkipSliceViolation {
	static class Failure extends java.lang.Exception {} // violation: Use an import instead of fully qualified name 'java.lang.Exception'.

	abstract static class Task implements java.lang.Runnable {} // violation: Use an import instead of fully qualified name 'java.lang.Runnable'.

	java.lang.Integer boxed; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.

	@java.lang.Deprecated // violation: Use an import instead of fully qualified name 'java.lang.Deprecated'.
	Object annotated() {
		return null;
	}

	Object caught() {
		try {
			fail();
		}
		catch (java.lang.Exception e) { // violation: Use an import instead of fully qualified name 'java.lang.Exception'.
			return e;
		}
		return null;
	}

	Supplier<Object> constructorReference() {
		return java.lang.Exception::new; // violation: Use an import instead of fully qualified name 'java.lang.Exception'.
	}

	void fail() throws java.lang.Exception { // violation: Use an import instead of fully qualified name 'java.lang.Exception'.
		throw new Failure();
	}
}
// === end ===

// === case: static_import_member_type_on_demand_skip ===
// imports: java.util.List
// imports: java.util.function.Function
// imports: static java.awt.geom.Point2D.*
// skip-reason: cannot shorten the name: a static import brings in a member type of the same name
class InputPreferImportStaticImportMemberTypeOnDemandSkipSliceViolation {
	record Box(java.lang.Double value) {} // violation: Use an import instead of fully qualified name 'java.lang.Double'.

	java.lang.Double field; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
	java.lang.Float second; // violation: Use an import instead of fully qualified name 'java.lang.Float'.

	Object array() {
		return new java.lang.Double[0]; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
	}

	Object cast(Object value) {
		return (java.lang.Double) value; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
	}

	Object classLiteral() {
		return java.lang.Double.class; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
	}

	double fieldAccess() {
		return java.lang.Double.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
	}

	boolean instanceOf(Object value) {
		return value instanceof java.lang.Double; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
	}

	double methodCall() {
		return java.lang.Double.parseDouble("1"); // violation: Use an import instead of fully qualified name 'java.lang.Double'.
	}

	Function<String, Object> methodReference() {
		return java.lang.Double::parseDouble; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
	}

	java.lang.Double returned() { // violation: Use an import instead of fully qualified name 'java.lang.Double'.
		return null;
	}

	List<java.lang.Double> typeArgument() { // violation: Use an import instead of fully qualified name 'java.lang.Double'.
		return null;
	}

	Object varargs(java.lang.Double... values) { // violation: Use an import instead of fully qualified name 'java.lang.Double'.
		return values;
	}

	List<? extends java.lang.Double> wildcardBound() { // violation: Use an import instead of fully qualified name 'java.lang.Double'.
		return null;
	}
}
// === end ===

// === case: static_import_member_type_on_demand_unknown_skip ===
// imports: static com.unknown.Missing.*
// skip-reason: cannot shorten the name: a class this file statically imports from cannot be read and may bring in a member type of the same name
class InputPreferImportStaticImportMemberTypeOnDemandUnknownSkipSliceViolation {
	java.lang.Double field; // violation: Use an import instead of fully qualified name 'java.lang.Double'.

	Object classLiteral() {
		return java.lang.Double.class; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
	}
}
// === end ===

// === case: static_import_member_type_same_file_owner_chain_skip ===
// package: com.etk2000.checkstyle.inputs.preferimport
// imports: java.awt.geom.Point2D
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportStaticImportMemberTypeSameFileOwnerChainSkipSliceViolation.ChainHolder.*
// skip-reason: cannot shorten the name: a static import brings in a member type of the same name
class InputPreferImportStaticImportMemberTypeSameFileOwnerChainSkipSliceViolation {
	abstract static class ChainHolder extends PointHolder {}

	abstract static class PointHolder extends Point2D {}

	java.lang.Double field; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
}
// === end ===

// === case: static_import_member_type_same_file_owner_single_skip ===
// package: com.etk2000.checkstyle.inputs.preferimport
// imports: java.awt.geom.Point2D
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportStaticImportMemberTypeSameFileOwnerSingleSkipSliceViolation.PointHolder.Double
// skip-reason: cannot shorten the name: a static import brings in a member type of the same name
class InputPreferImportStaticImportMemberTypeSameFileOwnerSingleSkipSliceViolation {
	abstract static class PointHolder extends Point2D {}

	java.lang.Double field; // violation: Use an import instead of fully qualified name 'java.lang.Double'.

	Object point() {
		return new Double();
	}
}
// === end ===

// === case: static_import_member_type_same_file_owner_single_unknown_skip ===
// package: com.etk2000.checkstyle.inputs.preferimport
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportStaticImportMemberTypeSameFileOwnerSingleUnknownSkipSliceViolation.Holder.Double
// skip-reason: cannot shorten the name: a class this file statically imports from cannot be read and may bring in a member type of the same name
class InputPreferImportStaticImportMemberTypeSameFileOwnerSingleUnknownSkipSliceViolation {
	static class Holder extends MissingSupertype {}

	java.lang.Double field; // violation: Use an import instead of fully qualified name 'java.lang.Double'.

	Object used() {
		return Double;
	}
}
// === end ===

// === case: static_import_member_type_same_file_owner_strips ===
// package: com.etk2000.checkstyle.inputs.preferimport
// imports: com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportStaticImportMemberTypeSameFileOwnerStripsSliceViolation.Holder.*
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportStaticImportMemberTypeSameFileOwnerStripsSliceViolation.Plain.*
class InputPreferImportStaticImportMemberTypeSameFileOwnerStripsSliceViolation {
	static class Holder extends ClasspathMemberTypes {}

	static class Plain {}

	java.lang.Double field; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
	java.lang.Short protectedMember; // violation: Use an import instead of fully qualified name 'java.lang.Short'.

	short value() {
		return protectedMember.shortValue();
	}
}
// === end ===

// === case: static_import_member_type_same_file_owner_unknown_skip ===
// package: com.etk2000.checkstyle.inputs.preferimport
// imports: static com.etk2000.checkstyle.inputs.preferimport.InputPreferImportStaticImportMemberTypeSameFileOwnerUnknownSkipSliceViolation.Holder.*
// skip-reason: cannot shorten the name: a class this file statically imports from cannot be read and may bring in a member type of the same name
class InputPreferImportStaticImportMemberTypeSameFileOwnerUnknownSkipSliceViolation {
	static class Holder extends MissingSupertype {}

	java.lang.Double field; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
}
// === end ===

// === case: static_import_member_type_same_package_on_demand_skip ===
// package: com.etk2000.checkstyle.testhelpers
// imports: static com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes.*
// skip-reason: cannot shorten the name: a static import brings in a member type of the same name
class InputPreferImportStaticImportMemberTypeSamePackageOnDemandSkipSliceViolation {
	java.lang.Byte packagePrivate; // violation: Use an import instead of fully qualified name 'java.lang.Byte'.
	java.lang.Short protectedMember; // violation: Use an import instead of fully qualified name 'java.lang.Short'.

	byte max() {
		return java.lang.Byte.MAX_VALUE; // violation: Use an import instead of fully qualified name 'java.lang.Byte'.
	}
}
// === end ===

// === case: static_import_member_type_same_package_on_demand_strips ===
// package: com.etk2000.checkstyle.testhelpers
// imports: static com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes.*
class InputPreferImportStaticImportMemberTypeSamePackageOnDemandStripsSliceViolation {
	com.etk2000.checkstyle.testhelpers.ClasspathScopeBase base; // violation: Use an import instead of fully qualified name 'com.etk2000.checkstyle.testhelpers.ClasspathScopeBase'.

	java.lang.Character privateMember() { // violation: Use an import instead of fully qualified name 'java.lang.Character'.
		return null;
	}
}
// === end ===

// === case: static_import_member_type_same_package_on_demand_unknown_strips ===
// package: com.etk2000.checkstyle.testhelpers
// imports: static com.unknown.Missing.*
class InputPreferImportStaticImportMemberTypeSamePackageOnDemandUnknownStripsSliceViolation {
	com.etk2000.checkstyle.testhelpers.ClasspathScopeBase base; // violation: Use an import instead of fully qualified name 'com.etk2000.checkstyle.testhelpers.ClasspathScopeBase'.
}
// === end ===

// === case: static_import_member_type_same_package_same_file_owner_skip ===
// package: com.etk2000.checkstyle.testhelpers
// imports: static com.etk2000.checkstyle.testhelpers.InputPreferImportStaticImportMemberTypeSamePackageSameFileOwnerSkipSliceViolation.Holder.*
// skip-reason: cannot shorten the name: a static import brings in a member type of the same name
class InputPreferImportStaticImportMemberTypeSamePackageSameFileOwnerSkipSliceViolation {
	static class Holder extends ClasspathMemberTypes {}

	java.lang.Byte packagePrivate; // violation: Use an import instead of fully qualified name 'java.lang.Byte'.
}
// === end ===

// === case: static_import_member_type_same_package_single_package_private_skip ===
// package: com.etk2000.checkstyle.testhelpers
// imports: static com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes.Byte
// skip-reason: cannot shorten the name: a static import brings in a member type of the same name
class InputPreferImportStaticImportMemberTypeSamePackageSinglePackagePrivateSkipSliceViolation {
	java.lang.Byte typed; // violation: Use an import instead of fully qualified name 'java.lang.Byte'.

	Object used() {
		return new Byte();
	}
}
// === end ===

// === case: static_import_member_type_same_package_single_skip ===
// package: com.etk2000.checkstyle.testhelpers
// imports: static com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes.ClasspathScopeBase
// skip-reason: cannot shorten the name: a static import brings in a member type of the same name
class InputPreferImportStaticImportMemberTypeSamePackageSingleSkipSliceViolation {
	com.etk2000.checkstyle.testhelpers.ClasspathScopeBase base; // violation: Use an import instead of fully qualified name 'com.etk2000.checkstyle.testhelpers.ClasspathScopeBase'.

	Object used() {
		return new ClasspathScopeBase();
	}
}
// === end ===

// === case: static_import_member_type_same_package_single_unknown_skip ===
// package: com.etk2000.checkstyle.testhelpers
// imports: static com.unknown.Missing.ClasspathScopeBase
// skip-reason: cannot shorten the name: a class this file statically imports from cannot be read and may bring in a member type of the same name
class InputPreferImportStaticImportMemberTypeSamePackageSingleUnknownSkipSliceViolation {
	com.etk2000.checkstyle.testhelpers.ClasspathScopeBase base; // violation: Use an import instead of fully qualified name 'com.etk2000.checkstyle.testhelpers.ClasspathScopeBase'.

	Object used() {
		return ClasspathScopeBase;
	}
}
// === end ===

// === case: static_import_member_type_single_other_name_strips ===
// imports: static java.awt.geom.Point2D.Float
class InputPreferImportStaticImportMemberTypeSingleOtherNameStripsSliceViolation {
	java.lang.Double field; // violation: Use an import instead of fully qualified name 'java.lang.Double'.

	Object point() {
		return new Float(1, 2);
	}
}
// === end ===

// === case: static_import_member_type_single_skip ===
// imports: java.util.List
// imports: static java.awt.geom.Point2D.Double
// skip-reason: cannot shorten the name: a static import brings in a member type of the same name
class InputPreferImportStaticImportMemberTypeSingleSkipSliceViolation {
	java.lang.Double field; // violation: Use an import instead of fully qualified name 'java.lang.Double'.

	Object array() {
		return new java.lang.Double[0]; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
	}

	Object cast(Object value) {
		return (java.lang.Double) value; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
	}

	Object classLiteral() {
		return java.lang.Double.class; // violation: Use an import instead of fully qualified name 'java.lang.Double'.
	}

	double fieldAccess() {
		return java.lang.Double.MAX_VALUE;
	}

	Object point() {
		return new Double(1, 2);
	}

	List<java.lang.Double> typeArgument() { // violation: Use an import instead of fully qualified name 'java.lang.Double'.
		return null;
	}
}
// === end ===

// === case: static_import_member_type_single_two_imports_skip ===
// package: com.etk2000.checkstyle.testhelpers
// imports: static com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes.Integer
// imports: static com.etk2000.checkstyle.testhelpers.InputPreferImportStaticImportMemberTypeSingleTwoImportsSkipSliceViolation.Holder.Integer
// skip-reason: cannot shorten the name: a static import brings in a member type of the same name
class InputPreferImportStaticImportMemberTypeSingleTwoImportsSkipSliceViolation {
	static class Holder {
		static int Integer;
	}

	java.lang.Integer typed; // violation: Use an import instead of fully qualified name 'java.lang.Integer'.

	Object record() {
		return new Integer();
	}
}
// === end ===

// === case: static_import_member_type_single_unknown_skip ===
// imports: static com.unknown.Shapes.Double
// skip-reason: cannot shorten the name: a class this file statically imports from cannot be read and may bring in a member type of the same name
class InputPreferImportStaticImportMemberTypeSingleUnknownSkipSliceViolation {
	java.lang.Double field; // violation: Use an import instead of fully qualified name 'java.lang.Double'.

	Object used() {
		return Double;
	}
}
// === end ===

// === case: static_import_member_type_wildcard_reason_wins_skip ===
// imports: java.util.*
// imports: static java.awt.geom.Point2D.*
// skip-reason: cannot shorten the name: the short name is a java.lang type but a wildcard import in scope could bind the same simple name
class InputPreferImportStaticImportMemberTypeWildcardReasonWinsSkipSliceViolation {
	java.lang.Double field; // violation: Use an import instead of fully qualified name 'java.lang.Double'.

	List<String> list() {
		return null;
	}
}
// === end ===

// === case: text_block_package_masked ===
// skip-reason: cannot shorten the name: no single-type import, same-package type, or java.lang type binds the short name (adding an import is the general PreferImport fixer's job)
class InputPreferImportTextBlockPackageMaskedSliceViolation {
	com.etk2000.checkstyle.gradle.fix.PreferImportFixer fixer; // violation: Use an import instead of fully qualified name 'com.etk2000.checkstyle.gradle.fix.PreferImportFixer'.
	String s = """
package com.etk2000.checkstyle.gradle.fix;
""";
}
// === end ===

// === case: text_block_wildcard_java_lang_strips ===
class InputPreferImportTextBlockWildcardJavaLangStripsSliceViolation {
	java.lang.Runnable task; // violation: Use an import instead of fully qualified name 'java.lang.Runnable'.
	String s = """
import foo.*;
""";
}
// === end ===

// === case: throws_fqn ===
// imports: java.io.IOException
class InputPreferImportThrowsSliceViolation {
	void m() throws java.io.IOException {} // violation: Use an import instead of fully qualified name 'java.io.IOException'.
}
// === end ===

// === case: type_arg_two_fqns_one_line ===
// imports: java.util.List
// imports: java.util.Map
// imports: java.util.Set
class InputPreferImportTypeArgTwoFqnsOneLineSliceViolation {
	Map<java.util.List, java.util.Set> field; // violation: Use an import instead of fully qualified name 'java.util.List'. // violation: Use an import instead of fully qualified name 'java.util.Set'.
}
// === end ===

// === case: whitespace_split_qualified_name ===
// skip-reason: cannot shorten the name: the qualified name is not contiguous at the violation column (comments or unusual whitespace split the dotted run)
class InputPreferImportWhitespaceSplitSliceViolation {
	java . util . List field; // violation: Use an import instead of fully qualified name 'java.util.List'.
}
// === end ===

// === case: wildcard_lower_bound_type_arg ===
// imports: java.util.List
// imports: java.util.Set
class InputPreferImportWildcardLowerBoundTypeArgSliceViolation {
	List<? super java.util.Set> field; // violation: Use an import instead of fully qualified name 'java.util.Set'.
}
// === end ===

// === case: wildcard_only ===
// imports: java.util.*
// skip-reason: cannot shorten the name: resolution depends on a wildcard import whose contents cannot be confirmed from this file
class InputPreferImportWildcardOnlySliceViolation {
	java.util.Map<String, Integer> field; // violation: Use an import instead of fully qualified name 'java.util.Map'.
}
// === end ===

// === case: wildcard_upper_bound_type_arg ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferImportWildcardUpperBoundTypeArgSliceViolation {
	List<? extends java.util.Map> field; // violation: Use an import instead of fully qualified name 'java.util.Map'.
}
// === end ===