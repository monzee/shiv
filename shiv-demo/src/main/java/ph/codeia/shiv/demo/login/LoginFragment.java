package ph.codeia.shiv.demo.login;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import javax.inject.Inject;

import ph.codeia.shiv.Shared;
import ph.codeia.shiv.demo.R;

/*
 * This file is a part of the Shiv project.
 */


public class LoginFragment extends Fragment {
	private final LoginModel model;
	private final PartialLoginView partialView;

	@Inject
	public LoginFragment(
		@Shared LoginModel model,
		PartialLoginView partialView
	) {
		super(R.layout.fragment_login);
		this.model = model;
		this.partialView = partialView;
	}

	@Override
	public void onViewCreated(
		@NonNull View view,
		@Nullable Bundle savedInstanceState
	) {
		LoginView loginView = partialView.bind(view);
		loginView.start(model);
		model.state().observe(getViewLifecycleOwner(), loginView);
	}
}
