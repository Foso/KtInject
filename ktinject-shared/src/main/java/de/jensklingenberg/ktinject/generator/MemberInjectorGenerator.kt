package de.jensklingenberg.ktinject.generator

import com.squareup.kotlinpoet.*
import de.jensklingenberg.ktinject.common.extensions.GenMemberInjector
import de.jensklingenberg.ktinject.common.extensions.addStatements
import de.jensklingenberg.ktinject.common.extensions.asParameterOf
import de.jensklingenberg.ktinject.internal.MembersInjector
import de.jensklingenberg.ktinject.internal.Provider
import java.io.File




fun generateMemberInjectors(genMemberInjector: GenMemberInjector) {
    val instanceArgName = "instance"

    val constructorParameterList = genMemberInjector.injectProperty.map { it.type }
            .map {
                val propName = it.simpleName.toLowerCase()
                ParameterSpec.builder(propName + "Provider", it.asParameterOf(Provider::class)).build()
            }
    val injectTargetClass = genMemberInjector.injectedClass

    val companionfunctionsList = genMemberInjector.injectProperty.map {
        val propName = it.type.simpleName.toLowerCase()
        FunSpec.builder("inject" + it.type.simpleName)
                .addParameter(ParameterSpec.builder(instanceArgName, injectTargetClass).build())
                .addParameter(ParameterSpec.builder(propName, it.type).build())
                .addStatement("instance.${it.name}=$propName")
                .build()
    }


    val companion = TypeSpec.companionObjectBuilder()
            .addFunctions(companionfunctionsList)
            .build()

    val membersInjectorFunctionStatement = genMemberInjector.injectProperty.map {
        val propName = it.type.simpleName.toLowerCase()

        "inject" + it.type.simpleName + "(" + instanceArgName + ",${propName}Provider.get())"
    }

    val props = genMemberInjector.injectProperty.map { it.type }
            .map {
                val propName = it.simpleName.toLowerCase()
                PropertySpec.builder(propName + "Provider", it.asParameterOf(Provider::class)).initializer(it.simpleName.toLowerCase() + "Provider").build()
            }


    val membersInjectorFunction = FunSpec.builder("injectMembers").addParameter(ParameterSpec.builder("instance", injectTargetClass)
            // .defaultValue("\"pie\"")
            .build())
            .addStatements(membersInjectorFunctionStatement)
            .addModifiers(KModifier.OVERRIDE)
            .build()

    val file = FileSpec.builder(genMemberInjector.injectedClass.packageName, "${injectTargetClass.simpleName}_MembersInjector")
            .addType(TypeSpec.classBuilder("${injectTargetClass.simpleName}_MembersInjector")
                    .addSuperinterface(injectTargetClass.asParameterOf(MembersInjector::class))
                    .addType(companion)
                    .addProperties(props)
                    .primaryConstructor(FunSpec.constructorBuilder()
                            .addParameters(constructorParameterList)
                            .build())

                    .addFunction(membersInjectorFunction)

                    .build())
            .build()
    //file.writeTo(System.out)
    file.writeTo(File(genMemberInjector.filePath))

}