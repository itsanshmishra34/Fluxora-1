package com.example.logging

import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.ConcurrentLinkedQueue

enum class LogLevel {
    INFO,
    WARN,
    ERROR
}

data class LogEntry(
    val timestampMillis: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwable: Throwable? = null
) {
    val formattedStackTrace: String?
        get() {
            if (throwable == null) return null
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            return sw.toString()
        }
}

/**
 * Centralized logging and error reporting service for Fluxora.
 * Buffers recent in-memory log events and provides uncaught exception capture.
 */
object FluxoraLogger {
    private const val DEFAULT_TAG = "FluxoraRuntime"
    private const val MAX_LOG_CAPACITY = 100

    private val logQueue = ConcurrentLinkedQueue<LogEntry>()
    private var isHandlerInstalled = false

    fun info(tag: String = DEFAULT_TAG, message: String) {
        log(LogLevel.INFO, tag, message, null)
    }

    fun warn(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        log(LogLevel.WARN, tag, message, throwable)
    }

    fun error(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        log(LogLevel.ERROR, tag, message, throwable)
    }

    fun recordException(throwable: Throwable, contextTag: String = DEFAULT_TAG) {
        log(
            level = LogLevel.ERROR,
            tag = contextTag,
            message = throwable.localizedMessage ?: throwable.message ?: "Uncaught Exception",
            throwable = throwable
        )
    }

    private fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        val entry = LogEntry(
            level = level,
            tag = tag,
            message = message,
            throwable = throwable
        )

        logQueue.add(entry)
        while (logQueue.size > MAX_LOG_CAPACITY) {
            logQueue.poll()
        }

        when (level) {
            LogLevel.INFO -> Log.i(tag, message)
            LogLevel.WARN -> Log.w(tag, message, throwable)
            LogLevel.ERROR -> Log.e(tag, message, throwable)
        }
    }

    fun getLogs(): List<LogEntry> {
        return logQueue.toList()
    }

    fun getRecentErrors(): List<LogEntry> {
        return logQueue.filter { it.level == LogLevel.ERROR }
    }

    fun getLatestError(): LogEntry? {
        return logQueue.lastOrNull { it.level == LogLevel.ERROR }
    }

    fun clearLogs() {
        logQueue.clear()
    }

    @Synchronized
    fun installGlobalExceptionHandler(onUncaughtException: ((Thread, Throwable) -> Unit)? = null) {
        if (isHandlerInstalled) return
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            recordException(throwable, "UncaughtThreadException")
            onUncaughtException?.invoke(thread, throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
        isHandlerInstalled = true
        info("FluxoraLogger", "Global Uncaught Exception Handler successfully installed.")
    }
}
