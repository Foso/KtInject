package de.jensklingenberg.ktinject.generator

import de.jensklingenberg.ktinject.MyComponent
import de.jensklingenberg.ktinject.MyProviderMethod
import de.jensklingenberg.ktinject.ParameterType
import de.jensklingenberg.ktinject.ReturnType
import de.jensklingenberg.ktinject.common.extensions.addImport
import de.jensklingenberg.ktinject.common.extensions.addPackage
import de.jensklingenberg.ktinject.internal.Factory
import de.jensklingenberg.ktinject.internal.Provider
import de.jensklingenberg.ktinject.model.GenFactoryClass
import java.io.File


fun generateFactory(genFactoryClass: GenFactoryClass, myComponent: MyComponent) {

    val provideFunctionName = genFactoryClass.providerMethod.name
    val providedType = genFactoryClass.providerMethod.returnType
    val module = genFactoryClass.module

    fun imports(providerMethod: MyProviderMethod): String = providerMethod.parameters.joinToString(separator = "\n") {
        addImport(it.packageWithName())
    }

    val factoryClassName = "${module.simpleName}_${provideFunctionName.capitalize()}Factory"

    fun classParameter(myProviderMethod: MyProviderMethod): String {

        val args = myProviderMethod.parameters.map { it.name }.joinToString(separator = ",") { name ->
            "private val " + name.decapitalize() + "Provider : Provider<" + name + ">"
        }

        return "private val instance: ${module.simpleName}" + if (args.isNotEmpty()) {
            ", $args"
        } else {
            ""
        }
    }

    fun classParameter1(myProviderMethod: MyProviderMethod): String {

        val args = myProviderMethod.parameters.map { it.name }.joinToString(separator = ",") { name ->
            name.decapitalize() + " : " + name
        }


        return "instance: ${module.simpleName}" + if (args.isEmpty()) {
            ""
        } else {
            ", $args"
        }
    }

    fun createFunctionParameter(parameters: List<ParameterType>): String {

        val args = parameters.map { it.name }.joinToString(separator = ",") { name ->
            name.decapitalize() + "Provider : Provider<" + name + ">"
        }

        return "instance: ${module.simpleName}" + if (args.isEmpty()) {
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

        return "instance" + if (args.isEmpty()) {
            ""
        } else {
            ", $args"
        }
    }

    val provideFunctionArgs = genFactoryClass.dependencies.map { it.simpleName.toLowerCase() }.joinToString { it }

    val providerMethod=genFactoryClass.providerMethod

    fun factoryTemplate(): String {
        return """
    ${addPackage(module.packageName)}
    ${addImport(Factory::class.java.name)}
    ${addImport(Provider::class.java.name)}
    ${addImport("de.jensklingenberg.ktinject.internal.MPreconditions.Companion.checkNotNull")}
    ${addImport(providedType.packageWithName())}
    ${imports(providerMethod)}

    class ${factoryClassName}(${classParameter(providerMethod)}) : Factory<${providedType.name}> {
      
      override fun get() = ${provideFunctionName}(${getFunctionArguments(providerMethod)})
    
      companion object {
        fun ${provideFunctionName}(${classParameter1(providerMethod)}): ${providedType.name} {
          val errorMessage = "Cannot return null from a non-@Nullable @Provides method"
          return checkNotNull(instance.${provideFunctionName}($provideFunctionArgs), errorMessage  )
        }
    
        fun create(${createFunctionParameter(providerMethod.parameters)}) = ${factoryClassName}( ${createFunctionArguments(providerMethod.parameters)})
      }
    }
    """.trimIndent()
    }

    File(genFactoryClass.filePath + "/" + module.packageName.replace(".", "/") + "/" + genFactoryClass.className + ".kt").writeText(factoryTemplate())


}
