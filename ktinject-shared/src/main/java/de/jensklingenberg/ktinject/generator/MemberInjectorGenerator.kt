package de.jensklingenberg.ktinject.generator

import de.jensklingenberg.ktinject.common.extensions.addImport
import de.jensklingenberg.ktinject.common.extensions.addPackage
import de.jensklingenberg.ktinject.internal.MembersInjector
import de.jensklingenberg.ktinject.internal.Provider
import de.jensklingenberg.ktinject.model.InjectedClass
import java.io.File


fun generateMemberInjectors(injectedClass: InjectedClass, buildFolder: String) {

    val injectTargetClassName = injectedClass.className

    //Name of inject Class
    val cName = injectTargetClassName.name

    val classParameter = injectedClass.injectedProperties.map { it.type }
            .joinToString(separator = ",") {
                val propName = it.name.toLowerCase()
                val T = it.name
                "val ${propName}Provider: Provider<${T}>"
            }

    val injectMembersBody =  injectedClass.injectedProperties.map { it.type }.joinToString("\n") {
        val propName = it.name.toLowerCase()
        val T = it.name

        "inject${T}(instance,${propName}Provider.get())"
    }


    val injectFunctions =  injectedClass.injectedProperties.joinToString(separator = "\n") {
        val T = it.type.name
        val varName = it.name

        "fun inject${T}(instance: ${cName}, ${varName}: ${T}) {instance.${varName} = ${varName}}"
    }

    val otherimports =  injectedClass.injectedProperties.joinToString(separator = "\n") {
        addImport(it.type.packageName + "." + it.type.name)
    }

    fun memberInjectorTemplate() = """
    ${addPackage("de.jensklingenberg.ktinject")}
    ${addImport(MembersInjector::class.java.name)}
    ${addImport(Provider::class.java.name)}
    $otherimports

    class ${cName}_MembersInjector(${classParameter}) : MembersInjector<${cName}> {

      override fun injectMembers(instance: ${cName}) {
        $injectMembersBody
      }
    
      companion object {
        $injectFunctions
      }
    }

"""

    File(buildFolder + "/" + injectTargetClassName.packageName.replace(".", "/") + "/" + injectTargetClassName.name + "_MembersInjector.kt").writeText(memberInjectorTemplate())


}