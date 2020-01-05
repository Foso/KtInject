package de.jensklingenberg.ktinject.internal

interface Lazy<T> {
    fun get(): T
}