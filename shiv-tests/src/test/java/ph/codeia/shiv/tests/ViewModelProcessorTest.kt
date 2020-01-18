package ph.codeia.shiv.tests

import androidx.lifecycle.HijackedViewModelStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Binds
import dagger.Component
import dagger.Module
import org.junit.Assert.assertSame
import org.junit.Test
import ph.codeia.shiv.InjectingViewModelFactory
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
@Component(modules = [Shiv::class, Bindings::class])
interface SharedComponent {
	fun inject(test: ViewModelProcessorTest)
}

@Singleton
@Component(modules = [Shiv::class, ViewModelBindings::class, Bindings::class])
interface FactoryComponent {
	val providers: Map<Class<*>, @JvmSuppressWildcards Provider<ViewModel>>
}

class FactoryUser @Inject constructor(val factory: InjectingViewModelFactory)

class ViewModelProcessorTest {
	@Inject
	lateinit var owner: StoreOwner
	@[Inject Shared]
	lateinit var model1: Model1
	@[Inject Shared]
	lateinit var model2: Model2

	@Test
	fun `injects the same qualified instance even when unscoped`() {
		DaggerSharedComponent.create().inject(this)
		assert(owner.store.contains(Model1::class.java))
		assert(owner.store.contains(Model2::class.java))
		assertSame(model1, model2.model1)
	}

	@Test
	fun `InjectingViewModelFactory can create injectable view models`() {
		val providers = DaggerFactoryComponent.create().providers
		assert(Model1::class.java in providers)
		assert(Model2::class.java in providers)
	}
}