package ph.codeia.shiv.tests

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.*

/*
 * This file is a part of the Shiv project.
 */


class SaneFragment : Fragment() {
	lateinit var vm: SaneViewModel

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		vm = ViewModelProvider(requireActivity()).get()
	}
}


class SaneViewModel(val handle: SavedStateHandle) : ViewModel() {
	var isUntouched = true

	override fun onCleared() {
		// apparently it's too late to save when this gets called
		val count = handle["clear-count"] ?: 0
		handle["clear-count"] = count + 1
	}
}