package com.lzx.kvpref

import com.lzx.pref.KvPrefModel

object SpFileDemo : KvPrefModel("spFileName") {
    var people: People? by objPrefNullable(People().apply { age = 100;name = "吃狗屎" })
    var otherpeople: People by objPref()
    var name: String by stringPref()
    var otherName: String? by stringPrefNullable()
    var age: Int by intPref()
    var height: Long by longPref()
    var weight: Float by floatPref()
    var isGay: Boolean by booleanPref(false, key = "是否是变态")
}

/**
 * 兼容java的写法
 */
object SpFileDemoJava {
    fun setPeople(people: People) {
        SpFileDemo.people = people
    }

    fun getPeople() = SpFileDemo.people
}

class People {
    var age: Int = 0
    var name: String? = null

    override fun toString(): String {
        return "People(age=$age, name=$name)"
    }
}