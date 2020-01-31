package ph.codeia.shiv.demo.wiring;

import android.app.Application;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelStoreOwner;

import dagger.Binds;
import dagger.BindsInstance;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import shiv.SharedViewModelProviders;
import shiv.ViewModelBindings;

/*
 * This file is a part of the Shiv project.
 */


@Per.Activity
@Subcomponent(modules = {
	SharedViewModelProviders.class,
	ViewModelBindings.class,
	ModelComponent.Providers.class
})
public interface ModelComponent {
	ViewComponent viewComponent();

	@Subcomponent.Factory
	interface Factory {
		ModelComponent create(@BindsInstance AppCompatActivity activity);
	}

	@Module
	abstract class Providers {
		@Binds
		public abstract ViewModelStoreOwner vmStoreOwner(AppCompatActivity activity);

		@Provides
		public static SavedStateViewModelFactory savedStateVmFactory(
			AppCompatActivity activity
		) {
			return new SavedStateViewModelFactory(
				(Application) activity.getApplicationContext(),
				activity
			);
		}
	}
}
