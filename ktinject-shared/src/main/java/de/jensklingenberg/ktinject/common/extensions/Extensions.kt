package de.jensklingenberg.ktinject.common.extensions

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

fun ClassDescriptor.getPackage(): String {
    return this.original.fqNameSafe.asString().substringBefore("." + this.original.name)
}

fun ValueParameterDescriptor.getTypePackage(): String {
    return toString().substringAfter(name.asString() + ":").substringBefore(".$type").trim()
}

fun PropertyDescriptor.getTypePackage(): String {
    return toString().substringAfter(name.asString() + ":").substringBefore(".$type").trim()
}

 fun FunctionDescriptor.returnTypePackage(): String {
    return this.toString().substringAfter("): ").substringBefore("." + this.returnType.toString())
}


fun addImport(packageName: String): String = "import $packageName"
fun addPackage(packageName: String): String = "package $packageName"
