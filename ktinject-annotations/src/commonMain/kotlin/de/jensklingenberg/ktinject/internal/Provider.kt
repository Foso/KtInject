package de.jensklingenberg.ktinject.internal

interface Provider<T> {
    fun get(): T
}