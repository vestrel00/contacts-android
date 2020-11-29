package contacts.util

/**
 * True if the sequence emits no elements.
 *
 * This is a cheaper version of using `[Sequence.count] == 0` for checking if a sequence is empty.
 * Unlike [Sequence.count], this will not execute sequence functions that may not affect the number
 * emitted elements (e.g. [Sequence.map]) unless succeeded by a sequence function that does
 * (e.g. [Sequence.filter].
 *
 * Here are some examples to make it clearer.
 *
 * #### Example 1
 *
 * ```
 * val sequenceOfLetters = sequenceOf("a", "b")
 *      .map { println(it) }
 *
 * println("count(): ${sequenceOfLetters.count()}")
 * println("isEmpty: ${sequenceOfLetters.isEmpty()}")
 * ```
 *
 * outputs
 *
 * ```
 * a
 * b
 * count(): 2
 * isEmpty: false
 * ```
 *
 * Notice how the call to count() invoked the map function but the call to isEmpty (this function)
 * did not. That's because [Sequence.map] may not alter the number of elements emitted.
 *
 * #### Example 2
 *
 * ```
 * val sequenceOfLetters = sequenceOf("a", "b")
 *      .filter { false }
 *      .map { println(it) }
 *
 * println("count(): ${sequenceOfLetters.count()}")
 * println("isEmpty: ${sequenceOfLetters.isEmpty()}")
 * ```
 *
 * outputs
 *
 * ```
 * count(): 0
 * isEmpty: true
 * ```
 *
 * In this case, the map function was not invoked by count not isEmpty because no elements made it
 * through the filter. However, if we change it to `filter { true }` we get;
 *
 * ```
 * a
 * b
 * count(): 0
 * isEmpty: true
 * ```
 *
 * Notice how the call to count() invoked the map function but the call to isEmpty (this function)
 * did not.
 *
 *  * #### Example 3
 *
 * ```
 * val sequenceOfLetters = sequenceOf("a", "b")
 *      .map { println(it) }
 *      .filter { false }
 *
 * println("count(): ${sequenceOfLetters.count()}")
 * println("isEmpty: ${sequenceOfLetters.isEmpty()}")
 * ```
 *
 * outputs
 *
 * ```
 * a
 * b
 * count(): 0
 * a
 * b
 * isEmpty: true
 * ```
 *
 * In this case, the map function is also invoked by isEmpty because map is succeeded by filter (a
 * function that may alter the number of elements emitted in the sequence. If we change it to
 * `filter { true }` we get;
 *
 * ```
 * a
 * b
 * count(): 2
 * a
 * isEmpty: false
 * ```
 *
 * The call to count invoked the map function as many times as the initial number of elements in the
 * sequence. However, the call to isEmpty only invoked map once because we are only checking for the
 * first element. This difference in performance becomes wider as the number of elements in the
 * sequence increases.
 */
internal fun Sequence<*>.isEmpty(): Boolean = !iterator().hasNext()

/**
 * True if the sequence emits at least one element.
 *
 * This is a cheaper version of using `[Sequence.count] > 0` for checking if a sequence is not
 * empty. For more info, see [isEmpty].
 */
internal fun Sequence<*>.isNotEmpty(): Boolean = iterator().hasNext()