package ph.codeia.shiv.demo.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.Module;
import dagger.Provides;
import ph.codeia.shiv.demo.AppFlow;

/*
 * This file is a part of the Shiv project.
 */


@Module
public class LoginModule extends ViewModel {
	private final MutableLiveData<State> state = new MutableLiveData<>();
	private String username;
	private String password;

	public LoginModule(SavedStateHandle savedState) {
		state.setValue(new State());
		username = savedState.get("username");
		password = savedState.get("password");
	}

	@Provides
	public LiveData<? extends Login.State> state() {
		return state;
	}

	@SuppressWarnings("ConstantConditions")
	@Provides
	public Login.Actions actions(Login.Service auth) {
		return new Login.Actions() {
			@Override
			public void setUsername(CharSequence value) {
				username = value.toString();
				validateIfActive();
			}

			@Override
			public void setPassword(CharSequence value) {
				password = value.toString();
				validateIfActive();
			}

			@Override
			public void login() {
				State currentState = state.getValue();
				currentState.tag = Tag.BUSY;
				state.setValue(currentState);
				auth.login(username, password, new Login.Service.Completion() {
					final State newState = new State();

					@Override
					public void ok(String token) {
						newState.token = token;
						newState.tag = Tag.DONE;
						state.postValue(newState);
					}

					@Override
					public void denied() {
						failed(new Login.Error("Wrong username or password"));
					}

					@Override
					public void unavailable() {
						failed(new Login.Error("Service unavailable"));
					}

					@Override
					public void failed(Throwable cause) {
						newState.cause = cause;
						newState.tag = Tag.FAILED;
						state.postValue(newState);
					}
				});
			}

			@Override
			public void reset() {
				State currentState = state.getValue();
				currentState.errors = auth.validate(username, password);
				currentState.tag = Tag.ACTIVE;
				state.setValue(currentState);
			}

			void validateIfActive() {
				if (state.getValue().tag == Tag.ACTIVE) {
					reset();
				}
			}
		};
	}

	private enum Tag {
		IDLE, ACTIVE, BUSY, FAILED, DONE
	}

	private static class State implements Login.State {
		Tag tag = Tag.IDLE;
		Login.ValidationErrors errors;
		Throwable cause;
		String token;

		@Override
		public void dispatch(Login.Case k) {
			switch (tag) {
				case IDLE:
					k.idle();
					break;
				case ACTIVE:
					k.active(errors);
					break;
				case BUSY:
					k.busy();
					break;
				case FAILED:
					k.failed(cause);
					break;
				case DONE:
					k.done(token);
					break;
			}
		}
	}
}
