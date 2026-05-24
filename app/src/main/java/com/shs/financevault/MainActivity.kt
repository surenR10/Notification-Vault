package com.shs.financevault

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.shs.financevault.alerts.AlertEngine
import com.shs.financevault.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        AlertEngine.setupChannel(this)

        // Bottom navigation setup
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment
        b.bottomNav.setupWithNavController(navHost.navController)

        // Permission banner
        b.bannerPermission.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        b.bannerPermission.isVisible = !isPermissionGranted()
    }

    private fun isPermissionGranted(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            ?: return false
        val cn = ComponentName(this, FinanceListenerService::class.java)
        return !TextUtils.isEmpty(flat) && flat.contains(cn.flattenToString())
    }
}
