package ph.codeia.shiv.demo.provision;

import androidx.fragment.app.FragmentFactory;

import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import ph.codeia.shiv.demo.AppFlow;
import ph.codeia.shiv.demo.MainFragment;
import shiv.FragmentBindings;

/*
 * This file is a part of the Shiv project.
 */


@Per.Configuration
@Subcomponent(modules = {FragmentBindings.class, ViewComponent.Providers.class})
public interface ViewComponent {
	FragmentFactory fragmentFactory();

	@Module
	abstract class Providers {
		@Binds
		public abstract AppFlow bind(MainFragment main);
	}
}
