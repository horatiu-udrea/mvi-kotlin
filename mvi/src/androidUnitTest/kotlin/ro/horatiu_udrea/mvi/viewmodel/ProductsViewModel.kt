package ro.horatiu_udrea.mvi.viewmodel

import ro.horatiu_udrea.mvi.base.IntentHandler
import ro.horatiu_udrea.mvi.handlers.CancelCurrentThenRun
import ro.horatiu_udrea.mvi.handlers.Run

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
    val products: List<Product>? = null
)

data class Product(val id: Int, val name: String, val price: Double)

sealed interface ProductsIntent : IntentHandler<S, I, D> {
    data object RefreshProducts : I, CancelCurrentThenRun<S, I, D>({ state ->
        // Change state to loading products, no description provided when it's trivial
        state.change { oldState -> oldState.copy(products = null) }

        // Access dependencies from scope
        val products = getProductsUseCase()

        // Change the state and provide a description
        state.change("Products updated") { oldState ->
            oldState.copy(products = products)
        }
    })

    data class BuyProduct(val product: Product) : I, Run<S, I, D>({ state ->
        // Access current state and use it.
        // Do this only when you need up-to-date info,
        // otherwise include values in the intent data class and send them from UI
        val productNumber = state.read("Read current number of products") { it.products?.size }
        trackProductNumberUseCase(productNumber ?: 0)

        buyProductUseCase(product)

        // Express the reason for which the state was not changed
        state.keep("Bought product, refreshing products now")

        // Use other intents if needed. Does not suspend, only schedules intent handling.
        state.schedule(RefreshProducts)
    })
}

// Group all dependencies here. This can be injected using your favorite DI tool.
class ProductsDependencies(
    val getProductsUseCase: suspend () -> List<Product>,
    val buyProductUseCase: suspend (Product) -> Unit,
    val trackProductNumberUseCase: suspend (Int) -> Unit
)