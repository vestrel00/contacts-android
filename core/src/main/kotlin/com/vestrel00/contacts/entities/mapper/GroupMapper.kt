package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.Group
import com.vestrel00.contacts.entities.cursor.GroupsCursor

internal class GroupMapper(private val groupsCursor: GroupsCursor) : EntityMapper<Group> {

    override val value: Group
        get() = Group(
            id = groupsCursor.id,

            title = groupsCursor.title,

            readOnly = groupsCursor.readOnly,
            favorites = groupsCursor.favorites,
            autoAdd = groupsCursor.autoAdd,

            account = groupsCursor.account
        )
}
