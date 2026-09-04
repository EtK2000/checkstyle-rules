package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

/**
 * A key logged at both severities carries a {@code .warning} sibling whose text uses softer
 * wording. Slice markers pin the rendered text, which tells the two keys apart; these cases pin
 * the key itself at every log site, so a site that renders the right text through the wrong key
 * still fails.
 */
public class SeverityKeySplitTest {
	@Nonnull
	private static Stream<Arguments> sites() {
		return Stream.of(
				Arguments.of(PreferCollectionInterfaceCheck.class, "prefercollectioninterface", "array_list_to_list_constructor_param", List.of("error:prefer.replacement")),
				Arguments.of(
						PreferCollectionInterfaceCheck.class,
						"prefercollectioninterface",
						"final_owner_splits_the_two_tiers",
						List.of("warning:prefer.replacement.warning", "error:prefer.replacement")
				),
				Arguments.of(PreferCollectionInterfaceCheck.class, "prefercollectioninterface", "hash_map_to_map_fqn", List.of("error:prefer.replacement")),
				Arguments.of(PreferCollectionInterfaceCheck.class, "prefercollectioninterface", "hash_map_to_map_fqn_warns", List.of("warning:prefer.replacement.warning")),
				Arguments.of(PreferCollectionInterfaceCheck.class, "prefercollectioninterface", "hash_map_to_map_warns", List.of("warning:prefer.replacement.warning")),
				Arguments.of(PreferVarCheck.class, "prefervar", "multi_var_warns", List.of("warning:prefer.var.local.warning")),
				Arguments.of(
						PreferVarCheck.class,
						"prefervar",
						"parse_int",
						List.of(
								"error:prefer.var.local",
								"warning:prefer.var.local.warning",
								"warning:prefer.var.local.warning",
								"warning:prefer.var.local.warning"
						)
				),
				Arguments.of(PreferVarCheck.class, "prefervar", "reassigned_to_a_different_type_warns", List.of("warning:prefer.var.local.warning")),
				Arguments.of(PreferVarCheck.class, "prefervar", "reassigned_to_the_same_type", List.of("error:prefer.var.local")),
				Arguments.of(
						PreferVarCheck.class,
						"prefervar",
						"same_file_narrowing_nested_class_overload_warns",
						List.of("warning:prefer.var.local.warning")
				),
				Arguments.of(
						PreferVarCheck.class,
						"prefervar",
						"try_resource_first_of_two_narrows_warns",
						List.of("warning:prefer.var.try.resource.warning", "error:prefer.var.try.resource")
				),
				Arguments.of(
						PreferVarCheck.class,
						"prefervar",
						"try_resource_overload_selection_changes_warns",
						List.of("warning:prefer.var.try.resource.warning")
				)
		);
	}

	@MethodSource("sites")
	@ParameterizedTest(name = "{1}/{2}")
	public void testEachSiteLogsTheKeyOfItsSeverity(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull String topic,
			@Nonnull String caseName,
			@Nonnull List<String> expected
	) throws Exception {
		final var slice = TestResources.loadCaseSlice(topic, caseName);
		final var events = BaseCheckTest.runCheckInline(checkClass, BaseCheckTest.stripViolationMarkers(String.join("\n", slice.inputLines())));
		assertEquals(expected, events.stream().map(event -> event.getSeverityLevel().getName() + ':' + event.getViolation().getKey()).toList());
	}
}