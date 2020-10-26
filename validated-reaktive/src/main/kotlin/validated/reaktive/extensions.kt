/**
 * @author Nikolaus Knop
 */

package validated.reaktive

import reaktive.Reactive
import reaktive.collection.ReactiveCollection
import reaktive.dependencies
import reaktive.value.ReactiveValue
import reaktive.value.binding.*
import reaktive.value.reactiveValue
import validated.*

typealias ReactiveValidated<T> = ReactiveValue<Validated<T>>

typealias ValidatedReactive<T> = Validated<ReactiveValue<T>>

inline fun <T, F> ReactiveValidated<T>.mapValidated(crossinline transform: (value: T) -> F)
        : ReactiveValidated<F> = map { it.map(transform) }

inline fun <T, F> ReactiveValidated<T>.flatMapValidated(crossinline transform: (value: T) -> Validated<F>)
        : ReactiveValidated<F> = map { it.flatMap(transform) }

inline fun <T, F> ValidatedReactive<T>.mapReactive(crossinline transform: (value: T) -> F)
        : ValidatedReactive<F> = map { it.map(transform) }

inline fun <T, F> ValidatedReactive<T>.flatMapReactive(crossinline transform: (value: T) -> ReactiveValue<F>)
        : ValidatedReactive<F> = map { it.flatMap(transform) }

fun <T> ValidatedReactive<T>.transpose(): ReactiveValidated<T> =
    fold(onValid = { it.map(::valid) }, onInvalid = ::reactiveValue)

inline fun <R> composeReactive(
    vararg deps: Reactive,
    crossinline body: CompositionBody<R>.() -> R
): ReactiveValidated<R> = binding<Validated<R>>(dependencies(*deps)) { compose(body) }

inline fun <A, T> composeReactive(a: ReactiveValidated<A>, crossinline transform: (a: A) -> T): ReactiveValidated<T> =
    composeReactive<T>(a) { transform(a.get().invoke()) }

inline fun <A, B, T> composeReactive(
    a: ReactiveValidated<A>,
    b: ReactiveValidated<B>,
    crossinline transform: (a: A, b: B) -> T
): ReactiveValidated<T> = composeReactive<T>(a, b) { (transform(a.get().invoke(), b.get().invoke())) }

inline fun <A, B, C, T> composeReactive(
    a: ReactiveValidated<A>,
    b: ReactiveValidated<B>,
    c: ReactiveValidated<C>,
    crossinline transform: (a: A, b: B, c: C) -> T
): ReactiveValidated<T> =
    composeReactive<T>(a, b, c) { transform(a.get().invoke(), b.get().invoke(), c.get().invoke()) }

inline fun <A, B, C, D, T> composeReactive(
    a: ReactiveValidated<A>,
    b: ReactiveValidated<B>,
    c: ReactiveValidated<C>,
    d: ReactiveValidated<D>,
    crossinline transform: (a: A, b: B, c: C, d: D) -> T
): ReactiveValidated<T> =
    composeReactive<T>(a, b, c, d) { transform(a.get().invoke(), b.get().invoke(), c.get().invoke(), d.get().invoke()) }

inline fun <A, B, C, D, E, T> composeReactive(
    a: ReactiveValidated<A>,
    b: ReactiveValidated<B>,
    c: ReactiveValidated<C>,
    d: ReactiveValidated<D>,
    e: ReactiveValidated<E>,
    crossinline transform: (a: A, b: B, c: C, d: D, e: E) -> T
): ReactiveValidated<T> = composeReactive<T>(a, b, c, d, e) {
    transform(
        a.get().invoke(),
        b.get().invoke(),
        c.get().invoke(),
        d.get().invoke(),
        e.get().invoke()
    )
}

fun <E> Iterable<Validated<E>>.filterValid(): List<E> {
    val result = mutableListOf<E>()
    for (e in this) e.ifValid { result.add(it) }
    return result
}