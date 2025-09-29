package com.app.tourism_app.database.data.remote

import android.content.Context

fun requireOnlineOrNull(ctx: Context): String? {
    return if (!NetworkMonitor.isOnlineNow(ctx)) "You’re offline. Connect to the internet." else null
}

fun friendlyNetError(t: Throwable): String = when (t) {
    is java.net.UnknownHostException -> "No internet connection."
    is java.net.SocketTimeoutException -> "Network timeout. Try again."
    is retrofit2.HttpException -> "Server error: ${t.code()}"
    else -> t.message ?: "Network error."
}