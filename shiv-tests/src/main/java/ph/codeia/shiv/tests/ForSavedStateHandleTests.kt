package ph.codeia.shiv.tests

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import dagger.BindsInstance
import dagger.Component
import ph.codeia.shiv.InjectingFragmentFactory
import ph.codeia.shiv.Shared
import ph.codeia.shiv.Shiv
import shiv.FragmentBindings
import shiv.SharedViewModelProviders
import javax.inject.Inject

/*
 * This file is a part of the Shiv project.
 */

class LauncherFragment : Fragment() {
	override fun onAttach(context: Context) {
		super.onAttach(context)
		DaggerTestComponent.factory()
			.create(this)
			.factory
			.let {
				childFragmentManager.fragmentFactory = it
			}
	}

	fun testFragment(): SSFragment = run {
		var fragment = childFragmentManager.findFragmentByTag("testFragment") as? SSFragment
		if (fragment == null) {
			childFragmentManager.commitNow {
				add(SSFragment::class.java, null, "testFragment")
			}
			fragment = childFragmentManager.findFragmentByTag("testFragment") as SSFragment
		}
		fragment
	}
}

class SSFragment @Inject constructor(
	@Shared val vm: SSViewModel,
	@Shared val otherVm: ColliderViewModel
) : Fragment() {
	private var save: (() -> Unit)? = null

	fun onSaveInstanceState(block: () -> Unit) {
		save = block
	}

	override fun onSaveInstanceState(outState: Bundle) {
		save?.invoke()
	}
}

class SSViewModel @Inject constructor(val handle: SavedStateHandle) : ViewModel() {
	var isUntouched = true
}

class ColliderViewModel @Inject constructor(val handle: SavedStateHandle) : ViewModel()

@Component(modules = [
	Shiv::class,
	FragmentBindings::class,
	SharedViewModelProviders::class
])
interface TestComponent {
	val factory: InjectingFragmentFactory

	@Component.Factory
	interface Factory {
		fun create(@BindsInstance owner: ViewModelStoreOwner): TestComponent
	}
}

