package androidx.lifecycle;

/*
 * This file is a part of the Shiv project.
 */


public class HijackedViewModelStore extends ViewModelStore {
	private static final String KEY_PREFIX = "androidx.lifecycle.ViewModelProvider.DefaultKey:";

	public ViewModel get(Class<? extends ViewModel> cls) {
		return get(KEY_PREFIX + cls.getCanonicalName());
	}

	public boolean contains(Class<? extends ViewModel> cls) {
		return keys().contains(KEY_PREFIX + cls.getCanonicalName());
	}
}
