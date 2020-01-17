# Shiv
Constructor injection for fragments and view models using dagger2 with
minimal boilerplate.


## Installation [ ![Download](https://api.bintray.com/packages/monzee/jvm/shiv-runtime/images/download.svg) ](https://bintray.com/monzee/jvm/shiv-runtime/_latestVersion)
Replace the version numbers below with the one in the image above.
```
dependencies {
    implementation "ph.codeia.shiv:shiv-runtime:0.1.0"
    annotationProcessor "ph.codeia.shiv:shiv-compiler:0.1.0"
}
```


## Requirements and assumptions
- dagger2 in your build dependencies
- no existing binding to `FragmentFactory` and `ViewModelProvider.Factory` in
  the graph
- constructor injection only (may change in the future)
- using `androidx.*` packages
- all view models are owned by the activity and thus shared by every fragment


## Usage
The following code examples are simplified in order to highlight the important parts
of the process. Please see the demo module to see how the components typically look
like in actual projects. All generated code are in java7, so kotlin or d8 is not
required.

1. Define the classes to be injected. This step is done first in order to trigger
   the module code generation. You can fill in the dependencies later. Hit `Ctrl-F9`
   to build the project.

    ```kotlin
    class LoginFragment @Inject constructor() : Fragment(R.layout.fragment_login)

    class LoginModel @Inject constructor() : ViewModel()
    ```

2. Define the components. Since fragments are shorter-lived than view models, all
   injected fragments must be bound in a subcomponent of wherever the view models
   are bound. This is so that you can't inject view-related objects into a view
   model's constructor and cause a memory leak.

   Install the generated `shiv.SharedViewModelProviders` module to the view model
   component. In the factory or builder interface of the component, add a
   `@BindsInstance`-annotated `ViewModelStoreOwner` parameter/builder method.
   Expose the fragment subcomponent or its factory/builder here.

    ```kotlin
    @Component(modules = [SharedViewModelProviders::class])
    interface ModelComponent {
        val viewComponent: ViewComponent

        @Component.Factory
        interface Factory {
            fun create(@BindsInstance owner: ViewModelStoreOwner): ModelComponent
        }
    }
    ```

3. Install the bundled `Shiv` and the generated `shiv.FragmentBindings` modules
   into the subcomponent. Expose the type `FragmentFactory` from the subcomponent.
   Rebuild the project with `Ctrl-F9` to generate the dagger implementations.

    ```kotlin
    @Subcomponent(modules = [Shiv::class, FragmentBindings::class])
    interface ViewComponent {
        val fragmentFactory: FragmentFactory
    }
    ```

4. Override the activity's `#onCreate` method to use the fragment factory built
   by dagger. Make sure to do this before calling `super.onCreate(...)`.

    ```kotlin
    class MainActivity : AppCompatActivity(R.layout.activity_main) {
        override fun onCreate(savedInstanceState: Bundle?) {
            supportFragmentManager.fragmentFactory = DaggerModelComponent.factory()
                .create(this)
                .viewComponent
                .fragmentFactory
            super.onCreate(savedInstanceState)
        }
    }
    ```

5. Go back and fill in the rest of the fragment and view model classes. Install
   additional modules as required. Qualify view model dependencies with `@Shared`.
   If you forget to use `@Shared` in the fragment's constructor parameter, you
   would get an orphan view model that would not survive configuration changes.

    ```kotlin
    class LoginFragment @Inject constructor(
        @Shared private val model: LoginModel
    ) : Fragment(R.layout.fragment_login) {
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            // ...
            val submitButton = view.findViewById(R.id.submit_button)
            submitButton.setOnClickListener { model.login() }
            model.state().observe(viewLifecycleOwner) {
                // ...
            }
        }
    }

    class LoginModel @Inject constructor(
        private val auth: AuthService
    ) : ViewModel() {
        private val state = MutableLiveData<LoginState>().also { it.value = LoginState() }

        fun state(): LiveData<LoginState> = state

        fun login() {
            // ...
        }
    }
    ```

   **DO NOT** request `Fragment` classes from `ViewModel`s because even though it
   might compile, you won't get the same instance attached to the activity. If you
   somehow managed to inject a live fragment to a view model, that is a bad situation
   that you must rectify because you have just leaked the activity context and all
   the views hanging on it.

6. After this, you can write new fragments and view models and they will automatically
   be part of the object graph. Just don't forget the `@Inject` and `@Shared`
   annotations.


## But what about...

### ...`AndroidViewModel`?

`AndroidViewModel`s are view models that have a constructor dependency on an
`Application`. The view model can then build other objects that need an application
context (e.g. anything that needs file IO). If you really need an application, you
can bind it to the dagger graph via `@BindsInstance` in a (sub)component factory/builder
and it will be provided to your view model constructor. You probably don't need
an `Application` though but a service that depends on `Context`. For clarity, it's
best to depend directly on that service instead.

### ...`SavedStateHandle`?

`SavedStateHandle` allows view models constructed by `SavedStateViewModelFactory`
to read and write to a `Bundle` that persists not only across configuration changes
but also process death and recreation. Theoretically, you could bind a `SavedStateHandle`
instance to the dagger graph, but creating and managing a `SavedStateHandle` is
not very simple. Unfortunately, the `SavedStateHandleController` class is package
private to `androidx.lifecycle` and I'm not about to copy-paste all that logic or
hijack their namespace. Perhaps it will become public in the future, but for now
you could bind the nullable `Bundle` that you receive in the activity `#onCreate`
hook and inject it to your view models.

### ...fragment-owned `ViewModel`s?

Sometimes you want a view model that is scoped to a particular fragment and not to
the activity. You might want the data to survive configuration changes, but you
also want the data to go away when the fragment is detached so that when the fragment
is re-attached, you get back to a clean state. In this case, you shouldn't inject
a `@Shared` view model to a fragment, but instead depend on a `ViewModelProvider.Factory`
and build your own view models using `ViewModelProvider` or the jetpack viewmodel
extension.

When the interface `ViewModelProvider.Factory` is requested anywhere in the graph,
the `shiv` processor generates a module called `shiv.ViewModelBindings` that should
be added to your dagger graph. The bundled `Shiv` module itself binds an
implementation of the `ViewModelProvider.Factory` that relies on this generated
module to populate a map multibinding of view model providers.

```kotlin
@Subcomponent(modules = [Shiv::class, FragmentBindings::class, ViewModelBindings::class])
interface ViewComponent {
    // ...
}

class SomeFragment @Inject constructor(
    private val vmFactory: ViewModelProvider.Factory
) : Fragment(R.layout.some_layout) {
    // using jetpack lifecycle-viewmodel-ktx
    private val model: SomeViewModel by viewModels { vmFactory }

    // ...
}
```

Note that `SomeViewModel` above must be reachable by dagger, so an `@Inject`ed
constructor is required even if it's empty.

### ...if I already have a binding to `FragmentFactory` or `ViewModelProvider.Factory`?

Don't install the `Shiv` module. Instead, use the concrete types `InjectingFragmentFactory`
and `InjectingViewModelFactory` anywhere you use `FragmentFactory` and
`ViewModelProvider.Factory` respectively.


## License
```
MIT License

Copyright (c) 2020 Mon Zafra

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
