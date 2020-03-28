package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.Entity

internal interface EntityMapper<out T : Entity> {
    val value: T
}