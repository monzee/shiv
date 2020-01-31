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

/*
 * This file is a part of the Shiv project.
 */


public class FizzBuzzFragment extends Fragment {
	private final ViewModelProvider.Factory factory;
	private FizzBuzzModel model;

	@Inject
	public FizzBuzzFragment(ViewModelProvider.Factory factory) {
		super(R.layout.fragment_fizzbuzz);
		this.factory = factory;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		model = new ViewModelProvider(this, factory).get(FizzBuzzModel.class);
		if (savedInstanceState != null) {
			model.restore(savedInstanceState.getInt("counter"));
		}
		TextView counterLabel = view.findViewById(R.id.counter_label);
		Button plusButton = view.findViewById(R.id.plus_button);
		Button minusButton = view.findViewById(R.id.minus_button);
		plusButton.setOnClickListener(o -> model.inc());
		minusButton.setOnClickListener(o -> model.dec());
		model.state().observe(getViewLifecycleOwner(), counterLabel::setText);
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putInt("counter", model.save());
	}
}
