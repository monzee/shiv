# Shiv
Constructor injection for fragments and view models using dagger2.


## Requirements and assumptions
- dagger2
- construction injection only
- using `androidx.*` packages
- single activity; using `androidx.navigation` system


## Usage
The following code examples are simplified in order to highlight the important parts
of the process. Please see the demo module to see how the components typically look
like in actual projects.

1. Define the classes to be injected. It's alright to keep the constructor empty.
   This step is done in order to trigger the module code generation. Hit `Ctrl-F9`
   in order to do so.

    ```kotlin
    class LoginFragment @Inject constructor() : Fragment(R.layout.fragment_login)
    
    class LoginModel @Inject constructor() : ViewModel()
    ```

2. Define the components. Since fragments are shorter-lived than view models, all
   injected fragments must be bound in a subcomponent of wherever the view models
   are bound. This ensures that you can't inject view-related objects into
   a view model's constructor (a memory leak). Install the generated module
   `shiv.FragmentBindings` to the subcomponent and `shiv.SharedViewModelProviders`
   to the parent component.
   
    ```kotlin
    @Component(modules = [SharedViewModelProviders::class])
    interface ModelComponent {
        val mainComponentFactory: MainComponent.Factory
    
        @Component.Factory
        interface Factory {
            fun create(@BindsInstance owner: ViewModelStoreOwner): ModelComponent
        }
    }
    
    @Subcomponent(modules = [FragmentBindings::class])
    interface MainComponent {
        @Subcomponent.Factory
        interface Factory {
            fun create(): MainComponent
        }
    }
    ```

3. Bind the bundled `Shiv` module to any component. Expose the type `FragmentFactory` in the
   subcomponent.

    ```kotlin
    @Subcomponent(modules = [FragmentBindings::class, Shiv::class])
    interface MainComponent {
        val fragmentFactory: FragmentFactory
        
        @Subcomponent.Factory
        interface Factory {
            fun create(): MainComponent
        }
    }
    ```

4. Subclass `NavHostFragment` and replace the factory of the child fragment manager with the one
   created by dagger. Make sure to reference this subclass in the main activity layout instead of
   `NavHostFragment`. Rebuild the project to trigger the generation of the dagger implementations.
   
    ```kotlin
    class MainFragment : NavHostFragment() {
        override fun onAttach(context: Context) {
            childFragmentManager.fragmentFactory = DaggerModelComponent.factory()
                .create(this)
                .mainComponentFactory
                .create()
                .fragmentFactory
            super.onAttach(context)
        }
    }
    
    class MainActivity : AppCompatActivity(R.layout.activity_main)
    ```
    ```xml
    <!-- activity_main.xml -->
    <!-- ... -->
        <fragment
            android:id="@+id/nav_host"
            android:name="my.package.MainFragment"
            app:defaultNavHost="true"
            app:navGraph="@navigation/main_graph"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />
    <!-- ... -->
    ```

5. Go back to the fragment and view models classes and fill out the dependencies. Qualify view model
   dependencies with `@Shared`. Install additional modules as required.
   
    ```kotlin
    class LoginFragment @Inject constructor(
        @Shared private val model: LoginModel
    ) : Fragment(R.layout.fragment_login) {
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            // ...
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

Copyright (c) 2019 Mon Zafra

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

