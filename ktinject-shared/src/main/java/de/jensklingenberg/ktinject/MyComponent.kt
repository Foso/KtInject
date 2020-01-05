package de.jensklingenberg.ktinject

import de.jensklingenberg.ktinject.model.ComponentFunction

class MyComponent(val name:MyClassName, val modules : List<MyModule> = emptyList(),val injectedClass: List<InjectedClass> = emptyList(),val componentFunctions: List<ComponentFunction> = emptyList())