package de.jensklingenberg.ktinject.generator

import com.squareup.kotlinpoet.ClassName
import de.jensklingenberg.ktinject.InjectedClass
import de.jensklingenberg.ktinject.MyModule
import de.jensklingenberg.ktinject.common.extensions.addImport
import de.jensklingenberg.ktinject.common.extensions.addPackage
import de.jensklingenberg.ktinject.internal.MPreconditions
import de.jensklingenberg.ktinject.internal.Provider
import de.jensklingenberg.ktinject.model.ComponentFunction
import java.io.File


class GenAppComponent(val compInterface: ClassName
                      , val modules: List<MyModule>
                      , val injectFunctions: List<ComponentFunction> = emptyList(),
                      val injectedClasses: List<InjectedClass> = emptyList())



fun generateCompo(genAppComponent: GenAppComponent, buildFolder: String) {


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
            val modFactories = it.myProvideFunctions.joinToString(separator = "\n") {provmed->
                addImport(modImport + "_" + provmed.name.capitalize() + "Factory") +"\n"+
                        addImport(provmed.returnType.packageWithName())
            }

            addImport(modImport) +"\n"+modFactories
        }

        return ter + "\n" +
                "import de.jensklingenberg.ktinject.di.AppComponent\n"
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

            val injectClassName = injclass.className.simpleName

            val ee = "private fun inject$injectClassName(instance: ${injectClassName}) {\n" +

                    injclass.injectedProperty.joinToString(separator = "\n") { prop ->
                        " ${injclass.className.simpleName}_MembersInjector.inject${prop.type.simpleName}(instance,provide${prop.type.simpleName}Provider.get())"
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
                .joinToString(transform = { providerMethod ->
                    val providerMethodName = providerMethod.name.decapitalize()
                    "private lateinit var ${providerMethodName}Provider: Provider<${providerMethod.returnType.name}>"
                }, separator = "\n")

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

                val createParameters = if(it.parameters.isEmpty()){
                    "$moduleObjectName"
                }else{
                    "${moduleObjectName},"+  it.parameters.joinToString(separator = ",") {returntyp->
                      val prov =  modules.flatMap { it.myProvideFunctions }.find { it.returnType.packageWithName() == returntyp.packageWithName() }
                        if(prov==null){
                            throw NotImplementedError("No prov method found")
                        }else{
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


            val tt = modules.joinToString(separator = "\n") {
                val moduleObjectName = it.className.name.decapitalize() //testModule
                val moduleClassName = it.className.name

                """
                    if ($moduleObjectName == null) {
                        $moduleObjectName = $moduleClassName()
                       }
                """.trimIndent()
            }

            val args = modules.joinToString(separator = ",") {
                val moduleObjectName = it.className.name.decapitalize() //testModule
                "$moduleObjectName!!"
            }

            return """
                                 ${tt}
                                 return KtInjectAppComponent(${args})
                                
                """.trimIndent()
        }

        return """
             
            ${fields()}

            ${settter()}

             |    
             |    fun build(): AppComponent{
           ${buildFunction()}
            |    }
             """.trimIndent()
    }


    fun componenSourceTemplate() = """
        ${addPackage("de.jensklingenberg.ktinject")}

        ${addImport(MPreconditions::class.java.name)}
        ${addImport(Provider::class.java.name)}
        ${imports(genAppComponent.modules)}

        class KtInjectAppComponent(${classParameter(genAppComponent.modules)}) : AppComponent {
            
        ${providers(genAppComponent.modules)}

            
        init{
             initialize()
        }
        
        fun initialize() {
          ${genInitFunction(genAppComponent.modules, genAppComponent.injectedClasses)}
        }
      

        ${overrideInjectFuns(genAppComponent.injectFunctions)}

        ${localInjectFunctions(genAppComponent.injectedClasses)}

        companion object {
            fun builder(): Builder =  Builder()
        }

        class Builder {
            ${builderPatternGenerator(genAppComponent.modules).trimMargin()}
         }
        }
    """.trimIndent()



    File(buildFolder + "/de/jensklingenberg/ktinject/KtInjectAppComponent.kt").writeText(componenSourceTemplate())
}