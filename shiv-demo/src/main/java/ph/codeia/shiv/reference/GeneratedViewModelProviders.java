package ph.codeia.shiv.reference;

import androidx.lifecycle.ViewModelStoreOwner;

import javax.inject.Provider;

import dagger.Module;
import dagger.Provides;
import ph.codeia.shiv.demo.LoginModel;
import ph.codeia.shiv.Shared;
import ph.codeia.shiv.Shiv;

/*
 * This file is a part of the Shiv project.
 */


@Module
public abstract class GeneratedViewModelProviders {
    @Provides
    @Shared
    public static LoginModel provideLoginModel(ViewModelStoreOwner owner, Provider<LoginModel> provider) {
        return Shiv.createViewModel(owner, provider, LoginModel.class);
    }
}
