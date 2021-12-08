package contacts.core.entities

import android.net.Uri
import kotlinx.parcelize.Parcelize

/**
 * [Entity] that holds options data modeling columns in the Contacts table.
 */
sealed interface OptionsEntity : Entity {

    // Contact and RawContacts have distinct Options. Therefore, there is no reference to a
    // contact or rawContactId here because this option may belong to a Contact rather than a
    // RawContact.

    /**
     * True if the contact or raw contact is starred (favorite). Use this to mark favorite contacts.
     *
     * Setting this to true results in the addition of a group membership to the favorites group of
     * the associated account. Setting it to false removes that membership. The inverse works too.
     * Adding a group membership to the favorites group results in this being set to true. Removing
     * the membership sets it to false.
     *
     * When there are no accounts, there are also no groups and group memberships that can exist.
     * Even though the favorites group does not exist, contacts may still be starred. When an
     * account is added, all of the starred contacts also gain a membership to the favorites group.
     * Therefore, this should be the preferred way of marking contacts as favorites instead of
     * group membership manipulation.
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
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary! That is when there are two separate concrete
 * types that we want to perform an operation on.
 *
 * This is why there are no interfaces for NewOptionsEntity, ExistingOptionsEntity,
 * ImmutableOptionsEntity, and MutableNewOptionsEntity. There are currently no library functions
 * that exist that need them.
 *
 * Please update this documentation if new abstractions are created.
 */

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
    override val sendToVoicemail: Boolean?

) : OptionsEntity, ExistingEntity, ImmutableEntityWithMutableType<MutableOptions> {

    override fun mutableCopy() = MutableOptions(
        id = id,

        starred = starred,
        customRingtone = customRingtone,
        sendToVoicemail = sendToVoicemail
    )
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
    override var sendToVoicemail: Boolean?

) : OptionsEntity, ExistingEntity, MutableEntity

/**
 * A new mutable [OptionsEntity].
 */
@Parcelize
data class NewOptions internal constructor(

    override var starred: Boolean?,
    override var customRingtone: Uri?,
    override var sendToVoicemail: Boolean?

) : OptionsEntity, NewEntity, MutableEntity {

    // For consumer use.
    constructor() : this(null, null, null)
}