package contacts.entities.mapper

import contacts.entities.Photo

internal class PhotoMapper : EntityMapper<Photo> {

    override val value: Photo
        get() = Photo()
}
