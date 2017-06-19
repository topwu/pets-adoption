package com.topwu.petsadopt.util

import android.graphics.Bitmap
import android.util.LruCache

class MapBitmapCache private constructor(maxSize: Int) : LruCache<String, Bitmap>(maxSize) {

    val KEY = "MAP_BITMAP_KEY"

    companion object {
        val DEFAULT_CACHE_SIZE = (Runtime.getRuntime().maxMemory() / 1024).toInt() / 8
        val instance: MapBitmapCache by lazy { MapBitmapCache(DEFAULT_CACHE_SIZE) }
    }

    fun getBitmap(): Bitmap {
        return get(KEY)
    }

    fun putBitmap(bitmap: Bitmap) {
        put(KEY, bitmap)
    }

    override fun sizeOf(key: String, value: Bitmap?): Int {
        return if (value == null) 0 else value.rowBytes * value.height / 1024
    }
}