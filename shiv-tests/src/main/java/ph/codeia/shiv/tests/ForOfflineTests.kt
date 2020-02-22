package ph.codeia.shiv.tests

import androidx.lifecycle.HijackedViewModelStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Binds
import dagger.Component
import dagger.Module
import ph.codeia.shiv.InjectingViewModelFactory
import ph.codeia.shiv.LateBound
import ph.codeia.shiv.Shared
import ph.codeia.shiv.Shiv
import shiv.SharedViewModelProviders
import shiv.ViewModelBindings
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/*
 * This file is a part of the Shiv project.
 */


class Model1 @Inject constructor() : ViewModel()
class Model2 @Inject constructor(@Shared val model1: Model1) : ViewModel()
class Model3 @Inject constructor(val model1: Model1) : ViewModel()

@Singleton
class StoreOwner @Inject constructor() : ViewModelStoreOwner {
	val store = HijackedViewModelStore()
	override fun getViewModelStore(): ViewModelStore = store
}

@Module(includes = [SharedViewModelProviders::class])
interface Bindings {
	@Binds
	fun owner(e: StoreOwner): ViewModelStoreOwner
}

@Singleton
@Component(modules = [Shiv::class, ViewModelBindings::class, Bindings::class])
interface FactoryComponent {
	val providers: Map<Class<*>, @JvmSuppressWildcards Provider<ViewModel>>
}

class FactoryUser @Inject constructor(val factory: InjectingViewModelFactory)


class Foo(val str: String, @LateBound val num: Int) {
	class Bar(@LateBound val bool: Boolean)
}

class Baz(val str: String, @LateBound val num: Int, @LateBound val bool: Boolean)

class Lorem(@LateBound val n: Int, val s: String, @LateBound val b: Boolean, val xs: DoubleArray)

class Ipsum protected constructor(@LateBound val s: String)
