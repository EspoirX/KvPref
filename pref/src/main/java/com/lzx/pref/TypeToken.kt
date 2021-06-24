package com.lzx.pref

import java.io.Serializable
import java.lang.reflect.*

open class TypeToken<T> protected constructor() {

    private val superclass = javaClass.genericSuperclass as ParameterizedType
    val type: Type = canonicalize(superclass.actualTypeArguments[0])

    companion object {

        internal val EMPTY_TYPE_ARRAY = arrayOf<Type>()

        internal fun canonicalize(type: Type): Type = if (type is Class<*>) {
            if (type.isArray) GenericArrayTypeImpl(canonicalize(type.componentType!!)) else type
        } else if (type is ParameterizedType) {
            ParameterizedTypeImpl(type.ownerType, type.rawType, *type.actualTypeArguments)
        } else if (type is GenericArrayType) {
            GenericArrayTypeImpl(type.genericComponentType)
        } else if (type is WildcardType) {
            WildcardTypeImpl(type.upperBounds, type.lowerBounds)
        } else {
            type
        }

        internal fun typeToString(type: Type): String {
            return if (type is Class<*>) type.name else type.toString()
        }

        internal fun checkNotPrimitive(type: Type) {
            checkArgument(type !is Class<*> || !type.isPrimitive)
        }

        internal fun <T> checkNotNull(obj: T?): T =
            if (obj != null) obj else throw NullPointerException()

        internal fun checkArgument(condition: Boolean) {
            if (!condition) throw IllegalArgumentException()
        }
    }
}

private class ParameterizedTypeImpl(ownerType: Type?, rawType: Type, vararg typeArguments: Type) :
    ParameterizedType, Serializable {
    private val ownerType: Type?
    private val rawType: Type
    private val typeArguments: List<Type>

    init {
        // require an owner type if the raw type needs it
        if (rawType is Class<*>) {
            val isStaticOrTopLevelClass =
                Modifier.isStatic(rawType.modifiers) || rawType.enclosingClass == null
            TypeToken.checkArgument(ownerType != null || isStaticOrTopLevelClass)
        }

        this.ownerType = if (ownerType == null) null else TypeToken.canonicalize(ownerType)
        this.rawType = TypeToken.canonicalize(rawType)
        this.typeArguments = typeArguments.map { TypeToken.canonicalize(it) }
    }

    override fun getActualTypeArguments() = typeArguments.toTypedArray()

    override fun getRawType() = rawType

    override fun getOwnerType() = ownerType

    override fun toString(): String {
        val stringBuilder = StringBuilder(30 * (typeArguments.size + 1))
        stringBuilder.append(TypeToken.typeToString(rawType))

        if (typeArguments.isEmpty()) {
            return stringBuilder.toString()
        }

        stringBuilder.append("<").append(TypeToken.typeToString(typeArguments[0]))
        for (i in 1 until typeArguments.size) {
            stringBuilder.append(", ").append(TypeToken.typeToString(typeArguments[i]))
        }
        return stringBuilder.append(">").toString()
    }
}

private class GenericArrayTypeImpl(componentType: Type) : GenericArrayType, Serializable {

    private val componentType: Type = TypeToken.canonicalize(componentType)

    override fun getGenericComponentType() = componentType

    override fun toString() = TypeToken.typeToString(componentType) + "[]"
}

private class WildcardTypeImpl(upperBounds: Array<Type>, lowerBounds: Array<Type>) : WildcardType,
    Serializable {
    private val upperBound: Type
    private val lowerBound: Type?

    init {
        TypeToken.checkArgument(lowerBounds.size <= 1)
        TypeToken.checkArgument(upperBounds.size == 1)

        if (lowerBounds.size == 1) {
            TypeToken.checkNotNull(lowerBounds[0])
            TypeToken.checkNotPrimitive(lowerBounds[0])
            TypeToken.checkArgument(upperBounds[0] === Any::class.java)
            this.lowerBound = TypeToken.canonicalize(lowerBounds[0])
            this.upperBound = Any::class.java

        } else {
            TypeToken.checkNotNull(upperBounds[0])
            TypeToken.checkNotPrimitive(upperBounds[0])
            this.lowerBound = null
            this.upperBound = TypeToken.canonicalize(upperBounds[0])
        }
    }

    override fun getUpperBounds() = arrayOf(upperBound)

    override fun getLowerBounds() =
        if (lowerBound != null) arrayOf(lowerBound) else TypeToken.EMPTY_TYPE_ARRAY

    override fun toString(): String = when {
        lowerBound != null -> "? super " + TypeToken.typeToString(lowerBound)
        upperBound === Any::class.java -> "?"
        else -> "? extends " + TypeToken.typeToString(upperBound)
    }
}
