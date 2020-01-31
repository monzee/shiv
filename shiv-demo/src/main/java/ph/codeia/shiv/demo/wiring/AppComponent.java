package ph.codeia.shiv.demo.wiring;

import android.content.Context;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

/*
 * This file is a part of the Shiv project.
 */


@Singleton
@Component(modules = {AppServices.class})
public abstract class AppComponent {
	public abstract ModelComponent.Factory modelComponentFactory();

	@Component.Factory
	public interface Factory {
		AppComponent create(@BindsInstance Context context);
	}

	private static AppComponent instance;

	public static AppComponent of(Context context) {
		if (instance == null) {
			instance = DaggerAppComponent.factory()
				.create(context.getApplicationContext());
		}
		return instance;
	}
}
