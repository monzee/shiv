package ph.codeia.shiv;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

/*
 * This file is a part of the Shiv project.
 */


public class SavedStateHandleHolder extends ViewModel {
	public final SavedStateHandle handle;

	public SavedStateHandleHolder(SavedStateHandle handle) {
		this.handle = handle;
	}
}
