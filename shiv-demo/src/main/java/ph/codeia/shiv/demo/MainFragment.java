package ph.codeia.shiv.demo;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;

import ph.codeia.shiv.reference.AppComponent;
import ph.codeia.shiv.reference.MainComponent;

/*
 * This file is a part of the Shiv project.
 */


public class MainFragment extends NavHostFragment implements AppFlow {
    @Override
    public void onAttach(@NonNull Context context) {
        MainComponent mainComponent = AppComponent.of(context)
                .viewModelComponentFactory()
                .create(this)
                .mainComponentFactory()
                .create(this);
        getChildFragmentManager()
                .setFragmentFactory(mainComponent.fragmentFactory());
        super.onAttach(context);
    }

    @Override
    public void handle(Throwable error, Runnable retry) {

    }

    @Override
    public void handle(Throwable error) {

    }

    @Override
    public void quit() {
        requireActivity().finish();
    }
}
