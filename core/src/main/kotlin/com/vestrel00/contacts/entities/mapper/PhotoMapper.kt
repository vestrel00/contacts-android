package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.Photo

internal class PhotoMapper : EntityMapper<Photo> {

    override val value: Photo
        get() = Photo()
}
