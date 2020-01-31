package ph.codeia.shiv.demo.login;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;

import javax.inject.Inject;

import ph.codeia.shiv.demo.R;
import ph.codeia.shiv.demo.wiring.ViewComponent;

/*
 * This file is a part of the Shiv project.
 */


public class LoginFragment extends Fragment {
	private final ViewComponent locator;
	private final SavedStateViewModelFactory factory;

	@Inject
	public LoginFragment(
		ViewComponent locator,
		SavedStateViewModelFactory factory
	) {
		super(R.layout.fragment_login);
		this.locator = locator;
		this.factory = factory;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		LoginModule module = new ViewModelProvider(this, factory)
			.get(LoginModule.class);
		locator.loginComponent(module)
			.loginView()
			.start(getViewLifecycleOwner(), view);
	}
}
