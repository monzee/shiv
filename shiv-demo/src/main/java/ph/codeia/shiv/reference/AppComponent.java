package ph.codeia.shiv.reference;

import android.content.Context;

import androidx.annotation.NonNull;

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
    private static AppComponent instance;

    @NonNull
    public static AppComponent of(Context context) {
        if (instance == null) {
            instance = DaggerAppComponent.factory()
                    .create(context.getApplicationContext());
        }
        return instance;
    }

    public abstract ViewModelComponent.Factory viewModelComponentFactory();

    @Component.Factory
    public interface Factory {
        AppComponent create(@BindsInstance Context context);
    }

    @Module
    public static abstract class Providers {
        @Singleton
        @Provides
        public static Executor io() {
            return Executors.newCachedThreadPool();
        }
    }
}
