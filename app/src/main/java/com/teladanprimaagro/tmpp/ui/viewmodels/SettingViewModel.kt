package com.teladanprimaagro.tmpp.ui.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Buat konstanta untuk nama SharedPreferences dan kunci data
private const val PREFS_NAME = "app_settings_prefs"
private const val KEY_MANDOR_LIST = "mandor_list"
private const val KEY_PEMANEN_LIST = "pemanen_list"
private const val KEY_BLOK_LIST = "blok_list"
private const val KEY_TPH_LIST = "tph_list"
private const val KEY_IS_LOGGED_IN = "is_logged_in"
private const val KEY_USER_ROLE = "user_role" // Kunci baru untuk peran pengguna

// Implementasikan Gson di build.gradle (app) jika belum: implementation("com.google.code.gson:gson:2.10.1")

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // Daftar-daftar Anda
    val mandorList = mutableStateListOf<String>()
    val pemanenList = mutableStateListOf<String>()
    val blokList = mutableStateListOf<String>()
    val tphList = mutableStateListOf<String>()

    init {
        // Muat data saat ViewModel dibuat
        loadSettings()

        // Jika daftar masih kosong setelah dimuat, isi dengan nilai default
        if (mandorList.isEmpty()) {
            mandorList.addAll(listOf("Nama Mandor Default")) // Ubah nilai default sesuai kebutuhan
        }
        if (pemanenList.isEmpty()) {
            pemanenList.addAll(listOf("Nama Pemanen Default"))
        }
        if (blokList.isEmpty()) {
            blokList.addAll(listOf("A01 Default", "A02 Default"))
        }
        if (tphList.isEmpty()) {
            tphList.addAll(listOf("TPH 001 Default", "TPH 002 Default", "TPH 003 Default"))
        }
    }

    private fun loadSettings() {
        // Muat Mandor
        val mandorJson = sharedPreferences.getString(KEY_MANDOR_LIST, null)
        if (mandorJson != null) {
            val type = object : TypeToken<List<String>>() {}.type
            val loadedList: List<String> = gson.fromJson(mandorJson, type)
            mandorList.addAll(loadedList)
        }

        // Muat Pemanen
        val pemanenJson = sharedPreferences.getString(KEY_PEMANEN_LIST, null)
        if (pemanenJson != null) {
            val type = object : TypeToken<List<String>>() {}.type
            val loadedList: List<String> = gson.fromJson(pemanenJson, type)
            pemanenList.addAll(loadedList)
        }

        // Muat Blok
        val blokJson = sharedPreferences.getString(KEY_BLOK_LIST, null)
        if (blokJson != null) {
            val type = object : TypeToken<List<String>>() {}.type
            val loadedList: List<String> = gson.fromJson(blokJson, type)
            blokList.addAll(loadedList)
        }

        // Muat TPH
        val tphJson = sharedPreferences.getString(KEY_TPH_LIST, null)
        if (tphJson != null) {
            val type = object : TypeToken<List<String>>() {}.type
            val loadedList: List<String> = gson.fromJson(tphJson, type)
            tphList.addAll(loadedList)
        }
    }

    private fun saveSettings(isLoggedIn: Boolean, userRole: String?) {
        with(sharedPreferences.edit()) {
            putString(KEY_MANDOR_LIST, gson.toJson(mandorList.toList()))
            putString(KEY_PEMANEN_LIST, gson.toJson(pemanenList.toList()))
            putString(KEY_BLOK_LIST, gson.toJson(blokList.toList()))
            putString(KEY_TPH_LIST, gson.toJson(tphList.toList()))
            putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            putString(KEY_USER_ROLE, userRole) // Simpan peran pengguna
            apply()
        }
    }

    // --- Fungsi Manajemen Sesi ---

    // Sesuaikan loginSuccess untuk menerima peran pengguna
    fun loginSuccess(role: String) {
        saveSettings(true, role)
        // TODO: Simpan token autentikasi yang sebenarnya di sini jika Anda sudah mengimplementasikannya
    }

    fun logout() {
        // Clear semua data terkait pengguna atau token saat logout
        mandorList.clear()
        pemanenList.clear()
        blokList.clear()
        tphList.clear()
        // TODO: Hapus token autentikasi yang sebenarnya dari penyimpanan aman
        saveSettings(false, null) // Set isLoggedIn ke false dan userRole ke null
        // Reset daftar ke default setelah logout, jika diinginkan
        resetToDefaults() // Panggil ini jika ingin daftar kembali ke default setelah logout
    }

    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Fungsi baru untuk mendapatkan peran pengguna
    fun getUserRole(): String? {
        return sharedPreferences.getString(KEY_USER_ROLE, null)
    }

    private fun saveDataLists() {
        with(sharedPreferences.edit()) {
            putString(KEY_MANDOR_LIST, gson.toJson(mandorList.toList()))
            putString(KEY_PEMANEN_LIST, gson.toJson(pemanenList.toList()))
            putString(KEY_BLOK_LIST, gson.toJson(blokList.toList()))
            putString(KEY_TPH_LIST, gson.toJson(tphList.toList()))
            apply()
        }
    }

    fun addMandor(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isNotBlank() && !mandorList.contains(trimmedName)) {
            mandorList.add(trimmedName)
            saveDataLists() // Panggil saveDataLists
        }
    }

    fun removeMandor(name: String) {
        mandorList.remove(name)
        saveDataLists() // Panggil saveDataLists
    }

    fun updateMandor(oldName: String, newName: String) {
        val index = mandorList.indexOf(oldName)
        val trimmedNewName = newName.trim()
        if (index != -1 && trimmedNewName.isNotBlank() && !mandorList.contains(trimmedNewName)) {
            mandorList[index] = trimmedNewName
            saveDataLists() // Panggil saveDataLists
        }
    }

    fun addPemanen(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isNotBlank() && !pemanenList.contains(trimmedName)) {
            pemanenList.add(trimmedName)
            saveDataLists()
        }
    }

    fun removePemanen(name: String) {
        pemanenList.remove(name)
        saveDataLists()
    }

    fun updatePemanen(oldName: String, newName: String) {
        val index = pemanenList.indexOf(oldName)
        val trimmedNewName = newName.trim()
        if (index != -1 && trimmedNewName.isNotBlank() && !pemanenList.contains(trimmedNewName)) {
            pemanenList[index] = trimmedNewName
            saveDataLists()
        }
    }

    fun addBlok(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isNotBlank() && !blokList.contains(trimmedName)) {
            blokList.add(trimmedName)
            saveDataLists()
        }
    }

    fun removeBlok(name: String) {
        blokList.remove(name)
        saveDataLists()
    }

    fun updateBlok(oldName: String, newName: String) {
        val index = blokList.indexOf(oldName)
        val trimmedNewName = newName.trim()
        if (index != -1 && trimmedNewName.isNotBlank() && !blokList.contains(trimmedNewName)) {
            blokList[index] = trimmedNewName
            saveDataLists()
        }
    }

    fun addTph(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isNotBlank() && !tphList.contains(trimmedName)) {
            tphList.add(trimmedName)
            saveDataLists()
        }
    }

    fun removeTph(name: String) {
        tphList.remove(name)
        saveDataLists()
    }

    fun updateTph(oldName: String, newName: String) {
        val index = tphList.indexOf(oldName)
        val trimmedNewName = newName.trim()
        if (index != -1 && trimmedNewName.isNotBlank() && !tphList.contains(trimmedNewName)) {
            tphList[index] = trimmedNewName
            saveDataLists()
        }
    }

    // Fungsi untuk mereset semua daftar ke nilai default (opsional)
    fun resetToDefaults() {
        mandorList.apply {
            clear()
            addAll(listOf("MANDOR A", "MANDOR B", "MANDOR C"))
        }
        pemanenList.apply {
            clear()
            addAll(listOf("PEMANEN X", "PEMANEN Y", "PEMANEN Z"))
        }
        blokList.apply {
            clear()
            addAll(listOf("BLOK A1", "BLOK A2", "BLOK B1", "BLOK B2"))
        }
        tphList.apply {
            clear()
            addAll(listOf("TPH 001", "TPH 002", "TPH 003"))
        }
        saveDataLists()
    }
}