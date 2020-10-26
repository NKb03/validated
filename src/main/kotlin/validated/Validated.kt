/**
 *@author Nikolaus Knop
 */

package validated

sealed class Validated<out T> {
    data class Valid<out T>(val value: T) : Validated<T>()

    data class Invalid(val reason: String) : Validated<Nothing>()

    object InvalidComponent : Validated<Nothing>()
}