package ph.codeia.shiv.reference;

import androidx.fragment.app.FragmentFactory;

import dagger.Binds;
import dagger.BindsInstance;
import dagger.Module;
import dagger.Subcomponent;
import ph.codeia.shiv.demo.AppFlow;
import ph.codeia.shiv.demo.MainFragment;
import shiv.FragmentBindings;

/*
 * This file is a part of the Shiv project.
 */


@Subcomponent(modules = {FragmentBindings.class, MainComponent.Providers.class})
public interface MainComponent {
    FragmentFactory fragmentFactory();

    @Subcomponent.Factory
    interface Factory {
        MainComponent create(@BindsInstance MainFragment main);
    }

    @Module
    abstract class Providers {
        @Binds
        public abstract AppFlow bind(MainFragment main);
    }
}
