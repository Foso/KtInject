package de.jensklingenberg.ktinject.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class Component(
        val modules: Array<KClass<*>> = emptyArray()
)