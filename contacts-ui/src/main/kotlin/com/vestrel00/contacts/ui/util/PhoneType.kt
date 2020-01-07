package com.vestrel00.contacts.ui.util

import android.content.res.Resources
import com.vestrel00.contacts.entities.MutablePhone
import com.vestrel00.contacts.entities.Phone

data class PhoneType internal constructor(
    val type: Phone.Type,
    val typeLabel: String,
    val userCustomType: Boolean
) {

    override fun toString(): String = typeLabel

    companion object {

        fun all(resources: Resources): MutableList<PhoneType> = Phone.Type.values()
            .asSequence()
            .map { type -> PhoneType(type, resources.getString(type.typeLabelResource), false) }
            .toMutableList()

        fun from(phone: Phone, resources: Resources): PhoneType =
            from(phone.type, phone.typeLabel(resources))

        fun from(phone: MutablePhone, resources: Resources): PhoneType =
            from(phone.type, phone.typeLabel(resources))

        fun userCustomType(typeLabel: String): PhoneType = from(Phone.Type.CUSTOM, typeLabel)

        private fun from(type: Phone.Type, typeLabel: String): PhoneType = PhoneType(
            type, typeLabel,
            type == Phone.Type.CUSTOM
        )
    }
}