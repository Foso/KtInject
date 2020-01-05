package de.jensklingenberg.ktinject

import com.squareup.kotlinpoet.ClassName

data class ReturnType(val packageName: String, val name: String){
    fun packageWithName() = "$packageName.$name"
}
typealias ParameterType = ReturnType

data class MyProviderMethod( val name: String,val returnType: ReturnType = ReturnType("", "void"),val parameters : List<ParameterType> = emptyList())
