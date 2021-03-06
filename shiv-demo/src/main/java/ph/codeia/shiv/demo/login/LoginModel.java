package ph.codeia.shiv.demo.login;

import androidx.core.util.Supplier;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

/*
 * This file is a part of the Shiv project.
 */


@SuppressWarnings("ConstantConditions")
public class LoginModel extends ViewModel {
	private final MutableLiveData<VisibleState> state = new MutableLiveData<>();
	private final Login.Service service;
	private final Runnable retry = this::activate;
	private String username = "";
	private String password = "";

	@Inject
	public LoginModel(Login.Service service) {
		this.service = service;
		state.setValue(new VisibleState(retry));
	}

	public LiveData<? extends Login.State> state() {
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

	public void login() {
		VisibleState current = state.getValue();
		switch (current.tag) {
			case IDLE:
				activate();
				// fallthrough
			case ACTIVE:
				if (current.validationResult.isValid()) {
					current.tag = Tag.BUSY;
					state.setValue(current);
					service.login(username, password, new Login.Service.Completion() {
						final VisibleState next = new VisibleState(retry);

						@Override
						public void ok(String token) {
							next.token = () -> {
								activate();
								return token;
							};
							next.tag = Tag.LOGGED_IN;
							state.postValue(next);
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
							state.postValue(next);
						}
					});
				}
				break;
			default:
				break;
		}
	}

	private void validateIfActive() {
		if (state.getValue().tag == Tag.ACTIVE) {
			activate();
		}
	}

	private void activate() {
		VisibleState current = state.getValue();
		current.validationResult = service.validate(username, password);
		current.tag = Tag.ACTIVE;
		state.setValue(current);
	}

	private enum Tag {IDLE, ACTIVE, BUSY, FAILED, LOGGED_IN}

	private static class VisibleState implements Login.State {
		final Runnable retry;
		Tag tag = Tag.IDLE;
		Login.ValidationErrors validationResult;
		Throwable cause;
		Supplier<String> token;

		VisibleState(Runnable retry) {
			this.retry = retry;
		}

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
					k.failed(cause, retry);
					break;
				case LOGGED_IN:
					k.loggedIn(token);
					break;
			}
		}
	}
}
