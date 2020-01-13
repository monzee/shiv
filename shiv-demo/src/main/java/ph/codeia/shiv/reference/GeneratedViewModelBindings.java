package ph.codeia.shiv.reference;

import androidx.lifecycle.ViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import ph.codeia.shiv.demo.LoginModel;

/*
 * This file is a part of the Shiv project.
 */


@Module
public interface GeneratedViewModelBindings {
    @Binds
    @IntoMap
    @ClassKey(LoginModel.class)
    ViewModel bind(LoginModel model);
}
