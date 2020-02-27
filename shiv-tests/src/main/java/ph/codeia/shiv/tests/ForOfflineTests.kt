package ph.codeia.shiv.tests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ph.codeia.shiv.LateBound
import ph.codeia.shiv.Shared
import javax.inject.Inject


/*
 * This file is a part of the Shiv project.
 */


class Model1 @Inject constructor() : ViewModel()

class Model2 @Inject constructor(@Shared val model1: Model1) : ViewModel()

class Model3 @Inject constructor(val model1: Model1) : ViewModel()

class FactoryUser @Inject constructor(val factory: ViewModelProvider.Factory)

class Foo(val str: String, @LateBound val num: Int) {
	class Bar(@LateBound val bool: Boolean)
}

class Baz(val str: String, @LateBound val num: Int, @LateBound val bool: Boolean)

class Lorem(@LateBound val n: Int, val s: String, @LateBound val b: Boolean, val xs: DoubleArray)

class Ipsum protected constructor(@LateBound val s: String)

