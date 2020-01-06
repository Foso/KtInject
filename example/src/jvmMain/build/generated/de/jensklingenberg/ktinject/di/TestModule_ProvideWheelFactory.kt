package de.jensklingenberg.ktinject.di
import de.jensklingenberg.ktinject.internal.Factory
import de.jensklingenberg.ktinject.internal.Provider
import de.jensklingenberg.ktinject.internal.MPreconditions
import de.jensklingenberg.ktinject.model.Wheel


class TestModule_ProvideWheelFactory(private val instance: TestModule ) : Factory<Wheel> {

  override fun get() = provideWheel(instance)

  companion object {
    fun provideWheel(instance: TestModule ): Wheel {
      val errorMessage = "Cannot return null from a non-@Nullable @Provides method"
      return MPreconditions.checkNotNull(instance.provideWheel(), errorMessage  )
    }

    fun create(instance: TestModule ) = TestModule_ProvideWheelFactory(instance )
  }
}