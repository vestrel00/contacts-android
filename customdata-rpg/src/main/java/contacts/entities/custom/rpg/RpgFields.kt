@file:Suppress("PropertyName")

package contacts.entities.custom.rpg

import contacts.core.AbstractCustomDataField
import contacts.core.AbstractCustomDataField.ColumnName
import contacts.core.AbstractCustomDataFieldSet
import contacts.core.entities.MimeType

object RpgFields : AbstractCustomDataFieldSet<RpgField>() {

    @JvmField
    val Profession = RpgProfessionFields()

    @JvmField
    val Stats = RpgStatsFields()

    override val all: Set<RpgField> = Profession.all + Stats.all

    override val forMatching: Set<RpgField> = Profession.forMatching + Stats.forMatching

    /**
     * Same as [all], but as a function. This mainly exists for Java support. This makes it visible
     * to Java consumers when accessing this using the object reference directly.
     */
    @JvmStatic
    fun all() = all

    /**
     * Same as [forMatching], but as a function. This makes it visible to Java consumers when
     * accessing this using the object reference directly.
     */
    @JvmStatic
    fun forMatching() = forMatching
}

sealed class RpgField(columnName: ColumnName) : AbstractCustomDataField(columnName)

@ConsistentCopyVisibility
data class RpgProfessionField internal constructor(private val columnName: ColumnName) :
    RpgField(columnName) {

    override val customMimeType: MimeType.Custom = RpgMimeType.Profession
}

class RpgProfessionFields internal constructor() :
    AbstractCustomDataFieldSet<RpgProfessionField>() {

    @JvmField
    val Title = RpgProfessionField(ColumnName.DATA)

    override val all: Set<RpgProfessionField> = setOf(Title)

    override val forMatching: Set<RpgProfessionField> = setOf(Title)
}

@ConsistentCopyVisibility
data class RpgStatsField internal constructor(private val columnName: ColumnName) :
    RpgField(columnName) {

    override val customMimeType: MimeType.Custom = RpgMimeType.Stats
}

class RpgStatsFields internal constructor() : AbstractCustomDataFieldSet<RpgStatsField>() {

    @JvmField
    val Level = RpgStatsField(ColumnName.DATA)

    @JvmField
    val Speed = RpgStatsField(ColumnName.DATA4)

    @JvmField
    val Strength = RpgStatsField(ColumnName.DATA5)

    @JvmField
    val Intelligence = RpgStatsField(ColumnName.DATA6)

    @JvmField
    val Luck = RpgStatsField(ColumnName.DATA7)

    override val all: Set<RpgStatsField> = setOf(Level, Speed, Strength, Intelligence, Luck)

    // Integers are typically not included here.
    override val forMatching: Set<RpgStatsField> = emptySet()
}