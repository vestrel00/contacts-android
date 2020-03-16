package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.Entity

internal interface EntityMapper<out K: Entity, out V: Entity> {

    val toImmutable: K

    val toMutable: V
}