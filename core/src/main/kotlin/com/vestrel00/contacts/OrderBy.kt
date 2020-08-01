package com.vestrel00.contacts

// Java consumers would have to access these static functions via OrderBykt instead of OrderBy.
// Using @file:JvmName("OrderBy") will not work because of the name clash with the OrderBy class.
// In order for Java consumers to use these via OrderBy instead of OrderBykt, we could redefine
// these functions in a companion object within the OrderBy class. However, we won't do this just
// because it creates duplicate code. Java users just need to migrate to Kotlin already...

private const val DEFAULT_IGNORE_CASE = true

/**
 * Default [ignoreCase] is true.
 */
@JvmOverloads
fun <T : Field> T.asc(ignoreCase: Boolean = DEFAULT_IGNORE_CASE): OrderBy<T> =
    Ascending(this, ignoreCase)

/**
 * Default [ignoreCase] is true.
 */
@JvmOverloads
fun <T : Field> T.desc(ignoreCase: Boolean = DEFAULT_IGNORE_CASE): OrderBy<T> =
    Descending(this, ignoreCase)

/**
 * Default [ignoreCase] is true.
 */
@JvmOverloads
fun <T : Field> Collection<T>.asc(ignoreCase: Boolean = DEFAULT_IGNORE_CASE):
        Set<OrderBy<T>> = asSequence().asc(ignoreCase).toSet()

/**
 * Default [ignoreCase] is true.
 */
@JvmOverloads
fun <T : Field> Collection<T>.desc(ignoreCase: Boolean = DEFAULT_IGNORE_CASE):
        Set<OrderBy<T>> = asSequence().desc(ignoreCase).toSet()

/**
 * Default [ignoreCase] is true.
 */
@JvmOverloads
fun <T : Field> Sequence<T>.asc(ignoreCase: Boolean = DEFAULT_IGNORE_CASE): Sequence<OrderBy<T>> =
    map { it.asc(ignoreCase) }

/**
 * Default [ignoreCase] is true.
 */
@JvmOverloads
fun <T : Field> Sequence<T>.desc(ignoreCase: Boolean = DEFAULT_IGNORE_CASE): Sequence<OrderBy<T>> =
    map { it.desc(ignoreCase) }

/**
 * Used as the ORDER BY clause in a query.
 *
 * ## Developer notes
 *
 * The type [T] is not exactly used in this class itself. Rather, it is used for adding type
 * restrictions when constructing instances at compile time.
 */
sealed class OrderBy<out T : Field> {

    internal abstract val field: T
    internal abstract val ignoreCase: Boolean
    protected abstract val order: String

    override fun toString(): String = if (ignoreCase) {
        "${field.columnName} COLLATE NOCASE $order"
    } else {
        "${field.columnName} $order"
    }
}

internal class Ascending<T : Field>(override val field: T, override val ignoreCase: Boolean) :
    OrderBy<T>() {

    override val order: String = "ASC"
}

internal class Descending<T : Field>(override val field: T, override val ignoreCase: Boolean) :
    OrderBy<T>() {

    override val order: String = "DESC"
}

/**
 * A set of one or more [OrderBy].
 *
 * ## Developer notes
 *
 * The type [T] is not exactly used in this class itself. Rather, it is used for adding type
 * restrictions when constructing instances at compile time.
 */
internal class CompoundOrderBy<out T : Field>(private val orderBys: Set<OrderBy<T>>) {

    override fun toString(): String = orderBys.joinToString(", ")
}