package contacts.ui.entities

import contacts.entities.CommonDataEntity
import contacts.entities.MutableEmail
import contacts.entities.MutablePhone

/**
 * Creates instances of [CommonDataEntity].
 */
interface CommonDataEntityFactory<K : CommonDataEntity> {

    /**
     * Returns a new instance of [K].
     */
    fun create(): K
}

object PhoneFactory : CommonDataEntityFactory<MutablePhone> {
    override fun create() = MutablePhone()
}

object EmailFactory : CommonDataEntityFactory<MutableEmail> {
    override fun create() = MutableEmail()
}
