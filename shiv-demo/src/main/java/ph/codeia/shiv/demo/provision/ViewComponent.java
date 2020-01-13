package ph.codeia.shiv.demo.provision;

import androidx.fragment.app.FragmentFactory;

import dagger.Binds;
import dagger.BindsInstance;
import dagger.Module;
import dagger.Subcomponent;
import ph.codeia.shiv.Shiv;
import ph.codeia.shiv.demo.AppFlow;
import ph.codeia.shiv.demo.MainFragment;
import shiv.FragmentBindings;

/*
 * This file is a part of the Shiv project.
 */


@Subcomponent(modules = {Shiv.class, FragmentBindings.class, ViewComponent.Providers.class})
public interface ViewComponent {
	FragmentFactory fragmentFactory();

	@Subcomponent.Factory
	interface Factory {
		ViewComponent create(@BindsInstance MainFragment main);
	}

	@Module
	abstract class Providers {
		@Binds
		public abstract AppFlow bind(MainFragment main);
	}
}
