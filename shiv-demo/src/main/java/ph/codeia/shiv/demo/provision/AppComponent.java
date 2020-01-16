package ph.codeia.shiv.demo.provision;

import android.content.Context;

import androidx.core.util.PatternsCompat;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import ph.codeia.shiv.demo.login.FakeLoginService;
import ph.codeia.shiv.demo.login.Login;

/*
 * This file is a part of the Shiv project.
 */


@Singleton
@Component(modules = {AppComponent.Providers.class})
public abstract class AppComponent {
	public abstract ModelComponent.Factory modelComponentFactory();

	private static AppComponent instance;

	public static AppComponent of(Context context) {
		if (instance == null) {
			instance = DaggerAppComponent.factory()
				.create(context.getApplicationContext());
		}
		return instance;
	}

	@Component.Factory
	public interface Factory {
		AppComponent create(@BindsInstance Context context);
	}

	@Module
	public static abstract class Providers {
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
		public static Random rng() {
			return new Random();
		}

		@Binds
		public abstract Login.Service loginService(FakeLoginService service);
	}
}
