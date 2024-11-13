package com.example.android_demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import ro.horatiu_udrea.mvi.MVIComponent

@Composable
fun ProductsScreen(onNavigateBack: () -> Unit) {
    val viewModel: MVIComponent<ProductsState, ProductsIntent> = viewModel<ProductsViewModel>()

    ProductsComposable(viewModel.composableState(), viewModel::sendIntent, onNavigateBack)
}

@Composable
fun ProductsComposable(
    state: ProductsState,
    sendIntent: (ProductsIntent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Box {
        if (state.productsLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }

        LazyColumn(Modifier.fillMaxSize()) {
            items(state.products) { product ->
                DisplayProduct(product, sendIntent)
            }
        }

        Button(
            modifier = Modifier.align(Alignment.BottomCenter),
            onClick = { sendIntent(ProductsIntent.RefreshProducts) },
            enabled = state.productsLoading.not()
        ) {
            Text("Refresh products")
        }

        // Show a dialog and dismiss the flag when user dismisses the dialog
        if (state.purchaseError) {
            Dialog(
                onDismissRequest = { sendIntent(ProductsIntent.DismissPurchaseError) }
            ) {
                Text("An error occurred while purchasing the product")
            }
        }

        // OR

        // Navigate back to the previous screen
        Effect(
            trigger = state.purchaseError,
            effect = onNavigateBack,
            dismissTrigger = { sendIntent(ProductsIntent.DismissPurchaseError) }
        )

        // Effect works with generic nullable objects as well, like enums or sealed hierarchies
        /*
        Effect(
            trigger = state.navigateToPage,
            effect = { page -> if(page is Page1) navigateToPage1() else navigateToPage2() },
            dismissTrigger = { page -> sendIntent(ProductsIntent.DismissNavigateFlag) }
        )
         */
    }
}

@Composable
fun DisplayProduct(product: Product, sendIntent: (ProductsIntent) -> Unit) {
    Row {
        Text(product.name)
        Text(product.price.toString())
        Button(onClick = { sendIntent(ProductsIntent.BuyProduct(product)) }) {
            Text("Buy product")
        }
    }
}

@Preview
@Composable
fun ProductsComposablePreview() {
    ProductsComposable(
        ProductsState(
            products = listOf(
                Product(1, "Product 1", 1.0),
                Product(2, "Product 2", 2.0),
                Product(3, "Product 3", 3.0)
            )
        ),
        sendIntent = {},
        onNavigateBack = {}
    )
}