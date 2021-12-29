package contacts.core

/**
 * Indicates indicates that there could be sensitive private user data that could be redacted, for
 * legal purposes. If you are logging contact data in production to remote data centers for analytics
 * or crash reporting, then it is important to redact certain parts of every contact's data.
 *
 * ## DISCLAIMER: This is NOT legal advice!
 *
 * This library is written and maintained purely by software developers with no official education or
 * certifications in any facet of law. Please review the redacted outputs of the APIs and entities
 * within this library with your legal team! This library will not be held liable for any privacy
 * violations!
 *
 * With that out of the way, let's move on to the good stuff =)
 *
 * For example,
 *
 * ```
 * Contact: id=1, email { address="vestrel00@gmail.com" }, phone { number="(555) 555-5555" }, etc
 * ```
 *
 * when redacted,
 *
 * ```
 * Contact: id=1, email { address="*******************" }, phone { number="************" }, etc
 * ```
 *
 * Notice that all characters in private user data are replaced with "*". Redacted strings are not as
 * useful as the non-redacted counterpart. However, we still have the following valuable information;
 *
 * - is the string null or not?
 * - how long is the string?
 *
 * Database row IDs (and typically non-string properties) do not have to be redacted unless they
 * contain sensitive information.
 *
 * The [redactedCopy] function will return an actual copy of the entity, except with sensitive data
 * redacted. In addition to logging, this will allow consumers to do cool things like implementing a
 * redacted contact view! Imagine a button that the user can press to redact everything in their
 * contact form. Cool? Yes! Useful? Maybe? :grin:
 *
 * Redacted copies have [isRedacted] set to true to indicate that data has already been redacted.
 *
 * ## Insert and update operations on redacted entities
 *
 * This library will not stop you from using redacted entities in insert and update APIs. You could
 * build some cool stuff using it. I'll let your imagination take over from here =)
 *
 * ## Developer notes
 *
 * I know that we cannot prevent consumers of this API from violating privacy laws if they really
 * want to. BUT, the library should provide consumers an easy way to be GDPR-compliant! This is not
 * necessary for all libraries to implement but this library deals with sensitive, private user data.
 * Therefore, we need to be extra careful and provide consumers a GDPR-compliant way to log everything
 * in this library!
 */
// We could get fancy and take advantage of recursive generic types
// interface Redactable<T: Redactable<T>>
// However, that would add a lot of ugly "typing" all over the library. Furthermore, recursive
// generic types are really only supported since Kotlin 1.6. We also don't know what versions of
// Java support it. Therefore, since this is a public API, we should avoid exposing recursive
// generic types to consumers.
interface Redactable {

    /**
     * If this is true, this has already been redacted.
     */
    val isRedacted: Boolean

    /**
     * Returns a redacted copy of this entity.
     *
     * Note that it does not matter if this has already been redacted ([isRedacted] is true). A
     * redaction will be performed regardless just to be safe. You can never know what kind of
     * shady stuff people do with data class copy functions. You can never be too careful =)
     */
    fun redactedCopy(): Redactable

    /**
     * Returns a redacted copy of this entity as a String.
     */
    fun redactedString(): String = redactedCopy().toString()

    /**
     * Returns a redacted copy of this string. All characters are replaced with [REDACTED_CHAR].
     */
    fun String.redact(): String = redactString()
}

private const val REDACTED_CHAR = "*"

// FIXME? Preserve spaces, tabs, and newlines?
internal fun String.redactString(): String = REDACTED_CHAR.repeat(length)

internal fun Collection<String>.redactStrings(): List<String> = map { it.redactString() }

internal fun Sequence<String>.redactStrings(): Sequence<String> = map { it.redactString() }

internal fun String.redactStringOrThis(redact: Boolean): String =
    if (redact) redactString() else this

internal fun Collection<String>.redactStringsOrThis(redact: Boolean): List<String> =
    map { it.redactStringOrThis(redact) }

internal fun Sequence<String>.redactStringsOrThis(redact: Boolean): Sequence<String> =
    map { it.redactStringOrThis(redact) }

/**
 * If [redact] is true, returns a redacted copy of this entity. Otherwise, just returns this.
 *
 * This will not undo redaction of an already-redacted entity even if [redact] is false.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Redactable> T.redactedCopyOrThis(redact: Boolean): T =
    if (redact) redactedCopy() as T else this

/**
 * Returns a redacted copy of every element in this collection.
 */
