package com.teladanprimaagro.tmpp.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.teladanprimaagro.tmpp.data.UserRole
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME = "app_settings_prefs"
private const val KEY_UNIQUE_NO_FORMAT = "unique_no_format"
private const val DEFAULT_UNIQUE_NO_FORMAT = "AME1"
private const val KEY_MANDOR_LIST = "mandor_list"
private const val KEY_PEMANEN_LIST = "pemanen_list"
private const val KEY_BLOK_LIST = "blok_list"
private const val KEY_TPH_LIST = "tph_list"
private const val KEY_SUPIR_LIST = "supir_list"
private const val KEY_KENDARAAN_LIST = "kendaraan_list"
private const val KEY_IS_LOGGED_IN = "is_logged_in"
private const val KEY_USER_ROLE = "user_role"
private const val KEY_SPB_LAST_MONTH = "spb_last_month"
private const val KEY_SPB_LAST_YEAR = "spb_last_year"
private const val KEY_SPB_FORMAT = "spb_format"
private const val DEFAULT_SPB_FORMAT = "AME/TPE"
private const val KEY_AFD_CODE = "afd_code"
private const val DEFAULT_AFD_CODE = "AFD1"

private const val KEY_SELECTED_MANDOR_LOADING = "selected_mandor_loading"
private const val DEFAULT_MANDOR_LOADING = "Pilih Mandor Loading"

