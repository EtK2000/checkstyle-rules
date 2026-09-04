package com.etk2000.checkstyle;

import javax.annotation.CheckReturnValue;

/**
 * Base for checks that suggest APIs unavailable below some Android API level.
 * Owns the {@code minSdk} property so the gating checks share one declaration.
 */
abstract class AbstractMinSdkCheck extends AbstractAstCheck {
	private int minSdk = Integer.MAX_VALUE;

	/**
	 * Whether the configured target platform is new enough to offer an API
	 * introduced at {@code required}.
	 */
	@CheckReturnValue
	protected final boolean minSdkAtLeast(int required) {
		return minSdk >= required;
	}

	/** Called by Checkstyle via reflection when {@code minSdk} is set in the config. */
	@SuppressWarnings("unused")
	public void setMinSdk(int minSdk) {
		this.minSdk = minSdk;
	}
}