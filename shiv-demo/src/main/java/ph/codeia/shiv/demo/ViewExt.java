package ph.codeia.shiv.demo;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.Observer;

/*
 * This file is a part of the Shiv project.
 */


public interface ViewExt {

	default View.OnClickListener onClick(View view, Runnable block) {
		View.OnClickListener listener = o -> block.run();
		view.setOnClickListener(listener);
		return listener;
	}

	default TextWatcher afterTextChanged(
		TextView view,
		Observer<CharSequence> listener
	) {
		TextWatcher watcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
				listener.onChanged(editable);
			}
		};
		view.addTextChangedListener(watcher);
		return watcher;
	}
}
