package contacts.sample.cheatsheet.basics.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.LookupQuery
import contacts.core.entities.Contact

class QueryContactsByLookupKeyActivity : Activity() {

    fun getContactByLookupKey(lookupKey: String): Contact? =
        Contacts(this)
            .lookupQuery()
            .whereLookupKeyMatches(lookupKey)
            .find()
            .firstOrNull()

    fun getContactByLookupKeyWithId(lookupKey: String, contactId: Long): Contact? =
        Contacts(this)
            .lookupQuery()
            .whereLookupKeyWithIdMatches(LookupQuery.LookupKeyWithId(lookupKey, contactId))
            .find()
            .firstOrNull()
}