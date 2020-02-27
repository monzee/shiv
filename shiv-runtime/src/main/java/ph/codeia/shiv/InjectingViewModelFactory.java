package ph.codeia.shiv;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

/*
 * This file is a part of the Shiv project.
 */


public class InjectingViewModelFactory implements ViewModelProvider.Factory {
	private final Map<Class<?>, Provider<ViewModel>> providers;

	@Inject
	public InjectingViewModelFactory(Map<Class<?>, Provider<ViewModel>> providers) {
		this.providers = providers;
	}

	@SuppressWarnings("unchecked")
	@NonNull
	@Override
	public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
		Provider<ViewModel> provider = providers.get(modelClass);
		if (provider != null) {
			SavedStateHolder.key = modelClass.getCanonicalName() + ":SavedStateHolder";
			return (T) provider.get();
		}
		else {
			throw new IllegalArgumentException(""
				+ "[Shiv] Unbound ViewModel class "
				+ modelClass.getSimpleName()
				+ ". Did you forget to annotate its constructor with @Inject?"
			);
		}
	}
}
