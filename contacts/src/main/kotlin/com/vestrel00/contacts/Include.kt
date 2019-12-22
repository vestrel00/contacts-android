package com.vestrel00.contacts

@Suppress("FunctionName")
internal fun Include(vararg fields: Field): Include = Include(fields.asSequence())

internal class Include(fields: Sequence<Field>) {

    val fields: Set<AbstractField> = sequence<AbstractField> {
        for (field in fields) {
            when (field) {
                is AbstractField -> yield(field)
                is FieldSet -> yieldAll(field.fields)
            }
        }
    }.toSet()

    val columnNames: Array<out String>
        get() = fields.map { it.columnName }.toTypedArray()

    override fun toString(): String = columnNames.joinToString(", ")
}