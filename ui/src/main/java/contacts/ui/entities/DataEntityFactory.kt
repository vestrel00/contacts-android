package contacts.ui.entities

import contacts.core.entities.*

/**
 * Creates instances of [DataEntity].
 */
interface DataEntityFactory<E : DataEntity> {

    /**
     * Returns a new instance of [E].
     */
    fun create(): E
}

object AddressFactory : DataEntityFactory<MutableAddress> {
    override fun create() = MutableAddress()
}

object EmailFactory : DataEntityFactory<MutableEmail> {
    override fun create() = MutableEmail()
}

object EventFactory : DataEntityFactory<MutableEvent> {
    override fun create() = MutableEvent()
}

object ImFactory : DataEntityFactory<MutableIm> {
    override fun create() = MutableIm()
}

object PhoneFactory : DataEntityFactory<MutablePhone> {
    override fun create() = MutablePhone()
}

object RelationFactory : DataEntityFactory<MutableRelation> {
    override fun create() = MutableRelation()
}

object WebsiteFactory : DataEntityFactory<MutableWebsite> {
    override fun create() = MutableWebsite()
}