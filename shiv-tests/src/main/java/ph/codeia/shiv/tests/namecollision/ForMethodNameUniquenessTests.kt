package ph.codeia.shiv.tests.namecollision

import androidx.lifecycle.ViewModel
import ph.codeia.shiv.Shared
import javax.inject.Inject


/*
 * This file is a part of the Shiv project.
 */

class Model1 @Inject constructor() : ViewModel()
class Model2 @Inject constructor(@Shared val model1: Model1) : ViewModel()
class Model3 @Inject constructor(val model1: Model1) : ViewModel()
