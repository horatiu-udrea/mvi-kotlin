package com.example.android_demo

import com.example.android_demo.util.MainDispatcherExtension
import com.example.android_demo.util.testIntent
import com.example.android_demo.util.testViewModel
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MainDispatcherExtension::class)
class ProductsViewModelTest {

    private val productList = listOf(
        Product(1, "Product 1", 1.0),
        Product(2, "Product 2", 2.0),
        Product(3, "Product 3", 3.0)
    )

    private val dependencies = D(
        getProductsUseCase = { productList },
        buyProductUseCase = { true },
        trackProductNumberUseCase = {}
    )

    @Test
    fun `Fetch products`() = runTest {
        testViewModel(ProductsViewModel(dependencies)) {
            state.products shouldBe emptyList()
            state.productsLoading shouldBe false

            sendIntent(ProductsIntent.RefreshProducts)

            state.products shouldBe productList
            state.productsLoading shouldBe false

            sendIntent(ProductsIntent.RefreshProducts)

            state.products shouldBe productList
            state.productsLoading shouldBe false
        }
    }

    @Test
    fun `Test intent RefreshProducts`() = runTest {
        val (producedStates, scheduledIntents) = testIntent(
            ProductsIntent.RefreshProducts,
            dependencies,
            initialState = ProductsState(products = emptyList(), productsLoading = false)
        )
        producedStates shouldBe listOf(
            ProductsState(products = emptyList(), productsLoading = true),
            ProductsState(products = productList, productsLoading = false)
        )
        scheduledIntents should beEmpty()
    }
}