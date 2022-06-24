package contacts.sample.cheatsheet.accounts.kotlin

import android.accounts.Account
import android.app.Activity
import contacts.core.Contacts
import contacts.core.accounts.AccountsLocalRawContactsUpdate
import contacts.core.entities.RawContact

class UpdateLocalRawContactsAccountsActivity : Activity() {

    fun associateLocalRawContactToAccount(
        localRawContact: RawContact, account: Account
    ): AccountsLocalRawContactsUpdate.Result = Contacts(this)
        .accounts()
        .updateLocalRawContactsAccount()
        .addToAccount(account)
        .localRawContacts(localRawContact)
        .commit()
}