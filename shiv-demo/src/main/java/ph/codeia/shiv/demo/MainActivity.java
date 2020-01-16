package ph.codeia.shiv.demo;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import ph.codeia.shiv.demo.wiring.AppComponent;

/*
 * This file is a part of the Shiv project.
 */


public class MainActivity extends AppCompatActivity {
	private final AppBarConfiguration config =
		new AppBarConfiguration.Builder(R.id.login)
			.build();
	private NavController nav;

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

	@Override
	protected void onResumeFragments() {
		nav = Navigation.findNavController(this, R.id.nav_host);
		NavigationUI.setupActionBarWithNavController(this, nav, config);
	}

	@Override
	public boolean onSupportNavigateUp() {
		return NavigationUI.navigateUp(nav, config) || super.onSupportNavigateUp();
	}
}
