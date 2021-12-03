package contacts.core.entities.mapper

import contacts.core.entities.Photo

internal object PhotoMapper : DataEntityMapper<Photo> {

    override val value: Photo
        get() = Photo()
}
