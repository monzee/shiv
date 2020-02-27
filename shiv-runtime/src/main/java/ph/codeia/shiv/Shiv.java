package ph.codeia.shiv;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentFactory;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import javax.inject.Provider;

import dagger.Binds;
import dagger.Module;

/*
 * This file is a part of the Shiv project.
 */


@Module
public abstract class Shiv {
	@Binds
	public abstract FragmentFactory bindFragmentFactory(InjectingFragmentFactory factory);

	@Binds
	public abstract ViewModelProvider.Factory bindVMFactory(InjectingViewModelFactory factory);

	@NonNull
	public static <VM extends ViewModel> VM createViewModel(
		ViewModelStoreOwner owner,
		Provider<VM> provider,
		Class<VM> cls
	) {
		return vmProvider(owner, provider).get(cls);
	}

	private static <VM extends ViewModel> ViewModelProvider vmProvider(
		ViewModelStoreOwner owner,
		final Provider<VM> provider
	) {
		return new ViewModelProvider(owner, new ViewModelProvider.Factory() {
			@SuppressWarnings("unchecked")
			@NonNull
			@Override
			public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
				return (T) provider.get();
			}
		});
	}
}
