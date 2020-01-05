package de.jensklingenberg.ktinject

import com.squareup.kotlinpoet.ClassName
import de.jensklingenberg.ktinject.generator.GenAppComponent
import de.jensklingenberg.ktinject.generator.generateCompo

/**
 * Factories,Injector, Component
 */

class KtGenerator(private val myComponent: MyComponent, buildFolder: String){
    init {



        val compInterface = ClassName(myComponent.name.packageName, myComponent.name.packageName)

        generateCompo(GenAppComponent(compInterface, myComponent.modules, myComponent.componentFunctions, myComponent.injectedClass), buildFolder)

    }

}