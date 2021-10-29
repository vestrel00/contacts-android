package contacts.ui.entities

import contacts.core.entities.*

/**
 * Creates instances of [CommonDataEntity].
 */
interface CommonDataEntityFactory<E : CommonDataEntity> {

    /**
     * Returns a new instance of [E].
     */
    fun create(): E
}

object AddressFactory : CommonDataEntityFactory<MutableAddress> {
    override fun create() = MutableAddress()
}

object EmailFactory : CommonDataEntityFactory<MutableEmail> {
    override fun create() = MutableEmail()
}

object EventFactory : CommonDataEntityFactory<MutableEvent> {
    override fun create() = MutableEvent()
}

object ImFactory : CommonDataEntityFactory<MutableIm> {
    override fun create() = MutableIm()
}

object PhoneFactory : CommonDataEntityFactory<MutablePhone> {
    override fun create() = MutablePhone()
}

object RelationFactory : CommonDataEntityFactory<MutableRelation> {
    override fun create() = MutableRelation()
}

object WebsiteFactory : CommonDataEntityFactory<MutableWebsite> {
    override fun create() = MutableWebsite()
}