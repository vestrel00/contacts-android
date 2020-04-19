package com.vestrel00.contacts

@Suppress("FunctionName")
internal fun Include(vararg fields: Field): Include = Include(fields.asSequence())

internal class Include(fields: Sequence<Field>) {

    val fields: Set<AbstractField> by lazy(LazyThreadSafetyMode.NONE) {
        mutableSetOf<AbstractField>().apply {
            for (field in fields) {
                when (field) {
                    is AbstractField -> add(field)
                    is FieldSet -> addAll(field.fields)
                }
            }
        }
    }

    val columnNames: Array<out String> by lazy(LazyThreadSafetyMode.NONE) {
        this.fields.asSequence().map { it.columnName }.toSet().toTypedArray()
    }

    override fun toString(): String = columnNames.joinToString(", ")
}