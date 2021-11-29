package contacts.ui.entities

import android.content.res.Resources
import contacts.core.entities.*

/**
 * Creates instance of [DataEntityType].
 */
interface DataEntityTypeFactory<E : DataEntity, T : DataEntity.Type> {

    /**
     * Returns all the system types.
     *
     * The [DataEntityType.typeLabel] is a system-defined string.
     */
    fun systemTypes(resources: Resources): MutableList<DataEntityType<T>>

    /**
     * Creates a new user custom [DataEntityType] with the given [labelStr].
     *
     * The [DataEntityType.typeLabel] is a user input string.
     */
    fun userCustomType(labelStr: String): DataEntityType<T>

    /**
     * Returns the [DataEntityType] of the given [data] [E].
     *
     * If the [DataEntity.Type] [T] is null, it will default to a non-null system type.
     *
     * The [DataEntityType.typeLabel] may be a user input string or a system-defined string.
     */
    fun from(resources: Resources, data: E): DataEntityType<T>
}

object AddressTypeFactory : DataEntityTypeFactory<MutableAddress, Address.Type> {

    override fun systemTypes(resources: Resources): MutableList<DataEntityType<Address.Type>> =
        Address.Type.values()
            .asSequence()
            .map { type -> DataEntityType(type, type.labelStr(resources, null), false) }
            .toMutableList()

    override fun userCustomType(labelStr: String): DataEntityType<Address.Type> =
        DataEntityType(Address.Type.CUSTOM, labelStr, true)

    override fun from(
        resources: Resources, data: MutableAddress
    ): DataEntityType<Address.Type> =
        (data.type ?: DEFAULT_TYPE).let { type ->
            DataEntityType(type, type.labelStr(resources, data.label), type.isCustomType)
        }

    private val DEFAULT_TYPE = Address.Type.HOME
}

object EmailTypeFactory : DataEntityTypeFactory<MutableEmail, Email.Type> {

    override fun systemTypes(resources: Resources): MutableList<DataEntityType<Email.Type>> =
        Email.Type.values()
            .asSequence()
            .map { type -> DataEntityType(type, type.labelStr(resources, null), false) }
            .toMutableList()

    override fun userCustomType(labelStr: String): DataEntityType<Email.Type> =
        DataEntityType(Email.Type.CUSTOM, labelStr, true)

    override fun from(resources: Resources, data: MutableEmail): DataEntityType<Email.Type> =
        (data.type ?: DEFAULT_TYPE).let { type ->
            DataEntityType(type, type.labelStr(resources, data.label), type.isCustomType)
        }

    private val DEFAULT_TYPE = Email.Type.HOME
}

object EventTypeFactory : DataEntityTypeFactory<MutableEvent, Event.Type> {

    override fun systemTypes(resources: Resources): MutableList<DataEntityType<Event.Type>> =
        Event.Type.values()
            .asSequence()
            .map { type -> DataEntityType(type, type.labelStr(resources, null), false) }
            .toMutableList()

    override fun userCustomType(labelStr: String): DataEntityType<Event.Type> =
        DataEntityType(Event.Type.CUSTOM, labelStr, true)

    override fun from(
        resources: Resources, data: MutableEvent
    ): DataEntityType<Event.Type> =
        (data.type ?: DEFAULT_TYPE).let { type ->
            DataEntityType(type, type.labelStr(resources, data.label), type.isCustomType)
        }

    private val DEFAULT_TYPE = Event.Type.BIRTHDAY
}

object ImsTypeFactory : DataEntityTypeFactory<MutableIm, Im.Protocol> {

    override fun systemTypes(resources: Resources): MutableList<DataEntityType<Im.Protocol>> =
        Im.Protocol.values()
            .asSequence()
            .map { protocol ->
                DataEntityType(protocol, protocol.labelStr(resources, null), false)
            }
            .toMutableList()

    override fun userCustomType(labelStr: String): DataEntityType<Im.Protocol> =
        DataEntityType(Im.Protocol.CUSTOM, labelStr, true)

    override fun from(
        resources: Resources, data: MutableIm
    ): DataEntityType<Im.Protocol> =
        (data.type ?: DEFAULT_TYPE).let { protocol ->
            DataEntityType(
                protocol,
                protocol.labelStr(resources, data.label),
                protocol.isCustomType
            )
        }

    private val DEFAULT_TYPE = Im.Protocol.AIM
}

object PhoneTypeFactory : DataEntityTypeFactory<MutablePhone, Phone.Type> {

    override fun systemTypes(resources: Resources): MutableList<DataEntityType<Phone.Type>> =
        Phone.Type.values()
            .asSequence()
            .map { type -> DataEntityType(type, type.labelStr(resources, null), false) }
            .toMutableList()

    override fun userCustomType(labelStr: String): DataEntityType<Phone.Type> =
        DataEntityType(Phone.Type.CUSTOM, labelStr, true)

    override fun from(resources: Resources, data: MutablePhone): DataEntityType<Phone.Type> =
        (data.type ?: DEFAULT_TYPE).let { type ->
            DataEntityType(type, type.labelStr(resources, data.label), type.isCustomType)
        }

    private val DEFAULT_TYPE = Phone.Type.MOBILE
}

object RelationTypeFactory : DataEntityTypeFactory<MutableRelation, Relation.Type> {

    override fun systemTypes(resources: Resources): MutableList<DataEntityType<Relation.Type>> =
        Relation.Type.values()
            .asSequence()
            .map { type -> DataEntityType(type, type.labelStr(resources, null), false) }
            .toMutableList()

    override fun userCustomType(labelStr: String): DataEntityType<Relation.Type> =
        DataEntityType(Relation.Type.CUSTOM, labelStr, true)

    override fun from(
        resources: Resources, data: MutableRelation
    ): DataEntityType<Relation.Type> =
        (data.type ?: DEFAULT_TYPE).let { type ->
            DataEntityType(type, type.labelStr(resources, data.label), type.isCustomType)
        }

    private val DEFAULT_TYPE = Relation.Type.ASSISTANT
}