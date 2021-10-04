package contacts.core.entities.mapper

import contacts.core.entities.Photo

internal class PhotoMapper : EntityMapper<Photo> {

    override val value: Photo
        get() = Photo()
}
