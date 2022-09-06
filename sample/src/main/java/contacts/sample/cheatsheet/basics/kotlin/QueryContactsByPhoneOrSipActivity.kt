package contacts.sample.cheatsheet.basics.kotlin

import android.app.Activity
import contacts.core.*
import contacts.core.entities.Contact

class QueryContactsByPhoneOrSipActivity : Activity() {

    fun getContactsWithPhoneNumberThatExactlyMatches(text: String?): List<Contact> =
        Contacts(this)
            .phoneLookupQuery()
            .match(PhoneLookupQuery.Match.PHONE_NUMBER)
            .whereExactlyMatches(text)
            .find()

    fun getContactsWithSipAddressThatExactlyMatches(text: String?): List<Contact> =
        Contacts(this)
            .phoneLookupQuery()
            .match(PhoneLookupQuery.Match.SIP_ADDRESS)
            .whereExactlyMatches(text)
            .find()
}