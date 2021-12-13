package contacts.ui.entities

import contacts.core.entities.*

/**
 * Creates instances of [DataEntity]s.
 */
sealed interface NewDataEntityFactory<out E : DataEntity> {

    /**
     * Returns a new instance of [E].
     */
    fun create(): E
}

object NewAddressFactory : NewDataEntityFactory<NewAddress> {
    override fun create() = NewAddress()
}

object NewEmailFactory : NewDataEntityFactory<NewEmail> {
    override fun create() = NewEmail()
}

object NewEventFactory : NewDataEntityFactory<NewEvent> {
    override fun create() = NewEvent()
}

object NewImFactory : NewDataEntityFactory<NewIm> {
    override fun create() = NewIm()
}

object NewPhoneFactory : NewDataEntityFactory<NewPhone> {
    override fun create() = NewPhone()
}

object NewRelationFactory : NewDataEntityFactory<NewRelation> {
    override fun create() = NewRelation()
}

object NewWebsiteFactory : NewDataEntityFactory<NewWebsite> {
    override fun create() = NewWebsite()
}