package contacts.sample.cheatsheet.basics.kotlin

import android.accounts.Account
import android.app.Activity
import contacts.core.Contacts
import contacts.core.RawContactsFields
import contacts.core.entities.RawContact
import contacts.core.equalTo
import contacts.core.isNotNullOrEmpty

class QueryRawContactsActivity : Activity() {

    fun getAllRawContacts(): List<RawContact> = Contacts(this).rawContactsQuery().find()

    fun getAllFavoriteRawContacts(): List<RawContact> = Contacts(this)
        .rawContactsQuery()
        .rawContactsWhere(emptyList(), RawContactsFields.Options.Starred equalTo true)
        .find()

    fun getRawContactsForAccount(account: Account): List<RawContact> =
        Contacts(this)
            .rawContactsQuery()
            .rawContactsWhere(listOf(account), null)
            .find()

    fun getRawContactsForAllGoogleAccounts(): List<RawContact> =
        Contacts(this)
            .rawContactsQuery()
            .rawContactsWhere(emptyList(), RawContactsFields.AccountType equalTo "com.google")
            .find()

    fun getRawContactsThatHasANote(): List<RawContact> =
        Contacts(this)
            .rawContactsQuery()
            .where { Note.Note.isNotNullOrEmpty() }
            .find()

    fun getRawContactById(rawContactId: Long): RawContact? =
        Contacts(this)
            .rawContactsQuery()
            .rawContactsWhere(emptyList(), RawContactsFields.Id equalTo rawContactId)
            // alterAOSPly, .where { RawContact.Id equalTo rawContactId }
            .find()
            .firstOrNull()
}