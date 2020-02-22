package ph.codeia.shiv.tests

import android.content.Context
import androidx.fragment.app.*
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
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

class Launcher : Fragment() {
	override fun onAttach(context: Context) {
		super.onAttach(context)
		DaggerTestComponent.factory()
			.create(requireActivity())
			.factory
			.let {
				childFragmentManager.fragmentFactory = it
			}
	}

	fun testFragment(): TestFragment = run {
		var fragment = childFragmentManager.findFragmentByTag("testFragment") as? TestFragment
		if (fragment == null) {
			childFragmentManager.commitNow {
				add(TestFragment::class.java, null, "testFragment")
			}
			fragment = childFragmentManager.findFragmentByTag("testFragment") as TestFragment
		}
		fragment
	}
}

class TestFragment @Inject constructor(
	@Shared val vm: TestViewModel,
	@Shared val otherVm: ColliderViewModel
) : Fragment()

class TestViewModel @Inject constructor(val handle: SavedStateHandle) : ViewModel() {
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
		fun create(@BindsInstance activity: FragmentActivity): TestComponent
	}
}

