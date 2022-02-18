package contacts.sample

import android.app.Application
import contacts.core.Contacts
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.log.AndroidLogger
import contacts.entities.custom.gender.GenderRegistration
import contacts.entities.custom.googlecontacts.GoogleContactsRegistration
import contacts.entities.custom.handlename.HandleNameRegistration
import contacts.entities.custom.pokemon.PokemonRegistration
import contacts.entities.custom.rpg.RpgRegistration

class SampleApp : Application() {

    // Obviously, this is not the way to provide a singleton when using dependency injection
    // frameworks such as dagger, hilt, or koin. Again, this sample is made to be barebones!
    val contacts: Contacts by lazy(LazyThreadSafetyMode.NONE) {
        Contacts(
            this,
            logger = AndroidLogger(),
            customDataRegistry = CustomDataRegistry().register(
                GenderRegistration(),
                GoogleContactsRegistration(),
                HandleNameRegistration(),
                PokemonRegistration(),
                RpgRegistration()
            )
        )
    }
}