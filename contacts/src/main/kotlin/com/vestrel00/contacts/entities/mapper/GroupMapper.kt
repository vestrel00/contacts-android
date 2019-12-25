package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.MutableGroup
import com.vestrel00.contacts.entities.cursor.GroupCursor

internal class GroupMapper(private val groupCursor: GroupCursor) {

    val group: MutableGroup
        get() = MutableGroup(
            id = groupCursor.id,

            title = groupCursor.title,

            readOnly = groupCursor.readOnly,
            favorites = groupCursor.favorites,
            autoAdd = groupCursor.autoAdd,

            account = groupCursor.account
        )
}
