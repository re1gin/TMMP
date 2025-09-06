package com.teladanprimaagro.tmpp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SyncStatusViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)

    private val _syncMessage = MutableStateFlow("Status sinkronisasi: Idle")
    val syncMessage: StateFlow<String> = _syncMessage.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()

    init {
        // Obeservasi status unik Work "SyncPanenWork"
        workManager.getWorkInfosForUniqueWorkLiveData("SyncPanenWork")
            .observeForever { workInfos ->
                val workInfo = workInfos?.firstOrNull()
                workInfo?.let {
                    when (it.state) {
                        WorkInfo.State.ENQUEUED -> {
                            _isSyncing.value = true
                            _syncMessage.value = "Menunggu koneksi..."
                        }
                        WorkInfo.State.RUNNING -> {
                            _isSyncing.value = true
                            val progressData = it.progress
                            val currentUniqueNo = progressData.getString("currentUniqueNo")
                            val progress = progressData.getFloat("progress", 0f)
                            _syncProgress.value = progress

                            val status = if (currentUniqueNo != null) {
                                "Mengunggah data: $currentUniqueNo"
                            } else {
                                "Sinkronisasi sedang berjalan..."
                            }
                            _syncMessage.value = status
                        }
                        WorkInfo.State.SUCCEEDED -> {
                            _isSyncing.value = false
                            _syncProgress.value = 1f
                            _syncMessage.value = "Sinkronisasi berhasil! Data terkirim."
                        }
                        WorkInfo.State.FAILED -> {
                            _isSyncing.value = false
                            _syncProgress.value = 0f
                            val errorMsg = it.outputData.getString("error") ?: "Sinkronisasi gagal. Coba lagi nanti."
                            _syncMessage.value = errorMsg
                        }
                        WorkInfo.State.CANCELLED -> {
                            _isSyncing.value = false
                            _syncProgress.value = 0f
                            _syncMessage.value = "Sinkronisasi dibatalkan."
                        }
                        else -> {
                            _isSyncing.value = false
                            _syncProgress.value = 0f
                            _syncMessage.value = "Status sinkronisasi: Idle"
                        }
                    }
                }
            }
    }
}