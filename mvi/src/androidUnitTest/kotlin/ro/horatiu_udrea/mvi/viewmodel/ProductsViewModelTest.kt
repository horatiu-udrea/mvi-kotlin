package ro.horatiu_udrea.mvi.viewmodel

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

class ProductsViewModelTest : BehaviorSpec({
    extension(MainDispatcherExtension())

    val productList = listOf(
        Product(1, "Product 1", 1.0),
        Product(2, "Product 2", 2.0),
        Product(3, "Product 3", 3.0)
    )

    val dependencies = D(
        getProductsUseCase = { productList },
        buyProductUseCase = { },
        trackProductNumberUseCase = {}
    )

    // Test the entire viewmodel
    Context("I create a ProductsViewModel and it can fetch products") {
        Given("I create a ProductsViewModel") {
            testViewModel(ProductsViewModel(dependencies)) {
                Then("Products are loading") {
                    state.products shouldBe null
                }
                When("I refresh products") {
                    sendIntent(ProductsIntent.RefreshProducts)
                    Then("Products are in the list") {
                        state.products shouldBe productList
                    }
                }
                When("I refresh products again") {
                    sendIntent(ProductsIntent.RefreshProducts)
                    Then("Products are still in the list") {
                        state.products shouldBe productList
                    }
                }
            }

        }
    }

    // Test intent handling separately
    Context("I have old products and I refresh products") {
        Given("I have old products") {
            When("I refresh products") {
                val (producedStates, scheduledIntents) = testIntent(
                    ProductsIntent.RefreshProducts,
                    dependencies,
                    initialState = ProductsState(emptyList())
                )
                Then("Products were loading and now are available") {
                    producedStates shouldBe listOf(
                        ProductsState(products = null),
                        ProductsState(products = productList)
                    )
                }
                Then("There are no side effects") {
                    scheduledIntents should beEmpty()
                }
            }
        }
    }
})