package ph.codeia.shiv.demo;

import android.widget.Toast;

import androidx.navigation.fragment.NavHostFragment;

import javax.inject.Inject;

import ph.codeia.shiv.demo.wiring.Per;

/*
 * This file is a part of the Shiv project.
 */


@Per.Configuration
public class MainFragment extends NavHostFragment implements AppFlow {
	@Inject
	public MainFragment() {
	}

	@Override
	public void toHomeScreen(String authToken) {
		Toast.makeText(requireContext(), authToken, Toast.LENGTH_SHORT).show();
		getNavController().navigate(R.id.to_fizzbuzz);
	}

	@Override
	public void handle(Throwable error, Runnable retry) {
		Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
		retry.run();
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
