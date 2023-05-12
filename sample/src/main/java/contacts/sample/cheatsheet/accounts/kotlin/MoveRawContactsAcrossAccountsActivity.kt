package contacts.sample.cheatsheet.accounts.kotlin

import android.accounts.Account
import android.app.Activity
import contacts.core.Contacts
import contacts.core.accounts.MoveRawContactsAcrossAccounts
import contacts.core.entities.RawContact

class MoveRawContactsAcrossAccountsActivity : Activity() {

    fun moveRawContactToAccount(
        rawContact: RawContact, account: Account
    ): MoveRawContactsAcrossAccounts.Result = Contacts(this)
        .accounts()
        .move()
        .rawContactsTo(account, rawContact)
        .commit()
}