package contacts.sample.cheatsheet.customdata.kotlin

import android.app.Activity
import contacts.core.*
import contacts.core.data.*
import contacts.core.entities.*
import contacts.core.entities.custom.CustomDataRegistry
import contacts.entities.custom.multiplenotes.*

class IntegrateMultipleNotesCustomDataActivity : Activity() {

    val contacts = Contacts(this, false, CustomDataRegistry().register(MultipleNotesRegistration()))

    fun getContactsWithMultipleNotesCustomData(): List<Contact> = contacts
        .query()
        .where { MultipleNotesFields.Note.isNotNull() }
        .find()

    fun insertRawContactWithMultipleNotesCustomData(): Insert.Result = contacts
        .insert()
        .rawContact {
            addMultipleNotes(contacts) {
                note = "First note"
            }
            addMultipleNotes(contacts) {
                note = "Second note"
            }
        }
        .commit()

    fun updateRawContactMultipleNotesCustomData(rawContact: RawContact): Update.Result = contacts
        .update()
        .rawContacts(
            rawContact.mutableCopy {
                multipleNotes(contacts).firstOrNull()?.note = "A note"
            }
        )
        .commit()

    fun deleteMultipleNotesCustomDataFromRawContact(rawContact: RawContact): Update.Result =
        contacts
            .update()
            .rawContacts(
                rawContact.mutableCopy {
                    removeAllMultipleNotes(contacts)
                }
            )
            .commit()

    fun getAllMultipleNotes(): List<MultipleNotes> = contacts.data().query().multipleNotes().find()

    fun updateMultipleNotes(multipleNotes: MutableMultipleNotes): DataUpdate.Result =
        contacts.data().update().data(multipleNotes).commit()

    fun deleteMultipleNotes(multipleNotes: MultipleNotes): DataDelete.Result =
        contacts.data().delete().data(multipleNotes).commit()
}