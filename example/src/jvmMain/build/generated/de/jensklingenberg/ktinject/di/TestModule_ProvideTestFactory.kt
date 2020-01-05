package de.jensklingenberg.ktinject.di
import de.jensklingenberg.ktinject.internal.Factory
import de.jensklingenberg.ktinject.internal.Provider
import de.jensklingenberg.ktinject.internal.MPreconditions.Companion.checkNotNull
import de.jensklingenberg.ktinject.model.Test


class TestModule_ProvideTestFactory(private val instance: TestModule) : Factory<Test> {
  
  override fun get() = provideTest(instance)

  companion object {
    fun provideTest(instance: TestModule): Test {
      val errorMessage = "Cannot return null from a non-@Nullable @Provides method"
      return checkNotNull(instance.provideTest(), errorMessage  )
    }

    fun create(instance: TestModule) = TestModule_ProvideTestFactory( instance)
  }
}