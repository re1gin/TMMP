package com.teladanprimaagro.tmpp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.teladanprimaagro.tmpp.workers.SyncPanenWorker
import com.teladanprimaagro.tmpp.workers.SyncPengirimanWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SyncStatusViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)

    private val _syncMessage = MutableStateFlow("Status sinkronisasi: Idle")
    val syncMessage: StateFlow<String> = _syncMessage.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()

    private val _currentSyncId = MutableStateFlow<String?>(null)
    val currentSyncId: StateFlow<String?> = _currentSyncId.asStateFlow()

    private val workNames = listOf("SyncPanenWork", "SyncPengirimanWork")

    init {
        viewModelScope.launch {
            val workInfoFlows = workNames.map { workName ->
                workManager.getWorkInfosForUniqueWorkFlow(workName)
                    .map { workInfos -> workInfos.firstOrNull() to workName }
            }
            combine(workInfoFlows) { workInfoPairs ->
                val activeWorkInfos = workInfoPairs.filter { it.first != null }
                when {
                    activeWorkInfos.isEmpty() -> {
                        _isSyncing.value = false
                        _syncProgress.value = 0f
                        _syncMessage.value = "Status sinkronisasi: Idle"
                        _currentSyncId.value = null
                    }
                    activeWorkInfos.any { it.first?.state == WorkInfo.State.RUNNING } -> {
                        val runningWork = activeWorkInfos.first { it.first?.state == WorkInfo.State.RUNNING }
                        val workInfo = runningWork.first!!
                        val workName = runningWork.second
                        val progressData = workInfo.progress
                        val currentId = progressData.getString("currentUniqueNo") ?: progressData.getString("currentWorkerId")
                        val totalItems = progressData.getInt("totalItems", 1)
                        val currentItem = progressData.getInt("currentItem", 1)
                        _isSyncing.value = true
                        _syncProgress.value = if (totalItems > 0) currentItem.toFloat() / totalItems else progressData.getFloat("progress", 0f)
                        _syncMessage.value = if (currentId != null) {
                            "Mengunggah data ${workName.replace("Sync", "")}: $currentId"
                        } else {
                            "Sinkronisasi ${workName.replace("Sync", "")} sedang berjalan..."
                        }
                        _currentSyncId.value = currentId
                    }
                    activeWorkInfos.any { it.first?.state == WorkInfo.State.ENQUEUED } -> {
                        _isSyncing.value = true
                        _syncMessage.value = "Menunggu koneksi..."
                        _currentSyncId.value = null
                    }
                    activeWorkInfos.all { it.first?.state == WorkInfo.State.SUCCEEDED } -> {
                        _isSyncing.value = false
                        _syncProgress.value = 1f
                        _syncMessage.value = "Sinkronisasi berhasil! Data terkirim."
                        _currentSyncId.value = null
                    }
                    activeWorkInfos.any { it.first?.state == WorkInfo.State.FAILED } -> {
                        _isSyncing.value = false
                        _syncProgress.value = 0f
                        val failedWork = activeWorkInfos.first { it.first?.state == WorkInfo.State.FAILED }
                        val errorMsg = failedWork.first!!.outputData.getString("error") ?: "Sinkronisasi gagal. Coba lagi nanti."
                        _syncMessage.value = errorMsg
                        _currentSyncId.value = null
                    }
                    else -> {
                        _isSyncing.value = false
                        _syncProgress.value = 0f
                        _syncMessage.value = "Status sinkronisasi: Idle"
                        _currentSyncId.value = null
                    }
                }
            }.collect()
        }
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            try {
                workNames.forEach { workName ->
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    val syncWorkRequest = when (workName) {
                        "SyncPanenWork" -> OneTimeWorkRequestBuilder<SyncPanenWorker>()
                        "SyncPengirimanWork" -> OneTimeWorkRequestBuilder<SyncPengirimanWorker>()
                        else -> throw IllegalArgumentException("Unknown work name: $workName")
                    }.setConstraints(constraints).build()
                    workManager.enqueueUniqueWork(
                        workName,
                        ExistingWorkPolicy.REPLACE,
                        syncWorkRequest
                    )
                }
                _syncMessage.value = "Menunggu koneksi..."
            } catch (e: Exception) {
                _syncMessage.value = "Gagal memulai sinkronisasi: ${e.message}"
                Log.e("SyncStatusViewModel", "Failed to trigger manual sync", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("SyncStatusViewModel", "ViewModel cleared")
    }
}