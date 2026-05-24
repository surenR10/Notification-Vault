package com.shs.notificationvault

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.shs.notificationvault.databinding.ActivityMainBinding
import com.shs.notificationvault.ui.NotificationAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val vm: MainViewModel by viewModels()
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)

        setupList()
        setupFilterSpinner()
        setupStarToggle()
        observeData()

        // Permission banner tap → open settings
        b.bannerPermission.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionBanner()
    }

    // ── Menu ──────────────────────────────────────────────────────────────────

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search notifications…"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = true
            override fun onQueryTextChange(q: String?): Boolean {
                vm.setSearch(q?.takeIf { it.isNotBlank() })
                return true
            }
        })

        // Clear search when collapsed
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem) = true
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                vm.setSearch(null)
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_clear_unstarred -> {
            AlertDialog.Builder(this)
                .setTitle("Delete unstarred?")
                .setMessage("Starred notifications will be kept. All others will be permanently deleted.")
                .setPositiveButton("Delete") { _, _ ->
                    vm.deleteUnstarred()
                    Snackbar.make(b.root, "Unstarred notifications cleared", Snackbar.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }
        R.id.action_clear_all -> {
            AlertDialog.Builder(this)
                .setTitle("Delete ALL logs?")
                .setMessage("This cannot be undone. Every recorded notification will be erased.")
                .setPositiveButton("Delete All") { _, _ ->
                    vm.deleteAll()
                    Snackbar.make(b.root, "All logs cleared", Snackbar.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private fun setupList() {
        adapter = NotificationAdapter(
            onStar   = { vm.toggleStar(it) },
            onDelete = { vm.delete(it) }
        )
        b.rvNotifications.layoutManager = LinearLayoutManager(this)
        b.rvNotifications.adapter = adapter
    }

    private fun setupFilterSpinner() {
        vm.distinctApps.observe(this) { apps ->
            val labels = listOf("All Apps") + apps.map { it.appName }
            val pkgs   = listOf<String?>(null) + apps.map { it.appPackage }

            val spinnerAdapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_item, labels
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

            b.spinnerFilter.adapter = spinnerAdapter
            b.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                    vm.setFilter(pkgs[pos])
                }
                override fun onNothingSelected(p: AdapterView<*>?) = Unit
            }
        }
    }

    private fun setupStarToggle() {
        b.chipStarred.setOnCheckedChangeListener { _, checked ->
            vm.setShowStarred(checked)
        }
    }

    private fun observeData() {
        vm.notifications.observe(this) { list ->
            adapter.submitList(list)
            b.tvEmpty.isVisible = list.isEmpty()
            b.rvNotifications.isVisible = list.isNotEmpty()
        }
        vm.totalCount.observe(this) { count ->
            b.tvCount.text = "$count notifications logged"
        }
    }

    private fun updatePermissionBanner() {
        val enabled = isNotificationAccessGranted()
        b.bannerPermission.isVisible = !enabled
    }

    private fun isNotificationAccessGranted(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            ?: return false
        val cn = ComponentName(this, NotificationLogService::class.java)
        return flat.contains(cn.flattenToString())
    }
}
