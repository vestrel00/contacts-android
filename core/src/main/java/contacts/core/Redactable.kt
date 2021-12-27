package contacts.core

/**
 * Indicates that there could be sensitive private user data that could be redacted, for legal
 * purposes. If you are logging contact data in production to remote data centers for analytics or
 * crash reporting, then it is important to redact certain parts of every contact's data.
 *
 * ## DISCLAIMER: This is NOT legal advice!
 *
 * This library is written and maintained by pure software developers with no official education or
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
 * Notice that we are simply replacing all characters in the string with "*". This still gives us
 * valuable information such as;
 *
 * - is the string null or not?
 * - how long is the string?
 *
 * IDs and (typically non-string properties) do not have to be redacted unless they contain
 * sensitive information.
 *
 * The [redactedCopy] function will return an actual copy of the entity, except with sensitive data
 * redacted. In addition to logging, this will allow consumers to do cool things like implementing a
 * redacted contact view!
 *
 * Redacted copies have [isRedacted] set to true to indicate that data has already been redacted.
 *
 * ## Insert and update operations on redacted copies
 *
 * This library will not stop you from using redacted copies in insert and update APIs. You could
 * build some cool stuff using it. I'll let your imagination take over from here =)
 *
 * ## Developer notes
 *
 * I know that we cannot prevent consumers of this API from violating privacy laws if they really
 * want to. BUT, the library should provide consumers an easy way to be GDPR-compliant! This is not
 * necessary for all libraries to implement but this library deals with sensitive, private data.
 * Therefore, we need to be extra careful and provide consumers a GDPR-compliant way to log
 * everything in this library!
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
     */
    fun redactedCopy(): Redactable

    /**
     * Returns a redacted copy of this string. All characters are replaced with [REDACTED_CHAR].
     */
    fun String.redact(): String = redactString()
}

private const val REDACTED_CHAR = "*"

// FIXME? Preserve spaces, tabs, and newlines?
internal fun String.redactString(): String = REDACTED_CHAR.repeat(length)

/**
 * Returns a redacted copy of ever redactable element in this collection.
 */
// Unfortunately, this is an unchecked cast because we are not using recursive generic types.
// We should be okay as long as we apply some common sense =)
@Suppress("UNCHECKED_CAST")
fun <T : Redactable> Collection<T>.redactableCopies(): List<T> = map { it.redactedCopy() as T }

/**
 * Returns a redacted copy of ever redactable element in this sequence.
 */
// Unfortunately, this is an unchecked cast because we are not using recursive generic types.
// We should be okay as long as we apply some common sense =)
@Suppress("UNCHECKED_CAST")
fun <T : Redactable> Sequence<T>.redactableCopies(): Sequence<T> = map { it.redactedCopy() as T }
