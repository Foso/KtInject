package de.jensklingenberg.ktinject.generator

import de.jensklingenberg.ktinject.model.MyComponent
import de.jensklingenberg.mpapt.model.Element

/**
 * Factories,Injector, Component
 */

class KtGenerator(private val myComponent: MyComponent, buildFolder: String, provideFunctions: MutableList<Element.FunctionElement>){
    init {

        generateCompo(myComponent,GenAppComponent(myComponent.name, myComponent.componentFunctions, myComponent.injectedClass), buildFolder)

        facGen(myComponent, buildFolder, provideFunctions)

        myComponent.injectedClass.map { injectTargetClass->
                generateMemberInjectors(injectTargetClass, buildFolder = buildFolder)
        }

    }

}