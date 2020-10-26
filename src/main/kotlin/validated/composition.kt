/**
 * @author Nikolaus Knop
 */

package validated

import validated.Validated.InvalidComponent

interface CompositionBody<T> {
    fun <F> Validated<F>.get(): F

    operator fun <F> Validated<F>.invoke(): F

    fun terminate(result: Validated<T>): Nothing

    fun yield(value: T): Nothing

    fun error(reason: String): Nothing

    fun invalidComponent(): Nothing
}

@PublishedApi internal class TerminationSignal(val result: Validated<Any?>) : Exception()

@PublishedApi internal class CompositionBodyImpl<T> : CompositionBody<T> {
    override fun terminate(result: Validated<T>): Nothing = throw TerminationSignal(result)

    override fun <F> Validated<F>.get(): F = ifInvalid { invalidComponent() }

    override fun <F> Validated<F>.invoke(): F = get()

    override fun yield(value: T) = terminate(valid(value))

    override fun error(reason: String) = terminate(invalid(reason))

    override fun invalidComponent(): Nothing = terminate(InvalidComponent)
}

inline fun <T> compose(body: CompositionBody<T>.() -> T): Validated<T> = try {
    val result = CompositionBodyImpl<T>().body()
    valid(result)
} catch (ex: TerminationSignal) {
    @Suppress("UNCHECKED_CAST")
    ex.result as Validated<T>
}

inline fun <A, T> compose(a: Validated<A>, crossinline transform: (a: A) -> T) = compose<T> { transform(a()) }

inline fun <A, B, T> compose(a: Validated<A>, b: Validated<B>, crossinline transform: (a: A, b: B) -> T) =
    compose<T> { (transform(a(), b())) }

inline fun <A, B, C, T> compose(
    a: Validated<A>,
    b: Validated<B>,
    c: Validated<C>,
    crossinline transform: (a: A, b: B, c: C) -> T
) = compose<T> { transform(a(), b(), c()) }

inline fun <A, B, C, D, T> compose(
    a: Validated<A>,
    b: Validated<B>,
    c: Validated<C>,
    d: Validated<D>,
    crossinline transform: (a: A, b: B, c: C, d: D) -> T
) = compose<T> { transform(a(), b(), c(), d()) }

inline fun <A, B, C, D, E, T> compose(
    a: Validated<A>,
    b: Validated<B>,
    c: Validated<C>,
    d: Validated<D>,
    e: Validated<E>,
    crossinline transform: (a: A, b: B, c: C, d: D, e: E) -> T
) = compose<T> { transform(a(), b(), c(), d(), e()) }