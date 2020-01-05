package de.jensklingenberg


import com.squareup.kotlinpoet.ClassName
import de.jensklingenberg.ktinject.common.extensions.*
import de.jensklingenberg.ktinject.annotations.Component

import de.jensklingenberg.ktinject.annotations.Module
import de.jensklingenberg.ktinject.annotations.Provides

import de.jensklingenberg.ktinject.*
import de.jensklingenberg.ktinject.annotations.Inject
import de.jensklingenberg.ktinject.generator.generateFactory
import de.jensklingenberg.ktinject.generator.generateMemberInjectors
import de.jensklingenberg.ktinject.model.ComponentFunction
import de.jensklingenberg.ktinject.model.GenFactoryClass
import de.jensklingenberg.mpapt.common.nativeTargetPlatformName
import de.jensklingenberg.mpapt.common.readArgument
import de.jensklingenberg.mpapt.model.AbstractProcessor
import de.jensklingenberg.mpapt.model.Element
import de.jensklingenberg.mpapt.model.RoundEnvironment
import de.jensklingenberg.mpapt.utils.KonanTargetValues
import de.jensklingenberg.mpapt.utils.KotlinPlatformValues
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.lazy.descriptors.LazyClassDescriptor

class MpAptTestProcessor() : AbstractProcessor() {
    val TAG = "MyAnnotationProcessor"

    val provides = Provides::class.java.name
    val xinject = Inject::class.java.name

    val component = Component::class.java.name
    private val module = Module::class.java.name
    val provideFunctions = mutableListOf<Element.FunctionElement>()
    val myprovideFunctions = mutableListOf<MyProviderMethod>()

    val injectedProperties = mutableListOf<Element.PropertyElement>()
    val moduleClasses = mutableListOf<Element.ClassElement>()
    val componentClasses = mutableListOf<Element.ClassElement>()

    override fun process(roundEnvironment: RoundEnvironment) {

        roundEnvironment.getElementsAnnotatedWith(provides).forEach { element ->
            when (element) {
                is Element.FunctionElement -> {
                    provideFunctions.add(element)
                    val function = element.func
                    val returnTypePackage = function.returnTypePackage()
                    myprovideFunctions.add(MyProviderMethod(element.simpleName, ReturnType(returnTypePackage, function.returnType.toString())))
                }
            }
        }
        roundEnvironment.getElementsAnnotatedWith(xinject).forEach { element ->
            when (element) {
                is Element.PropertyElement -> {
                    //getFunctions.add(element.func)
                    injectedProperties.add(element)
                }
            }
        }

        roundEnvironment.getElementsAnnotatedWith(module).forEach { element ->
            when (element) {
                is Element.ClassElement -> {
                    moduleClasses.add(element)
                    //getFunctions.add(element.func)
                }
            }
        }

        roundEnvironment.getElementsAnnotatedWith(component).forEach { element ->
            when (element) {
                is Element.ClassElement -> {
                    componentClasses.add(element)

                    //getFunctions.add(element.func)
                }
            }
        }

    }

    override fun isTargetPlatformSupported(platform: TargetPlatform): Boolean {
        val targetName = platform.first().platformName

        return when (targetName) {
            KotlinPlatformValues.JS -> false
            KotlinPlatformValues.JVM -> true
            KotlinPlatformValues.NATIVE -> {
                return when (configuration.nativeTargetPlatformName()) {
                    KonanTargetValues.LINUX_X64, KonanTargetValues.MACOS_X64 -> {
                        false
                    }
                    else -> {
                        false
                    }
                }
            }
            else -> {
                log(targetName)
                false
            }
        }

    }

    override fun getSupportedAnnotationTypes(): Set<String> = setOf(provides, xinject, component, module)

    override fun processingOver() {

        generator()


    }


    fun myComponentBuilder(componentClass: Element.ClassElement, modules: List<MyModule>, injected: List<InjectedClass>, injectFunctionsInComponent: Collection<CallableMemberDescriptor>): MyComponent {
        val funcs = injectFunctionsInComponent.map {
            val functionName = it.name.toString()

            val parameter = it.valueParameters.first()
            val parameterPackageName = parameter.getTypePackage()
            val parameterName = parameter.name.toString()
            val parameterTypeName = parameter.type.toString()

            ComponentFunction(functionName, ParameterType(parameterPackageName, parameterTypeName))

        }

        val name = componentClass.simpleName

        return MyComponent(MyClassName(componentClass.pack, name), modules, injected, funcs)
    }