// Unfortunately, this is an unchecked cast because we are not using recursive generic types.
// We should be okay as long as we apply some common sense =)
@Suppress("UNCHECKED_CAST")
fun <T : Redactable> Collection<T>.redactedCopies(): List<T> = map { it.redactedCopy() as T }

/**
 * If [redact] is true, returns a redacted copy of every element in this collection. Otherwise, just
 * returns a copy of this collection as a list.
 *
 * This will not undo redaction of an already-redacted element even if [redact] is false.
 */
fun <T : Redactable> Collection<T>.redactedCopiesOrThis(redact: Boolean): List<T> =
    map { it.redactedCopyOrThis(redact) }

/**
 * Returns a redacted copy of every element in this sequence.
 */
// Unfortunately, this is an unchecked cast because we are not using recursive generic types.
// We should be okay as long as we apply some common sense =)
@Suppress("UNCHECKED_CAST")
fun <T : Redactable> Sequence<T>.redactedCopies(): Sequence<T> = map { it.redactedCopy() as T }

/**
 * If [redact] is true, returns a redacted copy of every element in this sequence. Otherwise, just
 * returns this.
 *
 * This will not undo redaction of an already-redacted element even if [redact] is false.
 */
fun <T : Redactable> Sequence<T>.redactedCopiesOrThis(redact: Boolean): Sequence<T> =
    map { it.redactedCopyOrThis(redact) }

/**
 * Returns a redacted copy of every key in this map.
 */
// Unfortunately, this is an unchecked cast because we are not using recursive generic types.
// We should be okay as long as we apply some common sense =)
@Suppress("UNCHECKED_CAST")
fun <K : Redactable, V> Map<K, V>.redactedKeys(): Map<K, V> = entries.associate {
    (it.key.redactedCopy() as K) to it.value
}

/**
 * If [redact] is true, returns a redacted copy of every key in this map. Otherwise, just returns
 * this.
 *
 * This will not undo redaction of an already-redacted key even if [redact] is false.
 */
fun <K : Redactable, V> Map<K, V>.redactedKeysOrThis(redact: Boolean): Map<K, V> =
    if (redact) redactedKeys() else this

/**
 * Returns a redacted copy of every value in this map.
 */
// Unfortunately, this is an unchecked cast because we are not using recursive generic types.
// We should be okay as long as we apply some common sense =)
@Suppress("UNCHECKED_CAST")
fun <K, V : Redactable> Map<K, V>.redactedValues(): Map<K, V> = entries.associate {
    it.key to (it.value.redactedCopy() as V)
}

/**
 * If [redact] is true, returns a redacted copy of every value in this map. Otherwise, just returns
 * this.
 *
 * This will not undo redaction of an already-redacted value even if [redact] is false.
 */
fun <K, V : Redactable> Map<K, V>.redactedValuesOrThis(redact: Boolean): Map<K, V> =
    if (redact) redactedValues() else this

/**
 * Returns a redacted copy of every key and value in this map.
 */
// Unfortunately, this is an unchecked cast because we are not using recursive generic types.
// We should be okay as long as we apply some common sense =)
@Suppress("UNCHECKED_CAST")
fun <K : Redactable, V : Redactable> Map<K, V>.redactedKeysAndValues(): Map<K, V> =
    entries.associate {
        (it.key.redactedCopy() as K) to (it.value.redactedCopy() as V)
    }

/**
 * If [redact] is true, returns a redacted copy of every key and value in this map. Otherwise, just
 * returns this.
 *
 * This will not undo redaction of an already-redacted key or value even if [redact] is false.
 */
fun <K : Redactable, V : Redactable> Map<K, V>.redactedKeysAndValuesOrThis(redact: Boolean): Map<K, V> =
    if (redact) redactedKeysAndValues() else this

/* We should not expose extensions to consumers that apply to non-library interface/class signatures
   unless we absolutely have no choice.

@Suppress("UNCHECKED_CAST")
fun <K, V> Map<K, V>.redactedCopies(): Map<K, V> = entries.associate {
    it.key.redactIfRedactable() to it.value.redactIfRedactable()
}

@Suppress("UNCHECKED_CAST")
private fun <T> T.redactIfRedactable(): T = if (this is Redactable) redactedCopy() as T else this
 */