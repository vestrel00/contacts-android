package com.vestrel00.contacts.entities

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Options internal constructor(

    /**
     * The id of this row in the Data table.
     */
    override val id: Long,

    // Contact and RawContacts have distinct Options. Therefore, there is no reference to a
    // rawContactId here because this option may belong to a Contact rather than a RawContact.

    /**
     * True if the contact is starred (favorite). Use this to mark favorite contacts.
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
    val starred: Boolean?,

    /**
     * The number of times a contact has been contacted.
     */
    val timesContacted: Int?,

    /**
     * The last time a contact was contacted.
     */
    val lastTimeContacted: Date?,

    /**
     * URI for a custom ringtone associated with the contact.
     */
    val customRingtone: Uri?,

    /**
     * Whether the contact should always be sent to voicemail.
     */
    val sendToVoicemail: Boolean?

) : Entity, Parcelable {

    fun toMutableOptions() = MutableOptions(
        id = id,

        starred = starred,

        timesContacted = timesContacted,
        lastTimeContacted = lastTimeContacted,

        customRingtone = customRingtone,

        sendToVoicemail = sendToVoicemail
    )
}

@Parcelize
data class MutableOptions internal constructor(

    /**
     * See [Options.id].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val id: Long,

    /**
     * See [Options.starred].
     */
    var starred: Boolean?,

    /**
     * See [Options.timesContacted].
     */
    var timesContacted: Int?,

    /**
     * See [Options.lastTimeContacted].
     */
    var lastTimeContacted: Date?,

    /**
     * See [Options.customRingtone].
     */
    var customRingtone: Uri?,

    /**
     * See [Options.sendToVoicemail].
     */
    var sendToVoicemail: Boolean?

) : Entity, Parcelable {

    constructor() : this(INVALID_ID, null, null, null, null, null)

    internal fun toOptions() = Options(
        id = id,

        starred = starred,

        timesContacted = timesContacted,
        lastTimeContacted = lastTimeContacted,

        customRingtone = customRingtone,

        sendToVoicemail = sendToVoicemail
    )
}