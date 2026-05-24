package com.shs.notificationvault.data

import android.content.Context

class NotificationRepository(context: Context) {

    private val dao = NotificationDatabase.get(context).dao()

    // Observables for the UI
    val all         = dao.getAll()
    val starred     = dao.getStarred()
    val distinctApps = dao.getDistinctApps()
    val totalCount  = dao.getTotalCount()

    fun byApp(pkg: String)   = dao.getByApp(pkg)
    fun search(query: String) = dao.search(query)

    suspend fun insert(n: NotificationEntity)  = dao.insert(n)
    suspend fun delete(n: NotificationEntity)  = dao.delete(n)
    suspend fun deleteAll()                    = dao.deleteAll()
    suspend fun deleteUnstarred()              = dao.deleteUnstarred()
    suspend fun toggleStar(n: NotificationEntity) = dao.update(n.copy(isStarred = !n.isStarred))
}
