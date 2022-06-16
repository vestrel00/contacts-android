package contacts.sample.cheatsheet.data.kotlin

import android.accounts.Account
import android.app.Activity
import contacts.core.*
import contacts.core.entities.*

class QueryDataActivity : Activity() {

    fun getAllEmails(): List<Email> = Contacts(this).data().query().emails().find()

    fun getEmailsForAccount(account: Account): List<Email> =
        Contacts(this).data().query().emails().accounts(account).find()

    fun getGmailEmailsInDescendingOrder(): List<Email> = Contacts(this)
        .data()
        .query()
        .emails()
        .where { Email.Address endsWith "@gmail.com" }
        .orderBy(Fields.Email.Address.desc(ignoreCase = true))
        .find()

    fun getWorkPhones(): List<Phone> = Contacts(this)
        .data()
        .query()
        .phones()
        .where { Phone.Type equalTo PhoneEntity.Type.WORK }
        .find()

    fun getUpTo10Mothers(): List<Relation> = Contacts(this)
        .data()
        .query()
        .relations()
        .where { Relation.Type equalTo RelationEntity.Type.MOTHER }
        .limit(10)
        .find()

    fun getContactBirthday(contactId: Long): Event? = Contacts(this)
        .data()
        .query()
        .events()
        .where { (Contact.Id equalTo contactId) and (Event.Type equalTo EventEntity.Type.BIRTHDAY) }
        .find()
        .firstOrNull()
}