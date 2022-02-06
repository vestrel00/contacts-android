package contacts.entities.custom.googlecontacts

import contacts.core.AbstractCustomDataField
import contacts.core.AbstractCustomDataField.ColumnName
import contacts.core.AbstractCustomDataFieldSet
import contacts.core.entities.MimeType

object GoogleContactsFields : AbstractCustomDataFieldSet<GoogleContactsField>() {

    @JvmField
    val FileAs = FileAsFields()

    @JvmField
    val UserDefined = UserDefinedFields()

    override val all: Set<GoogleContactsField> = FileAs.all + UserDefined.all

    override val forMatching: Set<GoogleContactsField> =
        FileAs.forMatching + UserDefined.forMatching
}

sealed class GoogleContactsField(columnName: ColumnName) : AbstractCustomDataField(columnName)

data class FileAsField internal constructor(private val columnName: ColumnName) :
    GoogleContactsField(columnName) {

    override val customMimeType: MimeType.Custom = GoogleContactsMimeType.FileAs
}

class FileAsFields internal constructor() : AbstractCustomDataFieldSet<FileAsField>() {

    @JvmField
    val Name = FileAsField(ColumnName.DATA)

    override val all: Set<FileAsField> = setOf(Name)

    override val forMatching: Set<FileAsField> = setOf(Name)
}

data class UserDefinedField internal constructor(private val columnName: ColumnName) :
    GoogleContactsField(columnName) {

    override val customMimeType: MimeType.Custom = GoogleContactsMimeType.UserDefined
}

class UserDefinedFields internal constructor() : AbstractCustomDataFieldSet<UserDefinedField>() {

    // Google chose to use the "TYPE" (data2) for the custom field name.
    @JvmField
    val Field = UserDefinedField(ColumnName.TYPE)

    // Google chose to use the data1 for the custom field value.
    @JvmField
    val Label = UserDefinedField(ColumnName.DATA)

    override val all: Set<UserDefinedField> = setOf(Field, Label)

    override val forMatching: Set<UserDefinedField> = setOf(Field, Label)
}