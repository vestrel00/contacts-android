package contacts.sample

import android.app.Application
import contacts.core.Contacts
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.log.AndroidLogger
import contacts.core.log.LoggerRegistry
import contacts.entities.custom.gender.GenderRegistration
import contacts.entities.custom.handlename.HandleNameRegistration

class SampleApp : Application() {

  // Obviously, this is not the way to provide a singleton when using dependency injection
  // frameworks such as dagger, hilt, or koin. Again, this sample is made to be barebones!
  val contacts: Contacts by lazy(LazyThreadSafetyMode.NONE) {
    Contacts(
      this,
      customDataRegistry = CustomDataRegistry().register(
        GenderRegistration(),
        HandleNameRegistration()
      ),
      loggerRegistry = LoggerRegistry(
        logger = AndroidLogger(),
        redactMessages = !BuildConfig.DEBUG, // Apps must provide it's own way to decide
      ),
    )
  }
}