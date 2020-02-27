package ph.codeia.shiv.demo.login;

import android.view.View;

import androidx.core.util.Supplier;

import ph.codeia.shiv.LateBound;
import ph.codeia.shiv.demo.AppFlow;
import ph.codeia.shiv.demo.ViewExt;
import ph.codeia.shiv.demo.databinding.FragmentLoginBinding;

/*
 * This file is a part of the Shiv project.
 */


public class LoginView implements Login.View, ViewExt {
	private final AppFlow go;
	private final FragmentLoginBinding views;

	public LoginView(AppFlow go, @LateBound View view) {
		this.go = go;
		views = FragmentLoginBinding.bind(view);
	}

	public void start(LoginModel model) {
		afterTextChanged(views.usernameField, model::setUsername);
		afterTextChanged(views.passwordField, model::setPassword);
		onClick(views.submitButton, model::login);
	}

	@Override
	public void idle() {
	}

	@Override
	public void active(Login.ValidationErrors validationResult) {
		views.username.setError(validationResult.username);
		views.password.setError(validationResult.password);
		views.submitButton.setEnabled(validationResult.isValid());
	}

	@Override
	public void busy() {
		views.submitButton.setEnabled(false);
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
