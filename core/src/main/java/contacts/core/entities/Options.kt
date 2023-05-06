package contacts.core.entities

import android.net.Uri
import kotlinx.parcelize.Parcelize

/**
 * [Entity] that holds options data modeling columns in the Contacts table.
 *
 * As per documentation in [android.provider.ContactsContract.Contacts],
 *
 * > Only certain columns of Contact are modifiable:
 * > - ContactsContract.ContactOptionsColumns.STARRED
 * > - ContactsContract.ContactOptionsColumns.CUSTOM_RINGTONE
 * > - ContactsContract.ContactOptionsColumns.SEND_TO_VOICEMAIL
 * > Changing any of these columns on the Contact also changes them on all constituent raw contacts.
 */
sealed interface OptionsEntity : Entity {

    // Contact and RawContacts have distinct Options. Therefore, there is no reference to a
    // contact or rawContactId here because this option may belong to a Contact rather than a
    // RawContact.

    /**
     * True if the contact or raw contact is starred (favorite). Use this to mark favorite contacts.
     *
     * ## Starred in Android & Favorites Group Membership
     *
     * When a Contact is starred, the Contacts Provider automatically adds a group membership to the
     * favorites group for all RawContacts linked to the Contact. Setting the Contact starred to
     * false removes all group memberships to the favorites group.
     *
     * The Contact's "starred" value is interdependent with memberships to the favorites group.
     * Adding a membership to the favorites group results in starred being set to true. Removing the
     * membership sets it to false. This behavior can cause bugs and increased code complexity for
     * API users. Therefore, the update APIs provided in this library overshadows membership
     * changes to the favorites group with the value of [starred]. In other words, the only way to
     * star or favorite Contacts and RawContacts is to set the value of [starred].
     *
     * Raw contacts that are not associated with an account may (or may not) have any group
     * memberships. Even though these RawContacts may not have a membership to a favorites group,
     * they may still be "starred" (favorited), which is not dependent on the existence of a
     * favorites group membership.
     */
    val starred: Boolean?

    /**
     * URI for a custom ringtone associated with the contact or raw contact.
     */
    val customRingtone: Uri?

    /**
     * Whether the contact or raw contact should always be sent to voicemail.
     */
    val sendToVoicemail: Boolean?

    /* Deprecated in API 29 - contains useless value for all Android versions from the Play store.
    /**
     * The number of times a contact has been contacted.
     */
    val timesContacted: Int?,

    /**
     * The last time a contact was contacted.
     */
    val lastTimeContacted: Date?,
     */

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(starred, customRingtone, sendToVoicemail)

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): OptionsEntity
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from OptionsEntity, there is only one interface that extends it; MutableOptionsEntity.
 *
 * The MutableOptionsEntity interface is used for library constructs that require an OptionsEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutableOptions and NewOptions. With this, we can create constructs that can
 * keep a reference to MutableOptions(s) or NewOptions(s) through the MutableOptionsEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewOptionsEntity, ExistingOptionsEntity, and
 * ImmutableOptionsEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [OptionsEntity]. `
 */
sealed interface MutableOptionsEntity : OptionsEntity, MutableEntity {

    override var starred: Boolean?
    override var customRingtone: Uri?
    override var sendToVoicemail: Boolean?

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutableOptionsEntity
}

/**
 * An existing immutable [OptionsEntity].
 */
@Parcelize
data class Options internal constructor(

    /**
     * The id of this row in the Contacts, RawContacts, or Data table.
     */
    override val id: Long,

    override val starred: Boolean?,
    override val customRingtone: Uri?,
    override val sendToVoicemail: Boolean?,

    override val isRedacted: Boolean

) : OptionsEntity, ExistingEntity, ImmutableEntityWithMutableType<MutableOptions> {

    override fun mutableCopy() = MutableOptions(
        id = id,

        starred = starred,
        customRingtone = customRingtone,
        sendToVoicemail = sendToVoicemail,

        isRedacted = isRedacted
    )

    // Nothing to redact.
    override fun redactedCopy() = copy(isRedacted = true)
}

/**
 * An existing mutable [OptionsEntity].
 */
@Parcelize
data class MutableOptions internal constructor(

    /**
     * The id of this row in the Contacts, RawContacts, or Data table.
     */
    override val id: Long,

    override var starred: Boolean?,
    override var customRingtone: Uri?,
    override var sendToVoicemail: Boolean?,

    override val isRedacted: Boolean

) : OptionsEntity, ExistingEntity, MutableOptionsEntity {

    // Nothing to redact.
    override fun redactedCopy() = copy(isRedacted = true)
}

/**
 * A new mutable [OptionsEntity].
 */
@Parcelize
data class NewOptions @JvmOverloads constructor(

    override var starred: Boolean? = null,
    override var customRingtone: Uri? = null,
    override var sendToVoicemail: Boolean? = null,

    override val isRedacted: Boolean = false

) : OptionsEntity, NewEntity, MutableOptionsEntity {

    // Nothing to redact.
    override fun redactedCopy() = copy(isRedacted = true)
}