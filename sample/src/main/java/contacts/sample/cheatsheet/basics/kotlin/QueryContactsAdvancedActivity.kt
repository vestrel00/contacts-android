package contacts.sample.cheatsheet.basics.kotlin

import android.accounts.Account
import android.app.Activity
import contacts.core.*
import contacts.core.entities.Contact
import contacts.core.util.lookupKeyIn

class QueryContactsAdvancedActivity : Activity() {

    fun getContactById(contactId: Long): Contact? = Contacts(this)
        .query()
        .where { Contact.Id equalTo contactId }
        .find()
        .firstOrNull()

    fun getContactByLookupKey(lookupKey: String): List<Contact> = Contacts(this)
        .query()
        .where { Contact.lookupKeyIn(lookupKey) }
        .find()

    fun getAllContactsForAGoogleAccount(): List<Contact> = Contacts(this)
        .query()
        .accounts(Account("email@gmail.com", "com.google"))
        .find()

    fun getOnlyFavoriteContacts(): List<Contact> = Contacts(this)
        .query()
        .where {
            Contact.Options.Starred equalTo true
        }
        .find()

    fun getContactsPartiallyMatchingDisplayName(): List<Contact> = Contacts(this)
        .query()
        .where {
            Contact.DisplayNamePrimary contains "alex"
        }
        .find()

    fun getContactsWithAtLeastOneGmailEmail(): List<Contact> = Contacts(this)
        .query()
        .where {
            Email.Address endsWith "@gmail.com"
        }
        .find()

    fun getContactsWithAtLeastOnePhoneNumber(): List<Contact> = Contacts(this)
        .query()
        .where {
            Phone.Number.isNotNullOrEmpty()
            // or Contact.HasPhoneNumber equalTo true
        }
        .find()

    fun getContactsWithAtLeastOnePhoneNumberAndEmail(): List<Contact> = Contacts(this)
        .query()
        .where {
            Phone.Number.isNotNullOrEmpty() and Email.Address.isNotNullOrEmpty()
            // or Contact.HasPhoneNumber equalTo true and Email.Address.isNotNullOrEmpty()
        }
        .find()
}