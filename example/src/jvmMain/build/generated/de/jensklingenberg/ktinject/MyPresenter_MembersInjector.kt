
    package de.jensklingenberg.ktinject
    import de.jensklingenberg.ktinject.internal.MembersInjector
    import de.jensklingenberg.ktinject.internal.Provider
    import de.jensklingenberg.ktinject.model.Test
import de.jensklingenberg.ktinject.model.Car

    class MyPresenter_MembersInjector(val testProvider: Provider<Test>,val carProvider: Provider<Car>) : MembersInjector<MyPresenter> {

      override fun injectMembers(instance: MyPresenter) {
        injectTest(instance,testProvider.get())
injectCar(instance,carProvider.get())
      }
    
      companion object {
        fun injectTest(instance: MyPresenter, test: Test) {instance.test= test}
fun injectCar(instance: MyPresenter, car: Car) {instance.car= car}
      }
    }

