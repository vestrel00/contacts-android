package contacts.core.entities

import android.provider.ContactsContract.CommonDataKinds
import contacts.core.entities.custom.CustomDataException
import contacts.core.entities.custom.CustomDataRegistry

/**
 * Determines the type of a particular row in the Data table.
 *
 * Consumers should not have to know about this unless they are integrating custom data.
 */
// This, and all of its usages should actually be internal. However, it is not possible to do so
// because this signature is declared in a public interface...
// Not using sealed interface here because we want to nest internal objects.
sealed class MimeType {

    abstract val value: String

    internal data object Address : MimeType() {
        override val value = CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
    }

    internal data object Email : MimeType() {
        override val value = CommonDataKinds.Email.CONTENT_ITEM_TYPE
    }

    internal data object Event : MimeType() {
        override val value = CommonDataKinds.Event.CONTENT_ITEM_TYPE
    }

    internal data object GroupMembership : MimeType() {
        override val value = CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
    }

    internal data object Im : MimeType() {
        override val value = CommonDataKinds.Im.CONTENT_ITEM_TYPE
    }

    internal data object Name : MimeType() {
        override val value = CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
    }

    internal data object Nickname : MimeType() {
        override val value = CommonDataKinds.Nickname.CONTENT_ITEM_TYPE
    }

    internal data object Note : MimeType() {
        override val value = CommonDataKinds.Note.CONTENT_ITEM_TYPE
    }

    internal data object Organization : MimeType() {
        override val value = CommonDataKinds.Organization.CONTENT_ITEM_TYPE
    }

    internal data object Phone : MimeType() {
        override val value = CommonDataKinds.Phone.CONTENT_ITEM_TYPE
    }

    internal data object Photo : MimeType() {
        override val value = CommonDataKinds.Photo.CONTENT_ITEM_TYPE
    }

    internal data object Relation : MimeType() {
        override val value = CommonDataKinds.Relation.CONTENT_ITEM_TYPE
    }

    internal data object SipAddress : MimeType() {
        override val value = CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE
    }

    internal data object Website : MimeType() {
        override val value = CommonDataKinds.Website.CONTENT_ITEM_TYPE
    }

    internal data object Unknown : MimeType() {
        override val value = ""
    }

    /**
     * The mime type of a custom data entity.
     */
    abstract class Custom : MimeType()

    internal companion object {

        fun fromValue(value: String?, customDataRegistry: CustomDataRegistry): MimeType =
            when (value) {
                Address.value -> Address
                Email.value -> Email
                Event.value -> Event
                GroupMembership.value -> GroupMembership
                Im.value -> Im
                Name.value -> Name
                Nickname.value -> Nickname
                Note.value -> Note
                Organization.value -> Organization
                Phone.value -> Phone
                Photo.value -> Photo
                Relation.value -> Relation
                SipAddress.value -> SipAddress
                Website.value -> Website
                null -> Unknown
                else -> try {
                    customDataRegistry.entryOf(value).mimeType
                } catch (cde: CustomDataException) {
                    // We may encounter custom data but not have a custom data entry for it.
                    Unknown
                }
            }
    }
}
