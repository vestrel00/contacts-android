package contacts.test.entities

import contacts.core.entities.custom.CustomDataRegistry

internal class TestDataRegistration : CustomDataRegistry.EntryRegistration {

    /**
     * Registers [TestData] components to the given [customDataRegistry], enabling queries, inserts,
     * updates, and deletes of [TestData].
     */
    override fun registerTo(customDataRegistry: CustomDataRegistry) {
        customDataRegistry.register(TestDataEntry())
    }
}