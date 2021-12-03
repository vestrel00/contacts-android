package contacts.ui.entities

import contacts.core.entities.*

/**
 * Creates instances of [DataEntity].
 */
sealed interface MutableDataEntityFactory<E : MutableDataEntity> {

    /**
     * Returns a new instance of [E].
     */
    fun create(): E
}

object MutableAddressFactory : MutableDataEntityFactory<MutableAddress> {
    override fun create() = MutableAddress()
}

object MutableEmailFactory : MutableDataEntityFactory<MutableEmail> {
    override fun create() = MutableEmail()
}

object MutableEventFactory : MutableDataEntityFactory<MutableEvent> {
    override fun create() = MutableEvent()
}

object MutableImFactory : MutableDataEntityFactory<MutableIm> {
    override fun create() = MutableIm()
}

object MutablePhoneFactory : MutableDataEntityFactory<MutablePhone> {
    override fun create() = MutablePhone()
}

object MutableRelationFactory : MutableDataEntityFactory<MutableRelation> {
    override fun create() = MutableRelation()
}

object MutableWebsiteFactory : MutableDataEntityFactory<MutableWebsite> {
    override fun create() = MutableWebsite()
}