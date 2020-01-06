package de.jensklingenberg.ktinject.di
import de.jensklingenberg.ktinject.internal.Factory
import de.jensklingenberg.ktinject.internal.Provider
import de.jensklingenberg.ktinject.internal.MPreconditions
import kotlin.String


class TestModule_ProvideDatabaseNameFactory(private val instance: TestModule ) : Factory<String> {

  override fun get() = provideDatabaseName(instance)

  companion object {
    fun provideDatabaseName(instance: TestModule ): String {
      val errorMessage = "Cannot return null from a non-@Nullable @Provides method"
      return MPreconditions.checkNotNull(instance.provideDatabaseName(), errorMessage  )
    }

    fun create(instance: TestModule ) = TestModule_ProvideDatabaseNameFactory(instance )
  }
}