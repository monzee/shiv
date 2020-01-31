package ph.codeia.shiv.demo.login;

import dagger.Subcomponent;

/*
 * This file is a part of the Shiv project.
 */


@Subcomponent(modules = LoginModule.class)
public interface LoginComponent {
	LoginView loginView();
}
