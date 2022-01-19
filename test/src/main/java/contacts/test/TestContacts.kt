package contacts.test

import android.content.Context
import contacts.core.BroadQuery
import contacts.core.Contacts
import contacts.core.ContactsPermissions
import contacts.core.Delete
import contacts.core.Insert
import contacts.core.Query
import contacts.core.Update
import contacts.core.accounts.Accounts
import contacts.core.data.Data
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.groups.Groups
import contacts.core.log.EmptyLogger
import contacts.core.log.Logger
import contacts.core.log.LoggerRegistry
import contacts.core.profile.Profile
import contacts.test.entities.TestDataRegistration

@JvmOverloads
@Suppress("FunctionName")
fun TestContacts(
    context: Context,
    customDataRegistry: CustomDataRegistry = CustomDataRegistry()
): Contacts = TestContacts(Contacts(context, customDataRegistry = customDataRegistry)).also {
    customDataRegistry.register(TestDataRegistration())
}

object TestContactsFactory {

    @JvmStatic
    @JvmOverloads
    fun create(
        context: Context,
        customDataRegistry: CustomDataRegistry = CustomDataRegistry()
    ): Contacts = TestContacts(context, customDataRegistry)
}

// Note that we cannot use "by" to delegate calls to the internal query because function calls will
// return the internal contacts instance instead of this test instance.
// class TestContacts(private val contacts: Contacts): Contacts by contacts

/**
 * TODO document this
 */
private class TestContacts(private val contactsApi: Contacts) : Contacts {

    override fun query(): Query = TestQuery(contactsApi.query(), contactsApi)

    override fun broadQuery(): BroadQuery {
        TODO("Not yet implemented")
    }

    override fun insert(): Insert {
        TODO("Not yet implemented")
    }

    override fun update(): Update {
        TODO("Not yet implemented")
    }

    override fun delete(): Delete {
        TODO("Not yet implemented")
    }

    override fun data(): Data {
        TODO("Not yet implemented")
    }

    override fun groups(): Groups {
        TODO("Not yet implemented")
    }

    override fun profile(): Profile {
        TODO("Not yet implemented")
    }

    override fun accounts() = accounts(false)

    override fun accounts(isProfile: Boolean): Accounts {
        TODO("Not yet implemented")
    }

    override val permissions = contactsApi.permissions

    override val accountsPermissions = contactsApi.accountsPermissions

    override val applicationContext = contactsApi.applicationContext

    override var loggerRegistry = contactsApi.loggerRegistry

    override val customDataRegistry = contactsApi.customDataRegistry

    override val apiListenerRegistry = contactsApi.apiListenerRegistry
}