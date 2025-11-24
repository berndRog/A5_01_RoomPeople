package de.rogallab.mobile.data.repositories

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map


/**
 * Converts a Flow<T> into Flow<Result<T>> while handling cancellation correctly.
 *
 * - Normal values are wrapped as Result.success(...)
 * - Values are mapped with `toEntity` and wrapped as Result.success(\...)
 * - Exceptions are converted to Result.failure(\...)
 * - CancellationException is re-thrown to propagate cancellation.
 *
 * This ensures that coroutine cancellation (e.g., when leaving a screen
 * or when using collectLatest) does not trigger UI error handling.
 */
inline fun <T, R> Flow<T>.asResult(
   crossinline toEntity: (T) -> R
): Flow<Result<R>> =
   this
      .map { value -> Result.success(toEntity(value)) }
      .catch { e ->
         if (e is CancellationException) throw e
         emit(Result.failure(e))
      }


