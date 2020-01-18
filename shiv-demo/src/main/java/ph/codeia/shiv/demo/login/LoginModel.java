package ph.codeia.shiv.demo.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

/*
 * This file is a part of the Shiv project.
 */


public class LoginModel extends ViewModel {
	private enum Tag {IDLE, ACTIVE, BUSY, FAILED, LOGGED_IN}

	private static class VisibleState implements Login.State {
		Tag tag = Tag.IDLE;
		Login.ValidationErrors validationResult;
		Throwable cause;
		String token;

		@Override
		public void dispatch(Login.View k) {
			switch (tag) {
				case IDLE:
					k.idle();
					break;
				case ACTIVE:
					k.active(validationResult);
					break;
				case BUSY:
					k.busy();
					break;
				case FAILED:
					k.failed(cause);
					break;
				case LOGGED_IN:
					k.loggedIn(token);
					break;
			}
		}
	}

	private final MutableLiveData<Login.State> state = new MutableLiveData<>();
	private final MutableLiveData<VisibleState> update = new MutableLiveData<>();
	private final Login.Service service;
	private VisibleState current = new VisibleState();
	private String username = "";
	private String password = "";

	@Inject
	public LoginModel(Login.Service service) {
		this.service = service;
		state.setValue(current);
		update.observeForever(next -> {
			current = next;
			state.setValue(next);
		});
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
		current.validationResult = service.validate(username, password);
		current.tag = Tag.ACTIVE;
		state.setValue(current);
	}

	public void login() {
		switch (current.tag) {
			case IDLE:
				activate();
				// fallthrough
			case ACTIVE:
				if (current.validationResult.isValid()) {
					current.tag = Tag.BUSY;
					state.setValue(current);
					service.login(username, password, new Login.Service.Completion() {
						final VisibleState next = new VisibleState();

						@Override
						public void ok(String token) {
							next.token = token;
							next.tag = Tag.LOGGED_IN;
							update.postValue(next);
						}

						@Override
						public void denied() {
							failed(new Login.Error("Bad username/password combination"));
						}

						@Override
						public void unavailable() {
							failed(new Login.Error("Service unavailable"));
						}

						@Override
						public void failed(Throwable cause) {
							next.cause = cause;
							next.tag = Tag.FAILED;
							update.postValue(next);
						}
					});
				}
				break;
			default:
				break;
		}
	}

	private void validateIfActive() {
		if (current.tag == Tag.ACTIVE) {
			activate();
		}
	}
}
