package ph.codeia.shiv.demo.provision;

import android.content.Context;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import ph.codeia.shiv.Shiv;

/*
 * This file is a part of the Shiv project.
 */


@Singleton
@Component(modules = {Shiv.class, AppComponent.Providers.class})
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
	}
}
