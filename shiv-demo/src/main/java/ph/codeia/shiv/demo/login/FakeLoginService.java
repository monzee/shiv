package ph.codeia.shiv.demo.login;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

import javax.inject.Inject;

/*
 * This file is a part of the Shiv project.
 */


public class FakeLoginService implements Login.Service {
	private final Executor io;
	private final Pattern emailPattern;
	private final Random rng;

	@Inject
	public FakeLoginService(Executor io, Pattern emailPattern, Random rng) {
		this.io = io;
		this.emailPattern = emailPattern;
		this.rng = rng;
	}

	@Override
	public Login.ValidationErrors validate(String username, String password) {
		return new Login.ValidationErrors(
			username == null || username.length() == 0 ? "required"
				: !emailPattern.matcher(username).matches() ? "bad email"
				: null,
			password == null || password.length() == 0 ? "required"
				: password.length() < 5 ? "too simple"
				: null
		);
	}

	@Override
	public void login(String username, String password, Completion block) {
		io.execute(() -> {
			try {
				Thread.sleep(2000);
				switch (rng.nextInt(10)) {
					case 0:
						block.denied();
						break;
					case 1:
						block.unavailable();
						break;
					case 2:
						if (rng.nextInt(10) == 0) {
							throw new IllegalStateException("random error");
						}
						// conditional fallthrough
					default:
						block.ok("congrats");
						break;
				}
			}
			catch (Exception e) {
				block.failed(e);
			}
		});
	}
}
