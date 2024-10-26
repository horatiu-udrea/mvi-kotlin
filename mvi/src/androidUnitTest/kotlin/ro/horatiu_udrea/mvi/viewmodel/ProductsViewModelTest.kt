package ro.horatiu_udrea.mvi.viewmodel

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.should
import ro.horatiu_udrea.mvi.MainDispatcherExtension

@Suppress("unused")
class ProductsViewModelTest : BehaviorSpec({
    extension(MainDispatcherExtension())
    val productList = listOf(
        Product(1, "Product 1", 1.0),
        Product(2, "Product 2", 2.0),
        Product(3, "Product 3", 3.0)
    )

    Context("I create a ProductsViewModel and it can fetch products") {
        Given("I create a ProductsViewModel") {
            val viewModel = ProductsViewModel(
                D(
                    getProductsUseCase = { productList },
                    buyProductUseCase = { },
                    trackProductNumberUseCase = {}
                )
            )
            Then("No products are in the list") {
                viewModel.state.value.products should beEmpty()
            }
            When("I refresh products") {
                viewModel.sendIntent(ProductsIntent.RefreshProducts)
                Then("Products are in the list") {
                    viewModel.state.value.products shouldContainExactly productList
                }
            }
            When("I refresh products again") {
                viewModel.sendIntent(ProductsIntent.RefreshProducts)
                Then("Products are still in the list") {
                    viewModel.state.value.products shouldContainExactly productList
                }
            }
        }
    }
})