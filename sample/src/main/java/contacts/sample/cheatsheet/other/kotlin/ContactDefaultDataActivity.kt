package contacts.sample.cheatsheet.other.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.entities.Phone
import contacts.core.util.*

class ContactDefaultDataActivity : Activity() {

    fun isPhoneTheDefaultPhone(phone: Phone): Boolean = phone.isDefault

    fun setPhoneAsDefault(phone: Phone): Boolean = phone.setAsDefault(Contacts(this))

    fun clearDefaultPhone(phone: Phone): Boolean = phone.clearDefault(Contacts(this))
}