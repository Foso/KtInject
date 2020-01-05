package de.jensklingenberg.ktinject.di

import de.jensklingenberg.ktinject.annotations.Component
import de.jensklingenberg.ktinject.MyPresenter
import de.jensklingenberg.ktinject.MySecondClass
import de.jensklingenberg.ktinject.di.data.SecondModule

@Component(modules = [TestModule::class, SecondModule::class])
interface AppComponent {
    fun inject(myPresenter: MyPresenter)
    fun inject(myPresenter: MySecondClass)


}