package de.jensklingenberg.ktinject.model

/**
 * A Class which contains @KtInject annotations
 */
class InjectedClass(val className: MyClassName, val injectedProperties: List<InjectProperty> = emptyList())