private fun getSpbCounterKeyForMandor(mandor: String): String = "spb_counter_$mandor"

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    val mandorList = mutableStateListOf<String>()
    val pemanenList = mutableStateListOf<String>()
    val blokList = mutableStateListOf<String>()
    val tphList = mutableStateListOf<String>()
    val supirList = mutableStateListOf<String>()
    val kendaraanList = mutableStateListOf<String>()

    val mandorLoadingOptions = listOf("A", "B", "C", "D")
    private val _selectedMandorLoading = MutableStateFlow(
        sharedPreferences.getString(KEY_SELECTED_MANDOR_LOADING, mandorLoadingOptions.first())
            ?: mandorLoadingOptions.first()
    )
    val selectedMandorLoading: StateFlow<String> = _selectedMandorLoading.asStateFlow()

    init {
        loadDataLists()
        ensureDefaultListsPopulated()
    }

    private fun loadDataLists() {
        val mandorJson = sharedPreferences.getString(KEY_MANDOR_LIST, null)
        if (mandorJson != null) {
            val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
            mandorList.addAll(gson.fromJson(mandorJson, type))
        }

        val pemanenJson = sharedPreferences.getString(KEY_PEMANEN_LIST, null)
        if (pemanenJson != null) {
            val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
            pemanenList.addAll(gson.fromJson(pemanenJson, type))
        }

        val blokJson = sharedPreferences.getString(KEY_BLOK_LIST, null)
        if (blokJson != null) {
            val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
            blokList.addAll(gson.fromJson(blokJson, type))
        }

        val tphJson = sharedPreferences.getString(KEY_TPH_LIST, null)
        if (tphJson != null) {
            val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
            tphList.addAll(gson.fromJson(tphJson, type))
        }

        val supirJson = sharedPreferences.getString(KEY_SUPIR_LIST, null)
        if (supirJson != null) {
            val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
            supirList.addAll(gson.fromJson(supirJson, type))
        }

        val kendaraanJson = sharedPreferences.getString(KEY_KENDARAAN_LIST, null)
        if (kendaraanJson != null) {
            val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
            kendaraanList.addAll(gson.fromJson(kendaraanJson, type))
        }
    }

    private fun ensureDefaultListsPopulated() {
        if (mandorList.isEmpty()) {
            mandorList.addAll(listOf("Mandor 1", "Mandor 2"))
        }
        if (pemanenList.isEmpty()) {
            pemanenList.addAll(listOf("Pemanen 1", "Pemanen 2"))
        }
        if (blokList.isEmpty()) {
            blokList.addAll(listOf("A01", "A02"))
        }
        if (tphList.isEmpty()) {
            tphList.addAll(listOf("TPH 001", "TPH 002"))
        }
        if (supirList.isEmpty()) {
            supirList.addAll(listOf("Pilih Supir", "Supir A", "Supir B"))
        }
        if (kendaraanList.isEmpty()) {
            kendaraanList.addAll(listOf("Pilih No Polisi", "B 1234", "B 5678 DEF"))
        }

        saveDataLists()
    }

    private fun saveDataLists() {
        with(sharedPreferences.edit()) {
            putString(KEY_MANDOR_LIST, gson.toJson(mandorList.toList()))
            putString(KEY_PEMANEN_LIST, gson.toJson(pemanenList.toList()))
            putString(KEY_BLOK_LIST, gson.toJson(blokList.toList()))
            putString(KEY_TPH_LIST, gson.toJson(tphList.toList()))
            putString(KEY_SUPIR_LIST, gson.toJson(supirList.toList()))
            putString(KEY_KENDARAAN_LIST, gson.toJson(kendaraanList.toList()))
            apply()
        }
    }

    private fun saveSessionStatus(isLoggedIn: Boolean, userRoleString: String?) {
        with(sharedPreferences.edit()) {
            putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            putString(KEY_USER_ROLE, userRoleString)
            apply()
        }
    }

    fun loginSuccess(role: UserRole) {
        saveSessionStatus(true, role.name)
    }

    fun logout() {
        saveSessionStatus(false, null)
    }

    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserRole(): UserRole? {
        val roleString = sharedPreferences.getString(KEY_USER_ROLE, null)
        return roleString?.let {
            try {
                UserRole.valueOf(it)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }

    fun setMandorLoading(mandor: String) {
        sharedPreferences.edit {
            putString(KEY_SELECTED_MANDOR_LOADING, mandor)
        }
        _selectedMandorLoading.value = mandor
    }

    fun addMandor(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isNotBlank() && !mandorList.contains(trimmedName)) {
            mandorList.add(trimmedName)
            saveDataLists()
        }
    }

    fun removeMandor(name: String) {
        mandorList.remove(name)
        saveDataLists()
    }

    fun updateMandor(oldName: String, newName: String) {
        val index = mandorList.indexOf(oldName)
        val trimmedNewName = newName.trim()
        if (index != -1 && trimmedNewName.isNotBlank() && !mandorList.contains(trimmedNewName)) {
            mandorList[index] = trimmedNewName
            saveDataLists()
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

    fun addSupir(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isNotBlank() && !supirList.contains(trimmedName) && trimmedName != "Pilih Supir") {
            supirList.add(trimmedName)
            saveDataLists()
        }
    }

    fun removeSupir(name: String) {
        if (name != "Pilih Supir") {
            supirList.remove(name)
            saveDataLists()
        }
    }

    fun updateSupir(oldName: String, newName: String) {
        val index = supirList.indexOf(oldName)
        val trimmedNewName = newName.trim()
        if (index != -1 && trimmedNewName.isNotBlank() && !supirList.contains(trimmedNewName) && trimmedNewName != "Pilih Supir") {
            supirList[index] = trimmedNewName
            saveDataLists()
        }
    }

    fun addKendaraan(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isNotBlank() && !kendaraanList.contains(trimmedName) && trimmedName != "Pilih No Polisi") {
            kendaraanList.add(trimmedName)
            saveDataLists()
        }
    }

    fun removeKendaraan(name: String) {
        if (name != "Pilih No Polisi") {
            kendaraanList.remove(name)
            saveDataLists()
        }
    }

    fun updateKendaraan(oldName: String, newName: String) {
        val index = kendaraanList.indexOf(oldName)
        val trimmedNewName = newName.trim()
        if (index != -1 && trimmedNewName.isNotBlank() && !kendaraanList.contains(trimmedNewName) && trimmedNewName != "Pilih No Polisi") {
            kendaraanList[index] = trimmedNewName
            saveDataLists()
        }
    }

    fun getSpbCounterForMandor(mandor: String): Int {
        val key = getSpbCounterKeyForMandor(mandor)
        return sharedPreferences.getInt(key, 0)
    }

    fun setSpbCounterForMandor(mandor: String, counter: Int) {
        val key = getSpbCounterKeyForMandor(mandor)
        sharedPreferences.edit { putInt(key, counter) }
    }

    fun resetAllSpbCounters() {
        sharedPreferences.edit {
            mandorLoadingOptions.forEach { mandor ->
                val key = getSpbCounterKeyForMandor(mandor)
                remove(key)
            }
        }
    }

    fun getSpbLastMonth(): Int {
        return sharedPreferences.getInt(KEY_SPB_LAST_MONTH, -1)
    }

    fun setSpbLastMonth(month: Int) {
        sharedPreferences.edit { putInt(KEY_SPB_LAST_MONTH, month) }
    }

    fun getSpbLastYear(): Int {
        return sharedPreferences.getInt(KEY_SPB_LAST_YEAR, -1)
    }

    fun setSpbLastYear(year: Int) {
        sharedPreferences.edit { putInt(KEY_SPB_LAST_YEAR, year) }
    }

    fun getUniqueNoFormat(): String {
        return sharedPreferences.getString(KEY_UNIQUE_NO_FORMAT, DEFAULT_UNIQUE_NO_FORMAT) ?: DEFAULT_UNIQUE_NO_FORMAT
    }

    fun setUniqueNoFormat(format: String) {
        sharedPreferences.edit { putString(KEY_UNIQUE_NO_FORMAT, format) }
    }

    fun getSpbFormat(): String {
        return sharedPreferences.getString(KEY_SPB_FORMAT, DEFAULT_SPB_FORMAT) ?: DEFAULT_SPB_FORMAT
    }

    fun setSpbFormat(format: String) {
        sharedPreferences.edit { putString(KEY_SPB_FORMAT, format) }
    }

    fun getAfdCode(): String {
        return sharedPreferences.getString(KEY_AFD_CODE, DEFAULT_AFD_CODE) ?: DEFAULT_AFD_CODE
    }

    fun setAfdCode(code: String) {
        sharedPreferences.edit { putString(KEY_AFD_CODE, code) }
    }
}