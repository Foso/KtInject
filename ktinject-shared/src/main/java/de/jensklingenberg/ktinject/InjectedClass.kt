package de.jensklingenberg.ktinject

import com.squareup.kotlinpoet.ClassName

/**
 * A Class which contains @KtInject annotations
 */
class InjectedClass(val className: ClassName, val injectedProperty: List<InjectProperty> = emptyList())