package ph.codeia.shiv.demo;

import androidx.navigation.fragment.NavHostFragment;

import javax.inject.Inject;

/*
 * This file is a part of the Shiv project.
 */


public class MainFragment extends NavHostFragment implements AppFlow {
	@Inject
	public MainFragment() {
	}

	@Override
	public void handle(Throwable error, Runnable retry) {
	}

	@Override
	public void handle(Throwable error) {
		if (error instanceof RuntimeException) {
			throw (RuntimeException) error;
		}
		else {
			throw new RuntimeException(error);
		}
	}

	@Override
	public void quit() {
		requireActivity().finish();
	}
}
