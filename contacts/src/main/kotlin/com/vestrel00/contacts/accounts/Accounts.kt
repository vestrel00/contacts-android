package com.vestrel00.contacts.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context

interface Accounts {

    fun allAccounts(context: Context): List<Account>

    fun accountsWithType(context: Context, type: String): List<Account>

    fun permissions(context: Context): AccountsPermissions
}

@Suppress("FunctionName")
fun Accounts(): Accounts = AccountsImpl()

@SuppressWarnings("MissingPermission")
private class AccountsImpl : Accounts {

    override fun allAccounts(context: Context): List<Account> = accounts(context) {
        AccountManager.get(context).accounts.toList()
    }

    override fun accountsWithType(context: Context, type: String) = accounts(context) {
        AccountManager.get(context).getAccountsByType(type).toList()
    }

    override fun permissions(context: Context) = AccountsPermissions(context)

    private inline fun accounts(context: Context, accounts: () -> List<Account>): List<Account> {
        if (!permissions(context).canGetAccounts()) {
            return emptyList()
        }

        return accounts()
    }
}