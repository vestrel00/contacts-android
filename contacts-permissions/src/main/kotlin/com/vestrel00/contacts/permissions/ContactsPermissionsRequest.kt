package com.vestrel00.contacts.permissions

import android.app.Activity
import com.vestrel00.contacts.*

suspend fun Contacts.queryWithPermission(activity: Activity): Query {
    val permissions = permissions(activity)
    if (!permissions.canQuery()) {
        permissions.requestQueryPermission(activity)
    }

    return query(activity)
}

suspend fun Contacts.insertWithPermission(activity: Activity): Insert {
    val permissions = permissions(activity)
    if (!permissions.canInsertUpdateDelete()) {
        permissions.requestInsertUpdateDeletePermission(activity)
    }

    return insert(activity)
}

suspend fun Contacts.updateWithPermission(activity: Activity): Update {
    val permissions = permissions(activity)
    if (!permissions.canInsertUpdateDelete()) {
        permissions.requestInsertUpdateDeletePermission(activity)
    }

    return update(activity)
}

suspend fun Contacts.deleteWithPermission(activity: Activity): Delete {
    val permissions = permissions(activity)
    if (!permissions.canInsertUpdateDelete()) {
        permissions.requestInsertUpdateDeletePermission(activity)
    }

    return delete(activity)
}