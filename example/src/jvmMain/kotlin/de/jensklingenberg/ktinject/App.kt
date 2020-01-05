package de.jensklingenberg.ktinject


import de.jensklingenberg.ktinject.di.AppComponent


class App() :Contract.View{

    companion object {
        lateinit var appi: AppComponent
    }

    init {
      appi=  KtInjectAppComponent.builder().build()
        println("Hey")
        MyPresenter(this)
        MySecondClass()
    }
}


