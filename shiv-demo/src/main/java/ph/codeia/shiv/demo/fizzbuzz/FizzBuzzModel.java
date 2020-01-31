package ph.codeia.shiv.demo.fizzbuzz;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

/*
 * This file is a part of the Shiv project.
 */


public class FizzBuzzModel extends ViewModel {
	private final MutableLiveData<String> state = new MutableLiveData<>();
	private int count = 1;

	@Inject
	public FizzBuzzModel() {
		update();
	}

	public LiveData<String> state() {
		return state;
	}

	public void inc() {
		count += 1;
		update();
	}

	public void dec() {
		count -= 1;
		update();
	}

	public int save() {
		return count;
	}

	public void restore(int count) {
		this.count = count;
		update();
	}

	private void update() {
		if (count % 15 == 0) {
			state.setValue("FizzBuzz");
		}
		else if (count % 3 == 0) {
			state.setValue("Fizz");
		}
		else if (count % 5 == 0) {
			state.setValue("Buzz");
		}
		else {
			state.setValue(String.valueOf(count));
		}
	}
}
