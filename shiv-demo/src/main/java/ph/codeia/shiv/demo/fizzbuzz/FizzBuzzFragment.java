package ph.codeia.shiv.demo.fizzbuzz;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import javax.inject.Inject;

import ph.codeia.shiv.demo.R;
import ph.codeia.shiv.demo.ViewExt;
import ph.codeia.shiv.demo.databinding.FragmentFizzbuzzBinding;

/*
 * This file is a part of the Shiv project.
 */


public class FizzBuzzFragment extends Fragment implements ViewExt {
	private final ViewModelProvider.Factory factory;

	@Inject
	public FizzBuzzFragment(ViewModelProvider.Factory factory) {
		super(R.layout.fragment_fizzbuzz);
		this.factory = factory;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		FizzBuzzModel model = new ViewModelProvider(this, factory)
			.get(FizzBuzzModel.class);
		FragmentFizzbuzzBinding views = FragmentFizzbuzzBinding.bind(view);
		onClick(views.plusButton, model::inc);
		onClick(views.minusButton, model::dec);
		model.state().observe(getViewLifecycleOwner(), views.counterLabel::setText);
	}
}
