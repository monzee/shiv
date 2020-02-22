package ph.codeia.shiv.tests

import dagger.Component
import org.junit.Assert.assertSame
import org.junit.Test
import ph.codeia.shiv.Shared
import ph.codeia.shiv.Shiv
import javax.inject.Inject
import javax.inject.Singleton

/*
 * This file is a part of the Shiv project.
 */


@Singleton
@Component(modules = [Shiv::class, Bindings::class])
interface SharedComponent {
	fun inject(test: ViewModelProcessorTest)
}

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
		assert(Model1::class.java in owner.store)
		assert(Model2::class.java in owner.store)
		assertSame(model1, model2.model1)
	}

	@Test
	fun `InjectingViewModelFactory can create injectable view models`() {
		val providers = DaggerFactoryComponent.create().providers
		assert(Model1::class.java in providers)
		assert(Model2::class.java in providers)
	}
}