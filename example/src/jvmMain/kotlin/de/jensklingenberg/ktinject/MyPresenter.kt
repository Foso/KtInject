package de.jensklingenberg.ktinject

import de.jensklingenberg.ktinject.annotations.Inject
import de.jensklingenberg.ktinject.model.Car
import de.jensklingenberg.ktinject.model.Test

class MyPresenter(val view: Contract.View): Contract.Presenter {


    @Inject
    lateinit var test: Test

    @Inject
    lateinit var car : Car

    init {
        App.appi.inject(this)
        test.druck()
        car.start()
        println(car.motor.name)
    }
}

