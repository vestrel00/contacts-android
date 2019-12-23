package com.vestrel00.contacts.accounts

import android.Manifest
import android.content.Context
import com.vestrel00.contacts.accounts.AccountsPermissions.Companion.GET_ACCOUNTS_PERMISSION
import com.vestrel00.contacts.isPermissionGrantedFor

interface AccountsPermissions {

    fun canGetAccounts(): Boolean

    companion object {
        const val GET_ACCOUNTS_PERMISSION: String = Manifest.permission.GET_ACCOUNTS
    }
}

@Suppress("FunctionName")
internal fun AccountsPermissions(context: Context): AccountsPermissions =
    AccountsPermissionsImpl(context)

private class AccountsPermissionsImpl(private val context: Context) : AccountsPermissions {

    override fun canGetAccounts() = context.isPermissionGrantedFor(GET_ACCOUNTS_PERMISSION)
}
