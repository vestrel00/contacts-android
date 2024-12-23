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

data object NewAddressFactory : NewDataEntityFactory<NewAddress> {
    override fun create() = NewAddress()
}

data object NewEmailFactory : NewDataEntityFactory<NewEmail> {
    override fun create() = NewEmail()
}

data object NewEventFactory : NewDataEntityFactory<NewEvent> {
    override fun create() = NewEvent()
}

@Suppress("Deprecation")
data object NewImFactory : NewDataEntityFactory<NewIm> {
    override fun create() = NewIm()
}

data object NewPhoneFactory : NewDataEntityFactory<NewPhone> {
    override fun create() = NewPhone()
}

data object NewRelationFactory : NewDataEntityFactory<NewRelation> {
    override fun create() = NewRelation()
}

data object NewWebsiteFactory : NewDataEntityFactory<NewWebsite> {
    override fun create() = NewWebsite()
}