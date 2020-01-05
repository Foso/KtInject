package de.jensklingenberg.ktinject.internal

interface MembersInjector<T> {

    fun injectMembers(instance: T)
}