package com.teladanprimaagro.tmpp.ui.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    // Daftar untuk Kemandoran
    val mandorList = mutableStateListOf(
        "MANDOR A",
        "MANDOR B",
        "MANDOR C"
    )

    // Daftar untuk Pemanen
    val pemanenList = mutableStateListOf(
        "PEMANEN X",
        "PEMANEN Y",
        "PEMANEN Z"
    )

    // Daftar untuk Blok
    val blokList = mutableStateListOf(
        "BLOK A1",
        "BLOK A2",
        "BLOK B1",
        "BLOK B2"
    )

    // Daftar untuk No. TPH
    val tphList = mutableStateListOf(
        "TPH 001",
        "TPH 002",
        "TPH 003"
    )

    /**
     * Menambahkan nama mandor baru ke daftar.
     * Mengabaikan jika nama kosong atau sudah ada.
     */
    fun addMandor(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isNotBlank() && !mandorList.contains(trimmedName)) {
            mandorList.add(trimmedName)
        }
    }

    /**
     * Menghapus nama mandor dari daftar.
     */
    fun removeMandor(name: String) {
        mandorList.remove(name)
    }

    /**
     * Memperbarui nama mandor yang sudah ada.
     */
    fun updateMandor(oldName: String, newName: String) {
        val index = mandorList.indexOf(oldName)
        val trimmedNewName = newName.trim()
        if (index != -1 && trimmedNewName.isNotBlank() && !mandorList.contains(trimmedNewName)) {
            mandorList[index] = trimmedNewName
        }
    }

    /**
     * Menambahkan nama pemanen baru ke daftar.
     */
    fun addPemanen(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isNotBlank() && !pemanenList.contains(trimmedName)) {
            pemanenList.add(trimmedName)
        }
    }

    /**
     * Menghapus nama pemanen dari daftar.
     */
    fun removePemanen(name: String) {
        pemanenList.remove(name)
    }

    /**
     * Memperbarui nama pemanen yang sudah ada.
     */
    fun updatePemanen(oldName: String, newName: String) {
        val index = pemanenList.indexOf(oldName)
        val trimmedNewName = newName.trim()
        if (index != -1 && trimmedNewName.isNotBlank() && !pemanenList.contains(trimmedNewName)) {
            pemanenList[index] = trimmedNewName
        }
    }

    /**
     * Menambahkan nama blok baru ke daftar.
     */
    fun addBlok(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isNotBlank() && !blokList.contains(trimmedName)) {
            blokList.add(trimmedName)
        }
    }

    /**
     * Menghapus nama blok dari daftar.
     */
    fun removeBlok(name: String) {
        blokList.remove(name)
    }

    /**
     * Memperbarui nama blok yang sudah ada.
     */
    fun updateBlok(oldName: String, newName: String) {
        val index = blokList.indexOf(oldName)
        val trimmedNewName = newName.trim()
        if (index != -1 && trimmedNewName.isNotBlank() && !blokList.contains(trimmedNewName)) {
            blokList[index] = trimmedNewName
        }
    }

    /**
     * Menambahkan nomor TPH baru ke daftar.
     */
    fun addTph(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isNotBlank() && !tphList.contains(trimmedName)) {
            tphList.add(trimmedName)
        }
    }

    /**
     * Menghapus nomor TPH dari daftar.
     */
    fun removeTph(name: String) {
        tphList.remove(name)
    }

    /**
     * Memperbarui nomor TPH yang sudah ada.
     */
    fun updateTph(oldName: String, newName: String) {
        val index = tphList.indexOf(oldName)
        val trimmedNewName = newName.trim()
        if (index != -1 && trimmedNewName.isNotBlank() && !tphList.contains(trimmedNewName)) {
            tphList[index] = trimmedNewName
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
    }
}