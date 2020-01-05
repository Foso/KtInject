package de.jensklingenberg.ktinject.model

import com.squareup.kotlinpoet.ClassName
import de.jensklingenberg.ktinject.MyProviderMethod


class GenFactoryClass(val dependencies: List<ClassName> = emptyList(), val module: ClassName, val providerMethod: MyProviderMethod, val className: String, val filePath: String)