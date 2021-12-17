package contacts.core.entities.mapper

import contacts.core.entities.Photo
import contacts.core.entities.cursor.PhotoCursor

internal class PhotoMapper(private val photoCursor: PhotoCursor) : DataEntityMapper<Photo> {

    override val value: Photo
        get() = Photo(
            id = photoCursor.dataId,
            rawContactId = photoCursor.rawContactId,
            contactId = photoCursor.contactId,

            isPrimary = photoCursor.isPrimary,
            isSuperPrimary = photoCursor.isSuperPrimary
        )
}
