package ph.codeia.shiv.demo.login;

import androidx.annotation.Nullable;

/*
 * This file is a part of the Shiv project.
 */


public interface Login {
	enum Tag {IDLE, ACTIVE, BUSY, FAILED, LOGGED_IN}

	class State {
		public Tag tag = Tag.IDLE;
		public ValidationErrors validationResult;
		public Throwable cause;
		public String token;

		public State() {
		}

		public State(State source) {
			tag = source.tag;
			validationResult = source.validationResult;
			cause = source.cause;
			token = source.token;
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
