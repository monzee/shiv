package ph.codeia.shiv.demo.login;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.android.material.textfield.TextInputLayout;

import javax.inject.Inject;

import ph.codeia.shiv.demo.AppFlow;
import ph.codeia.shiv.demo.R;

/*
 * This file is a part of the Shiv project.
 */


public class LoginView {
	private final LiveData<? extends Login.State> state;
	private final Login.Actions actions;
	private final AppFlow go;

	@Inject
	public LoginView(
		LiveData<? extends Login.State> state,
		Login.Actions actions,
		AppFlow go
	) {
		this.state = state;
		this.actions = actions;
		this.go = go;
	}

	public void start(LifecycleOwner owner, View root) {
		TextInputLayout username = root.findViewById(R.id.username);
		EditText usernameField = root.findViewById(R.id.username_field);
		TextInputLayout password = root.findViewById(R.id.password);
		EditText passwordField = root.findViewById(R.id.password_field);
		Button submitButton = root.findViewById(R.id.submit_button);
		usernameField.addTextChangedListener(afterChange(actions::setUsername));
		passwordField.addTextChangedListener(afterChange(actions::setPassword));
		submitButton.setOnClickListener(o -> actions.login());
		state.observe(owner, new Login.Case() {
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
			public void failed(Throwable cause) {
				if (cause instanceof Login.Error) {
					go.handle(cause, actions::reset);
				}
				else {
					go.handle(cause, () -> {
						Log.e("mz", "Login error", cause);
						go.quit();
					});
				}
			}

			@Override
			public void done(String token) {
				actions.reset();
				go.toHomeScreen(token);
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
