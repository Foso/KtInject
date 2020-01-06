package de.jensklingenberg.ktinject.generator

import de.jensklingenberg.ktinject.common.extensions.addImport
import de.jensklingenberg.ktinject.common.extensions.addPackage
import de.jensklingenberg.ktinject.internal.MPreconditions
import de.jensklingenberg.ktinject.internal.Provider
import de.jensklingenberg.ktinject.model.*
import java.io.File


class GenAppComponent(val compInterface: MyClassName,
                      val injectFunctions: List<ComponentFunction> = emptyList(),
                      val injectedClasses: List<InjectedClass> = emptyList())


fun generateCompo(myComponent: MyComponent, genAppComponent: GenAppComponent, buildFolder: String) {


    /**
     * Generate the code that overrides the inject functions of the Component
     *  @sample  "override fun inject(instance: MyPresenter) "
     */
    fun overrideInjectFuns(injectClasses: List<ComponentFunction>) = injectClasses.joinToString(separator = "\n") { injectClassName ->
        "override fun inject(instance: ${injectClassName.valueParameter.name}) {" +
                "inject${injectClassName.valueParameter.name}(instance) " +
                "}"
    }


    fun imports(modules: List<MyModule>): String {

        val ter = modules.joinToString(separator = "\n") {

            val modImport = it.className.packageName + "." + it.className.name
            /**
             * We assume that every provideMethod has a Factory Class and import all Returntypes of the provide Methods
             */
            val modFactories = it.myProvideFunctions.joinToString(separator = "\n") { provmed ->
                addImport(modImport + "_" + provmed.name.capitalize() + "Factory") + "\n" +
                        addImport(provmed.returnType.packageWithName())
            }

            addImport(modImport) + "\n" + modFactories
        }

        return ter
    }

    /**
     *
     */
    fun classParameter(modules: List<MyModule>) = modules.joinToString(separator = ",") {
        val className = it.className.name
        "private val " + className.decapitalize() + " : " + className
    }


    fun localInjectFunctions(injectedClasses: List<InjectedClass>): String {

        var tt = ""

        injectedClasses.forEach { injclass ->

            val injectClassName = injclass.className.name

            val ee = "private fun inject$injectClassName(instance: ${injectClassName}) {\n" +

                    injclass.injectedProperties.joinToString(separator = "\n") { prop ->
                        " ${injclass.className.name}_MembersInjector.inject${prop.type.name}(instance,provide${prop.type.name}Provider.get())"
                    } +
                    "  }\n"
            tt = tt + ee

        }

        return tt
    }

    /**
     * Add the Providers Object to the class
     * e.g.  lateinit var provideCarProvider: Provider<Car>
     */
    fun providers(modules: List<MyModule>): String {

        return modules
                .flatMap {
                    it.myProvideFunctions
                }
                .distinctBy { it.returnType }
                .joinToString(separator = "\n") { providerMethod ->
                    val variable = providerMethod.name.decapitalize()
                    val T = providerMethod.returnType.name
                    "private lateinit var ${variable}Provider: Provider<${T}>"
                }
    }

    fun genInitFunction(modules: List<MyModule>, injectedClasses: List<InjectedClass>): String {
        val te = modules.flatMap { it.myProvideFunctions }.sortedBy { it.parameters.size }
        //TODO:

        val rr = modules.joinToString(separator = "\n") { mymod ->

            val moduleName = mymod.className.name
            val moduleObjectName = moduleName.decapitalize()

            val fieldsDeclarationString = mymod.myProvideFunctions.joinToString(separator = "\n") {
                val providedTypeName = it.returnType.name
                val providedFunctionName = it.name

                val createParameters = if (it.parameters.isEmpty()) {
                    "$moduleObjectName"
                } else {
                    "${moduleObjectName}," + it.parameters.joinToString(separator = ",") { returntyp ->
                        val prov = modules.flatMap { it.myProvideFunctions }.find { it.returnType.packageWithName() == returntyp.packageWithName() }
                        if (prov == null) {
                            throw NotImplementedError("No prov method found")
                        } else {
                            "${prov.name}Provider"
                        }

                    }


                }

                "this.${providedFunctionName}Provider = ${moduleName}_${providedFunctionName.capitalize()}Factory.create(${createParameters})"

            }
            fieldsDeclarationString
        }

        return rr
    }

    /**
     * This generates the builder pattern
     */
    fun builderPatternGenerator(modules: List<MyModule>): String {

        fun fields(): String = modules.joinToString(separator = "\n") {
            val moduleObjectName = it.className.name.decapitalize() //testModule
            val moduleClassName = it.className.name

            """ private  var ${moduleObjectName}: ${moduleClassName}? = null"""
        }


        fun settter(): String = modules.joinToString(separator = "\n") {

            val moduleObjectName = it.className.name.decapitalize() //testModule
            val moduleClassName = it.className.name

            """ fun ${moduleObjectName}(instance: ${moduleClassName}): Builder {
             |       this.${moduleObjectName} = MPreconditions.checkNotNull(instance)
             |      return this
             |    }"""
        }

        fun buildFunction(): String {


            val moduleNullChecks = modules.joinToString(separator = "\n") {
                val moduleObjectName = it.className.name.decapitalize() //testModule
                val moduleClassName = it.className.name

                """
                    if ($moduleObjectName == null) {
                        $moduleObjectName = $moduleClassName()
                       }
                """.trimIndent()
            }

            val appComponentArgs = modules.joinToString(separator = ",") {
                val moduleObjectName = it.className.name.decapitalize() //testModule
                "$moduleObjectName!!"
            }

            return """
                                 $moduleNullChecks
                                 return KtInjectAppComponent($appComponentArgs)
                                
                """.trimIndent()
        }

        return """
             
            ${fields()}

            ${settter()}

             |    
             |    fun build(): ${genAppComponent.compInterface.name}{
           ${buildFunction()}
            |    }
             """.trimIndent()
    }


    fun componenSourceTemplate() = """
        ${addPackage("de.jensklingenberg.ktinject")}

        ${addImport(MPreconditions::class.java.name)}
        ${addImport(Provider::class.java.name)}
        ${imports(myComponent.modules)}
        ${addImport(genAppComponent.compInterface.packageWithName())}
        class KtInject${genAppComponent.compInterface.name}(${classParameter(myComponent.modules)}) : ${genAppComponent.compInterface.name} {
            
        ${providers(myComponent.modules)}

            
        init{
             initialize()
        }
        
        private fun initialize() {
          ${genInitFunction(myComponent.modules, genAppComponent.injectedClasses)}
        }
      

        ${overrideInjectFuns(genAppComponent.injectFunctions)}

        ${localInjectFunctions(genAppComponent.injectedClasses)}

        companion object {
            fun builder(): Builder =  Builder()
        }

        class Builder {
            ${builderPatternGenerator(myComponent.modules).trimMargin()}
         }
        }
    """.trimIndent()



    File(buildFolder + "/de/jensklingenberg/ktinject/KtInject${genAppComponent.compInterface.name}.kt").writeText(componenSourceTemplate())
}