package ph.codeia.shiv.tests

import androidx.fragment.app.testing.launchFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/*
 * This file is a part of the Shiv project.
 */


@RunWith(AndroidJUnit4::class)
class SavedStateHandleTest {
	@Test
	fun wow_you_cant_actually_save_to_the_bundle_during_onCleared() {
		// this is kinda dumb.
		// https://issuetracker.google.com/issues/143604138
		val scenario = launchFragment<SaneFragment>()
		scenario.onFragment {
			it.vm.isUntouched = false
		}
		scenario.recreate().onFragment {
			assert(it.vm.isUntouched)
			assertNull(it.vm.handle["clear-count"])
		}
	}

	@Test
	fun you_can_save_during_onSaveInstanceState_though() {
		val scenario = launchFragment<SaneFragment>()
		scenario.recreate().onFragment {
			assertTrue(it.vm.handle["saved"]!!)
		}
	}

	@Test
	fun can_recover_values_after_recreation() {
		val scenario = launchFragment<LauncherFragment>()
		scenario.onFragment {
			val vm = it.testFragment().vm
			vm.isUntouched = false
			vm.handle["n"] = 1024
			vm.handle["xs"] = arrayOf("lorem", "ipsum", "dolor", "sit", "amet")
		}
		scenario.recreate().onFragment {
			val vm = it.testFragment().vm
			assert(vm.isUntouched)
			assertEquals(1024, vm.handle["n"]!!)
			assertArrayEquals(
				arrayOf("lorem", "ipsum", "dolor", "sit", "amet"),
				vm.handle["xs"]
			)
		}
	}

	@Test
	fun does_not_overwrite_on_key_collision_with_a_different_handle() {
		val scenario = launchFragment<LauncherFragment>()
		scenario.onFragment {
			val test = it.testFragment()
			test.vm.handle["foo"] = "hello"
			test.otherVm.handle["foo"] = "hi"
		}
		scenario.recreate().onFragment {
			val test = it.testFragment()
			assertEquals("hello", test.vm.handle["foo"])
			assertEquals("hi", test.otherVm.handle["foo"])
		}
	}

	@Test
	fun can_recover_values_saved_during_onSaveInstanceState() {
		val scenario = launchFragment<LauncherFragment>()
		scenario.onFragment {
			val test = it.testFragment()
			test.vm.isUntouched = false
			test.onSaveInstanceState {
				test.vm.handle["saved"] = true
			}
		}
		scenario.recreate().onFragment {
			val test = it.testFragment()
			assert(test.vm.isUntouched)
			assert(test.vm.handle["saved"]!!)
		}
	}
}