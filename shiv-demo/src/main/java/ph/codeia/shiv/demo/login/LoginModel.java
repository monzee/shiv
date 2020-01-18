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

	private static class LocalState {
		Tag tag = Tag.IDLE;
		Login.ValidationErrors validationResult;
		Throwable cause;
		String token;
	}

	private final MutableLiveData<Login.State> state = new MutableLiveData<>();
	private final MutableLiveData<LocalState> update = new MutableLiveData<>();
	private final Login.Service service;
	private LocalState current = new LocalState();
	private String username = "";
	private String password = "";

	@Inject
	public LoginModel(Login.Service service) {
		this.service = service;
		state.setValue(emit(current));
		update.observeForever(next -> {
			current = next;
			state.setValue(emit(next));
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
		state.setValue(emit(current));
	}

	public void login() {
		switch (current.tag) {
			case IDLE:
				activate();
				// fallthrough
			case ACTIVE:
				if (current.validationResult.isValid()) {
					current.tag = Tag.BUSY;
					state.setValue(emit(current));
					service.login(username, password, new Login.Service.Completion() {
						final LocalState next = new LocalState();

						@Override
						public void ok(String token) {
							next.token = token;
							next.tag = Tag.LOGGED_IN;
							update.postValue(next);
						}

						@Override
						public void denied() {
							next.cause = new Login.Error("Bad username/password combination");
							next.tag = Tag.FAILED;
							update.postValue(next);
						}

						@Override
						public void unavailable() {
							next.cause = new Login.Error("Service unavailable");
							next.tag = Tag.FAILED;
							update.postValue(next);
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

	private static Login.State emit(LocalState current) {
		switch (current.tag) {
			case IDLE:
				return Login.View::idle;
			case ACTIVE:
				return k -> k.active(current.validationResult);
			case BUSY:
				return Login.View::busy;
			case FAILED:
				return k -> k.failed(current.cause);
			case LOGGED_IN:
				return k -> k.loggedIn(current.token);
			default:
				throw new AssertionError("unreachable");
		}
	}
}
