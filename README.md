# Model-View-Intent (MVI) components for Kotlin Multiplatform
Minimalistic MVI implementation for Kotlin Multiplatform. 

Key highlights:
- **Easy to write and maintain**: Start from the template and build your way through. Scheduling declarations like `RunIfNotRunning` and `CancelCurrentThenRun` ensure safety when executing multiple intents of the same type. If the file gets too big, the components can be easily split across multiple files.
- **Easy to navigate**: Just use your IDE's *Go to Definition* action to jump easily between sections, call sites and declaration sites - there is no intermediate interface.
- **Easy to document**: Definitions live with the declarations, so documentation has to be done only in one place.
- **Easy to test**: You can test the viewmodel altogether as well as individual intents.
- **Easy to use in UI and previews**: You just pass the `state` and the `sendIntent` function in `@Composable`s. No need for more callbacks or `State` variables. Also, since it's always just one state that contains default values, it's trivial to create previews for different cases.
- **Easy to observe and debug**: With the provided callbacks you can always respond to sent intents, state changes and unhandled errors, wherever they originate from - at runtime as well as during debugging.
- **Easy to customize and upgrade**: The simple design creates room for custom extensions and components, while the dependency on just two libraries makes it easy to upgrade at your own pace: Kotlin Standard Library and Kotlinx Coroutines.

> [!NOTE]  
> The library implementation is stable and safe to use in production.  
> API is still unstable and might change with newer versions, but there is no need to update.

> [!TIP]  
> At the moment, only JVM and Android artifacts are provided.  
> Support for the other platforms will be added soon.

### Include in your build
#### Using Gradle file configuration
In `build.gradle.kts` add:
```kotlin
dependencies {
    implementation("ro.horatiu-udrea:mvi:0.1.1")
}
```
#### Using Version Catalog
In `libs.versions.toml` add:
```toml
[versions]
mvi = "0.1.1"

[libraries]
mvi = { module = "ro.horatiu-udrea:mvi", version.ref = "mvi" }
```
and in `build.gradle.kts` add:
```kotlin
dependencies {
    implementation(libs.mvi)
}
```

### Android Example
Find full implementation in [android-demo sources](android-demo/src/main/java/com/example/android_demo) and tests for the components in [android-demo tests](android-demo/src/test/java/com/example/android_demo).

A template for Android Studio can be found [here](.idea/fileTemplates/MVI%20ViewModel.kt).

```kotlin
// Use "Go to definition" to easily navigate sections
typealias S = ProductsState
typealias I = ProductsIntent
typealias D = ProductsDependencies

class ProductsViewModel(dependencies: D) : MVIViewModel<S, I, D>(initialState = ProductsState(), dependencies) {

    // Put a breakpoint on the line with "Unit" and observe all received intents
    // You can also send intents here, for example when you want to start associated processes
    override fun onIntent(
        intent: I,
        sendIntent: (I) -> Unit
    ) = Unit

    // Put a breakpoint on the line with "Unit" and observe all state changes
    // Can also send intents for changes that can occur from multiple sources
    override fun onStateChange(
        description: String,
        sourceIntent: I,
        oldState: S,
        newState: S,
        sendIntent: (I) -> Unit
    ) = Unit

    // Override this for debugging or reporting critical errors to users via an intent
    override fun onException(
        intent: I,
        exception: Throwable,
        sendIntent: (I) -> Unit
    ) {
        super.onException(intent, exception, sendIntent)
    }
}

data class ProductsState(
    val products: List<Product> = emptyList(),
    val productsLoading: Boolean = false,
    val purchaseError: Boolean = false
)

data class Product(val id: Int, val name: String, val price: Double)

sealed interface ProductsIntent : IntentHandler<S, I, D> {
    data object RefreshProducts : I, RunIfNotRunning<S, I, D>({ state ->
        // Change the state and provide a description
        state.change("Products are loading") { oldState -> oldState.copy(productsLoading = true) }

        // Access dependencies from scope
        val products = getProductsUseCase()

        // Change the state and provide a description
        state.change("Products updated") { oldState ->
            oldState.copy(products = products, productsLoading = false)
        }
    })

    data class BuyProduct(val product: Product) : I, Run<S, I, D>(run@{ state ->
        // Access the current state and use it.
        // Do this only when you need up-to-date info,
        // otherwise include values in the intent data class and send them from UI
        val productNumber = state.read("Read current number of products") { it.products.size }

        trackProductNumberUseCase(productNumber)

        val successful = buyProductUseCase(product)

        if (!successful) {
            // Change state to loading products, no description provided when it's trivial
            state.change("Product purchase not successful") { it.copy(purchaseError = true) }
            return@run
        }

        // Express the reason for which the state was not changed
        state.keep("Bought product, refreshing products now")

        // Use other intents if needed. Does not suspend, only schedules intent handling.
        state.schedule(RefreshProducts)
    })

    data object DismissPurchaseError : I, Run<S, I, D>({ state ->
        // Change state with no description provided when it's trivial
        state.change { it.copy(purchaseError = false) }
    })
}

// Group all dependencies here. This can be injected using your favorite DI tool.
class ProductsDependencies(
    val getProductsUseCase: suspend () -> List<Product>,
    val buyProductUseCase: suspend (Product) -> Boolean,
    val trackProductNumberUseCase: suspend (Int) -> Unit
)
```