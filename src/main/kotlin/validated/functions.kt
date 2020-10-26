/**
 * @author Nikolaus Knop
 */

package validated

import validated.Validated.*

fun <T> valid(value: T): Validated<T> = Valid(value)

fun <T> invalid(reason: String): Validated<T> = Invalid(reason)

val invalidComponent: Validated<Nothing> get() = InvalidComponent

fun <T> invalidComponent(): Validated<T> = InvalidComponent

val Validated<*>.isValid: Boolean get() = this is Valid

val Validated<*>.isInvalid: Boolean get() = this is Invalid

val Validated<*>.isInvalidComponent: Boolean get() = this is InvalidComponent

inline fun <T : Any> T?.validated(default: () -> Validated<T>): Validated<T> =
    if (this != null) valid(this) else default()

inline fun <T, F> Validated<T>.fold(
    onValid: (value: T) -> F,
    onInvalid: (actual: Validated<Nothing>) -> F
): F = when (this) {
    is Valid            -> onValid(value)
    is Invalid          -> onInvalid(this)
    is InvalidComponent -> onInvalid(this)
}

inline fun <T, F> Validated<T>.map(transform: (value: T) -> F): Validated<F> =
    fold(onValid = { valid(transform(it)) }, onInvalid = { it })

inline fun <T, F> Validated<T>.flatMap(transform: (value: T) -> Validated<F>): Validated<F> =
    fold(onValid = transform, onInvalid = { it })

inline fun <T> Validated<T>.orElse(default: () -> Validated<T>): Validated<T> =
    fold({ valid(value = it) }, { default() })

fun <T> Validated<T>.or(default: Validated<T>): Validated<T> = orElse { default }

inline fun <T> Validated<T>.ifValid(action: (value: T) -> Unit) {
    fold(onValid = action, onInvalid = {})
}

inline fun <T> Validated<T>.ifInvalid(def: (invalid: Validated<Nothing>) -> T): T =
    fold(onValid = { v -> v }, onInvalid = def)

fun <T> Validated<T>.orNull(): T? = ifInvalid { null }

inline fun <T> Validated<T>.force(message: (actual: Validated<T>) -> String): T =
    ifInvalid { throw ValidationException(message(it)) }

fun <T> Validated<T>.force(): T = force { "expected valid but got $it" }

fun <T> Validated<T>.expectInvalid(): Validated<Nothing> =
    fold(onValid = { throw ValidationException("Expected invalid but got $this") }, onInvalid = { it })
