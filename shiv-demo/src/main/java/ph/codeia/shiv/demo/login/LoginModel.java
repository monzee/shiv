package ph.codeia.shiv.demo.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

/*
 * This file is a part of the Shiv project.
 */


@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
public class LoginModel extends ViewModel {
	private final MutableLiveData<Login.State> state = new MutableLiveData<>();
	private final Login.Service service;
	private String username = "";
	private String password = "";

	@Inject
	public LoginModel(Login.Service service) {
		this.service = service;
		state.setValue(new Login.State());
	}

	public LiveData<Login.State> state() {
		return state;
	}

	public void setUsername(CharSequence value) {
		username = value.toString();
		validateIfActive();
	}

	public void setPassword(CharSequence value) {
		password = value.toString();
		validateIfActive();
	}

	public void activate() {
		Login.State loginState = state.getValue();
		loginState.validationResult = service.validate(username, password);
		loginState.tag = Login.Tag.ACTIVE;
		state.setValue(loginState);
	}

	public void login() {
		Login.State loginState = state.getValue();
		switch (loginState.tag) {
			case IDLE:
				activate();
				// fallthrough
			case ACTIVE:
				if (loginState.validationResult.isValid()) {
					loginState.tag = Login.Tag.BUSY;
					state.setValue(loginState);
					Login.State copy = new Login.State(loginState);
					service.login(username, password, new Login.Service.Completion() {
						@Override
						public void ok(String token) {
							copy.token = token;
							copy.tag = Login.Tag.LOGGED_IN;
							state.postValue(copy);
						}

						@Override
						public void denied() {
							copy.cause = new Login.Error("Bad username/password combination");
							copy.tag = Login.Tag.FAILED;
							state.postValue(copy);
						}

						@Override
						public void unavailable() {
							copy.cause = new Login.Error("Service unavailable");
							copy.tag = Login.Tag.FAILED;
							state.postValue(copy);
						}

						@Override
						public void failed(Throwable cause) {
							copy.cause = cause;
							copy.tag = Login.Tag.FAILED;
							state.postValue(copy);
						}
					});
				}
				break;
			default:
				break;
		}
	}

	private void validateIfActive() {
		if (state.getValue().tag == Login.Tag.ACTIVE) {
			activate();
		}
	}
}
