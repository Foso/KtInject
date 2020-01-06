package de.jensklingenberg


import de.jensklingenberg.ktinject.annotations.Component
import de.jensklingenberg.ktinject.annotations.Inject
import de.jensklingenberg.ktinject.annotations.Module
import de.jensklingenberg.ktinject.annotations.Provides
import de.jensklingenberg.ktinject.common.extensions.getPackage
import de.jensklingenberg.ktinject.common.extensions.getTypePackage
import de.jensklingenberg.ktinject.common.extensions.returnTypePackage
import de.jensklingenberg.ktinject.generator.KtGenerator
import de.jensklingenberg.ktinject.model.*
import de.jensklingenberg.mpapt.common.nativeTargetPlatformName
import de.jensklingenberg.mpapt.common.readArgument
import de.jensklingenberg.mpapt.model.AbstractProcessor
import de.jensklingenberg.mpapt.model.Element
import de.jensklingenberg.mpapt.model.RoundEnvironment
import de.jensklingenberg.mpapt.utils.KonanTargetValues
import de.jensklingenberg.mpapt.utils.KotlinPlatformValues
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.resolve.constants.KClassValue
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
                }
            }
        }

        roundEnvironment.getElementsAnnotatedWith(component).forEach { element ->
            when (element) {
                is Element.ClassElement -> {
                    componentClasses.add(element)
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

        val groupProvide = provideFunctions.filter { selectedModulesPackages.contains(it.descriptor.getPackage() + "." + it.descriptor.name) }.groupBy { it.descriptor }
        val groupedInjected = injectedProperties.groupBy { it.propertyDescriptor.original.containingDeclaration.name }
        val injectFunctionsInComponent = (componentClasses.first().classDescriptor as LazyClassDescriptor).declaredCallableMembers

        val myModules = groupProvide.map {
            val classDesc = it.key
            val functions = it.value.map { element ->
                val function = element.func
                val returnTypePackage = function.returnTypePackage()
                val parameters = function.valueParameters.map { valParameter ->
                    ReturnType(valParameter.getTypePackage(), valParameter.type.toString())
                }

                MyProviderMethod(element.simpleName, ReturnType(returnTypePackage, function.returnType.toString()), parameters)
            }.sortedBy { it.parameters.size }

            MyModule(MyClassName(classDesc.getPackage(), classDesc.name.toString()), functions)
        }


        /**
         * Get all the modules that are set in the Component Annotation
         */
        val selectedModules = myModules.filter {
            selectedModulesPackages.contains(it.className.packageWithName())
        }

        val injectedClasses = groupedInjected.map {
            val injClassName = it.key.toString()

            val props = it.value.map { propertyElement ->
                InjectProperty(propertyElement.propertyDescriptor.name.toString(), MyClassName(propertyElement.propertyDescriptor.getTypePackage(), propertyElement.propertyDescriptor.returnType.toString()))
            }

            InjectedClass(MyClassName("de.jensklingenberg.ktinject", injClassName), props)
        }

        val myComponent = myComponentBuilder(componentClasses.first(), selectedModules, injectedClasses, injectFunctionsInComponent)

        KtGenerator(myComponent, buildFolder, provideFunctions)

        log("$TAG***Processor over ***")

    }


}

