# Shiv
Constructor injection for fragments and view models using dagger2 with
minimal boilerplate.


## Requirements and assumptions
- dagger2 in your build dependencies
- constructor injection only (may change in the future)
- using `androidx.*` packages
- all view models are owned by the activity and thus shared by every fragment


## Usage
The following code examples are simplified in order to highlight the important parts
of the process. Please see the demo module to see how the components typically look
like in actual projects. All generated code are in java7, so kotlin or d8 is not
required.

1. Define the classes to be injected. This step is done first in order to trigger
   the module code generation. You can fill in the dpendencies later. Hit `Ctrl-F9`
   to build the project.

    ```kotlin
    class LoginFragment @Inject constructor() : Fragment(R.layout.fragment_login)

    class LoginModel @Inject constructor() : ViewModel()
    ```

2. Define the components. Since fragments are shorter-lived than view models, all
   injected fragments must be bound in a subcomponent of wherever the view models
   are bound. This ensures that you can't inject view-related objects into
   a view model's constructor and cause a memory leak.

   Install the generated module `shiv.FragmentBindings` to the subcomponent. In
   the factory or builder interface of the module, add a `@BindsInstance`-
   annotated `ViewModelStoreOwner` parameter/builder method.

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
    @Subcomponent(modules = [FragmentBindings::class, Shiv::class])
    interface ViewComponent {
        val fragmentFactory: FragmentFactory
    }
    ```

4. Override the activity's `#onCreate` method to use the fragment factory built by dagger.
   Make sure to do this before calling `super.onCreate(...)`.

    ```kotlin
    class MainActivity : AppCompatActivity(R.layout.activity_main) {
        override fun onCreate(savedInstanceState: Bundle) {
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
   **DO NOT** request `Fragment` classes from `ViewModel`s because even though it
   might compile, you won't get the same instance attached to the activity.

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
