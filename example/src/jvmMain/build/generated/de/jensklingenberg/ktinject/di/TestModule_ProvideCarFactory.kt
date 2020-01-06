package de.jensklingenberg.ktinject.di
import de.jensklingenberg.ktinject.internal.Factory
import de.jensklingenberg.ktinject.internal.Provider
import de.jensklingenberg.ktinject.internal.MPreconditions
import de.jensklingenberg.ktinject.model.Car
import de.jensklingenberg.ktinject.model.Motor

class TestModule_ProvideCarFactory(private val instance: TestModule , private val motorProvider : Provider<Motor>) : Factory<Car> {

  override fun get() = provideCar(instance, motorProvider.get())

  companion object {
    fun provideCar(instance: TestModule , motor : Motor): Car {
      val errorMessage = "Cannot return null from a non-@Nullable @Provides method"
      return MPreconditions.checkNotNull(instance.provideCar(motor), errorMessage  )
    }

    fun create(instance: TestModule , motorProvider : Provider<Motor>) = TestModule_ProvideCarFactory(instance , motorProvider)
  }
}