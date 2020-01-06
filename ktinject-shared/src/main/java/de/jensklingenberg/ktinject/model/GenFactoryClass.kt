package de.jensklingenberg.ktinject.model

import com.squareup.kotlinpoet.ClassName


class GenFactoryClass(val dependencies: List<ClassName> = emptyList(), val module: ClassName, val providerMethod: MyProviderMethod, val className: String, val buildFolder: String)