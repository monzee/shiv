package ph.codeia.shiv;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

/*
 * This file is a part of the Shiv project.
 */


public class InjectingFragmentFactory extends FragmentFactory {
	private final Map<String, Provider<Fragment>> providers;

	@Inject
	public InjectingFragmentFactory(Map<String, Provider<Fragment>> providers) {
		this.providers = providers;
	}

	@NonNull
	@Override
	public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
		Provider<Fragment> provider = providers.get(className);
		if (provider != null) {
			return provider.get();
		}
		return super.instantiate(classLoader, className);
	}
}
