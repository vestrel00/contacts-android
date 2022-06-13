package contacts.sample.cheatsheet.basics.kotlin

import android.app.Activity
import contacts.core.*
import contacts.core.entities.Contact

class QueryContactsActivity : Activity() {

    fun getAllContacts(): List<Contact> = Contacts(this).broadQuery().find()

    fun getAllContactsWithFavoritesFirstOrderedByDisplayName(): List<Contact> = Contacts(this)
        .broadQuery()
        .orderBy(
            ContactsFields.Options.Starred.desc(),
            ContactsFields.DisplayNamePrimary.asc(ignoreCase = true)
        )
        .find()

    fun getContactsWithEmailOrDisplayNameThatPartiallyMatches(text: String?): List<Contact> =
        Contacts(this)
            .broadQuery()
            .match(BroadQuery.Match.EMAIL)
            .wherePartiallyMatches(text)
            .find()

    fun getContactsWithPhoneOrDisplayNameThatPartiallyMatches(text: String?): List<Contact> =
        Contacts(this)
            .broadQuery()
            .match(BroadQuery.Match.PHONE)
            .wherePartiallyMatches(text)
            .find()

    fun getAllContactsIncludingOnlyDisplayNameAndEmailAddresses(): List<Contact> = Contacts(this)
        .broadQuery()
        .include(
            Fields.Contact.DisplayNamePrimary,
            Fields.Email.Address
        )
        .find()

    fun get25Contacts(): List<Contact> = Contacts(this)
        .broadQuery()
        .limit(25)
        .find()

    fun get25ContactsSkippingTheFirst25(): List<Contact> = Contacts(this)
        .broadQuery()
        .offset(25)
        .limit(25)
        .find()
}