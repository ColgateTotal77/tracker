package com.colgateTotal77.tracker.core.ui

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ToastFailure(@param:StringRes val messageRes: Int) : Exception()

internal fun ViewModel.launchWithToast(
    @StringRes error: Int,
    @StringRes success: Int? = null,
    block: suspend () -> Unit,
) = viewModelScope.launch {
    try {
        withContext(Dispatchers.IO) { block() }
        success?.let { AppToast.show(it) }
    } catch (e: Exception) {
        when (e) {
            is CancellationException -> throw e
            is ToastFailure -> AppToast.show(e.messageRes, ToastType.Error)
            else -> {
                Log.e("ToastOperation", "Operation failed", e)
                AppToast.show(error, ToastType.Error)
            }
        }
    }
}
