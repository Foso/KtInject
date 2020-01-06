package de.jensklingenberg.ktinject.generator

import com.squareup.kotlinpoet.ClassName
import de.jensklingenberg.ktinject.common.extensions.*
import de.jensklingenberg.ktinject.internal.Factory
import de.jensklingenberg.ktinject.internal.MPreconditions
import de.jensklingenberg.ktinject.internal.Provider
import de.jensklingenberg.ktinject.model.*
import de.jensklingenberg.mpapt.model.Element
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import java.io.File


fun facGen(myComponent: MyComponent, buildFolder: String, provideFunctions: MutableList<Element.FunctionElement>){



    /**
     * Get all the provideFunctions in Modules that are used by AppComponent
     */
    val funcsInSelectedModules = provideFunctions.filter { func -> myComponent.modules.any { it.className.name == func.descriptor.name.toString() } }


    funcsInSelectedModules.map { it.func }.forEach {

        val containingClassDes = it.containingDeclaration as ClassDescriptor

        val module = ClassName(containingClassDes.getPackage(), it.containingDeclaration.name.asString())

        val provideFunctionName = it.name.toString()

        val returnType = ReturnType(it.returnTypePackage(), it.returnType.toString())

        val className = module.simpleName + "_" + provideFunctionName.capitalize() + "Factory"


        val deps = it.valueParameters.map {
            ClassName(it.getTypePackage(), it.returnType.toString())
        }

        val parameters = it.valueParameters.map {valParameter->
            ReturnType(valParameter.getTypePackage(), valParameter.type.toString())
        }
        generateFactory(GenFactoryClass(dependencies = deps, module = module, providerMethod = MyProviderMethod(provideFunctionName, returnType, parameters), className = className, buildFolder = buildFolder))
    }
}


fun generateFactory(genFactoryClass: GenFactoryClass) {

    val provideFunctionName = genFactoryClass.providerMethod.name
    val providedType = genFactoryClass.providerMethod.returnType
    val module = genFactoryClass.module

    fun imports(providerMethod: MyProviderMethod): String = providerMethod.parameters.joinToString(separator = "\n") {
        addImport(it.packageWithName())
    }

    val factoryClassName = "${module.simpleName}_${provideFunctionName.capitalize()}Factory"

    fun classParameter(myProviderMethod: MyProviderMethod): String {

        val args = myProviderMethod.parameters.map { it.name }.joinToString(separator = ",") { name ->
            val T = name
            val variable = name.decapitalize()
            "private val ${variable}Provider : Provider<$T>"
        }

        return  if (args.isNotEmpty()) {
            ", $args"
        } else {
            ""
        }
    }

    fun classParameter1(myProviderMethod: MyProviderMethod): String {

        val args = myProviderMethod.parameters.map { it.name }.joinToString(separator = ",") { name ->
            name.decapitalize() + " : " + name
        }


        return  if (args.isEmpty()) {
            ""
        } else {
            ", $args"
        }
    }

    fun createFunctionParameter(parameters: List<ParameterType>): String {

        val args = parameters.map { it.name }.joinToString(separator = ",") { name ->
            val T = name
            val variable = name.decapitalize()
            "${variable}Provider : Provider<$T>"
        }

        return if (args.isEmpty()) {
            ""
        } else {
            ", $args"
        }
    }

    fun getFunctionArguments(myProviderMethod: MyProviderMethod): String {

        val args = myProviderMethod.parameters.map { it.name }.joinToString(separator = ",") { name ->
            "${name.decapitalize()}Provider.get()"
        }


        return "instance" + if (args.isEmpty()) {
            ""
        } else {
            ", $args"
        }
    }

    fun createFunctionArguments(parameters: List<ParameterType>): String {

        val args = parameters.map { it.name }.joinToString(separator = ",") { name ->
            "${name.decapitalize()}Provider"
        }

        return if (args.isEmpty()) {
            ""
        } else {
            ", $args"
        }
    }

    val provideFunctionArgs = genFactoryClass.dependencies.map { it.simpleName.toLowerCase() }.joinToString { it }

    val providerMethod=genFactoryClass.providerMethod

    val Type = providedType.name
    val className = factoryClassName
    val fun1Name = provideFunctionName
    val fun1Args = getFunctionArguments(providerMethod)
    val fun1Parameters = "instance: ${module.simpleName} ${classParameter1(providerMethod)}"
    val classParameters = "private val instance: ${module.simpleName} ${classParameter(providerMethod)}"
    val classArgs = "instance ${createFunctionArguments(providerMethod.parameters)}"

    val funCreateParameter = "instance: ${module.simpleName} ${createFunctionParameter(providerMethod.parameters)}"

    fun factoryTemplate(): String {
        return """
    ${addPackage(module.packageName)}
    ${addImport(Factory::class.java.name)}
    ${addImport(Provider::class.java.name)}
    ${addImport(MPreconditions::class.java.name)}
    ${addImport(providedType.packageWithName())}
    ${imports(providerMethod)}

    class ${className}(${classParameters}) : Factory<${Type}> {
 
      override fun get() = ${fun1Name}(${fun1Args})

      companion object {
        fun ${fun1Name}(${fun1Parameters}): $Type {
          val errorMessage = "Cannot return null from a non-@Nullable @Provides method"
          return MPreconditions.checkNotNull(instance.${fun1Name}($provideFunctionArgs), errorMessage  )
        }

        fun create(${funCreateParameter}) = ${className}(${classArgs})
      }
    }
    """.trimIndent()
    }

    File(genFactoryClass.buildFolder + "/" + module.packageName.replace(".", "/") + "/" + genFactoryClass.className + ".kt").writeText(factoryTemplate())


}
