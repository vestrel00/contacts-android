package contacts.ui.entities

import android.content.res.Resources
import contacts.entities.*

/**
 * Creates instance of [CommonDataEntityType].
 */
interface CommonDataEntityTypeFactory<K : CommonDataEntity, T : CommonDataEntity.Type> {

    /**
     * Returns all the system types.
     *
     * The [CommonDataEntityType.typeLabel] is a system-defined string.
     */
    fun systemTypes(resources: Resources): MutableList<CommonDataEntityType<T>>

    /**
     * Creates a new user custom [CommonDataEntityType] with the given [labelStr].
     *
     * The [CommonDataEntityType.typeLabel] is a user input string.
     */
    fun userCustomType(labelStr: String): CommonDataEntityType<T>

    /**
     * Returns the [CommonDataEntityType] of the given [data] [K].
     *
     * If the [CommonDataEntity.Type] [T] is null, it will default to a non-null system type.
     *
     * The [CommonDataEntityType.typeLabel] may be a user input string or a system-defined string.
     */
    fun from(resources: Resources, data: K): CommonDataEntityType<T>
}

object PhoneTypeFactory : CommonDataEntityTypeFactory<MutablePhone, Phone.Type> {

    override fun systemTypes(resources: Resources): MutableList<CommonDataEntityType<Phone.Type>> =
        Phone.Type.values()
            .asSequence()
            .map { type -> CommonDataEntityType(type, type.labelStr(resources, null), false) }
            .toMutableList()

    override fun userCustomType(labelStr: String): CommonDataEntityType<Phone.Type> =
        CommonDataEntityType(Phone.Type.CUSTOM, labelStr, true)

    override fun from(resources: Resources, data: MutablePhone): CommonDataEntityType<Phone.Type> =
        (data.type ?: DEFAULT_TYPE).let { type ->
            CommonDataEntityType(type, type.labelStr(resources, data.label), type.isCustomType)
        }

    private val DEFAULT_TYPE = Phone.Type.MOBILE
}

object EmailTypeFactory : CommonDataEntityTypeFactory<MutableEmail, Email.Type> {

    override fun systemTypes(resources: Resources): MutableList<CommonDataEntityType<Email.Type>> =
        Email.Type.values()
            .asSequence()
            .map { type -> CommonDataEntityType(type, type.labelStr(resources, null), false) }
            .toMutableList()

    override fun userCustomType(labelStr: String): CommonDataEntityType<Email.Type> =
        CommonDataEntityType(Email.Type.CUSTOM, labelStr, true)

    override fun from(resources: Resources, data: MutableEmail): CommonDataEntityType<Email.Type> =
        (data.type ?: DEFAULT_TYPE).let { type ->
            CommonDataEntityType(type, type.labelStr(resources, data.label), type.isCustomType)
        }

    private val DEFAULT_TYPE = Email.Type.HOME
}

object AddressTypeFactory : CommonDataEntityTypeFactory<MutableAddress, Address.Type> {

    override fun systemTypes(resources: Resources): MutableList<CommonDataEntityType<Address.Type>> =
        Address.Type.values()
            .asSequence()
            .map { type -> CommonDataEntityType(type, type.labelStr(resources, null), false) }
            .toMutableList()

    override fun userCustomType(labelStr: String): CommonDataEntityType<Address.Type> =
        CommonDataEntityType(Address.Type.CUSTOM, labelStr, true)

    override fun from(
        resources: Resources, data: MutableAddress
    ): CommonDataEntityType<Address.Type> =
        (data.type ?: DEFAULT_TYPE).let { type ->
            CommonDataEntityType(type, type.labelStr(resources, data.label), type.isCustomType)
        }

    private val DEFAULT_TYPE = Address.Type.HOME
}

object ImsTypeFactory : CommonDataEntityTypeFactory<MutableIm, Im.Protocol> {

    override fun systemTypes(resources: Resources): MutableList<CommonDataEntityType<Im.Protocol>> =
        Im.Protocol.values()
            .asSequence()
            .map { protocol ->
                CommonDataEntityType(protocol, protocol.labelStr(resources, null), false)
            }
            .toMutableList()

    override fun userCustomType(labelStr: String): CommonDataEntityType<Im.Protocol> =
        CommonDataEntityType(Im.Protocol.CUSTOM, labelStr, true)

    override fun from(
        resources: Resources, data: MutableIm
    ): CommonDataEntityType<Im.Protocol> =
        (data.type ?: DEFAULT_TYPE).let { protocol ->
            CommonDataEntityType(
                protocol,
                protocol.labelStr(resources, data.label),
                protocol.isCustomType
            )
        }

    private val DEFAULT_TYPE = Im.Protocol.AIM
}