package com.lzx.kvpref

import com.lzx.pref.KvPrefModel
import com.lzx.pref.getWithKey
import com.lzx.pref.saveWithKey

object SpFileDemo : KvPrefModel("spFileName") {
    var people: People? by objPrefNullable(People().apply { age = 100;name = "吃狗屎" })
    var otherpeople: People by objPref()
    var name: String by stringPref()
    var otherName: String? by stringPrefNullable()
    var height: Long by longPref()
    var weight: Float by floatPref()
    var isGay: Boolean? by booleanPrefNullable(false, key = "是否是变态")

    var haha: String? = null
    var haha1: String? = null
    var haha2: String? = null
}

/**
 * 兼容java的写法
 */
object SpFileDemoJava {
    @JvmStatic
    var people: People?
        get() = SpFileDemo.people
        set(value) {
            SpFileDemo.people = value
        }

    @JvmStatic
    var name: String
        get() = SpFileDemo.name
        set(value) {
            SpFileDemo.name = value
        }

    @JvmStatic
    fun setHaha(key: String, value: String) {
        SpFileDemo.saveWithKey(SpFileDemo::haha2, key, value)
    }

    @JvmStatic
    fun getHaha(key: String) = SpFileDemo.getWithKey<Int>(SpFileDemo::haha, key)
}

class People {
    var age: Int = 0
    var name: String? = null

    override fun toString(): String {
        return "People(age=$age, name=$name)"
    }
}