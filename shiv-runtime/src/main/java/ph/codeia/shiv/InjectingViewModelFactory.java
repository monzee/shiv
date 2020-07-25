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
	private final SavedStateHolder.KeySetter key;

	@Inject
	public InjectingViewModelFactory(
		Map<Class<?>, Provider<ViewModel>> providers,
		SavedStateHolder.KeySetter key
	) {
		this.providers = providers;
		this.key = key;
	}

	@SuppressWarnings("unchecked")
	@NonNull
	@Override
	public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
		Provider<ViewModel> provider = providers.get(modelClass);
		if (provider != null) {
			String old = key.set(modelClass.getCanonicalName() + ":SavedStateHolder");
			T vm = (T) provider.get();
			key.set(old);
			return vm;
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
