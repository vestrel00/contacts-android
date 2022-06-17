package contacts.sample.cheatsheet.customdata.kotlin

import android.app.Activity
import contacts.core.*
import contacts.core.data.*
import contacts.core.entities.*
import contacts.core.entities.custom.CustomDataRegistry
import contacts.entities.custom.handlename.*

class IntegrateHandleNameCustomDataActivity : Activity() {

    val contacts = Contacts(this, CustomDataRegistry().register(HandleNameRegistration()))

    fun getContactsWithHandleNameCustomData(): List<Contact> = contacts
        .query()
        .where { HandleNameFields.Handle.isNotNull() }
        .find()

    fun insertRawContactWithHandleNameCustomData(): Insert.Result = contacts
        .insert()
        .rawContact {
            addHandleName(contacts) {
                handle = "The Beauty"
            }
        }
        .commit()

    fun updateRawContactHandleNameCustomData(rawContact: RawContact): Update.Result = contacts
        .update()
        .rawContacts(
            rawContact.mutableCopy {
                handleNames(contacts).firstOrNull()?.handle = "The Beast"
            }
        )
        .commit()

    fun deleteHandleNameCustomDataFromRawContact(rawContact: RawContact): Update.Result =
        contacts
            .update()
            .rawContacts(
                rawContact.mutableCopy {
                    removeAllHandleNames(contacts)
                }
            )
            .commit()

    fun getAllHandleName(): List<HandleName> = contacts.data().query().handleNames().find()

    fun updateHandleName(handleName: MutableHandleName): DataUpdate.Result =
        contacts.data().update().data(handleName).commit()

    fun deleteHandleName(handleName: HandleName): DataDelete.Result =
        contacts.data().delete().data(handleName).commit()
}