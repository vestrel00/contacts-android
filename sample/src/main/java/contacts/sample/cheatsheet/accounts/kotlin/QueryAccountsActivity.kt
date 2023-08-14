package contacts.sample.cheatsheet.accounts.kotlin

import android.accounts.Account
import android.app.Activity
import contacts.core.Contacts

class QueryAccountsActivity : Activity() {

    fun getAllAccounts(): List<Account> = Contacts(this).accounts().query().find()

    fun getAllGoogleAccounts(): List<Account> = Contacts(this)
        .accounts()
        .query()
        .withTypes("com.google")
        .find()
}