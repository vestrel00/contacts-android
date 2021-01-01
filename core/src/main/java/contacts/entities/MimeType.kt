package contacts.entities

import android.provider.ContactsContract.CommonDataKinds
import contacts.custom.CustomCommonDataRegistry

sealed class MimeType(internal val value: String) {

    object Address : MimeType(CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
    object Email : MimeType(CommonDataKinds.Email.CONTENT_ITEM_TYPE)
    object Event : MimeType(CommonDataKinds.Event.CONTENT_ITEM_TYPE)
    object GroupMembership : MimeType(CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
    object Im : MimeType(CommonDataKinds.Im.CONTENT_ITEM_TYPE)
    object Name : MimeType(CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
    object Nickname : MimeType(CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
    object Note : MimeType(CommonDataKinds.Note.CONTENT_ITEM_TYPE)
    object Organization : MimeType(CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
    object Phone : MimeType(CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
    object Photo : MimeType(CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
    object Relation : MimeType(CommonDataKinds.Relation.CONTENT_ITEM_TYPE)
    object SipAddress : MimeType(CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE)
    object Website : MimeType(CommonDataKinds.Website.CONTENT_ITEM_TYPE)
    object Unknown : MimeType("")

    abstract class Custom(value: String) : MimeType(value) {
        // Force concrete implementations to implements equals and hashCode, which can be manually
        // written or provided by being a data class.
        abstract override fun equals(other: Any?): Boolean

        abstract override fun hashCode(): Int
    }

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
                else -> customDataRegistry.customMimeTypeOf(value) ?: Unknown
            }
    }
}
