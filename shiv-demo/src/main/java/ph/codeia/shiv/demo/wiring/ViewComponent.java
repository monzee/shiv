package ph.codeia.shiv.demo.wiring;

import androidx.fragment.app.FragmentFactory;

import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import ph.codeia.shiv.Shiv;
import ph.codeia.shiv.demo.AppFlow;
import ph.codeia.shiv.demo.MainFragment;
import ph.codeia.shiv.demo.login.LoginComponent;
import ph.codeia.shiv.demo.login.LoginModule;
import shiv.FragmentBindings;

/*
 * This file is a part of the Shiv project.
 */


@Per.Configuration
@Subcomponent(modules = {
	Shiv.class,
	FragmentBindings.class,
	ViewComponent.Providers.class
})
public interface ViewComponent {
	FragmentFactory fragmentFactory();
	LoginComponent loginComponent(LoginModule module);

	@Module
	abstract class Providers {
		@Binds
		public abstract AppFlow appFlow(MainFragment main);
	}
}
