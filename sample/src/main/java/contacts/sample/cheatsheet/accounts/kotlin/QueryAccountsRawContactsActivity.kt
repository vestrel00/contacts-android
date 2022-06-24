package contacts.sample.cheatsheet.accounts.kotlin

import android.accounts.Account
import android.app.Activity
import contacts.core.Contacts
import contacts.core.entities.BlankRawContact
import contacts.core.equalTo

class QueryAccountsRawContactsActivity : Activity() {

    fun getAllRawContacts(): List<BlankRawContact> =
        Contacts(this).accounts().queryRawContacts().find()

    fun getRawContactsForAccount(account: Account): List<BlankRawContact> =
        Contacts(this)
            .accounts()
            .queryRawContacts()
            .accounts(account)
            .find()

    fun getRawContactsForAllGoogleAccounts(): List<BlankRawContact> =
        Contacts(this)
            .accounts()
            .queryRawContacts()
            .where { AccountType equalTo "com.google" }
            .find()

    fun getRawContactById(rawContactId: Long): BlankRawContact? =
        Contacts(this)
            .accounts()
            .queryRawContacts()
            .where { Id equalTo rawContactId }
            .find()
            .firstOrNull()
}