package ph.codeia.shiv.demo.wiring;

import androidx.lifecycle.ViewModelStoreOwner;

import dagger.BindsInstance;
import dagger.Subcomponent;
import shiv.SharedViewModelProviders;
import shiv.ViewModelBindings;

/*
 * This file is a part of the Shiv project.
 */


@Per.Activity
@Subcomponent(modules = {SharedViewModelProviders.class, ViewModelBindings.class})
public interface ModelComponent {
	ViewComponent viewComponent();

	@Subcomponent.Factory
	interface Factory {
		ModelComponent create(@BindsInstance ViewModelStoreOwner owner);
	}
}
