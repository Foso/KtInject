package de.jensklingenberg.ktinject.common.extensions

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import de.jensklingenberg.ktinject.InjectProperty
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import kotlin.reflect.KClass

fun ClassName.asParameterOf(asClassName: KClass<*>): ParameterizedTypeName {
    return asClassName.asClassName().parameterizedBy(this)

}

fun FunSpec.Builder.addStatements(statements: List<String>): FunSpec.Builder {

    statements.forEach {
        addStatement(it)
    }

    return this

}


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



class GenMemberInjector(val injectedClass: ClassName, val injectProperty: List<InjectProperty> = emptyList(), val filePath: String)



fun addImport(packageName: String): String = "import $packageName"
fun addPackage(packageName: String): String = "package $packageName"
