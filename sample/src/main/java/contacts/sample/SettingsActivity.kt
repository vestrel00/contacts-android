package contacts.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<View>(R.id.accounts).setOnClickListener { showAccounts() }
    }

    private fun showAccounts() {
        startActivity(Intent(Settings.ACTION_SYNC_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    companion object {
        fun showSettings(activity: Activity) {
            activity.startActivity(Intent(activity, SettingsActivity::class.java))
        }
    }
}