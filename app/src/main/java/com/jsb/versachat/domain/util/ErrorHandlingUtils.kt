package com.jsb.versachat.domain.util

import android.util.Log
import kotlinx.coroutines.CancellationException

inline fun <T> safeCall(
    tag: String = "SafeCall",
    action: () -> T
): Result<T> {
    return try {
        Result.Success(action())
    } catch (e: CancellationException) {
        throw e // Don't catch cancellation exceptions
    } catch (e: Exception) {
        Log.e(tag, "Safe call failed", e)
        Result.Error(e)
    }
}

suspend inline fun <T> safeSuspendCall(
    tag: String = "SafeSuspendCall",
    action: suspend () -> T
): Result<T> {
    return try {
        Result.Success(action())
    } catch (e: CancellationException) {
        throw e // Don't catch cancellation exceptions
    } catch (e: Exception) {
        Log.e(tag, "Safe suspend call failed", e)
        Result.Error(e)
    }
}

// Extension functions for better error messages
fun Throwable.toUserFriendlyMessage(): String {
    return when (this) {
        is java.net.UnknownHostException -> "No internet connection available"
        is java.net.SocketTimeoutException -> "Request timed out. Please try again"
        is java.net.ConnectException -> "Unable to connect to server"
        is retrofit2.HttpException -> {
            when (code()) {
                400 -> "Invalid request"
                401 -> "Authentication failed"
                403 -> "Access denied"
                404 -> "Service not found"
                429 -> "Too many requests. Please wait"
                500 -> "Server error. Please try again later"
                else -> "Network error (${code()})"
            }
        }
        else -> message ?: "An unexpected error occurred"
    }
}