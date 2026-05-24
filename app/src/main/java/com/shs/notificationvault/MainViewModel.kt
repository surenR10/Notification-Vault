package com.shs.notificationvault

import android.app.Application
import androidx.lifecycle.*
import com.shs.notificationvault.data.NotificationEntity
import com.shs.notificationvault.data.NotificationRepository
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = NotificationRepository(app)

    // Filter state
    private val _filterPkg   = MutableLiveData<String?>(null)
    private val _searchQuery = MutableLiveData<String?>(null)
    private val _showStarred = MutableLiveData(false)

    val distinctApps = repo.distinctApps
    val totalCount   = repo.totalCount

    /**
     * The displayed list reacts to filter / search / starred toggle in priority:
     *   search > starred > appFilter > all
     */
    val notifications: LiveData<List<NotificationEntity>> = _searchQuery.switchMap { query ->
        when {
            !query.isNullOrBlank() -> repo.search(query)
            else -> _showStarred.switchMap { starred ->
                if (starred) repo.starred
                else _filterPkg.switchMap { pkg ->
                    if (pkg != null) repo.byApp(pkg) else repo.all
                }
            }
        }
    }

    fun setSearch(q: String?)  { _searchQuery.value = q }
    fun setFilter(pkg: String?) { _filterPkg.value = pkg }
    fun setShowStarred(v: Boolean) { _showStarred.value = v }

    fun delete(n: NotificationEntity)  = viewModelScope.launch { repo.delete(n) }
    fun toggleStar(n: NotificationEntity) = viewModelScope.launch { repo.toggleStar(n) }
    fun deleteAll()       = viewModelScope.launch { repo.deleteAll() }
    fun deleteUnstarred() = viewModelScope.launch { repo.deleteUnstarred() }
}
