package contacts.core.util

/**
 * Returns a trimmed copy of this list based on the given [offset] and [limit].
 *
 * This will return an empty list if a negative number is provided or in case where an index
 * out of bounds exception would be thrown.
 */
internal fun <T> List<T>.offsetAndLimit(offset: Int, limit: Int): List<T> =
    if (offset >= size || offset < 0 || limit <= 0) {
        emptyList()
    } else {
        // Prevent an integer overflow by converting offset and limit to longs first,
        // performing addition, then converting back to integer. The resulting value is guaranteed
        // to not exceed the integer max value because we are using coerceAtMost to the size, which
        // is an integer.
        val toIndex = (offset.toLong() + limit.toLong()).coerceAtMost(size.toLong()).toInt()

        subList(offset, toIndex)
    }