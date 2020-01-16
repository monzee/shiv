package ph.codeia.shiv.demo.wiring;

import javax.inject.Scope;

/*
 * This file is a part of the Shiv project.
 */


public final class Per {
	@Scope
	public @interface Activity {
	}

	@Scope
	public @interface Configuration {
	}

	private Per() {
	}
}
