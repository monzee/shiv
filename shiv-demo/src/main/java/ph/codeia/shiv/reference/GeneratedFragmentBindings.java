package ph.codeia.shiv.reference;

import androidx.fragment.app.Fragment;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import ph.codeia.shiv.demo.LoginFragment;

/*
 * This file is a part of the Shiv project.
 */


@Module
public interface GeneratedFragmentBindings {
    @Binds
    @IntoMap
    @ClassKey(LoginFragment.class)
    Fragment loginFragment(LoginFragment fragment);
}
