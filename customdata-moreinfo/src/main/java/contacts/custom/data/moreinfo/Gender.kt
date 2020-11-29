package contacts.custom.data.moreinfo

import contacts.entities.CommonDataEntity
import contacts.entities.MimeType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Gender internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,
    
    val sex: 

): CommonDataEntity {
    override val mimeType: MimeType
        get() = TODO("Not yet implemented")

    override val isBlank: Boolean
        get() = TODO("Not yet implemented")
}