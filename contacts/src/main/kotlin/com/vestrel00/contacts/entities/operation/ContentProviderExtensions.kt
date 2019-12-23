package com.vestrel00.contacts.entities.operation

import android.content.ContentProviderOperation.Builder
import com.vestrel00.contacts.AbstractField

internal fun Builder.withValue(field: AbstractField, value: Any?): Builder =
    withValue(field.columnName, value)