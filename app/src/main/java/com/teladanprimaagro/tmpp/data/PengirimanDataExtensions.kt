package com.teladanprimaagro.tmpp.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.teladanprimaagro.tmpp.viewmodels.ScannedItem

private val gson = Gson()

fun PengirimanData.getDetailScannedItems(): List<ScannedItem> {
    val type = object : TypeToken<List<ScannedItem>>() {}.type
    return gson.fromJson(this.detailScannedItemsJson, type) ?: emptyList()
}