package ph.codeia.shiv;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Provides;
import dagger.Reusable;

/*
 * This file is a part of the Shiv project.
 */


public class SavedStateHolder extends ViewModel {
	static String key = "ph.codeia.shiv.SavedStateHolder";

	public final SavedStateHandle handle;

	public SavedStateHolder(SavedStateHandle handle) {
		this.handle = handle;
	}

	public interface KeySetter {
		void set(String value);
	}

	@Reusable
	public static class Handle {
		private final ViewModelProvider provider;

		@Inject
		public Handle(ViewModelStoreOwner owner) {
			provider = new ViewModelProvider(owner);
		}

		public SavedStateHandle get() {
			return provider.get(key, SavedStateHolder.class).handle;
		}
	}
}