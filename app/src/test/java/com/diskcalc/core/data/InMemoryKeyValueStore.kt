package com.diskcalc.core.data

/** Test double for [KeyValueStore] with the same semantics as `SharedPreferences`. */
class InMemoryKeyValueStore(initial: Map<String, Any> = emptyMap()) : KeyValueStore {

    private val values = initial.toMutableMap()

    var writeCount: Int = 0
        private set

    override fun getString(key: String, defaultValue: String?): String? =
        values[key] as? String ?: defaultValue

    override fun getInt(key: String, defaultValue: Int): Int =
        values[key] as? Int ?: defaultValue

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        values[key] as? Boolean ?: defaultValue

    override fun putString(key: String, value: String) {
        values[key] = value
        writeCount++
    }

    override fun putInt(key: String, value: Int) {
        values[key] = value
        writeCount++
    }

    override fun putBoolean(key: String, value: Boolean) {
        values[key] = value
        writeCount++
    }

    override fun remove(key: String) {
        values.remove(key)
    }

    /** Raw access for assertions. */
    fun raw(key: String): Any? = values[key]
}
