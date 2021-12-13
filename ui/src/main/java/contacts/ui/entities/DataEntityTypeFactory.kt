package contacts.ui.entities

import android.content.res.Resources
import contacts.core.entities.*

/**
 * Creates instance of [DataEntityType].
 */
sealed interface DataEntityTypeFactory<E : DataEntity, T : DataEntity.Type> {

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

object AddressTypeFactory : DataEntityTypeFactory<AddressEntity, AddressEntity.Type> {

    override fun systemTypes(resources: Resources):
            MutableList<DataEntityType<AddressEntity.Type>> = AddressEntity.Type.values()
        .asSequence()
        .map { type -> DataEntityType(type, type.labelStr(resources, null), false) }
        .toMutableList()

    override fun userCustomType(labelStr: String): DataEntityType<AddressEntity.Type> =
        DataEntityType(AddressEntity.Type.CUSTOM, labelStr, true)

    override fun from(
        resources: Resources, data: AddressEntity
    ): DataEntityType<AddressEntity.Type> =
        (data.type ?: DEFAULT_TYPE).let { type ->
            DataEntityType(type, type.labelStr(resources, data.label), type.isCustomType)
        }

    private val DEFAULT_TYPE = AddressEntity.Type.HOME
}

object EmailTypeFactory : DataEntityTypeFactory<EmailEntity, EmailEntity.Type> {

    override fun systemTypes(resources: Resources): MutableList<DataEntityType<EmailEntity.Type>> =
        EmailEntity.Type.values()
            .asSequence()
            .map { type -> DataEntityType(type, type.labelStr(resources, null), false) }
            .toMutableList()

    override fun userCustomType(labelStr: String): DataEntityType<EmailEntity.Type> =
        DataEntityType(EmailEntity.Type.CUSTOM, labelStr, true)

    override fun from(resources: Resources, data: EmailEntity): DataEntityType<EmailEntity.Type> =
        (data.type ?: DEFAULT_TYPE).let { type ->
            DataEntityType(type, type.labelStr(resources, data.label), type.isCustomType)
        }

    private val DEFAULT_TYPE = EmailEntity.Type.HOME
}

object EventTypeFactory : DataEntityTypeFactory<EventEntity, EventEntity.Type> {

    override fun systemTypes(resources: Resources): MutableList<DataEntityType<EventEntity.Type>> =
        EventEntity.Type.values()
            .asSequence()
            .map { type -> DataEntityType(type, type.labelStr(resources, null), false) }
            .toMutableList()

    override fun userCustomType(labelStr: String): DataEntityType<EventEntity.Type> =
        DataEntityType(EventEntity.Type.CUSTOM, labelStr, true)

    override fun from(
        resources: Resources, data: EventEntity
    ): DataEntityType<EventEntity.Type> =
        (data.type ?: DEFAULT_TYPE).let { type ->
            DataEntityType(type, type.labelStr(resources, data.label), type.isCustomType)
        }

    private val DEFAULT_TYPE = EventEntity.Type.BIRTHDAY
}

object ImsTypeFactory : DataEntityTypeFactory<ImEntity, ImEntity.Protocol> {

    override fun systemTypes(resources: Resources): MutableList<DataEntityType<ImEntity.Protocol>> =
        ImEntity.Protocol.values()
            .asSequence()
            .map { protocol ->
                DataEntityType(protocol, protocol.labelStr(resources, null), false)
            }
            .toMutableList()

    override fun userCustomType(labelStr: String): DataEntityType<ImEntity.Protocol> =
        DataEntityType(ImEntity.Protocol.CUSTOM, labelStr, true)

    override fun from(
        resources: Resources, data: ImEntity
    ): DataEntityType<ImEntity.Protocol> =
        (data.protocol ?: DEFAULT_TYPE).let { protocol ->
            DataEntityType(
                protocol,
                protocol.labelStr(resources, data.customProtocol),
                protocol.isCustomType
            )
        }

    private val DEFAULT_TYPE = ImEntity.Protocol.AIM
}

object PhoneTypeFactory : DataEntityTypeFactory<PhoneEntity, PhoneEntity.Type> {

    override fun systemTypes(resources: Resources): MutableList<DataEntityType<PhoneEntity.Type>> =
        PhoneEntity.Type.values()
            .asSequence()
            .map { type -> DataEntityType(type, type.labelStr(resources, null), false) }
            .toMutableList()

    override fun userCustomType(labelStr: String): DataEntityType<PhoneEntity.Type> =
        DataEntityType(PhoneEntity.Type.CUSTOM, labelStr, true)

    override fun from(resources: Resources, data: PhoneEntity): DataEntityType<PhoneEntity.Type> =
        (data.type ?: DEFAULT_TYPE).let { type ->
            DataEntityType(type, type.labelStr(resources, data.label), type.isCustomType)
        }

    private val DEFAULT_TYPE = PhoneEntity.Type.MOBILE
}

object RelationTypeFactory : DataEntityTypeFactory<RelationEntity, RelationEntity.Type> {

    override fun systemTypes(resources: Resources):
            MutableList<DataEntityType<RelationEntity.Type>> = RelationEntity.Type.values()
        .asSequence()
        .map { type -> DataEntityType(type, type.labelStr(resources, null), false) }
        .toMutableList()

    override fun userCustomType(labelStr: String): DataEntityType<RelationEntity.Type> =
        DataEntityType(RelationEntity.Type.CUSTOM, labelStr, true)

    override fun from(
        resources: Resources, data: RelationEntity
    ): DataEntityType<RelationEntity.Type> =
        (data.type ?: DEFAULT_TYPE).let { type ->
            DataEntityType(type, type.labelStr(resources, data.label), type.isCustomType)
        }

    private val DEFAULT_TYPE = RelationEntity.Type.ASSISTANT
}