package ph.codeia.shiv.demo.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.google.android.material.textfield.TextInputLayout;

import javax.inject.Inject;

import ph.codeia.shiv.Shared;
import ph.codeia.shiv.demo.AppFlow;
import ph.codeia.shiv.demo.R;

/*
 * This file is a part of the Shiv project.
 */


public class LoginFragment extends Fragment {
	private final LoginModel model;
	private final AppFlow go;

	@Inject
	public LoginFragment(@Shared LoginModel model, AppFlow go) {
		super(R.layout.fragment_login);
		this.model = model;
		this.go = go;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		TextInputLayout username = view.findViewById(R.id.username);
		EditText usernameField = view.findViewById(R.id.username_field);
		TextInputLayout password = view.findViewById(R.id.password);
		EditText passwordField = view.findViewById(R.id.password_field);
		Button submitButton = view.findViewById(R.id.submit_button);
		usernameField.addTextChangedListener(afterChange(model::setUsername));
		passwordField.addTextChangedListener(afterChange(model::setPassword));
		submitButton.setOnClickListener(o -> model.login());
		model.state().observe(getViewLifecycleOwner(), it -> {
			switch (it.tag) {
				case IDLE:
					break;
				case ACTIVE:
					username.setError(it.validationResult.username);
					password.setError(it.validationResult.password);
					submitButton.setEnabled(it.validationResult.isValid());
					break;
				case BUSY:
					submitButton.setEnabled(false);
					break;
				case FAILED:
					if (it.cause instanceof Login.Error) {
						go.handle(it.cause, model::validate);
					}
					else {
						go.handle(it.cause);
					}
					break;
				case LOGGED_IN:
					go.toHomeScreen(it.token);
					model.validate();
					break;
			}
		});
	}

	private static TextWatcher afterChange(Observer<CharSequence> block) {
		return new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
				block.onChanged(editable);
			}
		};
	}
}