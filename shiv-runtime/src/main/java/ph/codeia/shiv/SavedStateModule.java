package ph.codeia.shiv;

import androidx.lifecycle.SavedStateHandle;

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;

/*
 * This file is a part of the Shiv project.
 */


@Module
public abstract class SavedStateModule {
	@Provides
	@Reusable
	static SavedStateHolder.KeySetter provideKeySetter() {
		return new SavedStateHolder.KeySetter() {
			@Override
			public String set(String value) {
				String old = SavedStateHolder.key;
				SavedStateHolder.key = value;
				return old;
			}
		};
	}

	@Provides
	static SavedStateHandle provideSavedStateHandle(SavedStateHolder.Handle handle) {
		return handle.get();
	}
}