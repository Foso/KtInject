package de.jensklingenberg.ktinject

import de.jensklingenberg.ktinject.internal.MembersInjector
import de.jensklingenberg.ktinject.internal.Provider
import de.jensklingenberg.ktinject.model.Car
import de.jensklingenberg.ktinject.model.Test

class MySecondClass_MembersInjector(
  val testProvider: Provider<Test>,
  val carProvider: Provider<Car>
) : MembersInjector<MySecondClass> {
  override fun injectMembers(instance: MySecondClass) {
    injectTest(instance,testProvider.get())
    injectCar(instance,carProvider.get())
  }

  companion object {
    fun injectTest(instance: MySecondClass, test: Test) {
      instance.test=test
    }

    fun injectCar(instance: MySecondClass, car: Car) {
      instance.car=car
    }
  }
}
