package ph.codeia.shiv.demo.wiring;

import androidx.core.util.PatternsCompat;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import ph.codeia.shiv.demo.login.FakeLoginService;
import ph.codeia.shiv.demo.login.Login;

/*
 * This file is a part of the Shiv project.
 */

@Module
public abstract class AppServices {
	@Provides
	@Singleton
	public static Executor io() {
		return Executors.newCachedThreadPool();
	}

	@Provides
	public static Pattern emailPattern() {
		return PatternsCompat.EMAIL_ADDRESS;
	}

	@Provides
	@Reusable
	public static Random rng() {
		return new Random();
	}

	@Binds
	public abstract Login.Service loginService(FakeLoginService service);
}
