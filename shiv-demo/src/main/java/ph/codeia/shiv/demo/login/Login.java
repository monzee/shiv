package ph.codeia.shiv.demo.login;

import androidx.annotation.Nullable;
import androidx.core.util.Supplier;
import androidx.lifecycle.Observer;

/*
 * This file is a part of the Shiv project.
 */


public interface Login {
	interface State {
		void dispatch(Login.View k);
	}

	interface View extends Observer<Login.State> {
		void idle();
		void active(ValidationErrors validationResult);
		void busy();
		void failed(Throwable cause, Runnable retry);
		void loggedIn(Supplier<String> token);

		@Override
		default void onChanged(State state) {
			if (state != null) {
				state.dispatch(this);
			}
		}
	}

	class ValidationErrors {
		@Nullable
		public final String username;
		@Nullable
		public final String password;

		public ValidationErrors(
			@Nullable String username,
			@Nullable String password
		) {
			this.username = username;
			this.password = password;
		}

		public boolean isValid() {
			return username == null && password == null;
		}
	}

	interface Service {
		ValidationErrors validate(String username, String password);
		void login(String username, String password, Completion block);

		interface Completion {
			void ok(String token);
			void denied();
			void unavailable();
			void failed(Throwable cause);
		}
	}

	class Error extends IllegalStateException {
		public Error(String message) {
			super(message);
		}
	}
}
