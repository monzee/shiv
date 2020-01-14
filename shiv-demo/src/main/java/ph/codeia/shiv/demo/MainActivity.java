package ph.codeia.shiv.demo;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ph.codeia.shiv.demo.provision.AppComponent;

/*
 * This file is a part of the Shiv project.
 */


public class MainActivity extends AppCompatActivity {
	public MainActivity() {
		super(R.layout.activity_main);
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		getSupportFragmentManager().setFragmentFactory(
			AppComponent.of(this)
				.modelComponentFactory()
				.create(this)
				.viewComponent()
				.fragmentFactory()
		);
		super.onCreate(savedInstanceState);
	}
}
