package com.lzx.kvpref

import com.lzx.pref.*
import com.lzx.pref.property.DynamicKeyPref

object SpFileConfig : KvPrefModel("spFileName") {
    var people by objPrefNullable(People().apply { age = 100; name = "吃狗屎" })
    var otherpeople: People by objPref()
    var name by stringPref()
    var otherName by stringPrefNullable()
    var height by longPref()
    var weight by floatPref()
    var isGay by booleanPrefNullable(false, key = "是否是变态")


    var tagJson: DynamicKeyPref<String> by dynamicKeyPref()
        private set

    var tagJson2: DynamicKeyPref<String?> by dynamicKeyPrefNullable()
        private set

    var color : DynamicKeyPref<Int> by dynamicKeyPref(100)
        private set
}

class People {
    var age: Int = 0
    var name: String? = null

    override fun toString(): String {
        return "People(age=$age, name=$name)"
    }
}