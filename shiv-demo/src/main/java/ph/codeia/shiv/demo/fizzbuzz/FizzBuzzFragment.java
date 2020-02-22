package ph.codeia.shiv.demo.fizzbuzz;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import javax.inject.Inject;

import ph.codeia.shiv.demo.R;
import ph.codeia.shiv.demo.ViewExt;

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
		TextView counterLabel = view.findViewById(R.id.counter_label);
		Button plusButton = view.findViewById(R.id.plus_button);
		Button minusButton = view.findViewById(R.id.minus_button);
		onClick(plusButton, model::inc);
		onClick(minusButton, model::dec);
		model.state().observe(getViewLifecycleOwner(), counterLabel::setText);
	}
}
