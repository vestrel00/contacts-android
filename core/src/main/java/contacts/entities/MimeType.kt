package contacts.entities

import android.provider.ContactsContract.CommonDataKinds
import contacts.entities.custom.CustomCommonDataRegistry

sealed class MimeType {

    abstract val value: String

    object Address : MimeType() {
        override val value = CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
    }

    object Email : MimeType() {
        override val value = CommonDataKinds.Email.CONTENT_ITEM_TYPE
    }

    object Event : MimeType() {
        override val value = CommonDataKinds.Event.CONTENT_ITEM_TYPE
    }

    object GroupMembership : MimeType() {
        override val value = CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
    }

    object Im : MimeType() {
        override val value = CommonDataKinds.Im.CONTENT_ITEM_TYPE
    }

    object Name : MimeType() {
        override val value = CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
    }

    object Nickname : MimeType() {
        override val value = CommonDataKinds.Nickname.CONTENT_ITEM_TYPE
    }

    object Note : MimeType() {
        override val value = CommonDataKinds.Note.CONTENT_ITEM_TYPE
    }

    object Organization : MimeType() {
        override val value = CommonDataKinds.Organization.CONTENT_ITEM_TYPE
    }

    object Phone : MimeType() {
        override val value = CommonDataKinds.Phone.CONTENT_ITEM_TYPE
    }

    object Photo : MimeType() {
        override val value = CommonDataKinds.Photo.CONTENT_ITEM_TYPE
    }

    object Relation : MimeType() {
        override val value = CommonDataKinds.Relation.CONTENT_ITEM_TYPE
    }

    object SipAddress : MimeType() {
        override val value = CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE
    }

    object Website : MimeType() {
        override val value = CommonDataKinds.Website.CONTENT_ITEM_TYPE
    }

    object Unknown : MimeType() {
        override val value = ""
    }

    abstract class Custom : MimeType()

    internal companion object {

        fun fromValue(value: String?, customDataRegistry: CustomCommonDataRegistry): MimeType =
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
                else -> customDataRegistry.mimeTypeOf(value) ?: Unknown
            }
    }
}
