package contacts.test

import android.accounts.Account
import contacts.core.*
import contacts.core.entities.Contact
import contacts.test.entities.TestDataFields

// Note that we cannot use "by" to delegate calls to the internal query because function calls will
// return the internal query instance instead of this test instance.
// class TestQuery(private val query: Query): Query by query

/**
 * A version of [Query] that is used only for tests. It only includes and matches RawContacts with
 * a [contacts.test.entities.TestData].
 */
internal class TestQuery(private val query: Query) : Query {

    override fun includeBlanks(includeBlanks: Boolean): TestQuery = apply {
        query.includeBlanks(includeBlanks)
    }

    override fun accounts(vararg accounts: Account?): TestQuery = accounts(accounts.asSequence())

    override fun accounts(accounts: Collection<Account?>): TestQuery =
        accounts(accounts.asSequence())

    override fun accounts(accounts: Sequence<Account?>): TestQuery = apply {
        query.accounts(accounts)
    }

    override fun include(vararg fields: AbstractDataField): TestQuery = include(fields.asSequence())

    override fun include(fields: Collection<AbstractDataField>): TestQuery =
        include(fields.asSequence())

    override fun include(fields: Sequence<AbstractDataField>): TestQuery = apply {
        // Make sure to include test marker data in order to be supported by other test APIs.
        query.include(fields + TestDataFields.all)
    }

    override fun include(fields: Fields.() -> Collection<AbstractDataField>) =
        include(fields(Fields))

    override fun where(where: Where<AbstractDataField>?): TestQuery = apply {
        // Make sure only RawContacts with the test marker data are queried.
        query.where(
            if (where != null) {
                where and TestDataFields.Value.isNotNullOrEmpty()
            } else {
                TestDataFields.Value.isNotNullOrEmpty()
            }
        )
    }

    override fun where(where: Fields.() -> Where<AbstractDataField>?) = where(where(Fields))

    override fun orderBy(vararg orderBy: OrderBy<ContactsField>): TestQuery =
        orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Collection<OrderBy<ContactsField>>): TestQuery =
        orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Sequence<OrderBy<ContactsField>>): TestQuery = apply {
        query.orderBy(orderBy)
    }

    override fun orderBy(orderBy: ContactsFields.() -> Collection<OrderBy<ContactsField>>) =
        orderBy(orderBy(ContactsFields))

    override fun limit(limit: Int): TestQuery = apply {
        query.limit(limit)
    }

    override fun offset(offset: Int): TestQuery = apply {
        query.offset(offset)
    }

    override fun find(): List<Contact> = query.find()

    override fun find(cancel: () -> Boolean): List<Contact> = query.find(cancel)
}