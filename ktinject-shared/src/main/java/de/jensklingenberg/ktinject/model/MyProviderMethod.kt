package de.jensklingenberg.ktinject.model

typealias ReturnType = MyClassName

typealias ParameterType = ReturnType

data class MyProviderMethod(val name: String, val returnType: ReturnType = ReturnType("", "void"), val parameters : List<ParameterType> = emptyList())
