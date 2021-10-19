package com.lzx.pref.property

interface DynamicKeyPref<T : Any?> {

    fun get(key: String): T

    fun set(key: String, value: T)
}