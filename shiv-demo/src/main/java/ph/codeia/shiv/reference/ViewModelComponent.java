package ph.codeia.shiv.reference;

import androidx.lifecycle.ViewModelStoreOwner;

import dagger.BindsInstance;
import dagger.Subcomponent;
import shiv.SharedViewModelProviders;

/*
 * This file is a part of the Shiv project.
 */


@Subcomponent(modules = SharedViewModelProviders.class)
public interface ViewModelComponent {
    MainComponent.Factory mainComponentFactory();

    @Subcomponent.Factory
    interface Factory {
        ViewModelComponent create(@BindsInstance ViewModelStoreOwner owner);
    }
}
