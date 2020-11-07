package com.vestrel00.contacts.entities

import android.provider.ContactsContract.CommonDataKinds

enum class MimeType(internal val value: String) {

    ADDRESS(CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE),
    EMAIL(CommonDataKinds.Email.CONTENT_ITEM_TYPE),
    EVENT(CommonDataKinds.Event.CONTENT_ITEM_TYPE),
    GROUP_MEMBERSHIP(CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE),
    IM(CommonDataKinds.Im.CONTENT_ITEM_TYPE),
    NAME(CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE),
    NICKNAME(CommonDataKinds.Nickname.CONTENT_ITEM_TYPE),
    NOTE(CommonDataKinds.Note.CONTENT_ITEM_TYPE),
    ORGANIZATION(CommonDataKinds.Organization.CONTENT_ITEM_TYPE),
    PHONE(CommonDataKinds.Phone.CONTENT_ITEM_TYPE),
    PHOTO(CommonDataKinds.Photo.CONTENT_ITEM_TYPE),
    RELATION(CommonDataKinds.Relation.CONTENT_ITEM_TYPE),
    SIP_ADDRESS(CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE),
    WEBSITE(CommonDataKinds.Website.CONTENT_ITEM_TYPE),
    UNKNOWN("");

    companion object {

        fun fromValue(value: String?): MimeType = values().find { it.value == value } ?: UNKNOWN
    }
}
