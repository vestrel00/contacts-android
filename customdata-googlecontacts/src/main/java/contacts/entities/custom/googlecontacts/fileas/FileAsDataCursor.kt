package contacts.entities.custom.googlecontacts.fileas

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomDataCursor
import contacts.entities.custom.googlecontacts.GoogleContactsFields
import contacts.entities.custom.googlecontacts.FileAsField

internal class FileAsDataCursor(cursor: Cursor, includeFields: Set<FileAsField>) :
    AbstractCustomDataCursor<FileAsField>(cursor, includeFields) {

    val name: String? by string(GoogleContactsFields.FileAs.Name)
}