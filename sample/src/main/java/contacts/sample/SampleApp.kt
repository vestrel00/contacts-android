package contacts.sample

import android.app.Application
import contacts.entities.custom.GlobalCustomDataRegistry
import contacts.entities.custom.gender.GenderRegistration
import contacts.entities.custom.handlename.HandleNameRegistration

class SampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        /*
         * Register all custom data recognized by the application using the global registry. All
         * instances of the Contacts API, entities, and extension functions will default to using
         * this global registry. You may provide your own custom data registry when creating your
         * Contacts API instance(s). However, it is recommended to use this global registry to avoid
         * having to pass a registry instance to extension functions.
         *
         * NOTE that these custom data provided in this library are NOT SYNCED even if they belong
         * to a RawContact that is linked to an Account. Read more in the DEV_NOTES
         * "Custom Data / MimeTypes" section.
         */
        GlobalCustomDataRegistry.register(
            GenderRegistration(),
            HandleNameRegistration()
        )
    }
}