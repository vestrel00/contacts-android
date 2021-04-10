package contacts.sample

import android.app.Application
import contacts.entities.custom.GlobalCustomDataRegistry
import contacts.entities.custom.gender.GenderRegistration

class SampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Register all custom data recognized by the application using the global registry. All
        // instances of the Contacts API, entities, and extension functions will default to using
        // this global registry. You may provide your own custom data registry when creating your
        // Contacts API instance(s). However, I recommend just using this global registry to avoid
        // having to pass a registry instance to extension functions.
        GlobalCustomDataRegistry.register(
            GenderRegistration()
        )
    }
}