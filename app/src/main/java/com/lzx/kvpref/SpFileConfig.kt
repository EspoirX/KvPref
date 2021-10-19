package com.lzx.kvpref

import com.lzx.pref.KvPrefModel
import com.lzx.pref.property.DynamicKeyPref

object SpFileConfig : KvPrefModel("spFileName") {
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

    var tagJson: DynamicKeyPref<String> by dynamicKeyPref()
        private set

}

class People {
    var age: Int = 0
    var name: String? = null

    override fun toString(): String {
        return "People(age=$age, name=$name)"
    }
}