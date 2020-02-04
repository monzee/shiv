package ph.codeia.shiv.demo.login;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.core.util.Supplier;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.textfield.TextInputLayout;

import ph.codeia.shiv.LateBound;
import ph.codeia.shiv.demo.AppFlow;
import ph.codeia.shiv.demo.R;
import ph.codeia.shiv.demo.ViewExt;

/*
 * This file is a part of the Shiv project.
 */


public class LoginView implements Login.View, ViewExt {
	private final AppFlow go;
	private final TextInputLayout username;
	private final TextInputLayout password;
	private final EditText usernameField;
	private final EditText passwordField;
	private final Button submitButton;

	public LoginView(AppFlow go, @LateBound View view) {
		this.go = go;
		username = view.findViewById(R.id.username);
		usernameField = view.findViewById(R.id.username_field);
		password = view.findViewById(R.id.password);
		passwordField = view.findViewById(R.id.password_field);
		submitButton = view.findViewById(R.id.submit_button);
	}

	public void start(LoginModel model) {
		afterTextChanged(usernameField, model::setUsername);
		afterTextChanged(passwordField, model::setPassword);
		onClick(submitButton, model::login);
	}

	@Override
	public void idle() {
	}

	@Override
	public void active(Login.ValidationErrors validationResult) {
		username.setError(validationResult.username);
		password.setError(validationResult.password);
		submitButton.setEnabled(validationResult.isValid());
	}

	@Override
	public void busy() {
		submitButton.setEnabled(false);
	}

	@Override
	public void failed(Throwable cause, Runnable retry) {
		go.handle(cause, retry);
	}

	@Override
	public void loggedIn(Supplier<String> token) {
		go.toHomeScreen(token.get());
	}
}