    private fun generator() {
        /**
         * Read the values from the Component Annotation
         */
        val selectedModulesPackages = componentClasses.first().annotation?.readArgument("modules")?.map { it.value as KClassValue.Value.NormalClass }?.map { it.value.toString().replace("/", ".") }
                ?: emptyList()

        val buildFolder = "/home/jens/Code/2019/KtInject/example/src/jvmMain/build/generated"

        val groupProvide = provideFunctions.filter { selectedModulesPackages.contains(it.descriptor.getPackage() +"."+it.descriptor.name) }.groupBy { it.descriptor }
        val groupedInjected = injectedProperties.groupBy { it.propertyDescriptor.original.containingDeclaration.name }
        val injectFunctionsInComponent = (componentClasses.first().classDescriptor as LazyClassDescriptor).declaredCallableMembers

        val mymods = groupProvide.map {
            val classDesc = it.key
            val functions = it.value.map {element->
                val function = element.func
                val returnTypePackage = function.returnTypePackage()
                val parameters = function.valueParameters.map {valParameter->
                    ReturnType(valParameter.getTypePackage(),valParameter.type.toString())
                }

                MyProviderMethod(element.simpleName, ReturnType(returnTypePackage, function.returnType.toString()),parameters)
            }.sortedBy { it.parameters.size }

            MyModule(MyClassName(classDesc.getPackage(), classDesc.name.toString()),functions)
        }




        val testInjected = groupedInjected.map {
            val injClassName = it.key.toString()

            val props = it.value.map { propertyElement ->
                InjectProperty(propertyElement.propertyDescriptor.name.toString(), ClassName(propertyElement.propertyDescriptor.getTypePackage(), propertyElement.propertyDescriptor.returnType.toString()))


            }

            InjectedClass(ClassName("de.jensklingenberg.ktinject", injClassName), props)
        }

        val myComponent = myComponentBuilder(componentClasses.first(), mymods, testInjected, injectFunctionsInComponent)


        KtGenerator(myComponent, buildFolder)



        /**
         * Get all the modules that are set in the Component Annotation
         */
        val selectedModules = moduleClasses.filter {
            val desc = it.classDescriptor
            val packageName = desc.containingDeclaration.fqNameSafe.toString()

            val path = packageName + "." + desc.name
            //  path.equals("de.jensklingenberg.ktinject.di.SecondModule")
            selectedModulesPackages.contains(path)
        }

        /**
         * Get all the provideFunctions in Modules that are used by AppComponent
         */
        val funcsInSelectedModules = provideFunctions.filter { func -> selectedModules.any { it.classDescriptor == func.descriptor } }


        //componentClasses.first().annotation.readArgument("modules")
        ////moduleClasses.map { it.classDescriptor }.filter { it.containingDeclaration.fqNameSafe }


        // generateMembers()
        // (componentClasses.first().classDescriptor as LazyClassDescriptor).declaredCallableMembers
        val classImportThatUseInject = injectFunctionsInComponent.flatMap { it.valueParameters }.map { it.getTypePackage() + "." + it.type }
        log("$TAG***Processor over ***")


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
                ReturnType(valParameter.getTypePackage(),valParameter.type.toString())
            }
            generateFactory(GenFactoryClass(dependencies = deps, module = module, providerMethod = MyProviderMethod(provideFunctionName, returnType,parameters), className = className, filePath = buildFolder), myComponent)
        }

        val groupPropsByClass = injectedProperties.map { it.propertyDescriptor }.groupBy { it.containingDeclaration }
        injectedProperties.map { it.propertyDescriptor }.groupBy { it.containingDeclaration }
                .forEach { (entry, vale) ->

                    if (classImportThatUseInject.none { it == entry.fqNameSafe.asString() }) {

                        return
                    }

                    val injectProperties = vale.map { InjectProperty(it.name.asString(), ClassName(it.getTypePackage(), it.type.toString())) }

                    val injectTargetClass = ClassName((entry as ClassDescriptor).getPackage(), entry.name.asString())

                    generateMemberInjectors(GenMemberInjector(injectedClass = injectTargetClass, injectProperty = injectProperties, filePath = buildFolder))

                }



        // generateCompo(GenAppComponent(compInterface, modules, funcs, injected))
    }


}

