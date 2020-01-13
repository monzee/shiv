package ph.codeia.shiv.demo;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;

import ph.codeia.shiv.demo.provision.AppComponent;


/*
 * This file is a part of the Shiv project.
 */


public class MainFragment extends NavHostFragment implements AppFlow {
	@Override
	public void onAttach(@NonNull Context context) {
		getChildFragmentManager().setFragmentFactory(
			AppComponent.of(context)
				.modelComponentFactory()
				.create(this)
				.viewComponentFactory()
				.create(this)
				.fragmentFactory()
		);
		super.onAttach(context);
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
