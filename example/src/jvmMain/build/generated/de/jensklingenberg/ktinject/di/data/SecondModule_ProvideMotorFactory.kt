package de.jensklingenberg.ktinject.di.data
import de.jensklingenberg.ktinject.internal.Factory
import de.jensklingenberg.ktinject.internal.Provider
import de.jensklingenberg.ktinject.internal.MPreconditions.Companion.checkNotNull
import de.jensklingenberg.ktinject.model.Motor


class SecondModule_ProvideMotorFactory(private val instance: SecondModule) : Factory<Motor> {
  
  override fun get() = provideMotor(instance)

  companion object {
    fun provideMotor(instance: SecondModule): Motor {
      val errorMessage = "Cannot return null from a non-@Nullable @Provides method"
      return checkNotNull(instance.provideMotor(), errorMessage  )
    }

    fun create(instance: SecondModule) = SecondModule_ProvideMotorFactory( instance)
  }
}