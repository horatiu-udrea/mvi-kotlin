package com.example.android_demo

import com.example.android_demo.util.MainDispatcherExtension
import com.example.android_demo.util.testIntent
import com.example.android_demo.util.testViewModel
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
        buyProductUseCase = { true },
        trackProductNumberUseCase = {}
    )

    // Test the entire viewmodel
    Context("I create a ProductsViewModel and it can fetch products") {
        Given("I create a ProductsViewModel") {
            testViewModel(ProductsViewModel(dependencies)) {
                Then("Products are not loading") {
                    state.products shouldBe emptyList()
                    state.productsLoading shouldBe false
                }
                When("I refresh products") {
                    sendIntent(ProductsIntent.RefreshProducts)
                    Then("Products are in the list") {
                        state.products shouldBe productList
                        state.productsLoading shouldBe false
                    }
                }
                When("I refresh products again") {
                    sendIntent(ProductsIntent.RefreshProducts)
                    Then("Products are still in the list") {
                        state.products shouldBe productList
                        state.productsLoading shouldBe false
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
                    initialState = ProductsState(products = emptyList(), productsLoading = false)
                )
                Then("Products were loading and now are available") {
                    producedStates shouldBe listOf(
                        ProductsState(products = emptyList(), productsLoading = true),
                        ProductsState(products = productList, productsLoading = false)
                    )
                }
                Then("There are no side effects") {
                    scheduledIntents should beEmpty()
                }
            }
        }
    }
})